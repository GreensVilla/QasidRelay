package org.greensvilla.qasidrelay;

import static org.junit.Assert.*;
import android.app.Activity;
import android.telephony.SmsManager;
import org.junit.Test;

public class SmsFailureGuideTest {
    @Test public void successIsNotClaimedAsDelivered() {
        SmsFailureGuide.Guide success = SmsFailureGuide.forResult(Activity.RESULT_OK);
        assertTrue(success.label.contains("accepted"));
        assertNull(success.gatewayCode);
        assertFalse(success.label.toLowerCase().contains("delivered"));
    }

    @Test public void knownAndUnknownFailuresAreActionableAndSafe() {
        SmsFailureGuide.Guide noService = SmsFailureGuide.forResult(SmsManager.RESULT_ERROR_NO_SERVICE);
        assertEquals("RESULT_ERROR_NO_SERVICE", noService.gatewayCode);
        assertTrue(noService.suggestion.contains("signal"));
        SmsFailureGuide.Guide unknown = SmsFailureGuide.forResult(5784378);
        assertEquals("SMS_RESULT_OTHER", unknown.gatewayCode);
        assertFalse(unknown.suggestion.contains("5784378"));
    }

    @Test public void unknownCauseCannotInjectPrivateData() {
        SmsFailureGuide.Guide cause = SmsFailureGuide.forStoredCause("OTP 123456 +923001234567");
        assertEquals("SMS_RESULT_OTHER", cause.gatewayCode);
    }
}
