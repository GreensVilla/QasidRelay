package org.greensvilla.qasidrelay;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
        return START_STICKY;
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
        while (running) {
            GatewayConfig config = GatewayConfig.load(this);
            if (!config.isReady() || !hasSmsPermission()) {
                sleep(10);
                continue;
            }

            RelayClient client = new RelayClient(config);
            try {
                List<PendingMessage> messages = client.fetchPending();
                for (PendingMessage message : messages) {
                    try {
                        sender.send(message);
                        client.reportStatus(message, "sent", null);
                    } catch (Exception sendError) {
                        client.reportStatus(message, "failed", sendError.getClass().getSimpleName());
                    }
                }
            } catch (Exception error) {
                Log.w(TAG, "Polling failed", error);
            }
            sleep(config.pollSeconds);
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

    private Notification notification() {
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(this, CHANNEL_ID)
            : new Notification.Builder(this);
        return builder
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.relay_running))
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .build();
    }
}
