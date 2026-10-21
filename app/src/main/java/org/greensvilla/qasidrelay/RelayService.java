package org.greensvilla.qasidrelay;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import java.util.List;

public class RelayService extends Service {
    private static final String TAG = "QasidRelay";
    private static final String CHANNEL_ID = "relay-status";
    private static final String STALE_SMS_ATTEMPT_DETAIL = "stale_sms_attempt_without_android_callback";
    private volatile boolean running;
    private Thread worker;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(100, notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (worker == null || !worker.isAlive()) {
            running = true;
            worker = new Thread(this::runLoop, "QasidRelayWorker");
            worker.start();
        }
        // Never ask Android to silently recreate the SMS relay after process death.
        // A foreground operator action is required to resume.
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        if (worker != null) {
            worker.interrupt();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void runLoop() {
        SmsSender sender = new SmsSender();
        RelayStatusStore.markServiceRunning(this, true);
        try {
            while (running) {
                refreshNotification();
                GatewayConfig config = GatewayConfig.load(this);
                if (!config.messagingEnabled) {
                    RelayStatusStore.markWaiting(this, "Messaging is disabled by the operator; pending jobs will not be fetched.");
                    sleep(10);
                    continue;
                }
                String validationError = config.validationError();
                if (validationError != null) {
                    RelayStatusStore.markWaiting(this, validationError);
                    sleep(10);
                    continue;
                }
                if (!hasSmsPermission()) {
                    RelayStatusStore.markWaiting(this, "SMS permission is required before relay polling can start.");
                    sleep(10);
                    continue;
                }

                RelayClient client = new RelayClient(config);
                try {
                    if (!StatusReportQueue.flush(this, client)) {
                        sleep(config.pollSeconds);
                        continue;
                    }

                    RelayStatusStore.markPolling(this);
                    List<PendingMessage> messages = client.fetchPending();
                    RelayStatusStore.markPollSucceeded(this, messages.size());
                    RelayStatusStore.markGatewayPolicy(this, client.lastPolicySummary());
                    for (PendingMessage message : messages) {
                        // A foreground operator stop must prevent additional queued sends.
                        if (!running || !GatewayConfig.load(this).messagingEnabled) {
                            break;
                        }
                        String messageValidationError = message.validationError();
                        if (messageValidationError != null) {
                            RecentActivityStore.record(this, message.id, "failed", null);
                            StatusReportQueue.enqueue(this, message.id, "failed", messageValidationError);
                            StatusReportQueue.flush(this, client);
                            continue;
                        }
                        if (SmsAttemptStore.exists(this, message.id)) {
                            if (SmsAttemptStore.isStale(this, message.id, System.currentTimeMillis())) {
                                SmsAttemptStore.remove(this, message.id);
                                RecentActivityStore.record(this, message.id, "failed", STALE_SMS_ATTEMPT_DETAIL);
                                StatusReportQueue.enqueue(this, message.id, "failed", STALE_SMS_ATTEMPT_DETAIL);
                                StatusReportQueue.flush(this, client);
                                RelayStatusStore.markMessageSkipped(
                                    this,
                                    message,
                                    "Stale SMS attempt expired; failed status queued for the gateway."
                                );
                                continue;
                            }
                            RelayStatusStore.markMessageSkipped(
                                this,
                                message,
                                "SMS send attempt is still waiting for Android sent callbacks."
                            );
                            continue;
                        }
                        if (RelayMessageLedger.hasSeen(this, message.id)) {
                            RelayStatusStore.markMessageSkipped(this, message, "Message ID already has local terminal status.");
                            continue;
                        }

                        try {
                            if (!running || !GatewayConfig.load(this).messagingEnabled) {
                                break;
                            }
                            sender.send(this, message);
                            RelayStatusStore.markMessageQueued(this, message);
                        } catch (Exception sendError) {
                            SmsAttemptStore.remove(this, message.id);
                            RecentActivityStore.record(this, message.id, "failed", "SEND_EXCEPTION");
                            StatusReportQueue.enqueue(this, message.id, "failed", "SEND_EXCEPTION");
                            StatusReportQueue.flush(this, client);
                        }
                    }
                } catch (Exception error) {
                    Log.w(TAG, "Polling failed: " + GatewayFailureGuide.classify(error));
                    RelayStatusStore.markPollFailed(this, error);
                }
                sleep(config.pollSeconds);
            }
        } finally {
            RelayStatusStore.markServiceRunning(this, false);
        }
    }

    private boolean hasSmsPermission() {
        return Build.VERSION.SDK_INT < 23 ||
            checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void refreshNotification() {
        if (!running) return;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(100, notification());
        }
    }

    private Notification notification() {
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(this, CHANNEL_ID)
            : new Notification.Builder(this);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        RelayStatusStore.Snapshot state = RelayStatusStore.load(this);
        return builder
            .setContentTitle(getString(R.string.app_name))
            .setContentText(RelayNotificationText.summary(
                state.serviceRunning, state.gatewayCategory, state.pendingReportCount))
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .build();
    }
}
