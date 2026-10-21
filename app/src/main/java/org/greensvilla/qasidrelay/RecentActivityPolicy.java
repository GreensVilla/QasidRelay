package org.greensvilla.qasidrelay;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/** Pure formatting and retention rules shared by the activity store and JVM tests. */
final class RecentActivityPolicy {
    static final int MAX_EVENTS = 20;
    private RecentActivityPolicy() { }

    static String fingerprint(String messageId) {
        if (!PendingMessage.hasValidId(messageId)) {
            return "unknown";
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(messageId.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder("job-");
            for (int i = 0; i < 5; i++) {
                out.append(Character.forDigit((digest[i] >>> 4) & 15, 16));
                out.append(Character.forDigit(digest[i] & 15, 16));
            }
            return out.toString();
        } catch (NoSuchAlgorithmException impossible) {
            return "unknown";
        }
    }

    static String state(String value) {
        switch (value == null ? "" : value) {
            case "sending":
            case "sent":
            case "failed":
            case "report pending":
            case "reported":
            case "skipped":
                return value;
            default:
                return "unknown";
        }
    }

    static String cause(String value) {
        if ("RESULT_ERROR_NO_SERVICE".equals(value) ||
            "RESULT_ERROR_RADIO_OFF".equals(value) ||
            "RESULT_ERROR_NULL_PDU".equals(value) ||
            "RESULT_ERROR_GENERIC_FAILURE".equals(value) ||
            "SEND_EXCEPTION".equals(value) ||
            "stale_sms_attempt_without_android_callback".equals(value)) {
            return value;
        }
        return "";
    }

    static <T> void keepLast20(List<T> values) {
        while (values.size() > MAX_EVENTS) {
            values.remove(0);
        }
    }
}
