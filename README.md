# GXT Backend

Multi-module Spring Boot monolith for the GXT Agentic Full Stack Trading Platform.

## Stack

- Java 21, Spring Boot 3.4, Gradle Kotlin DSL
- PostgreSQL + Flyway
- Spring Security (JWT) + Gmail SMTP OTP
- Spring AI (optional; heuristic fallback when chat model is none)

## Modules

| Module | Status |
|--------|--------|
| gxt-common | Shared DTOs (including `gxtIdentity`) |
| gxt-identity | Register / login / email OTP / JWT / Google OAuth |
| gxt-strategy-builder | Strategy CRUD + chat |
| gxt-backtest | Deterministic backtest engine |

## Quick start (local)

```powershell
cd C:\General\knowladge-Base\GXT_BACKEND
docker compose up postgres -d
copy src\main\resources\application-local.yml.example src\main\resources\application-local.yml
.\gradlew.bat bootRun
```

- Health: http://localhost:8080/actuator/health
- Swagger: http://localhost:8080/swagger-ui.html

Local Postgres: database `gxt`, user `postgres`, password `postgres` (matches a typical local install and `docker-compose.yml`).

OTP is logged to the console locally (`gxt.otp.delivery=log`). For Gmail SMTP set:

```
GXT_OTP_DELIVERY=smtp
GXT_MAIL_USERNAME=connect.gxt@gmail.com
GXT_MAIL_PASSWORD=<gmail-app-password>
GXT_JWT_SECRET=<32+ character secret>
```

Do not commit the Gmail app password.

## Identity API

- `POST /api/v1/auth/register` `{ "name", "email", "password" }`
- `POST /api/v1/auth/login` `{ "email", "password" }`
- `POST /api/v1/auth/logout` (Bearer)
- `POST /api/v1/auth/oauth/google` `{ "idToken" }`
- `POST /api/v1/auth/verification/email/send` (Bearer)
- `POST /api/v1/auth/verification/email/confirm` `{ "code" }` (Bearer)
- `GET /api/v1/auth/verification/status` (Bearer)
- `GET /api/v1/users/me` (Bearer)
- `POST /api/v1/notify-me` `{ "name", "email" }` — waitlist; confirmation email if Gmail env is set

See `docs/api.md`.

## Git / secrets

A separate GitHub repo from the frontend is fine. **Never commit `.env`.** It holds Gmail and RDS passwords.

Before the first push, confirm `.env` is not listed:

```powershell
git status
git check-ignore -v .env
```

You want `gradle/wrapper`, `gradle/libs.versions.toml`, `gradlew`, and `gradlew.bat` **in** the repo. You do **not** want `**/build/` or `.env`.

## Deploy to the same EC2 (us-east-1)

SPA stays on port 80. Backend listens on **8080** on the instance. Nginx in the `gxtos` container proxies `/api/` → host `:8080`. RDS is Postgres — do not run a Postgres container on EC2.

1. RDS security group: inbound **5432** from the **EC2 security group only** (not `0.0.0.0/0`).
2. Initial database name on that instance should be `gxt-sandbox` (the RDS instance identifier). If login fails, check RDS → Configuration → DB name; it may still be `postgres`.
3. EC2 security group: keep **80** public. Do **not** open **8080** to the internet.
4. Build the JAR on your PC (Java bytecode is the same on ARM):

```powershell
cd C:\General\knowladge-Base\GXT_BACKEND
.\gradlew.bat bootJar -x test
```

5. Copy `build\libs\gxt-backend.jar` and a **server copy** of `.env` to the instance (SCP). On the box set a 32+ character `GXT_JWT_SECRET` (do not use the local default).
6. Install Corretto 21 aarch64, then:

```bash
export SPRING_PROFILES_ACTIVE=aws
# load .env then:
java -jar gxt-backend.jar
```

Or Docker: pass `--env-file .env -e SPRING_PROFILES_ACTIVE=aws -p 8080:8080`. Building Gradle **on** t4g.small (2 GB RAM) often OOMs — prefer the JAR copy.

7. Rebuild the frontend container after `nginx.conf` includes `location /api/`. Then `https://gxtos.com/api/v1/notify-me` hits the backend.
