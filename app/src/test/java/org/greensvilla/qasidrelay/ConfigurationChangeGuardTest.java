package org.greensvilla.qasidrelay;

import static org.junit.Assert.*;
import org.junit.Test;

public class ConfigurationChangeGuardTest {
    private GatewayConfig config(String host, String name, String key, boolean enabled) {
        return new GatewayConfig(host, name, key, 10, enabled);
    }

    @Test public void identicalConnectionIsNotBlocked() {
        GatewayConfig old = config("https://example.test", "device-1", "key-1", true);
        assertFalse(ConfigurationChangeGuard.credentialsChanged(old, old));
        assertNull(ConfigurationChangeGuard.blockReason(false, true, 3, true));
    }

    @Test public void rotationRequiresPausedAndSettledRelay() {
        GatewayConfig old = config("https://example.test", "device-1", "key-1", true);
        GatewayConfig changed = config("https://example.test", "device-1", "key-2", true);
        assertTrue(ConfigurationChangeGuard.credentialsChanged(old, changed));
        assertFalse(ConfigurationChangeGuard.gatewayChanged(old, changed));
        assertNotNull(ConfigurationChangeGuard.blockReason(true, true, 0, false));
        assertNotNull(ConfigurationChangeGuard.blockReason(true, false, 1, false));
        assertNotNull(ConfigurationChangeGuard.blockReason(true, false, 0, true));
        assertNull(ConfigurationChangeGuard.blockReason(true, false, 0, false));
    }

    @Test public void newGatewayIdentityCanBeDetectedSeparatelyFromKeyRotation() {
        GatewayConfig old = config("https://example.test", "device-1", "key-1", false);
        GatewayConfig next = config("https://other.test", "device-1", "key-1", false);
        assertTrue(ConfigurationChangeGuard.gatewayChanged(old, next));
    }
}
