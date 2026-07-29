# 07 — App version and update check

Status: `PLANNED`

Priority: `P1`

## Goal

Help operators know which QasidRelay version is installed and where to get the
latest signed prerelease.

## Problem

Manual APK installation is acceptable for early controlled use, but support
needs users to run known-good signed builds. The app should make version state
visible.

## Scope

- Show installed `versionName` and `versionCode`.
- Link to the latest GitHub release page or project download page.
- Optionally fetch latest release metadata if a safe, privacy-conscious update
  check is approved.
- Explain signature/update behavior: signed releases should update in place;
  older debug builds may require one uninstall.

## Out of scope

- Silent auto-update.
- Downloading/installing APKs inside the app.
- Forcing users to update before sending unless a separate compatibility gate is
  added server-side.

## UX requirements

- Add a small `App version` section in status/settings.
- If network checking is not implemented, simply provide a release link.
- If network checking is implemented, provide clear states: latest, update
  available, check failed.

## Development directions

- Prefer static release link first. Network update checks can be added later.
- If querying GitHub/API, avoid sending device ID, phone name, server URL, or
  private operational data.
- Add tests for version display formatting.

## Acceptance criteria

- Operator can identify installed version without Android settings.
- Release link opens externally.
- No secrets or device identifiers are sent to release infrastructure.
