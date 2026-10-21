package org.greensvilla.qasidrelay;

import static org.junit.Assert.*;
import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RecentActivityStoreInstrumentedTest {
    @Test public void redactedHistoryPersistsAndRetainsLast20Events() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        RecentActivityStore.clear(context);
        try {
            for (int i = 0; i < 28; i++) {
                RecentActivityStore.record(context, "test_history_" + i, "sent", null);
            }
            List<RecentActivityStore.Entry> recent = RecentActivityStore.recent(context);
            assertEquals(20, recent.size());
            assertEquals(RecentActivityPolicy.fingerprint("test_history_27"), recent.get(0).reference);
            String stored = context.getSharedPreferences(GatewayConfig.PREFS, Context.MODE_PRIVATE)
                .getString("ui_recent_activity_v1", "");
            assertFalse(stored.contains("test_history_"));
            assertFalse(stored.contains("+923001234567"));
        } finally {
            RecentActivityStore.clear(context);
        }
    }

    @Test public void clearDoesNotTouchInFlightAttemptsOrReportQueue() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        RecentActivityStore.clear(context);
        String id = "test_activity_" + System.nanoTime();
        int reportsBefore = StatusReportQueue.count(context);
        try {
            SmsAttemptStore.create(context, id, 1);
            RecentActivityStore.record(context, id, "sending", null);
            RecentActivityStore.clear(context);
            assertEquals(0, RecentActivityStore.recent(context).size());
            assertTrue(SmsAttemptStore.exists(context, id));
            assertEquals(reportsBefore, StatusReportQueue.count(context));
        } finally {
            SmsAttemptStore.remove(context, id);
            RecentActivityStore.clear(context);
        }
    }
}
