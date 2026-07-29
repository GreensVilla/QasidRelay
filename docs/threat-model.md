# Threat model

QasidRelay handles sensitive transactional SMS. The core risk is that an
attacker sends unauthorized messages through a real SIM or learns verification
codes from logs, device storage, or network traffic.

## In scope

- Protecting the server-to-device polling channel.
- Preventing unauthorized message sends.
- Reducing replay risk.
- Avoiding public exposure of OTP bodies.
- Making abuse boundaries clear for OSS users.

## Out of scope for the first version

- Reading inbound SMS.
- Carrier-grade delivery receipts.
- Multi-device scheduling.
- Google Play distribution.
- Bulk marketing workflows.

## Assumptions

- The Android device is owned and controlled by the operator.
- The SIM/carrier plan is authorized for the messages being sent.
- The server uses HTTPS.
- Device secrets are provisioned out of band.
- Setup QR/deep-link payloads are shown only by a trusted gateway operator flow
  and are treated as one-time secret material.

## Controls

- Device-initiated polling; no inbound device API is exposed.
- HMAC-signed requests.
- Timestamp and nonce replay protection expected on the server.
- Shared secret encrypted at rest with Android Keystore AES-GCM.
- `SEND_SMS` permission only; no `READ_SMS` in the initial app.
- No camera permission; optional QR setup uses the phone camera/QR scanner to
  open a `qasidrelay://setup` deep link.
- Android sent-result callbacks before reporting terminal status.
- Persistent status-report queue that retries failed gateway acknowledgements.
- Bounded local recent-message ledger to reduce duplicate sends for repeated
  message IDs.
- Server-owned message templates and throttles.
- No OTP codes in public dashboards, issue reports, or examples.

## Delivery tradeoffs

QasidRelay v0 cannot provide carrier-grade delivery receipts or strict
exactly-once SMS delivery. The app reports `sent` after Android accepts all SMS
parts through sent callbacks, queues status reports if the network is down, and
remembers recent message IDs locally to avoid repeated sends. The server must
still lease jobs atomically, make status handling idempotent, and avoid reusing a
message ID for different SMS content.

## Operational guidance

- Use a dedicated SIM and Android device.
- Lock the device physically and with a passcode.
- Disable battery optimizations only for the relay app if needed.
- Monitor carrier warnings and failed-send rates.
- Rotate device secrets if the phone is lost or reused.
- Rotate device secrets if a setup QR/link was exposed outside the intended
  operator setup flow.
- Do not use consumer SMS bundles for unsolicited or high-volume campaigns.
