package org.greensvilla.qasidrelay;

import static org.junit.Assert.*;
import org.junit.Test;

public class SupportLinksTest {
    @Test public void linksAreTrustedFixedHttpsPaths() {
        assertEquals(SupportLinks.LABELS.length, SupportLinks.URLS.length);
        for (String link : SupportLinks.URLS) {
            assertTrue(link.startsWith("https://github.com/GreensVilla/QasidRelay/"));
            assertFalse(link.contains("?"));
            assertFalse(link.contains("#"));
        }
    }

    @Test public void invalidSelectionCannotCreateLink() {
        assertNull(SupportLinks.url(-1));
        assertNull(SupportLinks.url(SupportLinks.URLS.length));
    }
}
