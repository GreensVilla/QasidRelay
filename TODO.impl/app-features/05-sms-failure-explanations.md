# 05 — Clear SMS failure explanations

Status: `PLANNED`

Priority: `P1`

## Goal

Translate Android SMS send result codes into operator-readable explanations and
next steps.

## Problem

Raw Android result codes are not useful to shop owners or support operators.
When a send fails, the app should explain likely causes such as no signal, SIM
problem, permission missing, radio off, or generic carrier failure.

## Scope

Map known `SmsManager` sent callback result codes to:

- short label;
- safe detail for app dashboard;
- suggested operator action;
- status detail sent back to the gateway.

## Out of scope

- Carrier delivery receipts.
- Reading delivery/inbox messages.
- Guaranteeing that Android `sent` means recipient received the SMS.

## UX requirements

- Use language like `Android accepted the SMS for sending`, not `Delivered`.
- For failure, state the likely fix: check SIM, signal, SMS balance/package,
  airplane mode, permission, or default SMS restrictions.
- Keep details short enough for notification/history rows.

## Development directions

- Centralize result-code mapping in one testable class.
- Keep gateway `status_detail` bounded and free of PII.
- Preserve multipart behavior: all parts must succeed before `sent`.
- Test generic unknown result code behavior.

## Acceptance criteria

- Known Android failure codes map to readable labels.
- Unknown codes remain safe and actionable.
- Gateway status details remain within protocol length limits.
- Real-device failure-mode notes are added when tested.
