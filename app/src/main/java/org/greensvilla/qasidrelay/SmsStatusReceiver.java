package org.greensvilla.qasidrelay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class SmsStatusReceiver extends BroadcastReceiver {
    static final String ACTION_SMS_SENT = "org.greensvilla.qasidrelay.SMS_SENT";
    static final String EXTRA_MESSAGE_ID = "message_id";
    static final String EXTRA_PART_INDEX = "part_index";
    static final String EXTRA_TOTAL_PARTS = "total_parts";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_SMS_SENT.equals(intent.getAction())) {
            return;
        }

        String messageId = intent.getStringExtra(EXTRA_MESSAGE_ID);
        int partIndex = intent.getIntExtra(EXTRA_PART_INDEX, 0);
        int totalParts = intent.getIntExtra(EXTRA_TOTAL_PARTS, 1);
        int resultCode = getResultCode();
        SmsAttemptStore.CompletedStatus completed = SmsAttemptStore.recordPartResult(
            context,
            messageId,
            partIndex,
            totalParts,
            resultCode,
            SmsFailureGuide.forResult(resultCode).gatewayCode
        );
        if (completed == null) {
            return;
        }

        RecentActivityStore.record(context, messageId, completed.status, completed.detail);
        StatusReportQueue.enqueue(context, messageId, completed.status, completed.detail);
        RelayStatusStore.markReportQueued(context, messageId, completed.status);
        GatewayConfig config = GatewayConfig.load(context);
        if (!config.isReady()) {
            return;
        }

        BroadcastReceiver.PendingResult pendingResult = goAsync();
        new Thread(() -> {
            try {
                StatusReportQueue.flush(context, new RelayClient(config));
            } finally {
                pendingResult.finish();
            }
        }, "QasidRelayStatusReporter").start();
    }

}
