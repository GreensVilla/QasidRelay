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
      "id": "msg_01J...",
      "to": "+15550100000",
      "body": "Your verification code is 000000."
    }
  ]
}
```

No pending messages may be returned as either:

```json
{ "messages": [] }
```

or HTTP `204`.

## Report message status

```http
POST /api/qasid-relay/v1/messages/msg_01J.../status
Content-Type: application/json

{
  "status": "sent",
  "detail": null
}
```

Allowed `status` values for the initial version:

- `sent`
- `failed`

Servers should treat status reports as idempotent.

## Message constraints

The server should enforce:

- destination allowlist or country/number policy;
- maximum message length;
- OTP/body templates owned by the server;
- per-recipient and per-device throttles;
- no bulk marketing endpoints.
