# QasidRelay handoff

Updated: 2026-07-29 (historical handoff; see addendum)

## Operator UX addendum — 2026-10-21 (unreleased)

Branch: `codex/operator-dashboard-diagnostics-ux`.

The first implementation adds an operator status summary and contextual actions,
safe support diagnostics copying, battery settings guidance, clearer timestamps,
and JVM tests. It preserves all current Android permissions and the existing
phone-initiated HTTPS/HMAC SMS protocol.

This is **not a signed release**. Before release, inspect GitHub Actions and
verify real-device operation, clipboard redaction, battery navigation and small
screen layouts. The UI still lacks dedicated HTTP category and report queue age
fields, and vendor-specific battery guidance requires tested evidence.

## Repository

- GitHub: <https://github.com/GreensVilla/QasidRelay>
- Visibility: public
- Default branch: `main`
- License: Apache-2.0
- Current handoff commit before this working pass: `f4a7f85`
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
- `v0.1.5` is published as a debug prerelease:
  - release URL: <https://github.com/GreensVilla/QasidRelay/releases/tag/v0.1.5>
  - release commit: `970e8d5e7c50fa36077fee7a06aa0a7d566353ea`
  - release PR: <https://github.com/GreensVilla/QasidRelay/pull/11>
  - release CI: GitHub Actions run `30456714288`
  - `static`: passed
  - `android`: passed, including `:app:testDebugUnitTest`,
    `:app:assembleDebug`, and debug APK artifact upload
  - asset: `QasidRelay-v0.1.5-debug.apk`
  - asset SHA-256:
    `28ff57cacdeb71a3f24406166017ae80cdb50d0a0c8e068fbafd8ac027457aa4`
  - notable change: setup QR/deep-link support for `qasidrelay://setup`.
    Scanning a trusted gateway QR pre-fills server URL, phone name, and private
    relay key without enabling messaging automatically. No camera permission or
    QR-scanning dependency was added.
- `v0.1.6` is published as the first stable-signed prerelease:
  - release URL: <https://github.com/GreensVilla/QasidRelay/releases/tag/v0.1.6>
  - release commit: `ddda5d8a1b043f2272518a78044ea545dd72d78d`
  - release PR: <https://github.com/GreensVilla/QasidRelay/pull/13>
  - release CI: GitHub Actions run `30459630711`
  - `static`: passed
  - `android`: passed, including `:app:testDebugUnitTest`,
    `:app:assembleDebug`, signed release APK build, and signed APK artifact
    upload
  - asset: `QasidRelay-v0.1.6-signed.apk`
  - asset SHA-256:
    `8c3c73d459ff936b63baddac5d628e3f4d5e34d1e2b04bdf472bed08aac8f21b`
  - install note: devices with earlier CI/debug APKs, including `v0.1.5`, must
    uninstall once before installing `v0.1.6`; later same-key signed builds with
    higher `versionCode` should update in place.
  - signing note: signing material is stored in GitHub Actions secrets; no
    signing key or password is committed to the repository.
- `v0.1.7` is published as a signed prerelease:
  - release URL: <https://github.com/GreensVilla/QasidRelay/releases/tag/v0.1.7>
  - release commit: `e88d181921e826db6f35488da474066f8122760c`
  - release PR: <https://github.com/GreensVilla/QasidRelay/pull/15>
  - release CI: GitHub Actions run `30467882387`
  - `static`: passed
  - `android`: passed, including `:app:testDebugUnitTest`,
    `:app:assembleDebug`, signed release APK build, and signed APK artifact
    upload
  - asset: `QasidRelay-v0.1.7-signed.apk`
  - asset SHA-256:
    `a4f9eb6361d82c2b366aaf43811893660425c9d2aee17d73ee3c1d3918cbc0ac`
  - notable fix: field testing showed `Could not encrypt shared secret with
    Android Keystore` while saving the private relay key. The fix lets Android
    Keystore generate the AES-GCM IV during encryption, then stores that IV with
    the ciphertext.
  - install note: this uses the same package ID and release-signing path as
    `v0.1.6`, with `versionCode` 8, so it should update `v0.1.6` in place.

## Real-device smoke evidence

On 2026-07-29, a controlled GreensVilla staging phone-verification OTP was
successfully sent through the full QasidRelay path: GreensVilla staging queued a
message, the signed `v0.1.7` Android app polled over HTTPS/HMAC, the phone sent
the SMS through the operator SIM, and the gateway recorded terminal `sent` with
one send attempt. No OTP code, real phone number, private relay key, or message
body is committed here.

This proves the first real-device controlled-SIM smoke path for the current
Android client and removes the prior Android-client smoke blocker. Further
evidence is still needed for more device models, battery behavior, dual-SIM
routing, and any future app feature touching SMS, notification, Keystore, or
foreground-service behavior.

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

1. Use [`TODO.impl/app-features/`](TODO.impl/app-features/README.md) as the
   ordered Android app feature backlog. Start with status dashboard, redacted
   diagnostics, and battery optimization guidance.
2. Run `connectedDebugAndroidTest` on a real device or emulator and record the
   Android Keystore evidence.
3. Add battery-optimization guidance from real devices and vendor-specific
   restrictions discovered during smoke testing.
4. Add dual-SIM selection only after testing the target phone.
5. Implement or deploy the minimal HTTPS/HMAC reference gateway for non-GreensVilla
   users, using the existing protocol docs as the contract.
6. Decide client-package naming conventions:
   - proposed by owner: `QasidRelay-rb`, `QasidRelay-js`,
     `QasidRelay-rust`, `QasidRelay-python`.
   - Keep protocol docs authoritative so client repos stay consistent.

## Current blocker

No committed-secret or public-repo blocker is known. The first controlled
GreensVilla staging OTP smoke has passed through the signed Android app. The
next meaningful Android-client blockers are additional real-device coverage,
Keystore instrumentation evidence, battery behavior evidence, and any hardware-specific validation required by future app features. Generic public use still
needs a reference gateway/site/docs path outside the Android client.
