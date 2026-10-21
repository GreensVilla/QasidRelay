# 13 — Guided real-device test mode

Status: `IN PROGRESS` (passive local checklist implemented)

Priority: `P2`

## Implemented so far

- Local opt-in test session with an explicit start/end time and checklist.
- Observes current configured/permission/service state and successful gateway
  polling after test start; never opens a second polling loop.
- Requires a new send/accepted callback, matching gateway status acknowledgement
  and zero pending reports to mark the local SMS smoke test passed.
- No generated test jobs, receiver inbox access or recipient numbers.
- Unit tests cover incomplete sessions and no carrier-delivery claims.

Still pending: emulator/device UI testing and one real SIM gateway test with
redacted evidence. Local pass is not carrier delivery proof.

## Goal

Provide an in-app checklist for first setup and smoke testing.

## Problem

QasidRelay is only truly proven on a real Android phone/SIM. A guided test mode
can reduce missed steps and create consistent evidence without exposing secrets.

## Scope

Checklist steps:

1. Confirm server URL, phone name, and key saved.
2. Confirm SMS permission granted.
3. Confirm notification/foreground service visibility.
4. Start relay service.
5. Wait for signed poll.
6. Ask server/operator to queue one controlled transactional test message.
7. Detect local send attempt and terminal status.
8. Confirm pending report queue is empty.
9. Offer redacted diagnostics copy.

## Out of scope

- Generating server messages directly from the app.
- Reading received SMS on the target phone.
- Proving carrier delivery receipts.

## UX requirements

- Label clearly as `Test mode`; it must not look like normal customer SMS flow.
- Remind operator to use a controlled recipient they own or are authorized to
  test.
- End with a clear pass/fail summary.

## Development directions

- Reuse existing status/report stores.
- Avoid a second polling engine; test mode observes the same relay state.
- Keep checklist state local and resettable.
- Add tests for checklist transitions where possible.

## Acceptance criteria

- A successful real-device smoke produces a local pass summary.
- Failure states tell the operator which step failed.
- No OTP/body/full recipient appears in the summary.
- Docs explain how to pair the checklist with gateway evidence.
