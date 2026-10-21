# QasidRelay implementation TODOs

This directory tracks implementation work intentionally deferred beyond the
phone-local Android `0.1.x` line.

QasidRelay should remain polling-first: the phone must not expose an inbound
public API. Future server or deployment work should implement the documented
gateway protocol and keep transactional-only abuse controls.

## Android app feature backlog

The detailed operator-app feature plan lives in
[`app-features/`](app-features/README.md). These tasks should be implemented
after the current signed APK baseline and real-device smoke evidence, unless a
field issue makes one task urgent.

Priority order:

1. status dashboard (first UI implementation on the operator UX branch; pending more gateway fields and QA);
2. redacted diagnostics (copy action implemented; pending device clipboard test);
3. battery optimization guidance (generic guidance implemented; vendor testing pending);
4. local message activity history;
5. SMS failure explanations;
6. safer setup/reset/key-rotation UX;
7. app update/version visibility;
8. dual-SIM selector;
9. server destination-policy display;
10. optional operator lock;
11. richer foreground notification;
12. connectivity/backoff visibility;
13. guided real-device test mode;
14. multi-profile support;
15. help/docs links.

Global rule: these features must not add `READ_SMS`, inbound phone APIs,
bulk-marketing workflows, cleartext HTTP, silent post-reboot sending, or secret
export paths.

## Near-term

- Implement or deploy a small HTTPS/HMAC reference gateway for non-GreensVilla
  users. GreensVilla now has its own gateway integration; the Android app
  remains a polling client, not the provider endpoint that external apps should
  call directly.
- Add expanded real-device smoke-test evidence beyond the first GreensVilla
  staging success:
  - device model and Android version;
  - authorized SIM/carrier plan type;
  - redacted status-screen screenshot;
  - signed polling/status logs from a test gateway;
  - exactly-one-send observation for one controlled test recipient.
- Add Android UI evidence to release notes after real-device testing.
- Run `connectedDebugAndroidTest` on a real device or emulator and record the
  Android Keystore instrumentation-test evidence.
- Keep the signed APK workflow healthy. Release signing is configured through
  GitHub Actions secrets; do not commit signing keys or keystore passwords.

## Reference gateway

Deferred for a later `0.x` milestone:

- Dockerized reference gateway with a small browser UI.
- Device enrollment flow that generates device ID and high-entropy shared
  secret.
- Local transactional test-message queue.
- Status dashboard showing pending, leased, sent, and failed jobs.
- Operator kill switch to stop returning pending messages.
- Destination allowlist and rate-limit settings.
- No list import, campaign, or bulk-marketing workflow.

The first reference gateway can be local/private-pilot oriented. Public
deployment options should be documented separately after the local workflow is
stable.

## Deployment and HTTPS options

Deferred until the reference gateway exists:

- Caddy or reverse-proxy example for HTTPS.
- Render/Fly/Railway/VPS deployment notes.
- Guidance for exposing a local gateway to a phone during testing without
  weakening HTTPS or HMAC requirements.
- Operational checklist for rotating device secrets.

## GreensVilla integration

GreensVilla is the motivating production use case, but QasidRelay should not be
tightly coupled to GreensVilla in the Android repo.

Preferred direction:

- Keep this repo as the Android client and protocol reference.
- Implement GreensVilla server endpoints in the GreensVilla repo later.
- Reuse `docs/protocol.md` and `docs/server-integration.md` as the contract.
- Add contract tests or shared signing vectors before wiring real messages.

This avoids a monorepo unless there is a concrete need for shared code,
shared release cadence, or a reusable reference gateway package.
