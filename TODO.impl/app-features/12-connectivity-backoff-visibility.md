# 12 — Connectivity and backoff visibility

Status: `PLANNED`

Priority: `P2`

## Goal

Show why polling is waiting: normal interval, no pending jobs, network error,
server error, authentication failure, kill switch, or backoff.

## Problem

When operators see no SMS activity, they need to distinguish normal idle state
from a broken connection or wrong key.

## Scope

Track and display coarse states:

- last successful poll;
- last no-content poll;
- network unreachable/timeout;
- TLS/HTTPS failure;
- HTTP 401/403 authentication failure;
- HTTP 5xx server failure;
- gateway kill switch or no jobs if server reports it;
- current retry/backoff delay.

## Out of scope

- Raw HTTP response bodies.
- Logging signatures/headers.
- Retrying aggressively enough to drain battery or overload the gateway.

## UX requirements

- Show action-oriented copy: `Check server URL`, `Rotate key`, `Check internet`,
  `Server is unavailable`, or `Waiting for messages`.
- Distinguish `No jobs` from `Cannot reach server`.

## Development directions

- Centralize HTTP result classification.
- Keep detail safe for status report/diagnostics.
- Do not include gateway response body in UI unless explicitly sanitized.
- Add unit tests for status classification.

## Acceptance criteria

- Auth failure is visibly different from no pending jobs.
- Network failure is visibly different from server 5xx.
- Backoff state is understandable.
- Polling behavior remains bounded and battery-conscious.
