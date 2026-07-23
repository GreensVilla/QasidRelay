# Contributing

Contributions are welcome when they preserve QasidRelay's narrow purpose:
lawful transactional SMS relay using an operator-owned Android phone and SIM.

## Development principles

- Keep the phone polling the server; do not expose a public inbound device API
  by default.
- Prefer minimal dependencies.
- Avoid collecting or storing inbound SMS unless a future feature explicitly
  justifies it.
- Do not add bulk-marketing workflows.
- Keep examples generic and secret-free.

## Pull requests

PRs should include:

- a clear summary;
- test/build notes;
- screenshots for UI changes;
- security impact if networking, signing, storage, or permissions changed.

## Local build

The Android project uses Gradle and the Android Gradle Plugin. A Gradle wrapper
will be added once the initial toolchain version is locked.
