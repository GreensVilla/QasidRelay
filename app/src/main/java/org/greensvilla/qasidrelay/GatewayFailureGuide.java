package org.greensvilla.qasidrelay;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLException;

/** Coarse, safe gateway network categories. Never retain exception messages. */
final class GatewayFailureGuide {
    private static final Pattern HTTP_CODE = Pattern.compile("\\bHTTP ([0-9]{3})\\b");

    private GatewayFailureGuide() { }

    static String classify(Throwable error) {
        Throwable cause = error;
        for (int depth = 0; cause != null && depth < 5; depth++, cause = cause.getCause()) {
            if (cause instanceof SSLException) {
                return "tls";
            }
            if (cause instanceof SocketTimeoutException) {
                return "timeout";
            }
            if (cause instanceof UnknownHostException || cause instanceof ConnectException ||
                cause instanceof SocketException) {
                return "network";
            }
            String message = cause.getMessage();
            if (message == null) {
                continue;
            }
            Matcher match = HTTP_CODE.matcher(message);
            if (match.find()) {
                int code = Integer.parseInt(match.group(1));
                if (code == 401 || code == 403) return "auth";
                if (code == 429) return "rate_limited";
                if (code >= 500 && code <= 599) return "server";
                if (code >= 400 && code <= 499) return "client";
                return "unknown";
            }
        }
        return "unknown";
    }

    static String title(String category) {
        switch (category == null ? "" : category) {
            case "idle": return "Connected — no pending SMS jobs";
            case "active": return "Connected — jobs received";
            case "auth": return "Authentication rejected";
            case "rate_limited": return "Gateway rate limit reached";
            case "server": return "Gateway server unavailable";
            case "client": return "Gateway request rejected";
            case "tls": return "Secure connection failed";
            case "timeout": return "Gateway connection timed out";
            case "network": return "Network connection unavailable";
            default: return "Gateway status unavailable";
        }
    }

    static String action(String category) {
        switch (category == null ? "" : category) {
            case "auth": return "Check the server's phone name and relay key.";
            case "rate_limited": return "Leave the relay running; check server limits.";
            case "server": return "Check gateway health and try again after recovery.";
            case "client": return "Check the configured server URL and protocol version.";
            case "tls": return "Check HTTPS certificate and phone date/time.";
            case "timeout":
            case "network": return "Check internet access, Wi-Fi and cellular data.";
            case "idle": return "Waiting for approved transactional SMS jobs.";
            case "active": return "The gateway recently returned pending jobs.";
            default: return "Verify gateway configuration and connectivity.";
        }
    }
}
