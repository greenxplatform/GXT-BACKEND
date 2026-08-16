# GXT Backend — Cross-Module Changelog

Living document for changes across all Gradle modules. Update on every feature merge.

---

## 2026-08-02 — Initial scaffold (all modules)

- **Added:** Multi-module Gradle monolith at `GXT_BACKEND` root with Java 21, Spring Boot 3.4.2.
- **Added:** Root-level `GxtApplication` bootstraps all 12 modules.
- **Added modules (skeleton):** `gxt-auth`, `gxt-dashboard`, `gxt-waitlist`, `gxt-strategy-library`, `gxt-simulation`, `gxt-watchlist`, `gxt-portfolio`, `gxt-self-broking`.
- **Added modules (implemented):** `gxt-common`, `gxt-market-data`, `gxt-strategy-builder`, `gxt-backtest`.
- **API impact:** Strategy CRUD, chat, backtest job APIs, market data APIs documented in [api.md](./api.md).
- **Migration:** Flyway `V1__init.sql` — strategies, chat_messages, strategy_spec_snapshots, backtest_jobs, backtest_reports, backtest_trades.

---

## AWS deployment path (ECS Fargate + RDS)

Documented initial deployment steps (manual / console):

1. **RDS PostgreSQL 16**
   - Create DB `gxt`, note endpoint, username, password.
   - Security group: allow inbound 5432 from ECS task SG only.

2. **ECR**
   - Create repository `gxt-backend`.
   - Build and push: `docker build -t gxt-backend .` → tag → `docker push`.

3. **Secrets Manager**
   - Store `GXT_DB_PASSWORD`, `ANTHROPIC_API_KEY`, `OPENAI_API_KEY`.

4. **ECS Fargate**
   - Task definition: image from ECR, port 8080, env `SPRING_PROFILES_ACTIVE=aws`.
   - Env vars: `GXT_DB_HOST`, `GXT_DB_PORT`, `GXT_DB_NAME`, `GXT_DB_USER`, secrets from Secrets Manager.
   - Service behind ALB; health check `/actuator/health`.

5. **Verify**
   - `GET https://<alb-host>/actuator/health` → UP
   - Run strategy create → chat → backtest flow via API.

Profile config: `src/main/resources/application-aws.yml`
