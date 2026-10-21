package org.greensvilla.qasidrelay;

/** Static trusted public resources; never construct URLs using device configuration. */
final class SupportLinks {
    static final String[] LABELS = {
        "Operator setup and device tests",
        "Android compatibility / troubleshooting",
        "Security and threat model",
        "Acceptable-use / abuse policy",
        "Protocol reference",
        "Server integration guide",
        "Security issue reporting",
        "Project issue tracker"
    };
    private static final String DOCS = "https://github.com/GreensVilla/QasidRelay/blob/main/";
    static final String[] URLS = {
        DOCS + "docs/real-device-testing.md",
        DOCS + "docs/android-compatibility.md",
        DOCS + "docs/threat-model.md",
        DOCS + "docs/abuse-policy.md",
        DOCS + "docs/protocol.md",
        DOCS + "docs/server-integration.md",
        DOCS + "SECURITY.md",
        "https://github.com/GreensVilla/QasidRelay/issues"
    };

    private SupportLinks() { }

    static String url(int index) {
        return index >= 0 && index < URLS.length ? URLS[index] : null;
    }
}
