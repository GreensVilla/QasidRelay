package org.greensvilla.qasidrelay;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class QasidSigner {
    private QasidSigner() {
    }

    static String bodySha256(String body) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashed = digest.digest((body == null ? "" : body).getBytes(StandardCharsets.UTF_8));
        return hex(hashed);
    }

    static String sign(String secret, String method, String pathWithQuery, String bodyHash, String timestamp, String nonce)
        throws Exception {
        String canonical = method.toUpperCase(Locale.US) + "\n" +
            pathWithQuery + "\n" +
            bodyHash + "\n" +
            timestamp + "\n" +
            nonce;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return hex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
    }

    private static String hex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format(Locale.US, "%02x", value));
        }
        return builder.toString();
    }
}
