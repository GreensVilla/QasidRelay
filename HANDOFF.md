# QasidRelay handoff

Updated: 2026-07-23

## Repository

- GitHub: <https://github.com/GreensVilla/QasidRelay>
- Visibility: public
- Default branch: `main`
- License: Apache-2.0
- Current handoff commit before this working pass: `dd02660`
- Description: `Open-source Android SIM relay for secure transactional SMS.`
- Topics: `android`, `android-app`, `otp`, `self-hosted`, `sim-gateway`,
  `sms`, `sms-gateway`, `transactional-sms`

## Current baseline and working pass

The repo started this pass from a CI-passing first Android/OSS baseline:

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

This working pass adds:

- Android SMS sent callbacks before queuing terminal status
- persistent retry queue for status reports
- bounded local recent-message ledger to reduce duplicate sends
- signer unit tests are configured for CI through `:app:testDebugUnitTest`
- status screen shows configuration, permissions, service state, last poll, last
  delivery event, and pending report count

## CI evidence

GitHub Actions run `29994009682` passed on `main`:

- `static`: required OSS files, placeholder scan, and restricted SMS-scope check
- `android`: `./gradlew :app:assembleDebug --no-daemon`

The only annotations were GitHub hosted-runner Node.js deprecation notices for
actions; both jobs passed.

This working pass adds unit tests to the Android CI job. Run
`./gradlew :app:testDebugUnitTest --no-daemon` and
`./gradlew :app:assembleDebug --no-daemon` before publishing or merging.

Branch CI evidence for `codex/qasidrelay-production-pass`:

- GitHub Actions run `30002679789`
- commit `53e81f8076a63866161b89998747e85e6388e5af`
- `static`: passed
- `android`: passed, including `:app:testDebugUnitTest` and
  `:app:assembleDebug`

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

Do not modify GreensVilla from this repository. Use
[`docs/server-integration.md`](docs/server-integration.md) as the integration
contract when a separate GreensVilla task is opened.

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
- [`docs/real-device-testing.md`](docs/real-device-testing.md)
- [`docs/server-integration.md`](docs/server-integration.md)

## Next QasidRelay tasks

1. Test APK on a real Android phone with a SIM.
2. Run the updated CI workflow after a JDK/Android SDK are available locally or
   in GitHub Actions.
3. Add a minimal reference server endpoint or contract tests.
4. Add Android instrumentation coverage for `SecureSecretStore` where practical.
5. Improve foreground-service resilience:
   - boot/restart behavior;
   - battery-optimization guidance from real devices;
   - vendor-specific restrictions.
6. Add dual-SIM selection only after testing the target phone.
7. Decide client-package naming conventions:
   - proposed by owner: `QasidRelay-rb`, `QasidRelay-js`,
     `QasidRelay-rust`, `QasidRelay-python`.
   - Keep protocol docs authoritative so client repos stay consistent.

## Current blocker

No active code blocker in QasidRelay. The next meaningful blocker is hardware:
we need a real Android phone/SIM test to verify SMS sending and background
polling behavior.
