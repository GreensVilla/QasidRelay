# 00 — Global app feature rules

Status: `PLANNED`

These rules apply to every task in this directory.

## Non-negotiable architecture rules

- The Android phone is a polling client. It must not expose an inbound API,
  socket listener, webhook receiver, local HTTP server, or remote-control port.
- The app must not request `READ_SMS` in the `0.x` line.
- The app must not read inbox messages, OTP replies, contacts, call logs, or
  notification contents.
- Cleartext HTTP must stay rejected outside explicit local developer builds.
- HMAC signing and HTTPS remain required for gateway communication.
- Private relay keys must remain protected with Android Keystore and must not be
  exportable through diagnostics, backup, clipboard helpers, logs, or UI.
- Messaging must remain operator-controlled. The app must not silently resume
  sending after reboot, package replacement, or force stop without a foreground
  operator action.

## Security and privacy rules

- Redact message bodies by default. OTP bodies must not appear in dashboard,
  history, diagnostics, crash logs, or screenshots unless a maintainer opens a
  separate explicit debug-only task.
- Redact phone numbers by default. Prefer last four digits or deterministic
  hashes when support correlation is required.
- Do not store long-lived plaintext copies of private relay keys.
- Do not log request signatures, shared secrets, raw setup links, setup QR
  contents, full phone numbers, or message bodies.
- Never add bulk import, campaign, contact-list, or marketing workflows.

## UX rules

- Prefer operator-readable state over raw logs.
- Every error should explain what the operator can do next.
- Dangerous actions must be explicit: disabling relay, forgetting server,
  rotating/pasting a key, clearing history, changing SIM, or changing gateway.
- The app should work on small/older Android screens without relying on modern
  Compose-only UI patterns.
- Keep setup simple for non-protocol users: server URL, phone name, private
  relay key, and clear next action.

## Development rules

- Keep Java/platform-widget implementation unless a separate task approves a UI
  framework migration.
- Preserve min SDK 23 unless compatibility testing justifies a change.
- Add unit tests for pure logic such as redaction, status formatting,
  diagnostics serialization, backoff labels, release parsing, and policy labels.
- Add instrumentation or real-device checks for Android APIs that cannot be
  proven by JVM tests.
- Keep CI checks for restricted SMS scope updated when permissions or manifests
  change.

## Release rules

- Bump `versionCode` and `versionName` for release-bearing app changes.
- Use signed release APKs for operator testing after `v0.1.6`.
- Keep debug APKs clearly marked as internal artifacts.
- Record real-device findings in `docs/real-device-testing.md` or a linked
  evidence file before claiming a hardware-sensitive feature is ready.
