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

Open QasidRelay on the phone, grant required permissions, then configure:

- server URL, for example `https://relay.example`;
- phone name, for example `qasid-device-01`, matching the server device ID;
- private relay key from the test gateway.

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
