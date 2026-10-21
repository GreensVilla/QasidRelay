# QasidRelay

QasidRelay is an open-source Android SIM relay for transactional SMS.

It lets a self-hosted application queue SMS messages, then an Android phone with
an authorized SIM polls that application, sends messages through the device SIM,
and reports delivery state back to the server.

QasidRelay is intended for lawful transactional messaging using a SIM and carrier
plan you are authorized to use. It is not designed for unsolicited bulk
messaging, scraping, spam, or bypassing carrier/provider rules.

## Status

Release line: `0.1.x` phone-local Android client.

Latest signed prerelease:
[`v0.1.7`](https://github.com/GreensVilla/QasidRelay/releases/tag/v0.1.7).

If you installed an older CI/debug APK such as `v0.1.5`, Android requires one
uninstall before installing the signed line because debug and release signing
certificates differ. Signed releases `v0.1.6` and newer use the same signing
path and increasing `versionCode`, so they should update in place.

The Android app has a working polling baseline, HMAC-signed requests,
Keystore-backed private relay key storage, simplified operator setup with
optional setup QR/deep-link prefill, local status reporting, Android sent
callbacks, manual restart handling, and request-signing unit tests. A controlled GreensVilla staging OTP smoke test passed on July 29, 2026. Broader device, battery and background-reliability testing remains necessary.

## Operator experience (unreleased)

The setup screen now offers a **local field check** (no network request),
explicitly guards changing server URL/device identity or rotating the key
while the relay is enabled or reports/SMS callbacks are outstanding, and
provides a confirmed **Forget this server** action. Reset clears server
credentials and gateway-specific local history after the safety checks.
It never auto-enables sending or exposes the saved private key.


The second pass adds **Recent activity** (up to 20 redacted local SMS events),
short operator explanations for common SMS send failures, and a confirmed
**Clear local activity** action. Clearing the display does not touch the
pending-report queue, in-flight attempts, or duplicate-send ledger.
Android `sent` callbacks mean accepted for sending—not recipient delivery.
The activity panel, like the dashboard, requires physical-device validation.


The operator UX branch adds a clearer relay overview with a suggested next
action, relative polling times, redacted diagnostics copying, and battery
settings guidance. **These updates are not in the signed v0.1.7 APK.**

Diagnostics omit relay keys, setup links, raw exception messages, message IDs,
SMS contents, and recipient numbers. Review the report before sharing it with
trusted support. Android battery optimization settings are advisory and do not
guarantee reliable background polling.

## Intended use cases

- Development and staging environments that need real SMS without committing to
  a hosted provider yet.
- Private pilots where a maintainer owns the Android device, SIM, and carrier
  package.
- Self-hosted applications that need a simple transactional SMS bridge.

For production at public scale, use a carrier-approved A2P/business SMS provider.

## How it works

```text
Your app/server
  -> stores pending SMS jobs
  <- QasidRelay polls over HTTPS
Android phone
  -> sends SMS using the installed SIM
  -> reports sent/failed status
```

The phone initiates all network traffic. That means the Android device does not
need a public IP address and should not expose an inbound HTTP server.

## Current Android scope

- Java-based Android app.
- Minimum SDK: Android 6.0 / API 23.
- Direct SMS sending through `SmsManager`.
- Phone-local messaging enable/disable control for operators.
- Optional setup deep link: `qasidrelay://setup`.
- Android sent-result callbacks before terminal status reporting.
- Persistent retry queue for status reports.
- Bounded recovery for stale SMS attempts that never receive Android callbacks.
- `remoteMessaging` foreground service for the relay worker.
- Polling-only network model.
- HTTPS-first configuration.
- HMAC-signed requests.
- Cleartext HTTP disabled by Android network security config.
- No camera permission; setup QR scanning is handled by the phone camera or QR
  scanner, then opened in QasidRelay as a deep link.
- No Android `READ_SMS` permission.
- No inbound SMS support in the initial release.
- No bulk-marketing features.

## Repository layout

```text
app/                  Android app module
docs/                 protocol, threat model, testing, and compatibility notes
TODO.impl/            deferred implementation tracks
.github/              CI, issue templates, and PR template
```

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

## Development checks

```sh
./gradlew :app:testDebugUnitTest --no-daemon
./gradlew :app:assembleDebug --no-daemon
```

GitHub Actions uploads `qasidrelay-debug-apk` for CI runs and
`qasidrelay-signed-release-apk` when release signing secrets are configured.
Use the signed release APK for operator testing; keep debug APKs for internal CI
validation only.

## Protocol

See [docs/protocol.md](docs/protocol.md).

For server implementation notes, see
[docs/server-integration.md](docs/server-integration.md).

## Security model

See [docs/threat-model.md](docs/threat-model.md).

## Android compatibility

See [docs/android-compatibility.md](docs/android-compatibility.md).

For manual phone validation, see
[docs/real-device-testing.md](docs/real-device-testing.md).

## License

Apache-2.0. See [LICENSE](LICENSE).
