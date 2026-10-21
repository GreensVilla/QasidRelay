package org.greensvilla.qasidrelay;

/** Passive smoke checklist; never creates SMS jobs or triggers sending. */
final class SmokeTestProgress {
    private SmokeTestProgress() {}

    static boolean canPass(boolean configured, boolean smsPermission, boolean relayRunning,
                           boolean polledAfterStart, boolean submittedAfterStart,
                           boolean reportedAfterStart, boolean lastStatusSent,
                           boolean reportAcknowledged, int pendingReports) {
        return configured && smsPermission && relayRunning &&
            polledAfterStart && submittedAfterStart && reportedAfterStart &&
            lastStatusSent && reportAcknowledged && pendingReports == 0;
    }

    static String summary(boolean active, boolean passed) {
        if (!active) return "Checklist inactive. Start a session before requesting a test SMS.";
        if (passed) return "Local relay smoke passed. Carrier delivery is NOT verified.";
        return "In progress. Use a controlled test recipient and review the steps below.";
    }
}
