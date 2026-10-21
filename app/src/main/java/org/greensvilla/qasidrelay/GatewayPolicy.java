package org.greensvilla.qasidrelay;

/** Strict allowlist for informational policy text supplied by a trusted gateway. */
final class GatewayPolicy {
    static final String NOT_REPORTED = "Policy not reported by server";
    private GatewayPolicy() {}

    static String summary(String mode, String countryPrefix) {
        if ("allowlist_only".equals(mode)) return "Allowlist only";
        if ("country_restricted".equals(mode)) {
            if (countryPrefix != null && countryPrefix.matches("\\+[0-9]{1,3}")) {
                return "Country restricted: " + countryPrefix;
            }
            return "Country restricted";
        }
        if ("self_managed".equals(mode)) {
            return "Unrestricted by gateway — verify operator policy";
        }
        return NOT_REPORTED;
    }
}
