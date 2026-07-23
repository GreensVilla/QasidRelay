package org.greensvilla.qasidrelay;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private EditText baseUrlInput;
    private EditText deviceIdInput;
    private EditText secretInput;
    private EditText pollSecondsInput;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        render();
    }

    private void render() {
        GatewayConfig config = GatewayConfig.load(this);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(20);
        layout.setPadding(padding, padding, padding, padding);
        scrollView.addView(layout);

        TextView title = new TextView(this);
        title.setText("QasidRelay");
        title.setTextSize(28);
        title.setTextColor(0xFF111827);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Secure polling-based Android SIM relay for transactional SMS. The device polls your server; it does not expose an inbound API.");
        subtitle.setTextColor(0xFF4B5563);
        layout.addView(subtitle);

        layout.addView(section("Gateway configuration"));

        baseUrlInput = input("Gateway base URL, e.g. https://example.com", config.baseUrl);
        deviceIdInput = input("Device ID", config.deviceId);
        secretInput = input("Shared secret", config.sharedSecret);
        secretInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pollSecondsInput = input("Poll interval seconds", String.valueOf(config.pollSeconds));
        pollSecondsInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        layout.addView(baseUrlInput);
        layout.addView(deviceIdInput);
        layout.addView(secretInput);
        layout.addView(pollSecondsInput);

        Button saveButton = new Button(this);
        saveButton.setText("Save configuration");
        saveButton.setOnClickListener(view -> saveConfig());
        layout.addView(saveButton);

        layout.addView(section("Device controls"));

        Button permissionButton = new Button(this);
        permissionButton.setText("Grant SMS permission");
        permissionButton.setOnClickListener(view -> requestSmsPermission());
        layout.addView(permissionButton);

        Button startButton = new Button(this);
        startButton.setText("Start relay");
        startButton.setOnClickListener(view -> {
            saveConfig();
            startService(new Intent(this, RelayService.class));
            updateStatus();
        });
        layout.addView(startButton);

        Button stopButton = new Button(this);
        stopButton.setText("Stop relay");
        stopButton.setOnClickListener(view -> {
            stopService(new Intent(this, RelayService.class));
            updateStatus();
        });
        layout.addView(stopButton);

        statusText = new TextView(this);
        statusText.setTextColor(0xFF111827);
        statusText.setPadding(0, dp(16), 0, 0);
        layout.addView(statusText);

        TextView policy = new TextView(this);
        policy.setText("Use only for authorized transactional SMS. QasidRelay does not read inbound SMS and does not support bulk marketing workflows.");
        policy.setTextColor(0xFF6B7280);
        policy.setPadding(0, dp(16), 0, 0);
        layout.addView(policy);

        setContentView(scrollView);
        updateStatus();
    }

    private EditText input(String hint, String value) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setText(value);
        input.setSingleLine(true);
        input.setTextColor(0xFF111827);
        input.setHintTextColor(0xFF6B7280);
        return input;
    }

    private TextView section(String text) {
        TextView section = new TextView(this);
        section.setText(text);
        section.setTextSize(16);
        section.setTextColor(0xFF1D4ED8);
        section.setPadding(0, dp(22), 0, dp(8));
        return section;
    }

    private void saveConfig() {
        int pollSeconds = parsePollSeconds(pollSecondsInput.getText().toString());
        new GatewayConfig(
            baseUrlInput.getText().toString(),
            deviceIdInput.getText().toString(),
            secretInput.getText().toString(),
            pollSeconds
        ).save(this);
        updateStatus();
    }

    private int parsePollSeconds(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException error) {
            return 10;
        }
    }

    private void requestSmsPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.SEND_SMS }, 1001);
        }
        updateStatus();
    }

    private void updateStatus() {
        GatewayConfig config = GatewayConfig.load(this);
        boolean hasSmsPermission = android.os.Build.VERSION.SDK_INT < 23 ||
            checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        String ready = config.isReady() ? "Configuration: ready" : "Configuration: incomplete";
        String permission = hasSmsPermission ? "SMS permission: granted" : "SMS permission: missing";
        if (statusText != null) {
            statusText.setText(ready + "\n" + permission);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
