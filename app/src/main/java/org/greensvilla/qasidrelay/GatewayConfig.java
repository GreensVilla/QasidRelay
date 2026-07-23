package org.greensvilla.qasidrelay;

import android.content.Context;
import android.content.SharedPreferences;

final class GatewayConfig {
    static final String PREFS = "qasidrelay";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_SHARED_SECRET = "shared_secret";
    private static final String KEY_POLL_SECONDS = "poll_seconds";

    final String baseUrl;
    final String deviceId;
    final String sharedSecret;
    final int pollSeconds;

    GatewayConfig(String baseUrl, String deviceId, String sharedSecret, int pollSeconds) {
        this.baseUrl = sanitizeBaseUrl(baseUrl);
        this.deviceId = deviceId == null ? "" : deviceId.trim();
        this.sharedSecret = sharedSecret == null ? "" : sharedSecret.trim();
        this.pollSeconds = Math.max(5, pollSeconds);
    }

    static GatewayConfig load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SecureSecretStore secretStore = new SecureSecretStore();
        return new GatewayConfig(
            prefs.getString(KEY_BASE_URL, ""),
            prefs.getString(KEY_DEVICE_ID, ""),
            secretStore.load(prefs.getString(KEY_SHARED_SECRET, "")),
            prefs.getInt(KEY_POLL_SECONDS, 10)
        );
    }

    void save(Context context) {
        SecureSecretStore secretStore = new SecureSecretStore();
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BASE_URL, baseUrl)
            .putString(KEY_DEVICE_ID, deviceId)
            .putString(KEY_SHARED_SECRET, secretStore.save(sharedSecret))
            .putInt(KEY_POLL_SECONDS, pollSeconds)
            .apply();
    }

    boolean isReady() {
        return baseUrl.startsWith("https://") && !deviceId.isEmpty() && !sharedSecret.isEmpty();
    }

    private static String sanitizeBaseUrl(String value) {
        String sanitized = value == null ? "" : value.trim();
        while (sanitized.endsWith("/")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        return sanitized;
    }
}
