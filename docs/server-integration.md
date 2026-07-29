# Server integration guide

This guide is for server applications, including a future GreensVilla
integration, that want to implement the QasidRelay polling protocol.

Do not change GreensVilla from this repository. Treat QasidRelay as a separate
Android client and protocol until an explicit integration task is opened in the
server application.

## Required endpoints

- `GET /api/qasid-relay/v1/messages/pending?device_id=<device_id>&limit=3`
- `POST /api/qasid-relay/v1/messages/<message_id>/status`

Both endpoints must require Qasid HMAC headers and HTTPS. The Android app does
not support cleartext HTTP and does not expose an inbound API on the phone.

## Device registry

Store one row per enrolled device:

- stable device ID;
- high-entropy shared secret;
- enabled/disabled state;
- allowed destination policy;
- last accepted timestamp and nonce window;
- audit metadata for operator review.

Secrets should be generated server-side, shown once to the operator, and rotated
by issuing a new value that the operator enters into the Android app.

Servers may also generate a one-time setup QR/deep link for operator setup:

```text
qasidrelay://setup?server_url=<url-encoded HTTPS origin>&phone_name=<url-encoded device ID>&private_relay_key=<url-encoded shared secret>
```

The QR/link contains the shared secret. Generate it locally in the trusted
server UI, show it only during create/rotate, never send it through third-party
QR services, and rotate the device secret if the QR/link is exposed.

## Message lifecycle

Use server-side state to prevent duplicate SMS:

1. Create a queued message with a stable ID matching `[A-Za-z0-9._-]{1,128}`.
2. On pending fetch, atomically lease up to the requested limit to that device.
3. Do not return leased or terminal messages in later pending responses.
4. Accept `sent` or `failed` status reports idempotently.
5. Keep late duplicate status reports as audit events, not as new send requests.

QasidRelay also keeps a bounded local recent-ID ledger and a persistent status
report queue. That reduces duplicate risk on the phone, but the server remains
responsible for authoritative message state and abuse controls.

## Status semantics

`sent` means Android's SMS sent callback returned success for every message part.
It is not a carrier delivery receipt. QasidRelay v0 does not request `READ_SMS`
and does not process inbound replies.

If status reporting is temporarily unavailable, the phone retries queued reports
before fetching new messages. Server implementations should accept late terminal
statuses even after a lease expires.

If Android never returns sent callbacks for a submitted SMS, QasidRelay fails
the stale local attempt closed after a bounded timeout and queues `failed` with
detail `stale_sms_attempt_without_android_callback`. The server should treat
that as a terminal device failure for the leased message and should not create a
new outbound job unless an operator deliberately requeues it.

## Abuse controls

Before returning a pending message, enforce:

- transactional templates owned by the server;
- destination allowlists or country policy;
- per-device and per-recipient throttles;
- maximum body length;
- audit logging without secrets or message bodies where possible;
- no bulk-marketing or list-upload workflows.

## Integration test checklist

- Verify the request signature with the vector in `docs/protocol.md`.
- Reject replayed nonces and stale timestamps.
- Return `204` or `{ "messages": [] }` when no message is available.
- Lease a pending message and ensure a second poll does not return it again.
- Accept duplicate status reports for the same message ID without side effects.
- Reject invalid message IDs and unsupported status values.
