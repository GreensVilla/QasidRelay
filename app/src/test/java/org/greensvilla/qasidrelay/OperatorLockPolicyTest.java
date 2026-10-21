package org.greensvilla.qasidrelay;
import static org.junit.Assert.*;
import org.junit.Test;

public class OperatorLockPolicyTest {
    @Test public void optInAndSecureDeviceRequired() {
        assertFalse(OperatorLockPolicy.needsCredential(false));
        assertTrue(OperatorLockPolicy.needsCredential(true));
        assertFalse(OperatorLockPolicy.canEnable(false));
        assertTrue(OperatorLockPolicy.canEnable(true));
    }
}
