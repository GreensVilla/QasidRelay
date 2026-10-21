package org.greensvilla.qasidrelay;

import static org.junit.Assert.*;
import org.junit.Test;

public class SafeDiagnosticsTest {
    @Test public void hostMustBeHttpsOriginWithoutSecrets() {
        assertEquals("relay.example", SafeDiagnostics.safeServerHost("https://relay.example"));
        assertEquals("not_configured", SafeDiagnostics.safeServerHost(
            "https://relay.example/?token=private-key"));
        assertEquals("not_configured", SafeDiagnostics.safeServerHost(
            "http://relay.example"));
    }

    @Test public void doesNotSerializeSensitiveRelayDetails() {
        String report = SafeDiagnostics.create(
            "abc-123", "0.1.8", 9, "Acme", "Phone", 35, "relay.example",
            true, true, true, false, false, "Poll failed: secret=TOPSECRET",
            123L, 456L, "sent: +923001234567", 2
        );
        assertTrue(report.contains("server_host: relay.example"));
        assertTrue(report.contains("relay_state: Unknown"));
        assertTrue(report.contains("last_sms_status: none"));
        assertFalse(report.contains("TOPSECRET"));
        assertFalse(report.contains("+923001234567"));
        assertFalse(report.contains("private-key"));
    }
}
