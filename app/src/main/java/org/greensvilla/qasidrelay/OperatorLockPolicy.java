package org.greensvilla.qasidrelay;

/** Local UI lock policy. The relay service must never depend on UI unlock. */
final class OperatorLockPolicy {
    private OperatorLockPolicy() {}
    static boolean needsCredential(boolean lockEnabled) {
        return lockEnabled;
    }
    static boolean canEnable(boolean deviceSecure) {
        return deviceSecure;
    }
}
