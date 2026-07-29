# 99 — Explicit app non-goals

Status: `PLANNED`

These are intentionally not planned for the Android app unless a future
maintainer explicitly changes the project scope after security review.

## Do not add

- `READ_SMS` permission.
- Inbox reading or OTP interception.
- Contact import.
- Bulk recipient lists.
- Campaign management.
- Marketing templates.
- SIM-farm coordination.
- Remote inbound API on the phone.
- WebSocket server on the phone.
- Local HTTP server on the phone.
- Silent auto-start of polling/sending after reboot or package update.
- Cleartext HTTP gateway communication.
- Export private relay key.
- Copy setup link/key through diagnostics.
- Display message body or OTP by default.
- Store unbounded local history.

## Reasoning

QasidRelay is a phone-local transactional SMS relay. Its credibility depends on
being narrow, auditable, and difficult to abuse. Convenience features that make
it look like a bulk SMS platform create legal, carrier, security, and reputation
risk.

## Maintainer rule

If a proposed feature touches any item above, stop implementation and open a
written design review first. The design must explain:

- why the existing polling transactional model is insufficient;
- what additional abuse controls are required;
- how secrets and OTP content stay protected;
- how the feature will be tested on real devices;
- why it belongs in QasidRelay instead of a separate product.
