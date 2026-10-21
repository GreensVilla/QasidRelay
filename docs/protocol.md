# QasidRelay protocol

This document defines the first polling protocol for QasidRelay devices.

All requests are initiated by the Android app. The server must expose HTTPS
endpoints, store pending SMS messages, and verify signed requests from known
devices.

## Device configuration

Each device needs:

- `base_url`: HTTPS origin for the server.
- `device_id`: stable server-issued identifier.
- `shared_secret`: high-entropy secret known only to the server and device.
- `poll_seconds`: polling interval, minimum 5 seconds in the Android app.

## Request signing

Every request includes:

- `X-Qasid-Device-Id`
- `X-Qasid-Timestamp`
- `X-Qasid-Nonce`
- `X-Qasid-Body-SHA256`
- `X-Qasid-Signature`

The canonical string is:

```text
METHOD
PATH_WITH_QUERY
BODY_SHA256_HEX
UNIX_TIMESTAMP_SECONDS
NONCE
```

`X-Qasid-Signature` is lowercase hex HMAC-SHA256 of the canonical string using
the device shared secret.

Reference signing vector:

- secret: `test-secret`
- method: `post`
- path/query: `/api/qasid-relay/v1/messages/pending?device_id=device-01&limit=3`
- body: empty string
- timestamp: `1735689600`
- nonce: `00000000-0000-4000-8000-000000000001`
- body SHA-256: `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`
- signature: `670662d1044a2704d617990a8724603f54f3ef7d3823152ea9d9d4b60bb556c9`

Server requirements:

- reject unknown devices;
- reject timestamps outside a short clock-skew window;
- reject replayed nonces;
- compare signatures in constant time;
- rate-limit devices and destination numbers.

## Fetch pending messages

```http
GET /api/qasid-relay/v1/messages/pending?device_id=<device_id>&limit=3
```

Response:

```json
{
  "messages": [
    {
      "id": "msg_01JABCDEFGHJKMNPQRSTVWXYZ",
      "to": "<E164_DESTINATION>",
      "body": "Your private pilot notification is ready."
    }
  ]
}
```

No pending messages may be returned as either:

```json
{ "messages": [] }
```

or HTTP `204`.

When returning messages, the server should atomically lease them to the polling
device. A leased message should not be returned again while the lease is active,
and a terminal `sent` or `failed` status should permanently close that message.

## Report message status

```http
POST /api/qasid-relay/v1/messages/msg_01JABCDEFGHJKMNPQRSTVWXYZ/status
Content-Type: application/json

{
  "status": "sent",
  "detail": null
}
```

Allowed `status` values for the initial version:

- `sent`
- `failed`

`sent` means Android accepted every SMS part through the `SmsManager` sent
callback. It is not a carrier delivery receipt and does not prove the recipient
handset received the message.

Servers must treat status reports as idempotent. They should accept duplicate or
late reports for the same message ID without creating another outbound SMS job.

## Message constraints

The server should enforce:

- stable message IDs matching `[A-Za-z0-9._-]{1,128}`;
- destination allowlist or country/number policy;
- maximum message length;
- OTP/body templates owned by the server;
- per-recipient and per-device throttles;
- no bulk marketing endpoints.

## Outbound Android SMS callback details

The optional `detail` in a terminal `failed` status should be a bounded,
safe diagnostic category, never a raw exception or SMS content. The current
Android client reports known categories `RESULT_ERROR_NO_SERVICE`,
`RESULT_ERROR_RADIO_OFF`, `RESULT_ERROR_NULL_PDU`,
`RESULT_ERROR_GENERIC_FAILURE`, and substitutes `SMS_RESULT_OTHER`
for unknown callback codes. Synchronous send exceptions use `SEND_EXCEPTION`.
The stale-attempt category remains
`stale_sms_attempt_without_android_callback`. Servers should accept
future safe categories rather than hardcode one exhaustive enum.
An Android `sent` status does not prove delivery to the recipient.

## Optional destination policy in pending response (backward compatible)

A gateway **may** attach this informational object to an HTTP 200 response
containing the existing `messages` array:

```json
{
  "messages": [],
  "destination_policy": {
    "mode": "country_restricted",
    "country_prefix": "+92"
  }
}
```

Recognized `mode` values: `allowlist_only`, `country_restricted`,
`self_managed`. A country prefix must be `+` followed by 1–3 ASCII digits.
The Android client shows an allowlisted label and never uses this value as
a substitute for server-side destination/rate-limit enforcement. Unsupported
or missing values display `Policy not reported by server`. Older gateways
need not implement the field. A `204` response cannot carry policy metadata.
