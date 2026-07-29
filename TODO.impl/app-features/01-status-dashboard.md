# 01 — Better operator status dashboard

Status: `PLANNED`

Priority: `P0`

## Goal

Give the operator one screen that answers: is QasidRelay configured, allowed to
send, currently polling, recently successful, or blocked by a fixable problem?

## Problem

The app already exposes core status, but after the GreensVilla staging smoke we
need a clearer operator-grade dashboard. A non-technical owner should not have
to infer state from logs, timestamps, or separate controls.

## Scope

Show these state groups on the main screen:

- configuration state: server URL present, phone name present, private key saved;
- permission state: `SEND_SMS`, notification permission where applicable;
- service state: disabled, enabled and running, enabled but stopped/manual
  restart required;
- gateway state: last poll time, last HTTP status category, auth failure,
  server failure, no pending jobs;
- SMS state: last send attempt, last terminal status, last failure detail;
- report queue state: pending status report count and oldest pending report age;
- device policy state when available from the gateway.

## Out of scope

- Showing message bodies or OTP content.
- Showing full recipient numbers.
- Adding remote control or inbound diagnostics.
- Adding charts or analytics.

## UX requirements

- Use plain labels: `Ready`, `Needs action`, `Polling`, `Waiting`, `Blocked`,
  `Failed`.
- Put the most important action near the relevant state, for example `Grant SMS
  permission`, `Start relay service`, `Check server URL`, or `Copy diagnostics`.
- Show timestamps as relative text plus exact local time when useful.
- Use conservative colors: success, warning, danger, neutral. Do not rely only
  on color.

## Development directions

- Reuse existing `RelayStatusStore` and extend it only with redacted/safe fields.
- Avoid storing raw HTTP response bodies.
- Use small view-rendering helpers or value objects so labels can be unit tested.
- Keep status updates cheap because they run while the foreground service is
  active.

## Acceptance criteria

- A configured/running phone shows `Ready` and recent poll/send status.
- A configured but stopped phone clearly says manual start is required.
- Missing SMS permission clearly blocks sending and gives one next action.
- Pending status reports are visible without exposing message contents.
- Unit tests cover status-label mapping.
- Real-device screenshot evidence is captured with secrets redacted.
