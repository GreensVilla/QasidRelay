# Changelog

## Unreleased — operator UI/UX improvements

### Small-screen usability

- Collapse technical status rows by default, with a Show/Hide details toggle.
- Promote start/stop and SMS-permission controls above advanced server setup.
- Confirm the compact operator view on a 320 × 640 Android emulator screenshot.


### Final software-side operator enhancements

- Fix automatic sticky relaunch request for SMS foreground service;
  remove raw exception text from polling log output.
- Refresh foreground notification from coarse gateway/pending-report status.
- Add passive guided test checklist, with a fresh send, gateway acceptance,
  recent polling and empty queue required before local smoke is marked passed.
- Add optional device-credential confirmation before sensitive operator actions.
  No additional permission or app-managed PIN.
- Parse and show optional read-only gateway destination-policy summary.
- Add JVM tests for notification text, smoke-test conditions, lock policy,
  and destination-policy sanitization.
- Add Android emulator instrumentation CI and release-gate instructions.


### Help and troubleshooting links

- Add external Help & documentation menu linking to maintained QasidRelay
  setup, compatibility, security, abuse, protocol, integration and issue docs.
- Keep the links static; never append gateway data, device identifiers or keys.
- Add JVM checks for trusted HTTPS host and repository-scoped paths.


### Gateway connectivity visibility

- Classify HTTP auth, rate-limit and server errors separately from idle/no-jobs.
- Provide network, timeout and TLS categories with operator-readable guidance.
- Persist only coarse gateway failure states, not arbitrary exception details.
- Show the configured polling interval without claiming adaptive backoff.
- Add JVM tests for category mapping and sensitive exception redaction.


### App version visibility

- Display installed versionName/versionCode on the operator screen.
- Provide a manual link to the official GitHub Releases page.
- Add pure JVM tests for safe version label formatting and release URL.
- No silent updater, extra permissions or background update traffic.


### Additional configuration safety

- Add `Check setup locally` with clear notice that it does not authenticate
  or communicate with the remote gateway.
- Block changing or forgetting server/key while messaging is enabled, an SMS
  attempt is in flight, or gateway reports remain queued.
- Add confirmed `Forget this server` flow, clearing encrypted credential
  references and gateway-specific local history without silently restarting.
- Stop the relay from beginning additional SMS submissions from an already
  fetched batch once messaging is disabled.
- Add JVM tests for credential-change guard decisions.


### Additional operator activity improvements

- Add a separate, persistent 20-event UI activity log with one-way message
  fingerprints rather than raw message IDs, recipient numbers or contents.
- Present pending gateway reports, status acceptance and recent send failures
  in an expandable panel (five events by default).
- Map common SMS callback failures to operator guidance and safe gateway details.
- Add a confirmed `Clear local activity` action; preserve pending status
  reports and duplicate-send safeguards.
- Add pure Java retention/failure tests and device-level persistence/isolation tests.
- Replace unknown Android result details with safe `SMS_RESULT_OTHER`.


### Added

- Operator status summary: Needs setup, Needs SMS permission, Messaging paused,
  Needs manual restart, Connecting, Connection problem, Polling delayed, Ready.
- Contextual next-action button; no automatic SMS activation.
- Redacted, copyable support diagnostics with per-copy correlation ID.
- Battery optimization guidance and Android system-settings shortcut.
- JVM tests for UI state mapping and diagnostic redaction.

### Changed

- Rounded panels/buttons and clearer polling timestamps.
- Last-delivery UI hides raw message IDs and detailed error messages.

### Remaining validation

- Android CI, on-device layout and accessibility, clipboard paste testing,
  battery-settings behavior, screenshots and vendor-specific background testing.
- Gateway HTTP category and pending report age are not yet displayed.



## 0.1.7 - 2026-07-29

### Fixed

- Fixed Android Keystore encryption on devices that reject caller-provided
  AES-GCM IVs when randomized encryption is required. QasidRelay now lets
  Android Keystore generate the IV and stores that IV with the encrypted private
  relay key.

### Notes

- This signed APK uses the same package ID and release-signing path as `0.1.6`,
  with `versionCode` 8, so it should update `0.1.6` in place.

## 0.1.6 - 2026-07-29

### Added

- Stable signed release APK support through GitHub Actions secrets.
- CI now builds and uploads `qasidrelay-signed-release-apk` when signing
  secrets are configured.

