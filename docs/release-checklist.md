# Release checklist

Use this before publishing an APK from GitHub Releases.

## Build

- `./gradlew :app:assembleDebug`
- Confirm `app/build/outputs/apk/debug/app-debug.apk` installs on a test device.

## Security

- Confirm no real shared secret is committed.
- Confirm no real phone number or OTP appears in examples or screenshots.
- Confirm `READ_SMS` is not requested unless a future release explicitly adds
  inbound SMS support.
- Confirm cleartext traffic remains disabled for release builds.

## Device smoke

- Install APK on a dedicated Android phone with an authorized SIM.
- Grant `SEND_SMS`.
- Configure gateway URL, device ID, and shared secret.
- Queue one test transactional SMS on the server.
- Confirm the phone sends exactly one SMS.
- Confirm the server receives exactly one terminal status.

## Documentation

- Update `README.md` status.
- Update `docs/protocol.md` for any protocol changes.
- Record known limitations in the GitHub release notes.
