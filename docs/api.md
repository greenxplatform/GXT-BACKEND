# GXT Backend API Reference

Base URL (local): `http://localhost:8080`

OpenAPI / Swagger UI: `/swagger-ui.html`  
OpenAPI JSON: `/v3/api-docs`

## Authentication (gxt-identity)

Email/password register + login, JWT Bearer, email OTP, optional Google OAuth.

| Method | Path | Auth |
|--------|------|------|
| POST | `/api/v1/auth/register` | public — body `{ "name", "email", "password" }` |
| POST | `/api/v1/auth/login` | public — body `{ "email", "password" }` |
| POST | `/api/v1/auth/logout` | Bearer |
| POST | `/api/v1/auth/oauth/google` | public — body `{ "idToken" }` |
| POST | `/api/v1/auth/verification/email/send` | Bearer |
| POST | `/api/v1/auth/verification/email/confirm` | Bearer — body `{ "code" }` |
| GET | `/api/v1/auth/verification/status` | Bearer |
| GET | `/api/v1/users/me` | Bearer |

Register returns a JWT and sends an OTP (console log locally, Gmail SMTP when `gxt.otp.delivery=smtp`).

## Notify Me (waitlist)

Public interest signup until GXT is generally available. Stored for later selective invites (private signup/login links — not sent yet).

| Method | Path | Auth |
|--------|------|------|
| POST | `/api/v1/notify-me` | public — body `{ "name", "email" }` |

Sends a confirmation email when Gmail env (`GXT_MAIL_USERNAME` / `GXT_MAIL_PASSWORD`) is set; otherwise logs the mail. Duplicate email returns 200 and does not resend.

Strategy APIs still accept optional `X-User-Id` until they read JWT.

---

## Strategy Builder (M4) — Implemented

### Create strategy

`POST /api/v1/strategies`

```json
{ "name": "Nifty SMA Trend Ride", "preferredMode": "fully_automated" }
```

### List strategies

`GET /api/v1/strategies`

### Get strategy

`GET /api/v1/strategies/{id}`

### Update strategy

`PATCH /api/v1/strategies/{id}`

```json
{ "name": "Updated name", "preferredMode": "semi_automated", "summary": "..." }
```

### Chat (LLM → strategy card)

`POST /api/v1/strategies/{id}/chat`

```json
{ "message": "I trade Nifty with a 20-day SMA trend ride" }
```

Response:

```json
{
  "assistantMessage": "...",
  "strategyCard": { "id": "...", "status": "DRAFT", "spec": { ... } },
  "validationErrors": []
}
```

### Chat history

`GET /api/v1/strategies/{id}/chat`

---

## Backtest Engine (M4 sub-module) — Implemented

### Submit backtest (async)

`POST /api/v1/strategies/{id}/backtest` → **202 Accepted**

```json
{ "jobId": "job_abc123", "status": "queued" }
```

### Poll job status

`GET /api/v1/backtests/jobs/{jobId}`

States: `queued` → `running` → `completed` | `failed`

### Get report

`GET /api/v1/backtests/{reportId}`

### Latest report for strategy

`GET /api/v1/strategies/{id}/backtest/latest`

---

## Market Data — Implemented

### List instruments

`GET /api/v1/market/instruments`

Returns: `NIFTY50`, `SENSEX`, constituent symbols.

### OHLCV series

`GET /api/v1/market/series?instrument=NIFTY50&from=2023-11-01&to=2025-11-11`

---

## Strategy Spec Contract (v1)

Machine-runnable JSON between LLM and Backtest Engine:

```json
{
  "instrument": "NIFTY50",
  "timeframe": "Daily",
  "direction": "long",
  "entryRules": ["Close crosses above SMA_20"],
  "exitRules": ["Close crosses below SMA_20"],
  "stopLoss": { "type": "percent", "value": 2 },
  "backtestWindow": { "from": "2023-01-01", "to": "2025-11-11" }
}
```

### Rule vocabulary v1

| Category | Supported values |
|----------|-----------------|
| Instruments | NIFTY50, SENSEX, RELIANCE, TCS, INFY, HDFCBANK, ICICIBANK, SBIN |
| Timeframe | Daily |
| Entry/Exit | `Close crosses above/below SMA_20`, `RSI_14 crosses above 30`, `Take profit at +3% from entry` |
| Stop loss | `{ "type": "percent", "value": N }` |

**Important:** Backtest path is deterministic — LLM is never called during backtest execution.

---

## Skeleton module health endpoints

| Module | Endpoint | HLD | Status |
|--------|----------|-----|--------|
| Strategy Builder | `GET /api/v1/strategy-builder/health` | M4 | active |
| Backtest Engine | `GET /api/v1/backtest/health` | M4 sub | active |
| Identity | `/api/v1/auth/*`, `/api/v1/users/me` | M1 | implemented |

---

## Error format

```json
{
  "timestamp": "2026-08-02T12:00:00Z",
  "status": 400,
  "error": "Validation Error",
  "message": "...",
  "path": "/api/v1/strategies/...",
  "details": ["Unsupported entry rule: ..."]
}
```
