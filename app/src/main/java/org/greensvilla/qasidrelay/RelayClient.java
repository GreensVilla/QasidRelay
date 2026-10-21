package org.greensvilla.qasidrelay;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

final class RelayClient {
    private static final String TAG = "QasidRelay";
    private final GatewayConfig config;
    private String lastPolicySummary = GatewayPolicy.NOT_REPORTED;

    RelayClient(GatewayConfig config) {
        this.config = config;
    }

    String lastPolicySummary() {
        return lastPolicySummary;
    }

    List<PendingMessage> fetchPending() throws Exception {
        lastPolicySummary = GatewayPolicy.NOT_REPORTED;
        String encodedDeviceId = URLEncoder.encode(config.deviceId, "UTF-8");
        String path = "/api/qasid-relay/v1/messages/pending?device_id=" + encodedDeviceId + "&limit=3";
        HttpURLConnection connection = null;
        try {
            connection = signedConnection("GET", path, "");
            int status = connection.getResponseCode();
            if (status == 204) {
                return new ArrayList<>();
            }
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Pending request failed with HTTP " + status);
            }

            String body = read(connection.getInputStream());
            JSONObject payload = new JSONObject(body);
            JSONObject policy = payload.optJSONObject("destination_policy");
            if (policy != null) {
                lastPolicySummary = GatewayPolicy.summary(
                    policy.optString("mode", ""), policy.optString("country_prefix", ""));
            }
            JSONArray messages = payload.optJSONArray("messages");
            List<PendingMessage> results = new ArrayList<>();
            if (messages == null) {
                return results;
            }

            for (int index = 0; index < messages.length(); index++) {
                JSONObject object = messages.getJSONObject(index);
                PendingMessage message = new PendingMessage(
                    object.getString("id"),
                    object.getString("to"),
                    object.getString("body")
                );
                if (!PendingMessage.hasValidId(message.id)) {
                    Log.w(TAG, "Skipping pending message with invalid id");
                    continue;
                }
                results.add(message);
            }
            return results;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    void reportStatus(String messageId, String status, String detail) throws Exception {
        if (!PendingMessage.hasValidId(messageId)) {
            throw new IllegalArgumentException("Invalid message id");
        }
        if (!"sent".equals(status) && !"failed".equals(status)) {
            throw new IllegalArgumentException("Invalid status");
        }

        JSONObject payload = new JSONObject();
        payload.put("status", status);
        payload.put("detail", detail == null ? JSONObject.NULL : detail);
        String path = "/api/qasid-relay/v1/messages/" + encodePathSegment(messageId) + "/status";
        String body = payload.toString();
        HttpURLConnection connection = null;
        try {
            connection = signedConnection("POST", path, body);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            try (OutputStream stream = connection.getOutputStream()) {
                stream.write(body.getBytes(StandardCharsets.UTF_8));
            }
            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new IllegalStateException("Status report failed with HTTP " + code);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection signedConnection(String method, String pathWithQuery, String body) throws Exception {
        URL url = new URL(config.baseUrl + pathWithQuery);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(15_000);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "QasidRelay/0.1");

        String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
        String nonce = UUID.randomUUID().toString();
        String bodyHash = QasidSigner.bodySha256(body);
        String signature = QasidSigner.sign(config.sharedSecret, method, pathWithQuery, bodyHash, timestamp, nonce);
        connection.setRequestProperty("X-Qasid-Device-Id", config.deviceId);
        connection.setRequestProperty("X-Qasid-Timestamp", timestamp);
        connection.setRequestProperty("X-Qasid-Nonce", nonce);
        connection.setRequestProperty("X-Qasid-Body-SHA256", bodyHash);
        connection.setRequestProperty("X-Qasid-Signature", signature);
        return connection;
    }

    private String encodePathSegment(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
    }

    private String read(InputStream stream) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }
}
