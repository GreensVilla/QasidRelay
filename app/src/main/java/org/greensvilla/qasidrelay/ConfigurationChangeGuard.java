package org.greensvilla.qasidrelay;

/** Prevents switching remote signing identity while unsettled SMS work remains. */
final class ConfigurationChangeGuard {
    private ConfigurationChangeGuard() { }

    static boolean credentialsChanged(GatewayConfig before, GatewayConfig after) {
        return !before.baseUrl.equals(after.baseUrl)
            || !before.deviceId.equals(after.deviceId)
            || !before.sharedSecret.equals(after.sharedSecret);
    }

    static boolean gatewayChanged(GatewayConfig before, GatewayConfig after) {
        return !before.baseUrl.equals(after.baseUrl)
            || !before.deviceId.equals(after.deviceId);
    }

    static String blockReason(boolean isChange, boolean enabled,
                              int queuedReports, boolean inFlight) {
        if (!isChange) {
            return null;
        }
        if (enabled) {
            return "Disable messaging before changing or forgetting this server.";
        }
        if (inFlight) {
            return "An SMS is still awaiting Android confirmation. Resolve it before changing servers or keys.";
        }
        if (queuedReports > 0) {
            return "Pending status reports must be acknowledged by the current server before changing configuration.";
        }
        return null;
    }
}
