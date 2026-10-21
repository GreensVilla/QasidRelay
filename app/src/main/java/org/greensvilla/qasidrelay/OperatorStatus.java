package org.greensvilla.qasidrelay;

/** Pure UI state mapping: does not mutate relay configuration or Android services. */
final class OperatorStatus {
    enum Level { READY, WARNING, BLOCKED, NEUTRAL }
    enum Action { NONE, CONFIGURE, PERMISSION, ENABLE, START }

    static final class Presentation {
        final String title;
        final String description;
        final Level level;
        final Action action;
        final String actionLabel;

        Presentation(String title, String description, Level level, Action action, String actionLabel) {
            this.title = title;
            this.description = description;
            this.level = level;
            this.action = action;
            this.actionLabel = actionLabel;
        }
    }

    private OperatorStatus() { }

    static Presentation resolve(boolean configured, boolean smsPermission, boolean enabled,
                                boolean running, String state, long lastSuccessAt,
                                long now, int pollSeconds) {
        if (!configured) {
            return new Presentation("Needs setup",
                "Connect a trusted HTTPS server, phone name, and private relay key.",
                Level.BLOCKED, Action.CONFIGURE, "Review server connection");
        }
        if (!smsPermission) {
            return new Presentation("Needs SMS permission",
                "Android must allow SMS sending before the relay can run.",
                Level.BLOCKED, Action.PERMISSION, "Grant SMS permission");
        }
        if (!enabled) {
            return new Presentation("Messaging paused",
                "This phone is not fetching jobs or sending new SMS.",
                Level.NEUTRAL, Action.ENABLE, "Enable messaging");
        }
        if (!running) {
            return new Presentation("Needs manual restart",
                "Messaging is enabled, but the relay service is stopped.",
                Level.WARNING, Action.START, "Start relay service");
        }
        if ("Poll failed".equals(state)) {
            return new Presentation("Connection problem",
                "The most recent gateway request failed. Check connectivity and server configuration.",
                Level.WARNING, Action.CONFIGURE, "Review server connection");
        }
        if (lastSuccessAt <= 0L) {
            return new Presentation("Connecting",
                "The relay is running and waiting for its first successful check.",
                Level.NEUTRAL, Action.NONE, "");
        }
        long staleAfterMillis = Math.max(120_000L, Math.max(5L, pollSeconds) * 4_000L);
        if (now - lastSuccessAt > staleAfterMillis) {
            return new Presentation("Polling delayed",
                "No recent successful gateway check. Verify network and battery restrictions.",
                Level.WARNING, Action.NONE, "");
        }
        return new Presentation("Ready",
            "Messaging is enabled, and the gateway was contacted recently.",
            Level.READY, Action.NONE, "");
    }
}
