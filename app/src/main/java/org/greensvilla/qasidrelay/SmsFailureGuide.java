package org.greensvilla.qasidrelay;

import android.app.Activity;
import android.telephony.SmsManager;

/** Safe, allowlisted SMS failure categories for UI and gateway status details. */
final class SmsFailureGuide {
    private SmsFailureGuide() { }

    static Guide forResult(int code) {
        if (code == Activity.RESULT_OK) {
            return new Guide("Android accepted SMS", "The carrier/recipient delivery is not confirmed.", null);
        }
        switch (code) {
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                return new Guide("No cellular service", "Check SIM registration and mobile signal.", "RESULT_ERROR_NO_SERVICE");
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                return new Guide("Cellular radio off", "Turn off airplane mode and check mobile network settings.", "RESULT_ERROR_RADIO_OFF");
            case SmsManager.RESULT_ERROR_NULL_PDU:
                return new Guide("SMS could not be prepared", "Restart the phone and try a controlled test message.", "RESULT_ERROR_NULL_PDU");
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                return new Guide("SMS send failed", "Check carrier balance, SMS package, SIM and signal.", "RESULT_ERROR_GENERIC_FAILURE");
            default:
                return new Guide("SMS send failed", "Check SIM, signal and carrier restrictions before retrying.", "SMS_RESULT_OTHER");
        }
    }

    static Guide forStoredCause(String cause) {
        switch (RecentActivityPolicy.cause(cause)) {
            case "RESULT_ERROR_NO_SERVICE":
                return forResult(SmsManager.RESULT_ERROR_NO_SERVICE);
            case "RESULT_ERROR_RADIO_OFF":
                return forResult(SmsManager.RESULT_ERROR_RADIO_OFF);
            case "RESULT_ERROR_NULL_PDU":
                return forResult(SmsManager.RESULT_ERROR_NULL_PDU);
            case "RESULT_ERROR_GENERIC_FAILURE":
                return forResult(SmsManager.RESULT_ERROR_GENERIC_FAILURE);
            case "stale_sms_attempt_without_android_callback":
                return new Guide("Send confirmation timed out",
                    "Check carrier/device state. Ask support before retrying the same job.",
                    "stale_sms_attempt_without_android_callback");
            case "SEND_EXCEPTION":
                return new Guide("Android could not start sending",
                    "Check SMS permission, SIM availability and restart the relay.",
                    "SEND_EXCEPTION");
            default:
                return new Guide("SMS send failed",
                    "Check SIM, signal and carrier restrictions.", "SMS_RESULT_OTHER");
        }
    }

    static final class Guide {
        final String label;
        final String suggestion;
        final String gatewayCode;

        Guide(String label, String suggestion, String gatewayCode) {
            this.label = label;
            this.suggestion = suggestion;
            this.gatewayCode = gatewayCode;
        }
    }
}
