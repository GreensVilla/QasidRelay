# 04 — Safe message activity history

Status: `IN PROGRESS` (initial redacted history implementation committed)

Priority: `P1`

## Implemented so far

- Independent activity preference with a 20-event cap, persisted across restarts.
- Raw message IDs replaced before storage with truncated SHA-256 fingerprints.
- No SMS bodies, phone numbers or arbitrary failure strings in local history.
- Panel shows last five events, with optional expansion and confirmed clearing.
- Clear action cannot erase the safety-critical deduplication ledger or queue.
- JVM redaction/retention tests and Android instrumentation test sources added.

Still pending: physical-device smoke evidence, small-screen accessibility,
attempt-count display and optional coarse recipient indicator. These latter
two fields are intentionally excluded until privacy requirements are reviewed.

## Goal

Show recent relay activity on the phone so operators can confirm what happened
without opening server logs.

## Problem

During testing, the server showed queued/leased/sent state, but the phone should
also explain recent local activity. This helps support diagnose carrier/device
issues after the gateway side is already correct.

## Scope

Store and show the last 20 local message events:

- message ID;
- coarse recipient redaction, for example `***2984`;
- state: received, sending, sent, failed, report queued, report delivered;
- attempt count;
- local timestamp;
- safe failure code/category.

## Out of scope

- Message bodies, OTPs, full phone numbers.
- Long-term audit log. The server remains the durable audit system.
- Exporting history in v1 unless routed through redacted diagnostics.

## UX requirements

- Section label: `Recent activity`.
- Empty state: `No messages handled by this phone yet.`
- Each row should be compact and readable on small screens.
- Provide a clear `Clear local activity` action only if it does not interfere
  with duplicate protection or pending report delivery.

## Development directions

- Do not reuse `RelayMessageLedger` blindly if that ledger is needed for
  duplicate protection. Keep UI history separate from safety state or document
  the coupling explicitly.
- Keep retention bounded by count and size.
- Persist across app restart.
- Redact before storage if possible; otherwise guarantee redaction before UI and
  diagnostics.

## Acceptance criteria

- A successful SMS creates safe activity rows.
- A failed SMS creates a readable failure row.
- Pending report retry state appears without exposing body/phone.
- History survives process restart.
- Tests cover retention and redaction.
