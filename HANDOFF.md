# QasidRelay handoff

Updated: 2026-07-29

## Repository

- GitHub: <https://github.com/GreensVilla/QasidRelay>
- Visibility: public
- Default branch: `main`
- License: Apache-2.0
- Current handoff commit before this working pass: `b248bf8`
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

The 0.1.x line now includes:

- Android SMS sent callbacks before queuing terminal status
- persistent retry queue for status reports
- bounded local recent-message ledger to reduce duplicate sends
- phone-local messaging enable/disable control for operators
- safe manual restart flow after reboot or package replacement
- Android Keystore and stale-attempt instrumentation coverage source
- `remoteMessaging` foreground-service declaration for the relay worker
- bounded stale-attempt recovery that queues a terminal `failed` status
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

Merged main CI evidence:

- GitHub Actions run `30004080082`
- commit `d7bbf9ebed3efa0d85eeff15896465c8a48a379b`
- `static`: passed
- `android`: passed, including `:app:testDebugUnitTest` and
  `:app:assembleDebug`

Release evidence:

- GitHub prerelease `v0.1.0`
- release URL: <https://github.com/GreensVilla/QasidRelay/releases/tag/v0.1.0>
- final release CI: GitHub Actions run `30004375738`
- release commit `7e0e1ed6085aeddd504e1979c040158f9722cd95`
- `static`: passed
- `android`: passed, including `:app:testDebugUnitTest` and
  `:app:assembleDebug`

Current release follow-up:

- `v0.1.1` adds phone-local messaging enable/disable control and deferred
  implementation TODOs.
- `v0.1.2` targets downloadable debug APK artifacts from GitHub Actions.
- `v0.1.3` is published as a debug prerelease:
  - release URL: <https://github.com/GreensVilla/QasidRelay/releases/tag/v0.1.3>
  - release commit: `b66fba271d4c2677e032e518f126e08ee25e0cf2`
  - release CI: GitHub Actions run `30432200987`
  - `static`: passed
  - `android`: passed, including `:app:testDebugUnitTest`,
    `:app:assembleDebug`, and debug APK artifact upload
  - asset: `QasidRelay-v0.1.3-debug.apk`
  - asset SHA-256:
    `345c681ebedf3a3befc7e05048e0926b0d6568ab012de11771491c4ee6706def`
- `v0.1.4` is published as a debug prerelease:
  - release URL: <https://github.com/GreensVilla/QasidRelay/releases/tag/v0.1.4>
  - release commit: `87e3161e8ef073bbd254f72323ec97b5e5a4bbda`
  - release CI: GitHub Actions run `30448230586`
  - `static`: passed
  - `android`: passed, including `:app:testDebugUnitTest`,
    `:app:assembleDebug`, and debug APK artifact upload
  - asset: `QasidRelay-v0.1.4-debug.apk`
  - asset SHA-256:
    `76accc0809c1b3ac9efac279e20a04033009b5fc84da476032652ce13bf20ceb`
  - notable change: setup now asks only for server URL, phone name, and private
    relay key; poll interval and redundant stop-service controls were removed
    from the main UI.

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
- [`TODO.impl/README.md`](TODO.impl/README.md)

## Next QasidRelay tasks

1. Test the `v0.1.4` APK on a real Android phone with an authorized SIM and a
   controlled recipient.
2. Run `connectedDebugAndroidTest` on a real device or emulator and record the
   Android Keystore evidence.
3. Implement or deploy the minimal HTTPS/HMAC reference gateway before routing
   real GreensVilla SMS traffic through QasidRelay.
4. Add battery-optimization guidance from real devices and vendor-specific
   restrictions discovered during smoke testing.
5. Add dual-SIM selection only after testing the target phone.
6. Decide client-package naming conventions:
   - proposed by owner: `QasidRelay-rb`, `QasidRelay-js`,
     `QasidRelay-rust`, `QasidRelay-python`.
   - Keep protocol docs authoritative so client repos stay consistent.

## Current blocker

No committed-secret or public-repo blocker is known. The next meaningful
blockers are outside this Android client: a small HTTPS/HMAC gateway, then a
real Android phone/SIM smoke test to verify SMS sending and foreground polling
behavior.
