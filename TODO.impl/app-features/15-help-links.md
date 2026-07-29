# 15 — Help, docs, and protocol links

Status: `PLANNED`

Priority: `P3`

## Goal

Make it easy for operators and developers to find official QasidRelay setup,
security, troubleshooting, and API/protocol documentation.

## Problem

As QasidRelay becomes reusable outside GreensVilla, support should not depend on
scattered chat notes. The app should link to maintained docs without embedding
long protocol explanations in the Android UI.

## Scope

Add links to:

- operator setup guide;
- troubleshooting guide;
- latest release/download page;
- security/threat model;
- abuse policy;
- server integration/API protocol;
- issue tracker or support policy if approved.

## Out of scope

- In-app web browser for documentation.
- Collecting analytics on link clicks.
- Sending device diagnostics automatically when opening help.

## UX requirements

- Use external browser intents.
- Help screen should work even when the relay is not configured.
- Make security/abuse boundaries visible enough that the project does not look
  like a bulk SMS tool.

## Development directions

- Keep URLs in one constants file.
- Prefer a QasidRelay public docs/site URL once available; use GitHub docs until
  then.
- Add tests for URL constants if practical.

## Acceptance criteria

- Help screen links open safely.
- Links do not include device ID, server URL, or private data.
- Docs are updated before release if URL structure changes.
