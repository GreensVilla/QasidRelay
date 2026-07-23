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
    private EditText pollSecondsInput;
    private TextView secretHelpText;
    private TextView noticeText;
    private TextView configStatusText;
    private TextView permissionStatusText;
    private TextView serviceStatusText;
    private TextView pollStatusText;
    private TextView messageStatusText;
    private TextView queueStatusText;

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
        subtitle.setText("Polling Android SIM SMS relay for authorized transactional messages.");
        subtitle.setTextColor(COLOR_MUTED);
        subtitle.setPadding(0, dp(4), 0, dp(16));
        layout.addView(subtitle);

        LinearLayout statusPanel = panel();
        statusPanel.addView(sectionTitle("Relay status"));
        configStatusText = statusRow(statusPanel, "Configuration");
        permissionStatusText = statusRow(statusPanel, "Permissions");
        serviceStatusText = statusRow(statusPanel, "Service");
        pollStatusText = statusRow(statusPanel, "Polling");
        messageStatusText = statusRow(statusPanel, "Last delivery");
        queueStatusText = statusRow(statusPanel, "Report queue");
        layout.addView(statusPanel);

        LinearLayout configPanel = panel();
        configPanel.addView(sectionTitle("Gateway configuration"));
        configPanel.addView(label("Gateway base URL"));
        baseUrlInput = input("https://relay.example", config.baseUrl);
        configPanel.addView(baseUrlInput);
        configPanel.addView(label("Device ID"));
        deviceIdInput = input("qasid-device-01", config.deviceId);
        configPanel.addView(deviceIdInput);
        configPanel.addView(label("Shared secret"));
        secretInput = input("Leave blank to keep the stored secret", "");
        secretInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        configPanel.addView(secretInput);
        secretHelpText = helper("");
        configPanel.addView(secretHelpText);
        configPanel.addView(label("Poll interval seconds"));
        pollSecondsInput = input("10", String.valueOf(config.pollSeconds));
        pollSecondsInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        configPanel.addView(pollSecondsInput);

        Button saveButton = primaryButton("Save configuration");
        saveButton.setOnClickListener(view -> saveConfig(true));
        configPanel.addView(saveButton);
        layout.addView(configPanel);

        LinearLayout controlPanel = panel();
        controlPanel.addView(sectionTitle("Device controls"));
        Button permissionButton = secondaryButton("Grant required permissions");
        permissionButton.setOnClickListener(view -> requestRequiredPermissions());
        controlPanel.addView(permissionButton);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setPadding(0, dp(8), 0, 0);
        Button startButton = primaryButton("Start relay");
        startButton.setOnClickListener(view -> startRelay());
        Button stopButton = secondaryButton("Stop relay");
        stopButton.setOnClickListener(view -> stopRelay());
        buttonRow.addView(startButton, weightedButtonParams());
        buttonRow.addView(stopButton, weightedButtonParams());
        controlPanel.addView(buttonRow);

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

    private LinearLayout.LayoutParams weightedButtonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        );
        params.setMargins(dp(4), 0, dp(4), 0);
        return params;
    }

    private boolean saveConfig(boolean showToast) {
        GatewayConfig existing = GatewayConfig.load(this);
        String providedSecret = secretInput.getText().toString().trim();
        String secret = providedSecret.isEmpty() ? existing.sharedSecret : providedSecret;
        GatewayConfig config = new GatewayConfig(
            baseUrlInput.getText().toString(),
            deviceIdInput.getText().toString(),
            secret,
            parsePollSeconds(pollSecondsInput.getText().toString())
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

    private int parsePollSeconds(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException error) {
            return 10;
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

    private void startRelay() {
        if (!saveConfig(false)) {
            return;
        }
        if (!hasSmsPermission()) {
            requestRequiredPermissions();
            showNotice("SMS permission is required before starting the relay.");
            return;
        }

        Intent intent = new Intent(this, RelayService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        RelayStatusStore.markServiceRunning(this, true);
        showNotice("Relay service starting.");
        updateStatus();
    }

    private void stopRelay() {
        stopService(new Intent(this, RelayService.class));
        RelayStatusStore.markServiceRunning(this, false);
        showNotice("Relay service stopped.");
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
        if (configStatusText != null) {
            configStatusText.setText("Configuration: " + (validationError == null ? "Ready" : validationError));
        }
        if (permissionStatusText != null) {
            permissionStatusText.setText("Permissions: SMS " + granted(hasSmsPermission()) +
                ", notifications " + granted(hasNotificationPermission()));
        }
        if (serviceStatusText != null) {
            serviceStatusText.setText("Service: " + (status.serviceRunning ? status.state : "Stopped") +
                "\n" + status.detail);
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
        if (secretHelpText != null) {
            secretHelpText.setText(config.sharedSecret.isEmpty()
                ? "Enter the gateway shared secret. It will be encrypted with Android Keystore."
                : "A shared secret is stored. Leave this blank to keep it, or enter a new value to rotate.");
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

    private void showNotice(String message) {
        if (noticeText != null) {
            noticeText.setText(message == null ? "" : message);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
