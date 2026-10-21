package org.greensvilla.qasidrelay;

import java.net.URI;

/** Allowlisted support fields only: never accepts message bodies, relay secrets or recipient numbers. */
final class SafeDiagnostics {
    private SafeDiagnostics() { }

    static String safeServerHost(String origin) {
        if (origin == null) {
            return "not_configured";
        }
        try {
            URI uri = URI.create(origin);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getRawUserInfo() != null || uri.getRawQuery() != null
                || uri.getRawFragment() != null || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())) {
                return "not_configured";
            }
            return clean(uri.getHost(), 100);
        } catch (IllegalArgumentException error) {
            return "not_configured";
        }
    }

    static String safeState(String state) {
        if ("Running".equals(state) || "Stopped".equals(state) || "Waiting".equals(state)
            || "Polling".equals(state) || "Poll failed".equals(state)
            || "Manual restart required".equals(state)) {
            return state;
        }
        return "Unknown";
    }

    static String safeMessageStatus(String status) {
        if ("sent".equals(status) || "failed".equals(status) || "queued".equals(status)
            || "skipped".equals(status)) {
            return status;
        }
        return "none";
    }

    private static String clean(String value, int maxLength) {
        String safe = value == null ? "unknown" : value.replaceAll("[^a-zA-Z0-9._ -]", "_");
        return safe.substring(0, Math.min(safe.length(), maxLength));
    }

    static String create(String correlationId, String version, int versionCode,
                         String manufacturer, String model, int androidSdk,
                         String serverHost, boolean messagingEnabled, boolean serviceRunning,
                         boolean smsPermission, boolean notificationPermission,
                         boolean batteryExempt, String state, long lastPollAt,
                         long lastSuccessAt, String messageStatus, int pendingReports) {
        StringBuilder result = new StringBuilder();
        result.append("QasidRelay diagnostics (redacted)\n");
        line(result, "support_id", clean(correlationId, 48));
        line(result, "app_version", clean(version, 32));
        line(result, "app_version_code", Integer.toString(versionCode));
        line(result, "android_sdk", Integer.toString(androidSdk));
        line(result, "device_manufacturer", clean(manufacturer, 48));
        line(result, "device_model", clean(model, 48));
        line(result, "server_host", clean(serverHost, 100));
        line(result, "messaging_enabled", Boolean.toString(messagingEnabled));
        line(result, "service_running", Boolean.toString(serviceRunning));
        line(result, "sms_permission", Boolean.toString(smsPermission));
        line(result, "notification_permission", Boolean.toString(notificationPermission));
        line(result, "battery_exempt", Boolean.toString(batteryExempt));
        line(result, "relay_state", safeState(state));
        line(result, "last_poll_epoch_ms", Long.toString(Math.max(0L, lastPollAt)));
        line(result, "last_success_epoch_ms", Long.toString(Math.max(0L, lastSuccessAt)));
        line(result, "last_sms_status", safeMessageStatus(messageStatus));
        line(result, "pending_reports", Integer.toString(Math.max(0, pendingReports)));
        result.append("Review this report before sharing with trusted support.\n");
        return result.toString();
    }

    private static void line(StringBuilder report, String key, String value) {
        report.append(key).append(": ").append(value).append('\n');
    }
}
