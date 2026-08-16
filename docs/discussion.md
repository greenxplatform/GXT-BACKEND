# GXT Backend — Discussion Log

Living document for architecture decisions, Q&A, and roadmap.  
Each entry has a unique response id and brief reference.

> **Security:** Never commit real API keys here. Use `.env` or environment variables only.

---

## Response log index

| id | date | topic |
|----|------|-------|
| `gxt-resp-20260809-001` | 2026-08-09 | Agentic service module — do we need it? naming & placement |
| `gxt-resp-20260809-002` | 2026-08-09 | Can I run the app now? DB config check |
| `gxt-resp-20260809-003` | 2026-08-09 | Cursor API key vs OpenAI key — what works in our backend |
| `gxt-resp-20260809-004` | 2026-08-09 | Recommended roadmap: run app → agent module → frontend |

---

## gxt-resp-20260809-001 — Agentic service: do we need a separate module?

**Question ref:** Should there be a dedicated agentic service between the user and the frontier LLM (polish English, structure prompts, multi-turn chat until final strategy)?

**Short answer:** **Yes — and your instinct is correct.** The current skeleton already has chat logic inside `gxt-strategy-builder`, but the HLD treats that as two layers:

| Layer | Responsibility today | Should become |
|-------|---------------------|---------------|
| **Agent / orchestrator** | Partially in `ChatOrchestrationService` + `SpringAiLlmChatClient` | Dedicated module |
| **Strategy Builder** | CRUD, live card, spec snapshots, persistence | Stays — no LLM inside backtest path |

### Recommended module name

**Primary recommendation: `gxt-strategy-agent`**

Alternatives (ranked):

1. **`gxt-strategy-agent`** — best fit: “agent that turns plain English into a structured strategy via conversation”
2. `gxt-strategy-orchestrator` — matches HLD term “GXT Backend Orchestrator” but sounds more infra than product
3. `gxt-conversation-agent` — clear but less domain-specific

Avoid generic names like `gxt-agentic-service` (too vague for a trading platform).

### What `gxt-strategy-agent` would own

```
User (frontend)
    ↓ plain English
gxt-strategy-agent          ← NEW dedicated module
    · query polish (grammar, clarity, no intent loss)
    · system prompt + GXT rule vocabulary
    · multi-turn loop user ↔ LLM
    · extract structured JSON plan from LLM replies
    ↓ structured spec draft
gxt-strategy-builder          ← existing
    · persist strategy, chat history, live card
    · spec snapshots / versioning
    ↓ user approves backtest (later step)
gxt-backtest                  ← existing, deterministic, NO LLM
```

### What stays out of the agent (by design)

- **Backtest engine** — never calls LLM (HLD rule)
- **Simulation / deploy** — later phases
- **Auth, waitlist, portfolio** — skeleton modules unchanged

### Practical approach (don’t rebuild everything)

We do **not** throw away the current implementation. Phase 2 plan:

1. Create **`modules/gxt-strategy-agent`** skeleton
2. **Move** from `gxt-strategy-builder`: `LlmChatClient`, `SpringAiLlmChatClient`, `HeuristicLlmChatClient`, `LlmProviderRouter`, `StrategyChatPromptBuilder`, and core of `ChatOrchestrationService`
3. **Keep** in `gxt-strategy-builder`: entities, repositories, `StrategyCrudService`, `StrategyPlanNormalizer`, REST controllers (controllers call agent service)
4. Agent exposes an internal API like `StrategyAgentService.converse(strategyId, userMessage)` → returns polished assistant text + normalized spec

This matches the plan you implemented and extends it cleanly.

---

## gxt-resp-20260809-002 — Can I run the app now? Database config

**Question ref:** Postgres `gxt` DB, user/password `postgres` — good to run?

**Short answer:** **Yes, you’re good to run** if PostgreSQL is running locally on port 5432.

Your `application-local.yml` datasource:

```yaml
url: jdbc:postgresql://localhost:5432/gxt
username: postgres
password: postgres
```

That matches a standard local Postgres setup. On first startup:

- **Flyway** runs `V1__init.sql` and creates all tables (if not already applied)
- **JPA** uses `ddl-auto: validate` (schema must match migrations)

### Run checklist

1. PostgreSQL running (`5432`)
2. Database `gxt` exists (you created it ✓)
3. Set OpenAI key via environment (see 003) — otherwise chat falls back to **heuristic mock** (no real LLM)
4. From repo root:

```powershell
cd C:\General\knowladge-Base\GXT_BACKEND
$env:OPENAI_API_KEY = "<your-openai-key>"
$env:GXT_AI_PROVIDER = "openai"
.\gradlew.bat bootRun
```

