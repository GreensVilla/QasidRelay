# 10 — Optional operator lock

Status: `IN PROGRESS` (opt-in device-credential gate committed)

Priority: `P2`

## Implemented so far

- Optional Android device-credential gate enabled only when a secure
  system screen lock already exists.
- Protects saving configuration, starting/stopping SMS relay, forgetting
  server, clearing activity, copying diagnostics, and disabling the lock.
- Cancelled/failed prompts discard the pending action.
- App does not store a PIN/password; background relay work does not depend
  on device unlock.
- Unit tests cover lock-enabled/secure-device policy.

Still pending: on-device credential intent testing, OEM compatibility,
screen-reader checks and review of lifecycle edge cases.

## Goal

Protect sensitive local actions with an optional PIN or device biometric prompt.

## Problem

A relay phone may be physically accessible. If someone can open the app, they
could disable messaging, change server details, paste a malicious key, or clear
configuration.

## Scope

Add optional lock before sensitive actions:

- edit server URL/phone name/key;
- forget server;
- enable/disable relay;
- change selected SIM;
- clear history/diagnostics settings.

Potential lock mechanisms:

- Android device credential/biometric prompt where available;
- app-local PIN only if securely stored and rate-limited;
- no lock by default for early testing unless operator enables it.

## Out of scope

- Enterprise device management.
- Remote lock/unlock.
- Storing private relay keys behind the app PIN instead of Android Keystore.

## UX requirements

- Keep setup usable without enabling lock.
- Explain what the lock protects and what it does not protect.
- Provide recovery guidance: if app-local PIN is forgotten, reset requires
  forgetting configuration and setting up again.

## Development directions

- Prefer AndroidX Biometric/device credential if adding dependency is accepted.
- If avoiding new dependency, defer until dependency policy is decided.
- Add tests for lock-required action routing, but real biometric behavior needs
  manual/instrumentation evidence.

## Acceptance criteria

- Sensitive actions can be protected when operator enables lock.
- Normal polling continues without user interaction after relay is started.
- Lock failure does not expose configuration secrets.
- Recovery path is documented.
