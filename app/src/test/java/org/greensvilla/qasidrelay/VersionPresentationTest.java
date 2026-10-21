package org.greensvilla.qasidrelay;

import static org.junit.Assert.*;
import org.junit.Test;

public class VersionPresentationTest {
    @Test public void formatsInstalledVersion() {
        assertEquals("Installed: 0.1.7 (build 8)", VersionPresentation.installed("0.1.7", 8));
    }

    @Test public void sanitizesUnexpectedMetadata() {
        assertEquals("Installed: unknown (build 0)", VersionPresentation.installed(null, -1));
        assertEquals("Installed: 0.1.8secret (build 9)",
            VersionPresentation.installed("0.1.8\nsecret", 9));
    }

    @Test public void officialReleaseLinkUsesHttps() {
        assertTrue(VersionPresentation.RELEASE_URL.startsWith("https://github.com/GreensVilla/QasidRelay/"));
    }
}
