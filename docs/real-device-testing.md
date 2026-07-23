# Real-device testing

Use a dedicated Android phone and an authorized SIM. Do not test with customer
data, real OTPs, or production recipient lists.

## Prerequisites

- Android 6.0 / API 23 or newer.
- Active SIM plan authorized to send the test message.
- Test gateway reachable over HTTPS with a valid certificate.
- Server-issued `device_id` and high-entropy shared secret.
- One controlled destination number owned by the tester.
- Android Debug Bridge (`adb`) available on the workstation.

## Build and install

```sh
./gradlew :app:assembleDebug --no-daemon
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Open QasidRelay on the phone, grant required permissions, then configure:

- gateway base URL, for example `https://relay.example`;
- device ID, for example `qasid-device-01`;
- shared secret from the test gateway;
- poll interval, minimum 5 seconds.

The shared secret field is intentionally blank after saving. Leave it blank to
keep the stored Keystore-encrypted secret, or enter a new value to rotate it.

## Smoke test

1. Confirm the status screen shows configuration ready and SMS permission
   granted.
2. Start the relay and confirm the foreground notification appears.
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
- Test with no mobile service or airplane mode. The gateway should eventually
  receive `failed` with an Android SMS result detail.
- Restart the app while a report is pending. The next service loop should retry
  the queued report before fetching more messages.
- Confirm the app never requests `READ_SMS`.
- Confirm cleartext HTTP gateway URLs are rejected by the app.

## Evidence to capture

- Device model, Android version, and carrier/SIM plan type.
- Redacted app status screenshot.
- Redacted gateway request logs showing signed polling and one terminal status.
- Any vendor-specific battery optimization changes required to keep the
  foreground service alive.
