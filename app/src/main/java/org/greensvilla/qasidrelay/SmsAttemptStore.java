package org.greensvilla.qasidrelay;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

final class SmsAttemptStore {
    private static final String KEY_TOTAL_PREFIX = "sms_attempt_total.";
    private static final String KEY_ACKED_PREFIX = "sms_attempt_acked.";
    private static final String KEY_CREATED_AT_PREFIX = "sms_attempt_created_at.";
    private static final long STALE_AFTER_MILLIS = 15L * 60L * 1000L;

    private SmsAttemptStore() {
    }

    static synchronized void create(Context context, String messageId, int totalParts) {
        if (!PendingMessage.hasValidId(messageId)) {
            return;
        }
        prefs(context).edit()
            .putInt(KEY_TOTAL_PREFIX + messageId, Math.max(1, totalParts))
            .putString(KEY_ACKED_PREFIX + messageId, "")
            .putLong(KEY_CREATED_AT_PREFIX + messageId, System.currentTimeMillis())
            .apply();
    }

    static synchronized void remove(Context context, String messageId) {
        prefs(context).edit()
            .remove(KEY_TOTAL_PREFIX + messageId)
            .remove(KEY_ACKED_PREFIX + messageId)
            .remove(KEY_CREATED_AT_PREFIX + messageId)
            .apply();
    }

    static synchronized boolean exists(Context context, String messageId) {
        return prefs(context).contains(KEY_TOTAL_PREFIX + messageId);
    }

    static synchronized boolean isStale(Context context, String messageId, long nowMillis) {
        SharedPreferences prefs = prefs(context);
        if (!prefs.contains(KEY_TOTAL_PREFIX + messageId)) {
            return false;
        }

        long createdAt = prefs.getLong(KEY_CREATED_AT_PREFIX + messageId, 0L);
        return createdAt <= 0L || nowMillis - createdAt >= STALE_AFTER_MILLIS;
    }

    static synchronized boolean hasAnyPending(Context context) {
        for (String key : prefs(context).getAll().keySet()) {
            if (key.startsWith(KEY_TOTAL_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    static long staleAfterMillis() {
        return STALE_AFTER_MILLIS;
    }

    static synchronized CompletedStatus recordPartResult(
        Context context,
        String messageId,
        int partIndex,
        int totalParts,
        int resultCode,
        String failureDetail
    ) {
        if (!PendingMessage.hasValidId(messageId)) {
            return null;
        }
        SharedPreferences prefs = prefs(context);
        if (!prefs.contains(KEY_TOTAL_PREFIX + messageId)) {
            return null;
        }
        if (resultCode != Activity.RESULT_OK) {
            remove(context, messageId);
            return new CompletedStatus("failed", failureDetail);
        }

        int total = Math.max(1, prefs.getInt(KEY_TOTAL_PREFIX + messageId, totalParts));
        String acked = prefs.getString(KEY_ACKED_PREFIX + messageId, "");
        String marker = "," + partIndex + ",";
        if (!("," + acked + ",").contains(marker)) {
            acked = acked.isEmpty() ? String.valueOf(partIndex) : acked + "," + partIndex;
            prefs.edit().putString(KEY_ACKED_PREFIX + messageId, acked).apply();
        }

        if (acked.split(",").length >= total) {
            remove(context, messageId);
            return new CompletedStatus("sent", null);
        }
        return null;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(GatewayConfig.PREFS, Context.MODE_PRIVATE);
    }

    static final class CompletedStatus {
        final String status;
        final String detail;

        CompletedStatus(String status, String detail) {
            this.status = status;
            this.detail = detail;
        }
    }
}
