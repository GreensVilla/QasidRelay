# 06 — Safer setup, reset, and key rotation UX

Status: `IN PROGRESS` (guarded setup/reset implementation committed)

Priority: `P1`

## Implemented so far

- Local-only setup validation with an explicit warning that no gateway call
  or credential authentication was performed.
- Server/device/key changes blocked while messaging is enabled, status reports
  are queued, or Android SMS attempts remain in flight.
- Confirmed Forget this server action, which refuses to run in unsafe states.
- After safe reset or gateway identity change: clear credentials, previous
  gateway-specific UI activity and deduplication state, and stale dashboard
  status; preserve explicit operator start requirements.
- Pure JVM tests for guard conditions and identity comparison.

Still pending: real-device reset/key-rotation tests, safely authenticated
remote connection-test design, and UI review of all failure paths.

## Goal

Make setup recovery safe when a server URL, phone name, or private relay key is
wrong, expired, rotated, or intentionally replaced.

## Problem

Operators need a simple way to recover from bad setup without uninstalling the
app. At the same time, setup actions touch the most sensitive configuration in
the app.

## Scope

Add or refine actions:

- `Test configuration` to check local readiness and optionally make a safe
  signed gateway request that does not send SMS.
- `Paste new private relay key` / `Rotate key` guidance.
- `Forget this server` to clear server URL, phone name, encrypted key, service
  state, pending non-terminal local UI history, and require fresh setup.
- Confirmations for destructive actions.
- Clear explanation that private key is hidden after saving and must be rotated
  from the trusted gateway if lost.

## Out of scope

- Exporting existing private relay keys.
- Showing saved private relay keys.
- Recovering keys from Android backups.
- Auto-enabling messaging after setup or reset.

## UX requirements

- After setup, the private key input remains blank with copy explaining that a
  key is saved.
- `Forget this server` must warn that the phone will stop polling until setup is
  repeated.
- Successful setup should end with `Configuration saved. Messaging is still
  disabled until you start the relay.`

## Development directions

- Reuse existing Keystore storage path; do not introduce a plaintext fallback.
- Clear pending status reports only if they cannot be safely associated with the
  old server. Prefer sending pending terminal reports before allowing reset, or
  make the reset warning explicit.
- Add unit tests for validation and reset behavior.

## Acceptance criteria

- Operator can recover from wrong key/server without uninstalling.
- App never shows or copies the saved key.
- Reset stops relay/polling immediately.
- Setup remains three primary fields for normal users.
