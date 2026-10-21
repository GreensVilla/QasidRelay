package org.greensvilla.qasidrelay;

/** Safe local version label without contacting any remote service. */
final class VersionPresentation {
    static final String RELEASE_URL = "https://github.com/GreensVilla/QasidRelay/releases";
    private VersionPresentation() { }

    static String installed(String version, int code) {
        String safeVersion = version == null ? "unknown" : version.replaceAll("[^0-9A-Za-z._-]", "");
        if (safeVersion.isEmpty()) {
            safeVersion = "unknown";
        }
        if (safeVersion.length() > 32) {
            safeVersion = safeVersion.substring(0, 32);
        }
        return "Installed: " + safeVersion + " (build " + Math.max(code, 0) + ")";
    }
}
