package org.greensvilla.qasidrelay;

import android.content.Context;
import android.content.SharedPreferences;
import java.net.URI;

final class GatewayConfig {
    static final String PREFS = "qasidrelay";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_SHARED_SECRET = "shared_secret";
    private static final String KEY_POLL_SECONDS = "poll_seconds";
    private static final String KEY_MESSAGING_ENABLED = "messaging_enabled";

    final String baseUrl;
    final String deviceId;
    final String sharedSecret;
    final int pollSeconds;
    final boolean messagingEnabled;

    GatewayConfig(String baseUrl, String deviceId, String sharedSecret, int pollSeconds, boolean messagingEnabled) {
        this.baseUrl = sanitizeBaseUrl(baseUrl);
        this.deviceId = deviceId == null ? "" : deviceId.trim();
        this.sharedSecret = sharedSecret == null ? "" : sharedSecret.trim();
        this.pollSeconds = Math.max(5, pollSeconds);
        this.messagingEnabled = messagingEnabled;
    }

    static GatewayConfig load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SecureSecretStore secretStore = new SecureSecretStore();
        return new GatewayConfig(
            prefs.getString(KEY_BASE_URL, ""),
            prefs.getString(KEY_DEVICE_ID, ""),
            secretStore.load(prefs.getString(KEY_SHARED_SECRET, "")),
            prefs.getInt(KEY_POLL_SECONDS, 10),
            prefs.getBoolean(KEY_MESSAGING_ENABLED, false)
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
            .putBoolean(KEY_MESSAGING_ENABLED, messagingEnabled)
            .apply();
    }

    static void forget(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_MESSAGING_ENABLED, false)
            .remove(KEY_BASE_URL)
            .remove(KEY_DEVICE_ID)
            .remove(KEY_SHARED_SECRET)
            .remove(KEY_POLL_SECONDS)
            .apply();
    }

    static void setMessagingEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_MESSAGING_ENABLED, enabled)
            .apply();
    }

    boolean isReady() {
        return validationError() == null;
    }

    String validationError() {
        if (baseUrl.isEmpty()) {
            return "Server URL is required.";
        }
        if (!isHttpsOrigin(baseUrl)) {
            return "Server URL must be an HTTPS origin, for example https://your-app.example.";
        }
        if (deviceId.isEmpty()) {
            return "Phone name is required.";
        }
        if (sharedSecret.isEmpty()) {
            return "Private relay key is required.";
        }
        return null;
    }

    private static String sanitizeBaseUrl(String value) {
        String sanitized = value == null ? "" : value.trim();
        while (sanitized.endsWith("/")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        return sanitized;
    }

    private static boolean isHttpsOrigin(String value) {
        try {
            URI uri = URI.create(value);
            return "https".equals(uri.getScheme()) &&
                uri.getHost() != null &&
                uri.getRawUserInfo() == null &&
                (uri.getRawPath() == null || uri.getRawPath().isEmpty()) &&
                uri.getRawQuery() == null &&
                uri.getRawFragment() == null;
        } catch (IllegalArgumentException error) {
            return false;
        }
    }
}
