package org.greensvilla.qasidrelay;

import static org.junit.Assert.*;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLHandshakeException;
import org.junit.Test;

public class GatewayFailureGuideTest {
    @Test public void httpFailuresAreNotConfusedWithNoJobs() {
        assertEquals("auth", GatewayFailureGuide.classify(
            new IllegalStateException("Pending request failed with HTTP 401")));
        assertEquals("auth", GatewayFailureGuide.classify(
            new IllegalStateException("Status report failed with HTTP 403")));
        assertEquals("server", GatewayFailureGuide.classify(
            new IllegalStateException("Pending request failed with HTTP 503")));
        assertEquals("rate_limited", GatewayFailureGuide.classify(
            new IllegalStateException("Pending request failed with HTTP 429")));
        assertEquals("Connected — no pending SMS jobs", GatewayFailureGuide.title("idle"));
    }
    @Test public void networkAndTlsErrorsAreDistinct() {
        assertEquals("timeout", GatewayFailureGuide.classify(new SocketTimeoutException("secret 123")));
        assertEquals("network", GatewayFailureGuide.classify(new UnknownHostException("gateway.private")));
        assertEquals("tls", GatewayFailureGuide.classify(new SSLHandshakeException("bad certificate")));
    }
    @Test public void unexpectedErrorMessagesAreNotReflectedToUI() {
        String category = GatewayFailureGuide.classify(
            new IllegalArgumentException("secret=TOPSECRET recipient=+923001234567"));
        assertEquals("unknown", category);
        assertFalse(GatewayFailureGuide.title(category).contains("TOPSECRET"));
        assertFalse(GatewayFailureGuide.action(category).contains("+923001234567"));
    }
}
