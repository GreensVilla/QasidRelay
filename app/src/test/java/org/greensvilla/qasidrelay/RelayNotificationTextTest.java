package org.greensvilla.qasidrelay;
import static org.junit.Assert.*;
import org.junit.Test;

public class RelayNotificationTextTest {
    @Test public void summaryUsesOnlySafeFields() {
        assertEquals("2 status report(s) waiting", RelayNotificationText.summary(true, "idle", 2));
        assertEquals("Authentication rejected; open app", RelayNotificationText.summary(true, "auth", 0));
        assertEquals("Starting relay; open app for details", RelayNotificationText.summary(false, "idle", 0));
        assertEquals("Relay running; open app for status",
            RelayNotificationText.summary(true, "secret=topsecret +92300", 0));
    }
}