5. Verify:
   - http://localhost:8080/actuator/health → UP
   - http://localhost:8080/swagger-ui.html

### Config tweaks needed for real OpenAI (not heuristic)

Current root `application.yml` has `spring.ai.model.chat: none` (disables default ChatModel). For OpenAI in local dev, set in **`application-local.yml`** (or env):

```yaml
spring:
  ai:
    model:
      chat: openai   # enable OpenAI chat model bean

gxt:
  ai:
    provider: openai
```

And ensure `OPENAI_API_KEY` is set in the environment — **not** hardcoded in yaml files that might be committed.

### Note on model choice

You mentioned **GPT “terra”** on OpenAI billing — for Spring AI use a supported model id such as `gpt-4o-mini` (cheap dev default) or `gpt-4o`. We can switch model id in `application-local.yml` once you confirm the exact model name from your OpenAI dashboard.

---

## gxt-resp-20260809-003 — Cursor API key vs OpenAI API key

**Question ref:** Can we use the Cursor subscription API key to talk to frontier models in our Spring Boot agent?

**Short answer:** **No — do not use the Cursor API key in GXT backend.** Use your **OpenAI API key** (platform.openai.com) via Spring AI.

### Two different key systems

| Key | Where from | Used for |
|-----|------------|----------|
| **Cursor user API key** (`crsr_...`) | Cursor Dashboard → API Keys | Cursor **Cloud Agents API**, CLI/CI automation **inside Cursor ecosystem** — not general chat completions for your app |
| **OpenAI API key** (`sk-proj-...` or `sk-...`) | platform.openai.com | Direct calls to OpenAI models — **this is what Spring AI needs** |
| **Cursor BYOK keys** | Cursor Settings → Models | Only powers chat **inside Cursor IDE** — not your external backend |

### How it works for GXT

```
GXT Backend (Spring AI)
    → HTTPS → api.openai.com
    → Authorization: Bearer <OPENAI_API_KEY>
    → model: gpt-4o-mini / gpt-4o / etc.
```

Your OpenAI key bills **your OpenAI account** ($5 credit is fine for dev). Cursor subscription is separate billing — it does not proxy as an OpenAI endpoint for custom servers.

### Security note on key stored in this file

A raw OpenAI key was pasted at the top of this file earlier. **Remove it from this file** and use:

```powershell
# .env (gitignored) or shell env
OPENAI_API_KEY=sk-proj-...
```

Add `docs/discussion.md` to `.gitignore` if you insist on keeping keys in notes — **better: never store keys in repo files.**

---

## gxt-resp-20260809-004 — Roadmap: what we do next (ordered)

**Question ref:** Run app → agent foundation → frontend wiring → testing

### Agreed sequence

| Step | Action | Outcome |
|------|--------|---------|
| **1** | Run backend locally with Postgres + OpenAI env | Confirm health, Swagger, create strategy + chat with real LLM |
| **2** | Enable OpenAI in `application-local.yml` (`chat: openai`, `gxt.ai.provider: openai`) | Replace heuristic fallback for dev |
| **3** | Create **`gxt-strategy-agent`** module; move LLM orchestration out of strategy-builder | Clean separation user ↔ agent ↔ builder |
| **4** | Enhance agent: query polish step + structured multi-turn loop | Better responses without changing backtest |
| **5** | Wire `gxtos` frontend to backend APIs | Replace localStorage mock |
| **6** | E2E test: chat → card → backtest → report | Full Phase 1 loop |

### What we already have (from last implementation)

- Multi-module Gradle monolith at `GXT_BACKEND` root
- 12 module skeletons; **implemented:** common, market-data, strategy-builder, backtest
- Chat today: heuristic fallback OR Spring AI when key + config enabled
- Backtest: async, deterministic, no LLM
- Docs: `docs/api.md`, `docs/changelog.md`

### Agent “improve response” goals (Phase 2)

1. **Pre-LLM polish** — fix grammar/clarity; preserve trader intent (small dedicated prompt or lightweight pass)
2. **GXT system prompt** — enforce rule vocabulary v1 (SMA, RSI, instruments list)
3. **Multi-turn** — agent mediates until `StrategyPlanNormalizer` accepts spec
4. **Validation loop** — if engine can’t run a rule, agent explains and asks user to refine (HLD open item #5)

Backtest / simulation / deploy remain **downstream** — only after user approves structured spec.

---

## Open items for next discussion entry

- [ ] Confirm exact OpenAI model id for dev (`gpt-4o-mini` vs your “terra” selection)
- [ ] Approve module name `gxt-strategy-agent` vs alternative
- [ ] After successful `bootRun`, proceed to extract agent module (Step 3)

---

*Last updated: 2026-08-09*
