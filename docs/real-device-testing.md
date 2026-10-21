# Real-device testing

Use a dedicated Android phone and an authorized SIM. Do not test with customer
data, real OTPs, or production recipient lists.

## Prerequisites

- Android 6.0 / API 23 or newer.
- Active SIM plan authorized to send the test message.
- Test gateway reachable over HTTPS with a valid certificate.
- Server-issued phone name/device ID and high-entropy private relay key.
- One controlled destination number owned by the tester.
- Android Debug Bridge (`adb`) available on the workstation.

## Build and install

```sh
./gradlew :app:assembleDebug --no-daemon
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Open QasidRelay on the phone, grant required permissions, then configure by
either scanning the gateway's one-time setup QR or entering the fields manually:

- server URL, for example `https://relay.example`;
- phone name, for example `qasid-device-01`, matching the server device ID;
- private relay key from the test gateway.

When scanning a setup QR, use the phone camera or a QR scanner and open the
`qasidrelay://setup` link in QasidRelay. The app pre-fills the server URL, phone
name, and private relay key; review the fields, then tap `Save configuration`.
Treat the QR and link as secret because they contain the private relay key.

The private relay key field is intentionally blank after saving. Leave it blank
to keep the stored Keystore-encrypted key, or enter a new value to rotate it.

Messaging starts disabled. Use the `Enable messaging` button only after the
gateway settings are correct and the test recipient is controlled by the tester.
Use `Disable messaging` to stop fetching new jobs and sending SMS from the
phone.

## Smoke test

1. Confirm the status screen shows configuration ready and SMS permission
   granted.
2. Tap `Enable messaging` and confirm the foreground notification appears.
3. Queue one transactional test message on the gateway using a synthetic message
   ID and a controlled destination.
4. Confirm the device sends exactly one SMS.
5. Confirm the gateway receives exactly one terminal status for the message ID.
6. Confirm the app status screen shows the last poll, last delivery event, and
   an empty report queue.

## Failure-path checks

- Stop network access after Android sends the SMS but before the status report
  reaches the gateway. The app should show a pending report and should flush it
  after connectivity returns.
- Tap `Disable messaging` and confirm the gateway does not receive further
  pending-message fetches from the phone.
- Test with no mobile service or airplane mode. The gateway should eventually
  receive `failed` with an Android SMS result detail.
- Restart the app while a report is pending. The next service loop should retry
  the queued report before fetching more messages.
- Restart or force-stop during an in-flight SMS attempt only in a controlled
  test. If Android never returns sent callbacks, the app should fail that stale
  attempt closed and queue a terminal `failed` status before sending any new
  job with the same message ID.
- Confirm the app never requests `READ_SMS`.
- Confirm the app never requests camera permission for setup QR handling.
- Confirm cleartext HTTP gateway URLs are rejected by the app.

## Reboot and app-update checks

QasidRelay should not automatically resume polling or sending SMS after a device
reboot or app update. The operator should restart the relay from the foreground
UI so post-reboot SMS sending never happens silently.

1. Enable messaging and confirm the relay foreground notification is visible.
2. Reboot the test phone, or simulate boot with:

   ```sh
   adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p org.greensvilla.qasidrelay
   ```

3. Open QasidRelay and confirm the status screen explains that a manual restart
   is required.
4. Confirm the messaging button says `Start relay service`, not `Disable
   messaging`.
5. Tap `Start relay service` and confirm polling resumes only after this user
   action.
6. Repeat the same expectation after reinstalling/updating the APK, or simulate
   package replacement with:

   ```sh
   adb shell am broadcast -a android.intent.action.MY_PACKAGE_REPLACED -p org.greensvilla.qasidrelay
   ```

## Evidence to capture

- Device model, Android version, and carrier/SIM plan type.
- Redacted app status screenshot.
- Redacted gateway request logs showing signed polling and one terminal status.
- Any vendor-specific battery optimization changes required to keep the
  foreground service alive.
- Reboot/package-replace behavior, including whether the phone shows `Start
  relay service` before polling resumes.

## Operator UI/UX smoke checklist (unreleased)

Run these checks on the operator UX branch before publishing a signed APK:

1. Without a server, confirm **Needs setup** and **Review server connection**
   appear and the action focuses setup. Never photograph or expose the key.
2. With valid setup and missing SMS permission, confirm **Needs SMS permission**
   and its permission action; sending stays blocked.
3. With permission but messaging disabled, confirm **Messaging paused**.
   **Enable messaging** must be an explicit operator action.
4. With messaging enabled and service stopped, confirm **Needs manual restart**.
   The app must not silently resume polling.
5. With successful gateway polling, confirm **Ready** and recent check times.
   Disable connectivity and confirm a warning appears instead of false health.
6. Confirm the last SMS status and report queue show no destination number,
   message content or message ID.
7. Copy diagnostics with and without configured gateway. Paste into an
   offline editor and confirm it contains no secret, setup QR/deep link,
   signature, OTP, message body, recipient, raw error message or message ID.
   Review clipboard text before sharing with trusted support.
8. Open battery settings and confirm the settings/fallback launches. Record
   the actual system behavior; no exemption should be granted automatically.
9. Test an older/small phone, large font scaling, screen rotation, foreground
   return and the Android notification. Check navigation and button visibility.

Record Android version, device model, screenshots with secrets redacted,
significant failures and any observed battery/poll interruptions. This
checklist is not evidence that real-device tests have already passed.

## Recent activity and failure guidance tests (unreleased)

- Send a controlled single-part SMS. Confirm **Submitted to Android**,
  **Android accepted SMS for sending**, pending gateway report, and report
  acknowledgement appear without the body, recipient or raw message ID.
- Produce more than 20 test events. Verify the 20-event limit and initial
  five-row collapsed view; reopen the app to confirm persistence.
- Induce a no-service/radio-off failure on an authorized test SIM. Confirm a
  readable suggestion and no sensitive detail in the UI.
- Tap **Clear local activity**; cancel once, confirm once. Verify pending
  gateway reports and duplicate-send protection still function afterwards.
- Execute `./gradlew :app:connectedDebugAndroidTest` on a physical test phone
  and record the results for `RecentActivityStoreInstrumentedTest`.
- Do not claim recipient delivery from Android sent callbacks.

## Configuration change and reset safety (unreleased)

1. On a controlled staging device, save a new HTTPS origin, device name and key.
   Tap **Check setup locally**. Verify its success message explicitly states
   that the gateway and signing secret were **not** checked remotely.
2. Start messaging and try to change the server URL or private key. Confirm it
   is rejected with **Disable messaging before changing** guidance.
3. Disable messaging; while a test SMS attempt or status report is still
   outstanding, attempt key rotation or **Forget this server**. Confirm both
   are blocked without losing the pending callback/report.
4. After all reports are acknowledged and callbacks settle, save a new server
   identity. Verify the new connection starts disabled, previous gateway UI
   history and message ledger are cleared, and stale last-poll health is reset.
5. On a separate test setup, tap **Forget this server**, cancel once, confirm
   once, and verify the app has no URL, device ID, or saved secret. Confirm it
   cannot resume SMS sending without explicit new setup and operator start.
6. Disable messaging just after a gateway returns multiple pending messages.
   Verify the device does not start new sends from that fetched batch once the
   operator stop takes effect. Capture any observed race separately.
7. Never run destructive reset tests using live OTPs, customer messages,
   production gateway credentials or a real user account.
