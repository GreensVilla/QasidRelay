package org.greensvilla.qasidrelay;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import java.util.ArrayList;

final class SmsSender {
    void send(Context context, PendingMessage message) {
        SmsManager manager = SmsManager.getDefault();
        ArrayList<String> parts = manager.divideMessage(message.body);
        SmsAttemptStore.create(context, message.id, parts.size());
        ArrayList<PendingIntent> sentIntents = sentIntents(context, message.id, parts.size());
        if (parts.size() > 1) {
            manager.sendMultipartTextMessage(message.to, null, parts, sentIntents, null);
        } else {
            manager.sendTextMessage(message.to, null, message.body, sentIntents.get(0), null);
        }
    }

    private ArrayList<PendingIntent> sentIntents(Context context, String messageId, int totalParts) {
        ArrayList<PendingIntent> intents = new ArrayList<>();
        for (int index = 0; index < Math.max(1, totalParts); index++) {
            Intent intent = new Intent(context, SmsStatusReceiver.class);
            intent.setAction(SmsStatusReceiver.ACTION_SMS_SENT);
            intent.putExtra(SmsStatusReceiver.EXTRA_MESSAGE_ID, messageId);
            intent.putExtra(SmsStatusReceiver.EXTRA_PART_INDEX, index);
            intent.putExtra(SmsStatusReceiver.EXTRA_TOTAL_PARTS, Math.max(1, totalParts));
            int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
            intents.add(PendingIntent.getBroadcast(context, requestCode(messageId, index), intent, flags));
        }
        return intents;
    }

    private int requestCode(String messageId, int partIndex) {
        return (messageId + ":" + partIndex).hashCode();
    }
}
