# 03 — Battery optimization guidance

Status: `PLANNED`

Priority: `P0`

## Goal

Help operators keep the foreground relay reliable on real Android devices,
especially vendor-customized phones that restrict background work aggressively.

## Problem

QasidRelay relies on foreground polling. Some Android vendors still restrict
network/service behavior even with a foreground notification. Operators need
clear guidance without the app making unsafe assumptions or silently bypassing
system protections.

## Scope

- Detect whether battery optimization is ignoring QasidRelay where Android APIs
  expose that state.
- Show a warning when optimization may interfere with polling.
- Provide device/vendor-specific help text for common vendors when confirmed by
  real testing.
- Provide a generic `Open battery settings` action using safe Android intents
  where available.
- Record manual guidance in docs for tested devices.

## Out of scope

- Auto-granting exemptions.
- Misleading users into disabling all device protections.
- Vendor-specific hacks that break Android policy or Play-style expectations.
- Claiming compatibility without testing the actual device/vendor.

## UX requirements

- Use cautious copy: `Battery settings may stop polling on some phones.`
- Tell the operator how to verify: last poll should keep updating while relay is
  running.
- If the settings intent is unavailable, show manual steps instead.

## Development directions

- Use Android public APIs only.
- Keep vendor guidance in a small data structure or markdown-backed copy that is
  easy to update after field testing.
- Do not request extra permissions for battery guidance.
- Add tests for mapping vendor/manufacturer names to help copy.

## Acceptance criteria

- App shows a generic battery guidance section.
- Tested phone evidence records whether changes were required.
- No new dangerous permissions are added.
- Operator can dismiss/understand the warning without disabling relay.
