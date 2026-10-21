package org.greensvilla.qasidrelay;

/** Concise notification text made only from allowlisted states and numeric counts. */
final class RelayNotificationText {
    private RelayNotificationText() {}

    static String summary(boolean running, String category, int pendingReports) {
        if (!running) return "Starting relay; open app for details";
        if (pendingReports > 0) {
            return Math.max(0, pendingReports) + " status report(s) waiting";
        }
        switch (category == null ? "" : category) {
            case "idle": return "Connected; waiting for SMS jobs";
            case "active": return "Gateway connected; jobs received";
            case "auth": return "Authentication rejected; open app";
            case "rate_limited": return "Gateway rate limited; open app";
            case "server": return "Gateway unavailable; open app";
            case "network": return "Network unavailable; open app";
            case "timeout": return "Gateway timed out; open app";
            case "tls": return "Secure connection failed; open app";
            case "client": return "Gateway rejected request; open app";
            default: return "Relay running; open app for status";
        }
    }
}
