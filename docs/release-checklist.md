# Release checklist

Use this before publishing an APK from GitHub Releases.

## Build

- `./gradlew :app:testDebugUnitTest --no-daemon`
- `./gradlew :app:assembleDebug --no-daemon`
- Confirm `app/build/outputs/apk/debug/app-debug.apk` installs on a test device.
- Confirm GitHub Actions uploaded the `qasidrelay-debug-apk` artifact.
- If attaching a debug APK to a prerelease, label it clearly as debug-signed and
  testing-only.

## Security

- Confirm no real private relay key/shared secret is committed.
- Confirm no real phone number or OTP appears in examples or screenshots.
- Confirm `READ_SMS` is not requested unless a future release explicitly adds
  inbound SMS support.
- Confirm cleartext traffic remains disabled for release builds.
- Confirm the relay service declares `remoteMessaging`, not `dataSync`, for the
  foreground service type.
- Confirm request-signing tests pass and any new protocol examples avoid real
  OTPs or phone numbers.

## Device smoke

- Install APK on a dedicated Android phone with an authorized SIM.
- Grant `SEND_SMS` and, on Android 13+, notification permission.
- Configure server URL, phone name/device ID, and private relay key.
- Confirm messaging is disabled until the operator taps `Enable messaging`.
- Queue one test transactional SMS on the server.
- Confirm the phone sends exactly one SMS.
- Confirm the server receives exactly one terminal status.
- Tap `Disable messaging` and confirm the phone stops fetching new jobs.
- Force a temporary network failure after send and confirm the status report
  queue retries before new messages are fetched.
- Confirm stale in-flight attempts fail closed with a terminal `failed` status
  instead of being skipped forever.
- Capture any vendor battery-optimization steps needed for stable foreground
  service operation.

## Documentation

- Update `README.md` status.
- Update `docs/protocol.md` for any protocol changes.
- Update `docs/real-device-testing.md` with device-specific findings.
- Record known limitations in the GitHub release notes.
- Do not present a debug APK as a production-signed release APK.
