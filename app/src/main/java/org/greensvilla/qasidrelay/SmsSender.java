package org.greensvilla.qasidrelay;

import android.telephony.SmsManager;
import java.util.ArrayList;

final class SmsSender {
    void send(PendingMessage message) {
        SmsManager manager = SmsManager.getDefault();
        ArrayList<String> parts = manager.divideMessage(message.body);
        if (parts.size() > 1) {
            manager.sendMultipartTextMessage(message.to, null, parts, null, null);
        } else {
            manager.sendTextMessage(message.to, null, message.body, null, null);
        }
    }
}
