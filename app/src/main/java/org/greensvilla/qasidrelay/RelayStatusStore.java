package org.greensvilla.qasidrelay;

import android.content.Context;
import android.content.SharedPreferences;

final class RelayStatusStore {
    private static final String KEY_SERVICE_RUNNING = "status_service_running";
    private static final String KEY_STATE = "status_state";
    private static final String KEY_DETAIL = "status_detail";
    private static final String KEY_LAST_POLL_AT = "status_last_poll_at";
    private static final String KEY_LAST_SUCCESS_AT = "status_last_success_at";
    private static final String KEY_LAST_MESSAGE_ID = "status_last_message_id";
    private static final String KEY_LAST_MESSAGE_STATUS = "status_last_message_status";
    private static final String KEY_LAST_MESSAGE_DETAIL = "status_last_message_detail";
    private static final String KEY_LAST_MESSAGE_AT = "status_last_message_at";
    private static final String KEY_PENDING_REPORT_COUNT = "status_pending_report_count";
    private static final String KEY_GATEWAY_CATEGORY = "status_gateway_category";

    private RelayStatusStore() {
    }

    static void resetForNewGateway(Context context) {
        edit(context)
            .putBoolean(KEY_SERVICE_RUNNING, false)
            .putString(KEY_STATE, "Stopped")
            .putString(KEY_DETAIL, "Configure a trusted gateway before enabling messaging.")
            .remove(KEY_LAST_POLL_AT)
            .remove(KEY_LAST_SUCCESS_AT)
            .remove(KEY_LAST_MESSAGE_ID)
            .remove(KEY_LAST_MESSAGE_STATUS)
            .remove(KEY_LAST_MESSAGE_DETAIL)
            .remove(KEY_LAST_MESSAGE_AT)
            .putInt(KEY_PENDING_REPORT_COUNT, 0)
            .remove(KEY_GATEWAY_CATEGORY)
            .apply();
    }

    static void markServiceRunning(Context context, boolean running) {
        edit(context)
            .putBoolean(KEY_SERVICE_RUNNING, running)
            .putString(KEY_STATE, running ? "Running" : "Stopped")
            .putString(KEY_DETAIL, running ? "Relay foreground service is active." : "Relay service is stopped.")
            .apply();
    }

    static void markManualRestartRequired(Context context, String detail) {
        edit(context)
            .putBoolean(KEY_SERVICE_RUNNING, false)
            .putString(KEY_STATE, "Manual restart required")
            .putString(KEY_DETAIL, detail)
            .apply();
    }

    static void markWaiting(Context context, String detail) {
        edit(context)
            .putString(KEY_STATE, "Waiting")
            .putString(KEY_DETAIL, detail)
            .apply();
    }

    static void markPolling(Context context) {
        edit(context)
            .putString(KEY_STATE, "Polling")
            .putString(KEY_DETAIL, "Checking for pending transactional SMS jobs.")
            .putLong(KEY_LAST_POLL_AT, System.currentTimeMillis())
            .apply();
    }

    static void markPollSucceeded(Context context, int messageCount) {
        edit(context)
            .putString(KEY_STATE, "Running")
            .putString(KEY_DETAIL, messageCount == 0 ? "No pending messages." : "Fetched " + messageCount + " pending message(s).")
            .putLong(KEY_LAST_SUCCESS_AT, System.currentTimeMillis())
            .putString(KEY_GATEWAY_CATEGORY, messageCount == 0 ? "idle" : "active")
            .apply();
    }

    static void markPollFailed(Context context, Exception error) {
        edit(context)
            .putString(KEY_STATE, "Poll failed")
            .putString(KEY_DETAIL, GatewayFailureGuide.title(GatewayFailureGuide.classify(error)))
            .putString(KEY_GATEWAY_CATEGORY, GatewayFailureGuide.classify(error))
            .apply();
    }

    static void markMessageQueued(Context context, PendingMessage message) {
        RecentActivityStore.record(context, message.id, "sending", null);
        edit(context)
            .putString(KEY_LAST_MESSAGE_ID, message.id)
            .putString(KEY_LAST_MESSAGE_STATUS, "queued")
            .putString(KEY_LAST_MESSAGE_DETAIL, "Submitted to Android SMS service; waiting for sent callback.")
            .putLong(KEY_LAST_MESSAGE_AT, System.currentTimeMillis())
            .apply();
    }

    static void markMessageSkipped(Context context, PendingMessage message, String detail) {
        RecentActivityStore.record(context, message.id, "skipped", null);
        edit(context)
            .putString(KEY_LAST_MESSAGE_ID, message.id)
            .putString(KEY_LAST_MESSAGE_STATUS, "skipped")
            .putString(KEY_LAST_MESSAGE_DETAIL, detail)
            .putLong(KEY_LAST_MESSAGE_AT, System.currentTimeMillis())
            .apply();
    }

