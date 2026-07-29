package org.greensvilla.qasidrelay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SecureSecretStoreInstrumentedTest {
    @Test
    public void saveReturnsEmptyForMissingSecrets() {
        SecureSecretStore store = new SecureSecretStore();

        assertEquals("", store.save(null));
        assertEquals("", store.save(""));
        assertEquals("", store.save("   "));
    }

    @Test
    public void saveEncryptsAndLoadDecryptsTrimmedSecret() {
        SecureSecretStore store = new SecureSecretStore();

        String encoded = store.save("  test-shared-secret  ");

        assertFalse(encoded.isEmpty());
        assertNotEquals("test-shared-secret", encoded);
        assertEquals("test-shared-secret", store.load(encoded));
    }

    @Test
    public void saveUsesDifferentCiphertextForSameSecret() {
        SecureSecretStore store = new SecureSecretStore();

        String first = store.save("test-shared-secret");
        String second = store.save("test-shared-secret");

        assertNotEquals(first, second);
        assertEquals("test-shared-secret", store.load(first));
        assertEquals("test-shared-secret", store.load(second));
    }

    @Test
    public void loadFailsClosedForInvalidPayloads() {
        SecureSecretStore store = new SecureSecretStore();

        assertEquals("", store.load(null));
        assertEquals("", store.load(""));
        assertEquals("", store.load("not-base64"));
        assertEquals("", store.load("c2hvcnQ="));
    }
}
