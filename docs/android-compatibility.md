# Android compatibility

The first QasidRelay Android app targets broad practical compatibility rather
than the oldest possible Android versions.

## Baseline

- Minimum SDK: Android 6.0 / API 23.
- Target SDK: API 35.
- Language: Java.
- UI: platform Android widgets.
- SMS API: `SmsManager` with sent-result `PendingIntent` callbacks.
- Operator control: messaging starts disabled and can be enabled or disabled
  from the phone UI.

## Why API 23

`SmsManager` itself is older, but API 23 is a practical support floor because it
uses Android's runtime permission model. Devices older than Android 6 are now
harder to secure, harder to test, and more likely to have TLS compatibility
issues.

If a real target phone is older than Android 6, compatibility can be revisited
after testing that exact device.

## Boot and restart behavior

QasidRelay targets API 35 and runs the relay as a `dataSync` foreground service.
Android 15 restricts `BOOT_COMPLETED` receivers from launching `dataSync`
foreground services directly, so QasidRelay does not auto-start polling or SMS
sending after device reboot.

Instead, the app records that a manual restart is required when it receives
`BOOT_COMPLETED` or `MY_PACKAGE_REPLACED` while messaging is enabled. The status
screen then shows the service as stopped and the main messaging button changes to
`Start relay service` so an operator can resume polling from the foreground UI.

This keeps the phone-local operator consent boundary intact and avoids hidden
post-reboot SMS sending.

## Known constraints

- The app requires telephony messaging hardware.
- The app requires the `SEND_SMS` permission.
- Android 13 and newer may also require `POST_NOTIFICATIONS` for the foreground
  relay notification to be visible.
- Dual-SIM routing is not implemented in the first version.
- Foreground service behavior varies across Android vendors; battery
  optimization settings may need manual adjustment.
- Disabling messaging stops fetching new jobs and stops the foreground relay
  service. Already-submitted Android SMS callbacks may still complete.
- `sent` status means Android accepted all SMS parts through sent callbacks; it
  is not a carrier delivery receipt.
