# 11 — Better foreground notification

Status: `IN PROGRESS` (redacted status notification implemented)

Priority: `P2`

## Implemented so far

- Foreground notification opens the app as before, now showing a short,
  allowlisted gateway state or bounded pending report count.
- Refreshes within the existing foreground worker loop without extra wakeups.
- No raw gateway error, recipient phone, OTP, message body, or unsafe action.
- `START_NOT_STICKY` avoids requested automatic restart after process death.
- JVM tests assert unknown strings are not echoed.

Still pending: notification visibility/screen-reader checks on real Android,
including Android 13+ permission behavior, and vendor background tests.

## Goal

Make the foreground notification more useful while staying concise and safe.

## Problem

The foreground notification proves the relay is active. It should also help the
operator notice if polling is stale or status reports are pending.

## Scope

Notification may show:

- running state;
- last poll relative time;
- pending status report count;
- last safe error category;
- action to open QasidRelay.

## Out of scope

- Message bodies, OTPs, full phone numbers.
- Notification action buttons that send, retry, or change config without opening
  the app.
- Silent background sending after reboot.

## UX requirements

- Keep notification text short enough for older Android notification layouts.
- If notification permission is missing on Android 13+, main dashboard should
  explain that the foreground service may be less visible.

## Development directions

- Reuse status model from the dashboard task where possible.
- Update notification only when state changes or at a conservative interval.
- Avoid excessive wakeups just to refresh relative time.

## Acceptance criteria

- Notification opens app reliably.
- Notification never exposes OTP/body/full recipient.
- Pending report count and stale polling states are represented safely.
- Real-device screenshots are captured with secrets redacted.
