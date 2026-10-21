package org.greensvilla.qasidrelay;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
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
import java.util.List;
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
    private static final int COLOR_SUCCESS = 0xFF166534;
    private static final int COLOR_WARNING = 0xFF92400E;

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
    private TextView summaryTitleText;
    private TextView summaryDescriptionText;
    private Button suggestedActionButton;
    private TextView batteryStatusText;
    private TextView installedVersionText;
    private LinearLayout activityList;
    private Button activityToggleButton;
    private Button clearActivityButton;
    private boolean showAllActivity;
    private TextView configStatusText;
    private TextView messagingStatusText;
    private TextView permissionStatusText;
    private TextView serviceStatusText;
    private TextView pollStatusText;
    private TextView gatewayStatusText;
    private TextView messageStatusText;
    private TextView queueStatusText;
    private Button messagingToggleButton;
    private TextView messagingHelpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        render();
        applySetupIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        applySetupIntent(intent);
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
        statusPanel.addView(helper("RELAY OVERVIEW"));
        summaryTitleText = new TextView(this);
        summaryTitleText.setTextSize(24);
        summaryTitleText.setTypeface(null, Typeface.BOLD);
        summaryTitleText.setTextColor(COLOR_TEXT);
        summaryTitleText.setPadding(0, dp(7), 0, dp(4));
        statusPanel.addView(summaryTitleText);
        summaryDescriptionText = helper("");
        summaryDescriptionText.setTextSize(14);
        summaryDescriptionText.setPadding(0, 0, 0, dp(8));
        statusPanel.addView(summaryDescriptionText);
        suggestedActionButton = secondaryButton("Review setup");
        suggestedActionButton.setOnClickListener(view -> performSuggestedAction());
        statusPanel.addView(suggestedActionButton);
        statusPanel.addView(sectionTitle("Details"));
        configStatusText = statusRow(statusPanel, "Configuration");
        messagingStatusText = statusRow(statusPanel, "Messaging");
        permissionStatusText = statusRow(statusPanel, "Permissions");
        serviceStatusText = statusRow(statusPanel, "Service");
        pollStatusText = statusRow(statusPanel, "Polling");
        gatewayStatusText = statusRow(statusPanel, "Gateway");
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

        Button checkSetupButton = secondaryButton("Check setup locally");
        checkSetupButton.setOnClickListener(view -> checkSetupLocally());
        configPanel.addView(checkSetupButton);

        Button forgetServerButton = secondaryButton("Forget this server");
        forgetServerButton.setTextColor(COLOR_DANGER);
        forgetServerButton.setOnClickListener(view -> confirmForgetServer());
        configPanel.addView(forgetServerButton);
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

        LinearLayout activityPanel = panel();
        activityPanel.addView(sectionTitle("Recent activity"));
        activityPanel.addView(helper("Only redacted local events are stored. This is not a carrier delivery receipt or an SMS inbox."));
        activityList = new LinearLayout(this);
        activityList.setOrientation(LinearLayout.VERTICAL);
        activityPanel.addView(activityList);
        activityToggleButton = secondaryButton("Show all activity");
        activityToggleButton.setOnClickListener(view -> {
            showAllActivity = !showAllActivity;
            updateRecentActivity();
        });
        activityPanel.addView(activityToggleButton);
        clearActivityButton = secondaryButton("Clear local activity");
        clearActivityButton.setOnClickListener(view -> confirmClearActivity());
        activityPanel.addView(clearActivityButton);
        layout.addView(activityPanel);

        LinearLayout diagnosticsPanel = panel();
        diagnosticsPanel.addView(sectionTitle("Support & diagnostics"));
        diagnosticsPanel.addView(helper("Copy a redacted status summary without relay keys, message contents, or recipient numbers."));
        Button copyDiagnosticsButton = secondaryButton("Copy diagnostics");
        copyDiagnosticsButton.setOnClickListener(view -> copyDiagnostics());
        diagnosticsPanel.addView(copyDiagnosticsButton);
        diagnosticsPanel.addView(helper("Only share diagnostics with trusted support. Review before sending."));
        layout.addView(diagnosticsPanel);

        LinearLayout batteryPanel = panel();
        batteryPanel.addView(sectionTitle("Battery & background reliability"));
        batteryStatusText = helper("");
        batteryPanel.addView(batteryStatusText);
        Button batterySettingsButton = secondaryButton("Open battery settings");
        batterySettingsButton.setOnClickListener(view -> openBatterySettings());
        batteryPanel.addView(batterySettingsButton);
        layout.addView(batteryPanel);

        LinearLayout versionPanel = panel();
        versionPanel.addView(sectionTitle("App version"));
        installedVersionText = helper(installedVersion());
        installedVersionText.setPadding(0, dp(4), 0, dp(6));
        versionPanel.addView(installedVersionText);
        Button releaseButton = secondaryButton("Open official releases");
        releaseButton.setOnClickListener(view -> openOfficialReleases());
        versionPanel.addView(releaseButton);
        Button helpButton = secondaryButton("Help & documentation");
        helpButton.setOnClickListener(view -> showDocumentationLinks());
        versionPanel.addView(helpButton);
        versionPanel.addView(helper("Updates are installed manually. Signed builds normally update in place; older debug builds may need one uninstall."));
        layout.addView(versionPanel);

        TextView policy = helper("Transactional SMS only: OTP, password recovery, operational alerts, and private pilot notifications. The app does not request READ_SMS and does not expose an inbound phone API.");
        policy.setPadding(0, 0, 0, dp(12));
        layout.addView(policy);

        setContentView(scrollView);
        updateStatus();
    }

    private LinearLayout panel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable background = new GradientDrawable();
        background.setColor(COLOR_PANEL);
        background.setCornerRadius(dp(14));
        background.setStroke(dp(1), COLOR_BORDER);
        panel.setBackground(background);
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
        section.setTypeface(null, Typeface.BOLD);
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
        button.setBackground(roundedBackground(COLOR_ACCENT));
        button.setAllCaps(false);
        button.setMinHeight(dp(46));
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(COLOR_TEXT);
        button.setBackground(roundedBackground(COLOR_BORDER));
        button.setAllCaps(false);
        button.setMinHeight(dp(46));
        return button;
    }

    private GradientDrawable roundedBackground(int color) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(dp(10));
        return background;
    }

    private void performSuggestedAction() {
        GatewayConfig config = GatewayConfig.load(this);
        RelayStatusStore.Snapshot status = RelayStatusStore.load(this);
        OperatorStatus.Presentation state = OperatorStatus.resolve(
            config.isReady(), hasSmsPermission(), config.messagingEnabled,
            status.serviceRunning, status.state, status.lastSuccessAt,
            System.currentTimeMillis(), config.pollSeconds
        );
        switch (state.action) {
            case CONFIGURE:
                baseUrlInput.requestFocus();
                showNotice("Check your server URL, phone name, and private relay key below.");
                break;
            case PERMISSION:
                requestRequiredPermissions();
                break;
            case ENABLE:
            case START:
                toggleMessaging();
                break;
            default:
                break;
        }
    }

    private boolean isBatteryExempt() {
        PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return manager != null && manager.isIgnoringBatteryOptimizations(getPackageName());
    }

    private void openBatterySettings() {
        try {
            startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
        } catch (RuntimeException error) {
            try {
                Intent details = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                details.setData(Uri.parse("package:" + getPackageName()));
                startActivity(details);
            } catch (RuntimeException fallbackError) {
                showNotice("Battery settings could not be opened. Open Android Settings, then find QasidRelay under Apps.");
            }
        }
    }

    private void confirmClearActivity() {
        new AlertDialog.Builder(this)
            .setTitle("Clear local activity?")
            .setMessage("This clears only the on-phone activity display. It will not delete pending status reports or duplicate-send safeguards.")
            .setNegativeButton("Cancel", (dialog, which) -> { })
            .setPositiveButton("Clear history", (dialog, which) -> {
                RecentActivityStore.clear(this);
                updateRecentActivity();
                Toast.makeText(this, "Local activity cleared", Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    private void updateRecentActivity() {
        if (activityList == null) {
            return;
        }
        List<RecentActivityStore.Entry> events = RecentActivityStore.recent(this);
        activityList.removeAllViews();
        if (events.isEmpty()) {
            TextView empty = helper("No messages handled by this phone yet.");
            empty.setPadding(0, dp(10), 0, dp(10));
            activityList.addView(empty);
        } else {
            int count = showAllActivity ? events.size() : Math.min(5, events.size());
            for (int i = 0; i < count; i++) {
                RecentActivityStore.Entry event = events.get(i);
                TextView item = new TextView(this);
                String label = activityLabel(event);
                String description = "";
                if ("failed".equals(event.state) && !event.cause.isEmpty()) {
                    description = "\n" + SmsFailureGuide.forStoredCause(event.cause).suggestion;
                }
                item.setText(label + " • " + relativeTime(event.timestamp) +
                    "\n" + event.reference + description);
                item.setTextSize(13);
                item.setTextColor("failed".equals(event.state) ? COLOR_DANGER : COLOR_TEXT);
                item.setPadding(0, dp(12), 0, dp(12));
                activityList.addView(item);
                if (i < count - 1) {
                    View separator = new View(this);
                    separator.setBackgroundColor(COLOR_BORDER);
                    activityList.addView(separator, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));
                }
            }
        }
        if (activityToggleButton != null) {
            activityToggleButton.setVisibility(events.size() > 5 ? View.VISIBLE : View.GONE);
            activityToggleButton.setText(showAllActivity ? "Show fewer events" : "Show all " + events.size() + " events");
        }
        if (clearActivityButton != null) {
            clearActivityButton.setVisibility(events.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private String activityLabel(RecentActivityStore.Entry event) {
        switch (event.state) {
            case "sending":
                return "Submitted to Android";
            case "sent":
                return "Android accepted SMS for sending";
            case "failed":
                return SmsFailureGuide.forStoredCause(event.cause).label;
            case "report pending":
                return "Gateway status report queued";
            case "reported":
                return "Gateway status confirmed";
            case "skipped":
                return "Duplicate or pending attempt skipped";
            default:
                return "Activity";
        }
    }

    private String installedVersion() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return VersionPresentation.installed(info.versionName, info.versionCode);
        } catch (NameNotFoundException error) {
            return VersionPresentation.installed(null, 0);
        }
    }

    private void openOfficialReleases() {
        openOfficialLink(VersionPresentation.RELEASE_URL);
    }

    private void showDocumentationLinks() {
        new AlertDialog.Builder(this)
            .setTitle("Help & documentation")
            .setItems(SupportLinks.LABELS, (dialog, selected) ->
                openOfficialLink(SupportLinks.url(selected)))
            .setNegativeButton("Close", (dialog, selected) -> { })
            .show();
    }

    private void openOfficialLink(String url) {
        if (url == null || !url.startsWith("https://github.com/GreensVilla/QasidRelay/")) {
            showNotice("Unrecognized documentation link.");
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (RuntimeException error) {
            showNotice("Cannot open a browser. View project docs on the official GreensVilla/QasidRelay GitHub page.");
        }
    }

    private void copyDiagnostics() {
        GatewayConfig config = GatewayConfig.load(this);
        RelayStatusStore.Snapshot status = RelayStatusStore.load(this);
        String versionName = "unknown";
        int versionCode = 0;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = info.versionName;
            versionCode = info.versionCode;
        } catch (NameNotFoundException ignored) {
            // Diagnostics are still useful without package metadata.
        }
        String report = SafeDiagnostics.create(
            java.util.UUID.randomUUID().toString(), versionName, versionCode,
            Build.MANUFACTURER, Build.MODEL, Build.VERSION.SDK_INT,
            SafeDiagnostics.safeServerHost(config.baseUrl),
            config.messagingEnabled, status.serviceRunning, hasSmsPermission(),
            hasNotificationPermission(), isBatteryExempt(), status.state,
            status.lastPollAt, status.lastSuccessAt, status.lastMessageStatus,
            status.pendingReportCount
        );
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            showNotice("Clipboard unavailable. Diagnostics were not copied.");
            return;
        }
        clipboard.setPrimaryClip(ClipData.newPlainText("QasidRelay diagnostics", report));
        Toast.makeText(this, "Redacted diagnostics copied. Review before sharing.", Toast.LENGTH_LONG).show();
    }

    private GatewayConfig proposedConfig() {
        GatewayConfig current = GatewayConfig.load(this);
        String newKey = secretInput.getText().toString().trim();
        return new GatewayConfig(baseUrlInput.getText().toString(),
            deviceIdInput.getText().toString(),
            newKey.isEmpty() ? current.sharedSecret : newKey,
            current.pollSeconds, current.messagingEnabled);
    }

    private void checkSetupLocally() {
        String problem = proposedConfig().validationError();
        showNotice(problem == null
            ? "Setup fields are valid locally. This does not check gateway access, credentials, or connectivity."
            : problem);
    }

    private String configurationChangeBlock(boolean changed, GatewayConfig existing) {
        return ConfigurationChangeGuard.blockReason(
            changed, existing.messagingEnabled, StatusReportQueue.count(this),
            SmsAttemptStore.hasAnyPending(this)
        );
    }

    private void confirmForgetServer() {
        GatewayConfig current = GatewayConfig.load(this);
        String block = configurationChangeBlock(true, current);
        if (block != null) {
            showNotice(block);
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("Forget this server?")
            .setMessage("The saved HTTPS server, phone name and encrypted relay key will be cleared. Messaging will remain disabled. This action cannot recover the key; obtain a new one from your trusted gateway.")
            .setNegativeButton("Cancel", (dialog, which) -> { })
            .setPositiveButton("Forget server", (dialog, which) -> {
                GatewayConfig latest = GatewayConfig.load(this);
                String currentBlock = configurationChangeBlock(true, latest);
                if (currentBlock != null) {
                    showNotice(currentBlock);
                    return;
                }
                stopService(new Intent(this, RelayService.class));
                GatewayConfig.forget(this);
                RelayMessageLedger.clear(this);
                RecentActivityStore.clear(this);
                RelayStatusStore.resetForNewGateway(this);
                baseUrlInput.setText("");
                deviceIdInput.setText("");
                secretInput.setText("");
                showNotice("Server forgotten. Enter new setup details before enabling messaging.");
                updateStatus();
            })
            .show();
    }

    private boolean saveConfig(boolean showToast) {
        GatewayConfig existing = GatewayConfig.load(this);
        GatewayConfig config = proposedConfig();

        String validationError = config.validationError();
        if (validationError != null) {
            showNotice(validationError);
            RelayStatusStore.markWaiting(this, validationError);
            updateStatus();
            return false;
        }

        boolean changed = ConfigurationChangeGuard.credentialsChanged(existing, config);
        String block = configurationChangeBlock(changed, existing);
        if (block != null) {
            showNotice(block);
            return false;
        }

        try {
            config.save(this);
            if (ConfigurationChangeGuard.gatewayChanged(existing, config)) {
                // Only when settled and disabled: old IDs/status belong to a different gateway.
                RelayMessageLedger.clear(this);
                RecentActivityStore.clear(this);
                RelayStatusStore.resetForNewGateway(this);
            }
            secretInput.setText("");
            showNotice("Configuration saved. Messaging stays disabled until you start the relay.");
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

    private void applySetupIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        SetupLink setupLink = SetupLink.parse(intent.getDataString());
        if (setupLink == null) {
            return;
        }

        GatewayConfig config = setupLink.toGatewayConfig(GatewayConfig.load(this));
        baseUrlInput.setText(config.baseUrl);
        deviceIdInput.setText(config.deviceId);
        secretInput.setText(config.sharedSecret);

        String validationError = config.validationError();
        if (validationError == null) {
            showNotice("Setup QR loaded. Review the fields, then tap Save configuration. Messaging stays disabled until you enable it.");
        } else {
            showNotice("Setup QR loaded, but the configuration needs review: " + validationError);
        }
        updateStatus();
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
        OperatorStatus.Presentation summary = OperatorStatus.resolve(
            validationError == null, hasSmsPermission(), config.messagingEnabled,
            status.serviceRunning, status.state, status.lastSuccessAt,
            System.currentTimeMillis(), config.pollSeconds
        );
        if (summaryTitleText != null) {
            summaryTitleText.setText(summary.title);
            summaryTitleText.setTextColor(summary.level == OperatorStatus.Level.READY
                ? COLOR_SUCCESS : (summary.level == OperatorStatus.Level.BLOCKED
                ? COLOR_DANGER : (summary.level == OperatorStatus.Level.WARNING
                ? COLOR_WARNING : COLOR_TEXT)));
        }
        if (summaryDescriptionText != null) {
            summaryDescriptionText.setText(summary.description);
        }
        if (suggestedActionButton != null) {
            suggestedActionButton.setVisibility(summary.action == OperatorStatus.Action.NONE
                ? View.GONE : View.VISIBLE);
            suggestedActionButton.setText(summary.actionLabel);
        }
        if (batteryStatusText != null) {
            batteryStatusText.setText(isBatteryExempt()
                ? "Battery optimization exemption detected. Confirm polling still works with the screen off."
                : "Battery settings may interrupt polling on some phones. Verify that the last successful poll keeps updating while the relay is active.");
        }
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
            serviceStatusText.setText("Service: " + SafeDiagnostics.safeState(status.state));
        }
        if (pollStatusText != null) {
            pollStatusText.setText("Last check: " + relativeTime(status.lastPollAt) +
                "\nLast successful check: " + relativeTime(status.lastSuccessAt));
        }
        if (gatewayStatusText != null) {
            gatewayStatusText.setText("Gateway: " +
                GatewayFailureGuide.title(status.gatewayCategory) +
                "\n" + GatewayFailureGuide.action(status.gatewayCategory) +
                (config.messagingEnabled
                    ? "\nConfigured check interval: " + config.pollSeconds + " seconds."
                    : ""));
        }
        if (messageStatusText != null) {
            if (status.lastMessageId.isEmpty()) {
                messageStatusText.setText("Latest SMS attempt: none");
            } else {
                messageStatusText.setText("Latest SMS state: " +
                    ("sent".equals(status.lastMessageStatus) ? "Android accepted for sending" :
                        SafeDiagnostics.safeMessageStatus(status.lastMessageStatus)) +
                    " (" + relativeTime(status.lastMessageAt) + ")");
            }
        }
        if (queueStatusText != null) {
            queueStatusText.setText("Report queue: " + status.pendingReportCount + " pending");
        }
        if (messagingToggleButton != null) {
            if (config.messagingEnabled && status.serviceRunning) {
                messagingToggleButton.setText("Disable messaging");
                messagingToggleButton.setBackground(roundedBackground(COLOR_DANGER));
            } else if (config.messagingEnabled) {
                messagingToggleButton.setText("Start relay service");
                messagingToggleButton.setBackground(roundedBackground(COLOR_ACCENT));
            } else {
                messagingToggleButton.setText("Enable messaging");
                messagingToggleButton.setBackground(roundedBackground(COLOR_ACCENT));
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
        updateRecentActivity();
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

    private String relativeTime(long timestampMillis) {
        if (timestampMillis <= 0L) {
            return "never";
        }
        long elapsed = Math.max(0L, System.currentTimeMillis() - timestampMillis);
        String relative;
        if (elapsed < 60_000L) {
            relative = "just now";
        } else if (elapsed < 3_600_000L) {
            relative = (elapsed / 60_000L) + " min ago";
        } else if (elapsed < 86_400_000L) {
            relative = (elapsed / 3_600_000L) + " hr ago";
        } else {
            relative = (elapsed / 86_400_000L) + " days ago";
        }
        return relative + " (" + formatTime(timestampMillis) + ")";
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
