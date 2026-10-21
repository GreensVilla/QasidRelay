# QasidRelay app feature backlog

Status: `IN PROGRESS` — tasks 01–07 partially implemented

This directory tracks planned Android app features for QasidRelay after the
`v0.1.7` signed prerelease and first real-device GreensVilla staging OTP smoke
success on 2026-07-29.

Initial code and JVM tests are available for tasks 01–07, but they do not
satisfy all acceptance criteria. Real-device screenshots, clipboard testing,
richer gateway status and vendor battery evidence are outstanding.

These tasks are intentionally app-focused. Gateway/server/site/SDK work belongs
in separate planning files unless a task explicitly depends on a server contract.

## Planning rules

- Work in numeric order unless a later task is explicitly needed to unblock a
  field test.
- Keep one app implementation task active at a time unless a maintainer opens a
  focused parallel branch.
- Do not weaken the security posture to improve convenience.
- Every feature must preserve polling-only behavior: the phone initiates all
  network traffic and exposes no inbound public API.
- Every feature must keep secrets out of screenshots, diagnostics, logs, crash
  reports, and copied support text.
- Every feature must include manual real-device notes before release when it
  touches SMS sending, background service behavior, permissions, notifications,
  Keystore, SIM selection, or Android vendor restrictions.

## Priority checklist

1. [`01-status-dashboard.md`](01-status-dashboard.md) — richer operator status
   dashboard.
2. [`02-redacted-diagnostics.md`](02-redacted-diagnostics.md) — safe copyable
   diagnostics for support.
3. [`03-battery-optimization-guidance.md`](03-battery-optimization-guidance.md)
   — device/vendor battery guidance.
4. [`04-message-activity-history.md`](04-message-activity-history.md) — local
   safe activity history.
5. [`05-sms-failure-explanations.md`](05-sms-failure-explanations.md) — clearer
   Android SMS error explanations.
6. [`06-safe-setup-reset.md`](06-safe-setup-reset.md) — safe setup/reset/key
   rotation UX.
7. [`07-app-update-check.md`](07-app-update-check.md) — current version and
   release-link visibility.
8. [`08-dual-sim-selector.md`](08-dual-sim-selector.md) — explicit SIM routing
   after real dual-SIM testing.
9. [`09-server-policy-display.md`](09-server-policy-display.md) — show gateway
   destination policy.
10. [`10-operator-lock.md`](10-operator-lock.md) — optional local lock before
    sensitive operator actions.
11. [`11-foreground-notification.md`](11-foreground-notification.md) — richer
    foreground notification state.
12. [`12-connectivity-backoff-visibility.md`](12-connectivity-backoff-visibility.md)
    — visible network/auth/backoff state.
13. [`13-real-device-test-mode.md`](13-real-device-test-mode.md) — guided smoke
    test mode.
14. [`14-multi-profile-support.md`](14-multi-profile-support.md) — later
    staging/production or multi-gateway profiles.
15. [`15-help-links.md`](15-help-links.md) — app links to docs, protocol, and
    troubleshooting.

Global guardrails are in [`00-global-rules.md`](00-global-rules.md). Explicit
non-goals are in [`99-non-goals.md`](99-non-goals.md).

## Definition of ready for an implementation task

A task is ready when:

- the target Android versions/devices are identified;
- any required gateway response or protocol field is documented;
- UI strings and failure states are sketched before coding;
- no permission expansion is required, or the permission expansion is explicitly
  justified and reviewed;
- test data uses synthetic message IDs and controlled recipient numbers only.

## Definition of done for an implementation task

A task is done only when:

- app code and tests are merged;
- relevant docs are updated;
- CI passes `static` and `android` jobs;
- real-device evidence is recorded when required;
- release notes describe behavior, limitations, and upgrade expectations;
- no secret, OTP, real phone number, or message body is committed.
