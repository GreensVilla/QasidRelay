# 08 — Dual-SIM selector

Status: `PLANNED`

Priority: `P2`

## Goal

Allow an operator on a dual-SIM phone to explicitly choose which SIM sends
QasidRelay SMS.

## Problem

Many Pakistani Android devices are dual-SIM. Without explicit selection, SMS may
use the system default SIM, which can be wrong for cost, package, compliance, or
support expectations.

## Scope

- Detect available SMS-capable subscriptions using public Android APIs.
- Display a safe SIM label/slot where available.
- Allow operator to choose a SIM for relay sends.
- Persist selected subscription ID carefully and handle invalidation when SIMs
  are removed/replaced.
- Show selected SIM in diagnostics and status using safe labels only.

## Out of scope

- Reading SIM contacts or phone numbers.
- Auto-switching based on destination.
- Multi-SIM load balancing.
- SIM-farm support.

## UX requirements

- Default state should be explicit: `System default SIM` or selected SIM.
- Changing SIM should require confirmation because it changes billing/carrier
  behavior.
- If selected SIM disappears, block sending and ask operator to choose again.

## Development directions

- Implement only after testing on at least one real dual-SIM device.
- Use `SmsManager` APIs that support subscription-specific sending when
  available for the min SDK target.
- Keep fallback behavior documented for older devices.
- Add instrumentation/manual tests because JVM tests cannot prove SIM routing.

## Acceptance criteria

- Single-SIM devices behave unchanged.
- Dual-SIM devices can select a SIM and retain the selection across restart.
- Missing/changed SIM blocks sending safely.
- Real-device evidence records model, Android version, and observed behavior.
