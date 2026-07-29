# 09 — Server destination policy display

Status: `PLANNED`

Priority: `P2`

## Goal

Show the operator what destination policy the trusted gateway applies: allowlist,
country restriction, or unrestricted/self-managed mode.

## Problem

The Android app should not enforce every business rule, but the operator should
understand whether the server will send only to controlled numbers or broader
recipients.

## Scope

Display a read-only policy summary when the gateway exposes it, for example:

- `Allowlist only`;
- `Country restricted: +92`;
- `Unrestricted by gateway`;
- rate limit summary if available;
- last policy refresh time.

## Out of scope

- Client-side replacement for server policy enforcement.
- Letting the Android app override server allowlists.
- Bulk/campaign policy support.

## UX requirements

- If policy is unavailable, show `Policy not reported by server` and keep normal
  operation unchanged.
- If policy is unrestricted, show a warning that the server/operator accepts the
  risk.
- Keep policy copy concise and non-technical.

## Development directions

- Requires a protocol extension. Document the gateway field before app code.
- Treat policy as informational only unless a later task explicitly adds client
  safety blocks.
- Cache policy with a timestamp, but refresh through normal polling/status
  paths, not a new inbound channel.

## Acceptance criteria

- App displays policy when a compatible gateway provides it.
- App remains compatible with old gateways that do not provide policy.
- Unrestricted policy is visibly different from allowlist policy.
- Tests cover parsing unknown policy values safely.
