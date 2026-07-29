package org.greensvilla.qasidrelay;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SmsAttemptStoreInstrumentedTest {
    @Test
    public void createTracksFreshAttemptsWithoutMarkingTerminalLedger() {
        Context context = targetContext();
        String messageId = testMessageId();

        SmsAttemptStore.create(context, messageId, 2);

        assertTrue(SmsAttemptStore.exists(context, messageId));
        assertFalse(SmsAttemptStore.isStale(context, messageId, System.currentTimeMillis()));
        assertFalse(RelayMessageLedger.hasSeen(context, messageId));

        SmsAttemptStore.remove(context, messageId);
    }

    @Test
    public void staleAttemptsExpireAfterTimeoutWindow() {
        Context context = targetContext();
        String messageId = testMessageId();

        SmsAttemptStore.create(context, messageId, 1);

        long staleNow = System.currentTimeMillis() + SmsAttemptStore.staleAfterMillis();
        assertTrue(SmsAttemptStore.isStale(context, messageId, staleNow));

        SmsAttemptStore.remove(context, messageId);
    }

    @Test
    public void removeClearsAttemptState() {
        Context context = targetContext();
        String messageId = testMessageId();

        SmsAttemptStore.create(context, messageId, 1);
        SmsAttemptStore.remove(context, messageId);

        assertFalse(SmsAttemptStore.exists(context, messageId));
        assertFalse(SmsAttemptStore.isStale(context, messageId, System.currentTimeMillis() + SmsAttemptStore.staleAfterMillis()));
    }

    private Context targetContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    private String testMessageId() {
        return "test_" + System.nanoTime();
    }
}
