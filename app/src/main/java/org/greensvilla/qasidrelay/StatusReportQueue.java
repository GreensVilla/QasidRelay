package org.greensvilla.qasidrelay;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

final class StatusReportQueue {
    private static final String KEY_PENDING_STATUS_REPORTS = "pending_status_reports";
    private static final int MAX_PENDING_REPORTS = 100;

    private StatusReportQueue() {
    }

    static synchronized void enqueue(Context context, String messageId, String status, String detail) {
        if (!PendingMessage.hasValidId(messageId)) {
            return;
        }

        JSONArray current = load(context);
        JSONArray updated = new JSONArray();
        boolean replaced = false;
        for (int index = 0; index < current.length(); index++) {
            JSONObject object = current.optJSONObject(index);
            if (object == null) {
                continue;
            }
            if (messageId.equals(object.optString("id"))) {
                updated.put(report(messageId, status, detail));
                replaced = true;
            } else {
                updated.put(object);
            }
        }
        if (!replaced) {
            updated.put(report(messageId, status, detail));
        }
        while (updated.length() > MAX_PENDING_REPORTS) {
            updated = withoutFirst(updated);
        }

        save(context, updated);
        RecentActivityStore.record(context, messageId, "report pending", detail);
        RelayMessageLedger.remember(context, messageId);
        RelayStatusStore.markPendingReportCount(context, updated.length());
    }

    static synchronized int count(Context context) {
        return load(context).length();
    }

    static boolean flush(Context context, RelayClient client) {
        while (true) {
            Report pending = peek(context);
            if (pending == null) {
                RelayStatusStore.markPendingReportCount(context, 0);
                return true;
            }

            try {
                client.reportStatus(pending.messageId, pending.status, pending.detail);
                removeFirst(context, pending);
                RelayStatusStore.markStatusReported(context, pending.messageId, pending.status);
            } catch (Exception error) {
                RelayStatusStore.markStatusReportFailed(context, pending.messageId, pending.status, error);
                return false;
            }
        }
    }

    private static synchronized Report peek(Context context) {
        JSONArray pending = load(context);
        if (pending.length() == 0) {
            return null;
        }
        JSONObject object = pending.optJSONObject(0);
        if (object == null) {
            save(context, withoutFirst(pending));
            return peek(context);
        }
        String id = object.optString("id", "");
        if (!PendingMessage.hasValidId(id)) {
            save(context, withoutFirst(pending));
            return peek(context);
        }
        return new Report(
            id,
            object.optString("status", "failed"),
            object.isNull("detail") ? null : object.optString("detail", null)
        );
    }

    private static synchronized void removeFirst(Context context, Report report) {
        JSONArray pending = load(context);
        JSONArray updated = new JSONArray();
        boolean removed = false;
        for (int index = 0; index < pending.length(); index++) {
            JSONObject object = pending.optJSONObject(index);
            if (!removed && object != null &&
                report.messageId.equals(object.optString("id")) &&
                report.status.equals(object.optString("status"))) {
                removed = true;
                continue;
            }
            if (object != null) {
                updated.put(object);
            }
        }
        save(context, updated);
        RelayStatusStore.markPendingReportCount(context, updated.length());
    }

    private static JSONArray load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(GatewayConfig.PREFS, Context.MODE_PRIVATE);
        try {
            return new JSONArray(prefs.getString(KEY_PENDING_STATUS_REPORTS, "[]"));
        } catch (Exception error) {
            return new JSONArray();
        }
    }

    private static void save(Context context, JSONArray pending) {
        context.getSharedPreferences(GatewayConfig.PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PENDING_STATUS_REPORTS, pending.toString())
            .apply();
    }

    private static JSONObject report(String messageId, String status, String detail) {
        JSONObject object = new JSONObject();
        try {
            object.put("id", messageId);
            object.put("status", status);
            object.put("detail", detail == null ? JSONObject.NULL : detail);
        } catch (Exception error) {
            throw new IllegalStateException("Could not build status report", error);
        }
        return object;
    }

    private static JSONArray withoutFirst(JSONArray source) {
        JSONArray updated = new JSONArray();
        for (int index = 1; index < source.length(); index++) {
            JSONObject object = source.optJSONObject(index);
            if (object != null) {
                updated.put(object);
            }
        }
        return updated;
    }

    private static final class Report {
        final String messageId;
        final String status;
        final String detail;

        Report(String messageId, String status, String detail) {
            this.messageId = messageId;
            this.status = status;
            this.detail = detail;
        }
    }
}
