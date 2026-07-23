package org.greensvilla.qasidrelay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class QasidSignerTest {
    @Test
    public void bodySha256HashesEmptyBody() throws Exception {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            QasidSigner.bodySha256("")
        );
    }

    @Test
    public void bodySha256TreatsNullAsEmptyBody() throws Exception {
        assertEquals(QasidSigner.bodySha256(""), QasidSigner.bodySha256(null));
    }

    @Test
    public void bodySha256HashesStatusJson() throws Exception {
        assertEquals(
            "a42751fc3d6e5c412234e3f5ba412e398a0528a5c7eb5913f41dd1a5237ceb62",
            QasidSigner.bodySha256("{\"status\":\"sent\",\"detail\":null}")
        );
    }

    @Test
    public void signUsesProtocolCanonicalForm() throws Exception {
        String signature = QasidSigner.sign(
            "test-secret",
            "post",
            "/api/qasid-relay/v1/messages/pending?device_id=device-01&limit=3",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            "1735689600",
            "00000000-0000-4000-8000-000000000001"
        );

        assertEquals(
            "670662d1044a2704d617990a8724603f54f3ef7d3823152ea9d9d4b60bb556c9",
            signature
        );
    }

    @Test
    public void signChangesWhenTimestampChanges() throws Exception {
        String first = QasidSigner.sign(
            "test-secret",
            "POST",
            "/api/qasid-relay/v1/messages/msg_123/status",
            "a42751fc3d6e5c412234e3f5ba412e398a0528a5c7eb5913f41dd1a5237ceb62",
            "1735689601",
            "nonce-abc"
        );
        String second = QasidSigner.sign(
            "test-secret",
            "POST",
            "/api/qasid-relay/v1/messages/msg_123/status",
            "a42751fc3d6e5c412234e3f5ba412e398a0528a5c7eb5913f41dd1a5237ceb62",
            "1735689602",
            "nonce-abc"
        );

        assertEquals("f56134bf9d2177d0fecbe89f9fdd261cc18fa6367e8b6fc90e57f4eae96de8f1", first);
        assertNotEquals(first, second);
    }
}
