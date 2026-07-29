package org.greensvilla.qasidrelay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SetupLinkTest {
    @Test
    public void parsesSetupLinkWithEncodedValues() {
        SetupLink setupLink = SetupLink.parse(
            "qasidrelay://setup?server_url=https%3A%2F%2Fgreensvilla-staging.onrender.com&phone_name=gv-staging-phone-01&private_relay_key=secret%2Bwith%26reserved%3Dchars"
        );

        assertEquals("https://greensvilla-staging.onrender.com", setupLink.baseUrl);
        assertEquals("gv-staging-phone-01", setupLink.deviceId);
        assertEquals("secret+with&reserved=chars", setupLink.sharedSecret);
    }

    @Test
    public void rejectsWrongScheme() {
        assertNull(SetupLink.parse("https://greensvilla-staging.onrender.com/setup"));
    }

    @Test
    public void rejectsIncompleteSetupLink() {
        assertNull(SetupLink.parse(
            "qasidrelay://setup?server_url=https%3A%2F%2Fgreensvilla-staging.onrender.com&phone_name=gv-staging-phone-01"
        ));
    }

    @Test
    public void keepsExistingPollAndMessagingSettingsWhenBuildingGatewayConfig() {
        SetupLink setupLink = SetupLink.parse(
            "qasidrelay://setup?server_url=https%3A%2F%2Fgreensvilla-staging.onrender.com&phone_name=gv-staging-phone-01&private_relay_key=secret"
        );
        GatewayConfig existing = new GatewayConfig(
            "https://old.example",
            "old-phone",
            "old-secret",
            30,
            true
        );

        GatewayConfig config = setupLink.toGatewayConfig(existing);

        assertEquals("https://greensvilla-staging.onrender.com", config.baseUrl);
        assertEquals("gv-staging-phone-01", config.deviceId);
        assertEquals("secret", config.sharedSecret);
        assertEquals(30, config.pollSeconds);
        assertTrue(config.messagingEnabled);
    }
}
