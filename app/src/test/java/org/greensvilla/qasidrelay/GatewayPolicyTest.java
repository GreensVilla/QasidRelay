package org.greensvilla.qasidrelay;
import static org.junit.Assert.*;
import org.junit.Test;
public class GatewayPolicyTest {
    @Test public void knownServerValuesAreReadable() {
        assertEquals("Allowlist only", GatewayPolicy.summary("allowlist_only", null));
        assertEquals("Country restricted: +92", GatewayPolicy.summary("country_restricted", "+92"));
        assertEquals("Country restricted", GatewayPolicy.summary("country_restricted", "secret"));
        assertTrue(GatewayPolicy.summary("self_managed", "").contains("Unrestricted"));
    }
    @Test public void maliciousServerStringsAreNeverRendered() {
        assertEquals(GatewayPolicy.NOT_REPORTED, GatewayPolicy.summary("OTP 123456", "+923000000000"));
        assertFalse(GatewayPolicy.summary("country_restricted", "<script>").contains("script"));
    }
}
