# QasidRelay handoff

Updated: 2026-07-23

## Repository

- GitHub: <https://github.com/GreensVilla/QasidRelay>
- Visibility: public
- Default branch: `main`
- License: Apache-2.0
- Current baseline commit: `db3ab447704e7b333f376dd7b238bb8aaa21fe24`
- Description: `Open-source Android SIM relay for secure transactional SMS.`
- Topics: `android`, `android-app`, `otp`, `self-hosted`, `sim-gateway`,
  `sms`, `sms-gateway`, `transactional-sms`

## Current baseline

The repo has a CI-passing first Android/OSS baseline:

- Java Android app module under `app/`
- package/application ID: `org.greensvilla.qasidrelay`
- min SDK: 23
- target/compile SDK: 35
- Gradle wrapper: `8.10.2`
- app sends SMS through `SmsManager`
- app polls the server; it does not expose an inbound device API
- no `READ_SMS` permission
- `SEND_SMS`, `INTERNET`, foreground-service, and notification permissions only
- cleartext HTTP disabled through Android network security config
- shared relay secret encrypted at rest with Android Keystore AES-GCM
- HMAC-signed server requests

## CI evidence

GitHub Actions run `29994009682` passed on `main`:

- `static`: required OSS files, placeholder scan, and restricted SMS-scope check
- `android`: `./gradlew :app:assembleDebug --no-daemon`

The only annotations were GitHub hosted-runner Node.js deprecation notices for
actions; both jobs passed.

## Protocol direction

The Android app expects a polling server contract documented in
[`docs/protocol.md`](docs/protocol.md):

- `GET /api/qasid-relay/v1/messages/pending?device_id=<device_id>&limit=3`
- `POST /api/qasid-relay/v1/messages/:id/status`
- signed headers:
  - `X-Qasid-Device-Id`
  - `X-Qasid-Timestamp`
  - `X-Qasid-Nonce`
  - `X-Qasid-Body-SHA256`
  - `X-Qasid-Signature`

Server-side requirements still need implementation in GreensVilla or a generic
reference server/client package.

## Security posture

Do not weaken these defaults without explicit discussion:

- no inbound public API on the phone;
- no inbound SMS reading in v0;
- no bulk-marketing workflows;
- no cleartext HTTP;
- no public OTP/phone-number examples;
- no committed secrets;
- HMAC signatures with timestamp and nonce replay protection;
- server-side rate limits and destination policy required.

Relevant docs:

- [`docs/threat-model.md`](docs/threat-model.md)
- [`docs/abuse-policy.md`](docs/abuse-policy.md)
- [`docs/release-checklist.md`](docs/release-checklist.md)
- [`docs/android-compatibility.md`](docs/android-compatibility.md)

## Next QasidRelay tasks

1. Test APK on a real Android phone with a SIM.
2. Add a minimal reference server endpoint or contract tests.
3. Add Android unit tests for `QasidSigner` and `SecureSecretStore` where
   practical.
4. Improve foreground-service resilience:
   - boot/restart behavior;
   - battery-optimization guidance;
   - visible last-poll/last-send status.
5. Add dual-SIM selection only after testing the target phone.
6. Decide client-package naming conventions:
   - proposed by owner: `QasidRelay-rb`, `QasidRelay-js`,
     `QasidRelay-rust`, `QasidRelay-python`.
   - Keep protocol docs authoritative so client repos stay consistent.

## Current blocker

No active code blocker in QasidRelay. The next meaningful blocker is hardware:
we need a real Android phone/SIM test to verify SMS sending and background
polling behavior.