### Changed

- Android metadata bumped to `versionCode` 7 / `versionName` `0.1.6`.
- Future signed APKs can update in place as long as the same signing key is
  retained and `versionCode` continues increasing.

### Notes

- Devices that already installed a previous CI debug-signed APK must uninstall
  once before installing this first stable signed APK. After that, signed
  QasidRelay releases should update normally.

## 0.1.5 - 2026-07-29

### Added

- Android setup deep link support for `qasidrelay://setup` links generated by a
  trusted gateway. Scanning a setup QR with the phone camera or QR scanner opens
  QasidRelay and pre-fills server URL, phone name, and private relay key.
- Unit coverage for setup-link parsing, encoded private keys, invalid schemes,
  and incomplete setup payloads.

### Security

- No camera permission or QR-scanning library was added. The phone's existing
  camera/QR scanner handles scanning, and QasidRelay only receives the explicit
  deep link opened by the operator.
- Setup links are treated as one-time secret provisioning material because they
  contain the private relay key.

## 0.1.4 - 2026-07-29

### Changed

- Android metadata bumped to `versionCode` 5 / `versionName` `0.1.4`.
- Operator setup UI now asks for only three required values: server URL, phone
  name, and private relay key.
- Removed the poll interval field from the main setup form; the app keeps its
  safe default polling interval.
- Removed the redundant `Stop foreground service` control; `Disable messaging`
  remains the single safe way to stop fetching and sending jobs.
- Setup and release docs now use the same operator-facing field names.

## 0.1.3 - 2026-07-29

### Changed

- Android metadata bumped to `versionCode` 4 / `versionName` `0.1.3`.
- Relay foreground service now declares `remoteMessaging` instead of `dataSync`,
  matching the app's phone-local SMS relay purpose and avoiding Android 15
  `dataSync` time-budget behavior.
- Handoff and testing docs now reflect the merged safe restart flow and current
  release state.
- Android instrumentation coverage now includes stale SMS attempt state.

### Fixed

- Stale SMS attempts that never receive Android sent callbacks now fail closed
  after a bounded timeout and queue a terminal `failed` status for the gateway,
  instead of being skipped forever as locally handled.

## 0.1.2 - 2026-07-23

### Added

- GitHub Actions debug APK artifact for phone-local testing.

### Changed

- Android metadata bumped to `versionCode` 3 / `versionName` `0.1.2`.

### Notes

- The downloadable APK is debug-signed and intended for controlled testing only.
  A signed release APK workflow remains deferred.

## 0.1.1 - 2026-07-23

### Added

- Phone-local messaging enable/disable control.
- `TODO.impl/` implementation backlog for deferred reference gateway,
  deployment, signed APK, and GreensVilla integration tracks.

## 0.1.0 - 2026-07-23

Initial OSS/private-pilot Android relay release candidate.

### Added

- Android app that polls an HTTPS gateway for transactional SMS jobs.
- HMAC-signed requests with timestamp, nonce, and body SHA-256 headers.
- Shared secret storage encrypted at rest with Android Keystore AES-GCM.
- Operator status screen for configuration, permissions, foreground service
  state, last poll, last delivery event, and pending status reports.
- SMS sending through Android `SmsManager` without requesting `READ_SMS`.
- Android sent-result callbacks before terminal status reporting.
- Persistent status-report retry queue.
- Bounded local recent-message ledger to reduce duplicate sends for repeated
  message IDs.
- JVM unit tests for request signing.
- Protocol, threat model, abuse policy, Android compatibility, real-device
  testing, server integration, and release checklist docs.

### Security posture

- Phone-initiated polling only; no inbound public API on the device.
- Cleartext HTTP disabled by Android network security config.
- Transactional SMS only: account verification, password recovery,
  operational alerts, and private-pilot notifications.
- Bulk marketing, spam, SIM farms, OTP interception, and inbound SMS reading are
  out of scope.

### Known limitations

- CI validates unit tests and debug APK build, but real-device SIM validation is
  still pending.
- `sent` means Android accepted all SMS parts through sent callbacks. It is not
  a carrier delivery receipt.
- Exactly-once SMS delivery is not guaranteed. Servers must lease pending jobs
  atomically and treat status reports as idempotent.
- Dual-SIM routing, inbound replies, Google Play distribution, signed release
  APKs, and GreensVilla server integration are out of scope for 0.1.0.
