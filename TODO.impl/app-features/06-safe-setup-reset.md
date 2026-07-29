# 06 — Safer setup, reset, and key rotation UX

Status: `PLANNED`

Priority: `P1`

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
