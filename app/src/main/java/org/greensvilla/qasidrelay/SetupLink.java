package org.greensvilla.qasidrelay;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

final class SetupLink {
    static final String SCHEME = "qasidrelay";
    static final String HOST = "setup";

    final String baseUrl;
    final String deviceId;
    final String sharedSecret;

    private SetupLink(String baseUrl, String deviceId, String sharedSecret) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.deviceId = deviceId == null ? "" : deviceId.trim();
        this.sharedSecret = sharedSecret == null ? "" : sharedSecret.trim();
    }

    static SetupLink parse(String rawUri) {
        if (rawUri == null || rawUri.trim().isEmpty()) {
            return null;
        }

        URI uri;
        try {
            uri = URI.create(rawUri);
        } catch (IllegalArgumentException error) {
            return null;
        }

        if (!SCHEME.equals(uri.getScheme()) || !HOST.equals(uri.getHost())) {
            return null;
        }

        Map<String, String> params = queryParams(uri.getRawQuery());
        SetupLink setupLink = new SetupLink(
            params.get("server_url"),
            params.get("phone_name"),
            params.get("private_relay_key")
        );
        if (!setupLink.isComplete()) {
            return null;
        }
        return setupLink;
    }

    GatewayConfig toGatewayConfig(GatewayConfig existing) {
        return new GatewayConfig(
            baseUrl,
            deviceId,
            sharedSecret,
            existing.pollSeconds,
            existing.messagingEnabled
        );
    }

    private boolean isComplete() {
        return !baseUrl.isEmpty() && !deviceId.isEmpty() && !sharedSecret.isEmpty();
    }

    private static Map<String, String> queryParams(String rawQuery) {
        Map<String, String> params = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return params;
        }

        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            int separator = pair.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = decode(pair.substring(0, separator));
            String value = decode(pair.substring(separator + 1));
            params.put(key, value);
        }

        return params;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException error) {
            throw new IllegalStateException("UTF-8 is required on Android", error);
        }
    }
}
