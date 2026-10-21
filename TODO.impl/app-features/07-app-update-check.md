# 07 — App version and update check

Status: `IN PROGRESS` (installed-version visibility and external release link committed)

Priority: `P1`

## Implemented so far

- Main screen shows local installed version name and build code.
- Explicit external browser link to official GitHub Releases page.
- Explains signed update-in-place and debug/uninstall behavior.
- No background API request, secret sharing, downloader or automatic installation.
- Pure JVM tests for safe formatting/link constants.

Still pending: physical device browser-intent test, version screenshots, and
optional later opt-in safe update metadata check if justified. No "latest"
state is claimed without a release check.

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
