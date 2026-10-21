# 14 — Multi-profile support

Status: `DEFERRED` (requires stable single-profile device validation and migration tests)

Priority: `P3`

## Deferral rationale

The current app stores one Keystore-protected server identity and persistent
outbound SMS attempt/report state. Migrating or switching identities without
physical-device testing risks losing outstanding delivery reports or sending
with a wrong account. Keep the explicit safe single-profile reset path until
migration, hardware and rollback tests are available.

## Goal

Allow advanced operators to keep separate gateway profiles, such as staging and
production, without repeatedly retyping setup details.

## Problem

During development and rollout, one phone may need to test staging while a later
production profile exists. Switching manually risks misconfiguration.

## Scope

Potential profile fields:

- display name;
- server URL;
- phone/device name;
- encrypted private relay key;
- messaging enabled/running state per active profile;
- selected SIM per profile after dual-SIM support exists;
- last status snapshot per profile.

## Out of scope

- Running multiple profiles concurrently.
- Load balancing messages across profiles.
- Sharing keys between profiles.
- Consumer-facing multi-account features.

## UX requirements

- Only one active profile can send at a time.
- Switching profile must stop the relay and require explicit start.
- Staging/production distinction should be visually clear.
- Deleting a profile requires confirmation.

## Development directions

- Defer until the single-profile app is stable and field-tested.
- Model profile storage carefully so each key remains Keystore-protected.
- Migration from existing single-profile preferences must be safe and reversible
  in testing.

## Acceptance criteria

- Existing single-profile installs migrate without losing working setup.
- Switching profile cannot silently keep sending from the old gateway.
- Tests cover migration and active-profile selection.
- Real-device upgrade test is recorded.
