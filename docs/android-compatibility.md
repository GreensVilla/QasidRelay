# Android compatibility

The first QasidRelay Android app targets broad practical compatibility rather
than the oldest possible Android versions.

## Baseline

- Minimum SDK: Android 6.0 / API 23.
- Target SDK: API 35.
- Language: Java.
- UI: platform Android widgets.
- SMS API: `SmsManager`.

## Why API 23

`SmsManager` itself is older, but API 23 is a practical support floor because it
uses Android's runtime permission model. Devices older than Android 6 are now
harder to secure, harder to test, and more likely to have TLS compatibility
issues.

If a real target phone is older than Android 6, compatibility can be revisited
after testing that exact device.

## Known constraints

- The app requires telephony messaging hardware.
- The app requires the `SEND_SMS` permission.
- Dual-SIM routing is not implemented in the first version.
- Foreground service behavior varies across Android vendors; battery
  optimization settings may need manual adjustment.
