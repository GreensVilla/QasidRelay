package org.greensvilla.qasidrelay;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class OperatorStatusTest {
    @Test public void setupAndPermissionBlockRelay() {
        assertEquals(OperatorStatus.Action.CONFIGURE,
            OperatorStatus.resolve(false, false, false, false, "", 0, 10000, 10).action);
        assertEquals(OperatorStatus.Action.PERMISSION,
            OperatorStatus.resolve(true, false, true, true, "Running", 9000, 10000, 10).action);
    }

    @Test public void pausedAndStoppedHaveSafeActions() {
        assertEquals(OperatorStatus.Action.ENABLE,
            OperatorStatus.resolve(true, true, false, false, "", 0, 10000, 10).action);
        assertEquals(OperatorStatus.Action.START,
            OperatorStatus.resolve(true, true, true, false, "Stopped", 0, 10000, 10).action);
    }

    @Test public void successfulRecentPollingIsReadyAndStalePollingIsNot() {
        assertEquals("Ready", OperatorStatus.resolve(
            true, true, true, true, "Running", 500_000L, 510_000L, 10).title);
        assertEquals("Polling delayed", OperatorStatus.resolve(
            true, true, true, true, "Running", 100_000L, 510_000L, 10).title);
        assertEquals("Connection problem", OperatorStatus.resolve(
            true, true, true, true, "Poll failed", 500_000L, 510_000L, 10).title);
    }
}
