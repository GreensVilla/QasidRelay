# QasidRelay

QasidRelay is an open-source Android SIM relay for transactional SMS.

It lets a self-hosted application queue SMS messages, then an Android phone with
an authorized SIM polls that application, sends messages through the device SIM,
and reports delivery state back to the server.

QasidRelay is intended for lawful transactional messaging using a SIM and carrier
plan you are authorized to use. It is not designed for unsolicited bulk
messaging, scraping, spam, or bypassing carrier/provider rules.

## Status

Release target: `0.1.0`.

The Android app has a working polling baseline, HMAC-signed requests,
Keystore-backed shared-secret storage, local status reporting, and
request-signing unit tests. The next major milestone is real-device validation
against a test gateway.

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
- Android sent-result callbacks before terminal status reporting.
- Persistent retry queue for status reports.
- Polling-only network model.
- HTTPS-first configuration.
- HMAC-signed requests.
- Cleartext HTTP disabled by Android network security config.
- No Android `READ_SMS` permission.
- No inbound SMS support in the initial release.
- No bulk-marketing features.

## Repository layout

```text
app/                  Android app module
docs/                 protocol, threat model, testing, and compatibility notes
.github/              CI, issue templates, and PR template
```

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

## Development checks

```sh
./gradlew :app:testDebugUnitTest --no-daemon
./gradlew :app:assembleDebug --no-daemon
```

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