    static void markReportQueued(Context context, String messageId, String status) {
        edit(context)
            .putString(KEY_LAST_MESSAGE_ID, messageId)
            .putString(KEY_LAST_MESSAGE_STATUS, status)
            .putString(KEY_LAST_MESSAGE_DETAIL, "Status queued for gateway acknowledgement.")
            .putLong(KEY_LAST_MESSAGE_AT, System.currentTimeMillis())
            .putInt(KEY_PENDING_REPORT_COUNT, StatusReportQueue.count(context))
            .apply();
    }

    static void markStatusReported(Context context, String messageId, String status) {
        RecentActivityStore.record(context, messageId, "reported", null);
        edit(context)
            .putString(KEY_LAST_MESSAGE_ID, messageId)
            .putString(KEY_LAST_MESSAGE_STATUS, status)
            .putString(KEY_LAST_MESSAGE_DETAIL, "Gateway status report accepted.")
            .putLong(KEY_LAST_MESSAGE_AT, System.currentTimeMillis())
            .putInt(KEY_PENDING_REPORT_COUNT, StatusReportQueue.count(context))
            .apply();
    }

    static void markStatusReportFailed(Context context, String messageId, String status, Exception error) {
        edit(context)
            .putString(KEY_LAST_MESSAGE_ID, messageId)
            .putString(KEY_LAST_MESSAGE_STATUS, status)
            .putString(KEY_LAST_MESSAGE_DETAIL, "Status report pending: " +
                GatewayFailureGuide.title(GatewayFailureGuide.classify(error)))
            .putString(KEY_GATEWAY_CATEGORY, GatewayFailureGuide.classify(error))
            .putLong(KEY_LAST_MESSAGE_AT, System.currentTimeMillis())
            .putInt(KEY_PENDING_REPORT_COUNT, StatusReportQueue.count(context))
            .apply();
    }

    static void markPendingReportCount(Context context, int count) {
        edit(context).putInt(KEY_PENDING_REPORT_COUNT, count).apply();
    }

    static Snapshot load(Context context) {
        SharedPreferences prefs = prefs(context);
        return new Snapshot(
            prefs.getBoolean(KEY_SERVICE_RUNNING, false),
            prefs.getString(KEY_STATE, "Stopped"),
            prefs.getString(KEY_DETAIL, "Relay service has not started."),
            prefs.getLong(KEY_LAST_POLL_AT, 0L),
            prefs.getLong(KEY_LAST_SUCCESS_AT, 0L),
            prefs.getString(KEY_LAST_MESSAGE_ID, ""),
            prefs.getString(KEY_LAST_MESSAGE_STATUS, ""),
            prefs.getString(KEY_LAST_MESSAGE_DETAIL, ""),
            prefs.getLong(KEY_LAST_MESSAGE_AT, 0L),
            prefs.getInt(KEY_PENDING_REPORT_COUNT, StatusReportQueue.count(context)),
            prefs.getString(KEY_GATEWAY_CATEGORY, "unknown")
        );
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(GatewayConfig.PREFS, Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor edit(Context context) {
        return prefs(context).edit();
    }

    private static String safeDetail(Exception error) {
        if (error == null) {
            return "unknown";
        }
        String message = error.getMessage();
        return error.getClass().getSimpleName() + (message == null || message.isEmpty() ? "" : ": " + message);
    }

    static final class Snapshot {
        final boolean serviceRunning;
        final String state;
        final String detail;
        final long lastPollAt;
        final long lastSuccessAt;
        final String lastMessageId;
        final String lastMessageStatus;
        final String lastMessageDetail;
        final long lastMessageAt;
        final int pendingReportCount;
        final String gatewayCategory;

        Snapshot(
            boolean serviceRunning,
            String state,
            String detail,
            long lastPollAt,
            long lastSuccessAt,
            String lastMessageId,
            String lastMessageStatus,
            String lastMessageDetail,
            long lastMessageAt,
            int pendingReportCount,
            String gatewayCategory
        ) {
            this.serviceRunning = serviceRunning;
            this.state = state == null ? "" : state;
            this.detail = detail == null ? "" : detail;
            this.lastPollAt = lastPollAt;
            this.lastSuccessAt = lastSuccessAt;
            this.lastMessageId = lastMessageId == null ? "" : lastMessageId;
            this.lastMessageStatus = lastMessageStatus == null ? "" : lastMessageStatus;
            this.lastMessageDetail = lastMessageDetail == null ? "" : lastMessageDetail;
            this.lastMessageAt = lastMessageAt;
            this.pendingReportCount = pendingReportCount;
            this.gatewayCategory = gatewayCategory == null ? "unknown" : gatewayCategory;
        }
    }
}
