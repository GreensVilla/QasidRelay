package org.greensvilla.qasidrelay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent == null ? "" : intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action) && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            return;
        }

        GatewayConfig config = GatewayConfig.load(context);
        if (config.messagingEnabled) {
            RelayStatusStore.markManualRestartRequired(
                context,
                "Device or app restarted. Android may block data-sync foreground services from starting at boot; open QasidRelay and tap Start relay service."
            );
        } else {
            RelayStatusStore.markServiceRunning(context, false);
        }
    }
}
