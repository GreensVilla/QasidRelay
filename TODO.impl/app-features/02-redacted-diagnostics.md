# 02 — Redacted copy diagnostics

Status: `IN PROGRESS` (initial copy action committed)

Priority: `P0`

## Implemented so far

- Redacted clipboard diagnostics with a generated support correlation ID.
- App/device/version, host-only server name, permission, battery, coarse relay
  state, polling timestamps and pending report count.
- Allowlisted serializer and JVM tests that reject raw sensitive status strings.

Pending: install/update metadata, optional preview, real-device clipboard
validation and broader privacy review of diagnostic device information.

## Goal

Add a `Copy diagnostics` action that lets an operator share enough information
for support without exposing secrets, OTPs, full phone numbers, or message
bodies.

## Problem

When a field device fails, support needs device/app state. Asking users to send
screenshots or logs risks leaking private relay keys, setup links, OTP content,
recipient numbers, or operational details.

## Scope

Diagnostics should include:

- app version name/code;
- Android SDK/version and manufacturer/model;
- package install/update time if available without extra permissions;
- configured server origin host only, not full setup links or query strings;
- phone/device name;
- messaging enabled/running state;
- SMS permission state;
- notification permission state when applicable;
- battery optimization status if available;
- last poll time and coarse result category;
- last SMS terminal status and coarse failure category;
- pending report count;
- selected SIM/subscription label only after dual-SIM support exists;
- a generated support correlation ID for this copy action.

## Redaction rules

- Private relay key: never included.
- Setup URI/QR/token: never included.
- HMAC headers/signature/nonce: never included.
- Message body/OTP: never included.
- Recipient phone: last four digits only, or omitted.
- Server URL: origin only, no path/query/fragment.

## UX requirements

- Button label: `Copy diagnostics`.
- Show a preview or short confirmation: `Copied redacted diagnostics. Review
  before sharing.`
- Include a warning that diagnostics are for trusted support channels only.

## Development directions

- Implement diagnostics as a dedicated serializer class with unit tests.
- Use stable key names so server-side support tools can parse it later.
- Prefer JSON or clearly sectioned plain text. JSON is better for future issue
  templates; plain text is better for WhatsApp/SMS support. If both are needed,
  generate plain text from the same redacted model.
- Never read external logs or app private storage files for diagnostics.

## Acceptance criteria

- Diagnostics can be copied from a configured and unconfigured app.
- Tests prove no private key, setup URI query, message body, or full E.164 phone
  number appears in sample diagnostics.
- Diagnostics remain useful when the service is stopped.
- Real-device copy/paste smoke is recorded.
