package org.greensvilla.qasidrelay;

import static org.junit.Assert.*;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RelayLifecycleInstrumentedTest {
    @Test public void fastSentCallbackCannotBecomeQueuedOrSkippedAgain() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences(GatewayConfig.PREFS, Context.MODE_PRIVATE);
        String id = "regression_" + System.nanoTime();
        PendingMessage message = new PendingMessage(id, "+923000000001", "never actually sent");
        try {
            RelayStatusStore.markStatusReported(context, id, "sent");
            RelayStatusStore.markMessageQueued(context, message);
            assertEquals("sent", RelayStatusStore.load(context).lastMessageStatus);
            RelayStatusStore.markMessageSkipped(context, message, "duplicate");
            assertEquals("sent", RelayStatusStore.load(context).lastMessageStatus);
        } finally {
            prefs.edit()
                .remove("status_last_message_id")
                .remove("status_last_message_status")
                .remove("status_last_message_detail")
                .remove("status_last_message_at").commit();
            RecentActivityStore.clear(context);
        }
    }

    @Test public void failureCannotBeOverwrittenByLateQueuedUpdate() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences(GatewayConfig.PREFS, Context.MODE_PRIVATE);
        String id = "regression_" + System.nanoTime();
        try {
            RelayStatusStore.markStatusReported(context, id, "failed");
            RelayStatusStore.markMessageQueued(context,
                new PendingMessage(id, "+923000000001", "never actually sent"));
            assertEquals("failed", RelayStatusStore.load(context).lastMessageStatus);
        } finally {
            prefs.edit()
                .remove("status_last_message_id")
                .remove("status_last_message_status")
                .remove("status_last_message_detail")
                .remove("status_last_message_at").commit();
            RecentActivityStore.clear(context);
        }
    }

    @Test public void freshTestProcessDoesNotAssumePersistedRunningStatus() {
        assertFalse("Only an actively running service can claim a live relay",
            RelayService.isActive());
    }
}
