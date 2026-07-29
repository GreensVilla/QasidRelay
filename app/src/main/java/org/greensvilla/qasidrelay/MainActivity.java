package org.greensvilla.qasidrelay;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity {
    private static final int PERMISSION_REQUEST = 1001;
    private static final int COLOR_BACKGROUND = 0xFFF8FAFC;
    private static final int COLOR_PANEL = 0xFFFFFFFF;
    private static final int COLOR_TEXT = 0xFF111827;
    private static final int COLOR_MUTED = 0xFF64748B;
    private static final int COLOR_BORDER = 0xFFE2E8F0;
    private static final int COLOR_ACCENT = 0xFF2563EB;
    private static final int COLOR_DANGER = 0xFFB91C1C;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            updateStatus();
            refreshHandler.postDelayed(this, 5_000L);
        }
    };

    private EditText baseUrlInput;
    private EditText deviceIdInput;
    private EditText secretInput;
    private TextView secretHelpText;
    private TextView noticeText;
    private TextView configStatusText;
    private TextView messagingStatusText;
    private TextView permissionStatusText;
    private TextView serviceStatusText;
    private TextView pollStatusText;
    private TextView messageStatusText;
    private TextView queueStatusText;
    private Button messagingToggleButton;
    private TextView messagingHelpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
        refreshHandler.removeCallbacks(refreshRunnable);
        refreshHandler.postDelayed(refreshRunnable, 5_000L);
    }

    @Override
    protected void onPause() {
        refreshHandler.removeCallbacks(refreshRunnable);
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            updateStatus();
        }
    }

    private void render() {
        GatewayConfig config = GatewayConfig.load(this);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(COLOR_BACKGROUND);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(20);
        layout.setPadding(padding, padding, padding, padding);
        scrollView.addView(layout);

        TextView title = new TextView(this);
        title.setText("QasidRelay");
        title.setTextSize(28);
        title.setTextColor(COLOR_TEXT);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Connect this phone to a trusted server, then send approved transactional SMS through its SIM.");
        subtitle.setTextColor(COLOR_MUTED);
        subtitle.setPadding(0, dp(4), 0, dp(16));
        layout.addView(subtitle);

        LinearLayout statusPanel = panel();
        statusPanel.addView(sectionTitle("Relay status"));
        configStatusText = statusRow(statusPanel, "Configuration");
        messagingStatusText = statusRow(statusPanel, "Messaging");
        permissionStatusText = statusRow(statusPanel, "Permissions");
        serviceStatusText = statusRow(statusPanel, "Service");
        pollStatusText = statusRow(statusPanel, "Polling");
        messageStatusText = statusRow(statusPanel, "Last delivery");
        queueStatusText = statusRow(statusPanel, "Report queue");
        layout.addView(statusPanel);

        LinearLayout configPanel = panel();
        configPanel.addView(sectionTitle("Server connection"));
        configPanel.addView(label("Server URL"));
        baseUrlInput = input("https://your-app.example", config.baseUrl);
        configPanel.addView(baseUrlInput);
        configPanel.addView(helper("The HTTPS app URL this phone asks for pending SMS jobs."));
        configPanel.addView(label("Phone name"));
        deviceIdInput = input("shop-phone-01", config.deviceId);
        configPanel.addView(deviceIdInput);
        configPanel.addView(helper("Must match the server's device ID for this phone."));
        configPanel.addView(label("Private relay key"));
        secretInput = input("Leave blank to keep the saved key", "");
        secretInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        configPanel.addView(secretInput);
        secretHelpText = helper("");
        configPanel.addView(secretHelpText);

        Button saveButton = primaryButton("Save configuration");
        saveButton.setOnClickListener(view -> saveConfig(true));
        configPanel.addView(saveButton);
        layout.addView(configPanel);

        LinearLayout controlPanel = panel();
        controlPanel.addView(sectionTitle("Messaging control"));
        Button permissionButton = secondaryButton("Grant required permissions");
        permissionButton.setOnClickListener(view -> requestRequiredPermissions());
        controlPanel.addView(permissionButton);

        messagingToggleButton = primaryButton("Enable messaging");
        messagingToggleButton.setOnClickListener(view -> toggleMessaging());
        controlPanel.addView(messagingToggleButton);

        messagingHelpText = helper("");
        messagingHelpText.setPadding(0, dp(10), 0, 0);
        controlPanel.addView(messagingHelpText);

        noticeText = helper("");
        noticeText.setPadding(0, dp(12), 0, 0);
        controlPanel.addView(noticeText);
        layout.addView(controlPanel);

        TextView policy = helper("Transactional SMS only: OTP, password recovery, operational alerts, and private pilot notifications. The app does not request READ_SMS and does not expose an inbound phone API.");
        policy.setPadding(0, 0, 0, dp(12));
        layout.addView(policy);

        setContentView(scrollView);
        updateStatus();
    }

    private LinearLayout panel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(COLOR_PANEL);
        panel.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(14));
        panel.setLayoutParams(params);
        if (Build.VERSION.SDK_INT >= 21) {
            panel.setElevation(dp(1));
        }
        return panel;
    }

    private TextView sectionTitle(String text) {
        TextView section = new TextView(this);
        section.setText(text);
        section.setTextSize(16);
        section.setTextColor(COLOR_TEXT);
        section.setPadding(0, 0, 0, dp(8));
        return section;
    }

    private TextView statusRow(LinearLayout parent, String label) {
        TextView row = new TextView(this);
        row.setTextColor(COLOR_TEXT);
        row.setTextSize(14);
        row.setPadding(0, dp(4), 0, dp(4));
        row.setText(label + ": --");
        parent.addView(row);
        return row;
    }

    private TextView label(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(COLOR_TEXT);
        label.setPadding(0, dp(10), 0, 0);
        return label;
    }

    private TextView helper(String text) {
        TextView helper = new TextView(this);
        helper.setText(text);
        helper.setTextColor(COLOR_MUTED);
        helper.setTextSize(13);
        return helper;
    }

    private EditText input(String hint, String value) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setText(value);
        input.setSingleLine(true);
        input.setTextColor(COLOR_TEXT);
        input.setHintTextColor(COLOR_MUTED);
        return input;
    }

    private Button primaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundColor(COLOR_ACCENT);
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(COLOR_TEXT);
        button.setBackgroundColor(COLOR_BORDER);
        return button;
    }

    private boolean saveConfig(boolean showToast) {
        GatewayConfig existing = GatewayConfig.load(this);
        String providedSecret = secretInput.getText().toString().trim();
        String secret = providedSecret.isEmpty() ? existing.sharedSecret : providedSecret;
        GatewayConfig config = new GatewayConfig(
            baseUrlInput.getText().toString(),
            deviceIdInput.getText().toString(),
            secret,
            existing.pollSeconds,
            existing.messagingEnabled
        );

        String validationError = config.validationError();
        if (validationError != null) {
            showNotice(validationError);
            RelayStatusStore.markWaiting(this, validationError);
            updateStatus();
            return false;
        }

        try {
            config.save(this);
            secretInput.setText("");
            showNotice("Configuration saved.");
            if (showToast) {
                Toast.makeText(this, "Configuration saved", Toast.LENGTH_SHORT).show();
            }
            updateStatus();
            return true;
        } catch (RuntimeException error) {
            showNotice(error.getMessage());
            return false;
        }
    }

    private void requestRequiredPermissions() {
        ArrayList<String> permissions = new ArrayList<>();
        if (!hasSmsPermission()) {
            permissions.add(Manifest.permission.SEND_SMS);
        }
        if (Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission()) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[0]), PERMISSION_REQUEST);
        }
        updateStatus();
    }

    private void toggleMessaging() {
        GatewayConfig config = GatewayConfig.load(this);
        RelayStatusStore.Snapshot status = RelayStatusStore.load(this);
        if (config.messagingEnabled && status.serviceRunning) {
            disableMessaging("Messaging disabled. No new pending jobs will be fetched or sent.");
        } else {
            enableMessaging();
        }
    }

    private void enableMessaging() {
        if (!saveConfig(false)) {
            return;
        }
        if (!hasSmsPermission()) {
            requestRequiredPermissions();
            showNotice("SMS permission is required before starting the relay.");
            return;
        }

        GatewayConfig.setMessagingEnabled(this, true);
        if (startRelayService()) {
            RelayStatusStore.markServiceRunning(this, true);
            showNotice("Messaging enabled. Relay service starting.");
        }
        updateStatus();
    }

    private boolean startRelayService() {
        Intent intent = new Intent(this, RelayService.class);
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            return true;
        } catch (RuntimeException error) {
            String detail = "Could not start relay service: " + safeDetail(error);
            RelayStatusStore.markManualRestartRequired(this, detail);
            showNotice(detail);
            return false;
        }
    }

    private void disableMessaging(String notice) {
        GatewayConfig.setMessagingEnabled(this, false);
        stopService(new Intent(this, RelayService.class));
        RelayStatusStore.markServiceRunning(this, false);
        showNotice(notice);
        updateStatus();
    }

    private boolean hasSmsPermission() {
        return Build.VERSION.SDK_INT < 23 ||
            checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasNotificationPermission() {
        return Build.VERSION.SDK_INT < 33 ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    private void updateStatus() {
        GatewayConfig config = GatewayConfig.load(this);
        RelayStatusStore.Snapshot status = RelayStatusStore.load(this);
        String validationError = config.validationError();
        boolean serviceNeedsStart = config.messagingEnabled && !status.serviceRunning;
        if (configStatusText != null) {
            configStatusText.setText("Configuration: " + (validationError == null ? "Ready" : validationError));
        }
        if (messagingStatusText != null) {
            messagingStatusText.setText("Messaging: " + (config.messagingEnabled
                ? (serviceNeedsStart ? "Enabled, service stopped" : "Enabled")
                : "Disabled"));
        }
        if (permissionStatusText != null) {
            permissionStatusText.setText("Permissions: SMS " + granted(hasSmsPermission()) +
                ", notifications " + granted(hasNotificationPermission()));
        }
        if (serviceStatusText != null) {
            serviceStatusText.setText("Service: " + status.state + "\n" + status.detail);
        }
        if (pollStatusText != null) {
            pollStatusText.setText("Polling: last poll " + formatTime(status.lastPollAt) +
                ", last success " + formatTime(status.lastSuccessAt));
        }
        if (messageStatusText != null) {
            if (status.lastMessageId.isEmpty()) {
                messageStatusText.setText("Last delivery: none");
            } else {
                messageStatusText.setText("Last delivery: " + status.lastMessageId + " -> " +
                    status.lastMessageStatus + "\n" + status.lastMessageDetail +
                    "\n" + formatTime(status.lastMessageAt));
            }
        }
        if (queueStatusText != null) {
            queueStatusText.setText("Report queue: " + status.pendingReportCount + " pending");
        }
        if (messagingToggleButton != null) {
            if (config.messagingEnabled && status.serviceRunning) {
                messagingToggleButton.setText("Disable messaging");
                messagingToggleButton.setBackgroundColor(COLOR_DANGER);
            } else if (config.messagingEnabled) {
                messagingToggleButton.setText("Start relay service");
                messagingToggleButton.setBackgroundColor(COLOR_ACCENT);
            } else {
                messagingToggleButton.setText("Enable messaging");
                messagingToggleButton.setBackgroundColor(COLOR_ACCENT);
            }
        }
        if (messagingHelpText != null) {
            if (config.messagingEnabled && status.serviceRunning) {
                messagingHelpText.setText("Enabled: this phone may fetch pending jobs and submit SMS through the SIM.");
            } else if (config.messagingEnabled) {
                messagingHelpText.setText("Enabled but not running: tap Start relay service to resume polling after reboot, app update, or service stop.");
            } else {
                messagingHelpText.setText("Disabled: this phone will not fetch pending jobs or send SMS.");
            }
        }
        if (secretHelpText != null) {
            secretHelpText.setText(config.sharedSecret.isEmpty()
                ? "Enter the private relay key from the server. It will be encrypted with Android Keystore."
                : "A private relay key is saved. Leave this blank to keep it, or enter a new key to rotate.");
        }
    }

    private String granted(boolean granted) {
        return granted ? "granted" : "missing";
    }

    private String formatTime(long timestampMillis) {
        if (timestampMillis <= 0L) {
            return "never";
        }
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(new Date(timestampMillis));
    }

    private String safeDetail(RuntimeException error) {
        if (error == null) {
            return "unknown";
        }
        String message = error.getMessage();
        return error.getClass().getSimpleName() + (message == null || message.isEmpty() ? "" : ": " + message);
    }

    private void showNotice(String message) {
        if (noticeText != null) {
            noticeText.setText(message == null ? "" : message);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
