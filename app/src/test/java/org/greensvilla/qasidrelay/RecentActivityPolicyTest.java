package org.greensvilla.qasidrelay;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class RecentActivityPolicyTest {
    @Test public void fingerprintsAreDeterministicAndDoNotExposeIdentifiers() {
        String id = "msg_example_123";
        assertEquals(RecentActivityPolicy.fingerprint(id), RecentActivityPolicy.fingerprint(id));
        assertTrue(RecentActivityPolicy.fingerprint(id).startsWith("job-"));
        assertFalse(RecentActivityPolicy.fingerprint(id).contains(id));
        assertEquals("unknown", RecentActivityPolicy.fingerprint("../not-valid"));
    }

    @Test public void historyRetainsOnlyLast20() {
        List<Integer> events = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            events.add(i);
        }
        RecentActivityPolicy.keepLast20(events);
        assertEquals(20, events.size());
        assertEquals(Integer.valueOf(25), events.get(0));
        assertEquals(Integer.valueOf(44), events.get(19));
    }

    @Test public void refusesUnsafeStatusAndDetails() {
        assertEquals("unknown", RecentActivityPolicy.state("sent +923001234567"));
        assertEquals("", RecentActivityPolicy.cause("server returned OTP 123456"));
        assertEquals("RESULT_ERROR_RADIO_OFF", RecentActivityPolicy.cause("RESULT_ERROR_RADIO_OFF"));
    }
}
