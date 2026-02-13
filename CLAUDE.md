# CLAUDE.md — MindTrack

## Project Overview

MindTrack is a personal mental health tracking application. It tracks psychiatrist interviews (structured notes + audio transcription), activities (homework, habits, custom), journal entries, goals with milestones, and provides AI coaching via Claude API. Messaging integration with Telegram and WhatsApp for spontaneous check-ins.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.4.2, Maven, Spring Security (OAuth2 + JWT), Spring Data JPA, Flyway
- **Frontend:** Vue.js 3 (Composition API), TypeScript, Vite, Pinia, Vue Router, Chart.js
- **Database:** Aurora Serverless v2 MySQL (prod), H2 in-memory (local)
- **Infrastructure:** Terraform, AWS Lambda (SnapStart), API Gateway, CloudFront, S3, EventBridge, Secrets Manager
- **CI/CD:** GitHub Actions, SonarCloud

## Architecture

Modular monolith — single Spring Boot app with 10 modules:
`auth`, `admin`, `interview`, `activity`, `journal`, `goals`, `ai`, `messaging`, `analytics`, `common`

Each module has: `controller/`, `service/`, `repository/`, `model/`, `dto/`, `config/`

## Running Locally

```bash
# Backend (H2 in-memory, no external deps)
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local

# Frontend (proxies /api to backend)
cd frontend && npm run dev
```

Note: Maven requires explicit JAVA_HOME on this machine:
```bash
JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home" /opt/homebrew/bin/mvn <command>
```

## Running Tests

```bash
# Backend
cd backend && mvn verify

# Frontend
cd frontend && npm run test:unit
cd frontend && npm run lint

# Infrastructure
bash infra/tests/unit/validate.sh
```

## Code Conventions

- **Java:** Checkstyle (Google-based), 4-space indent, 120 char width. Config: `config/checkstyle/checkstyle.xml`
- **TypeScript/Vue:** ESLint + Prettier, 2-space indent, no semicolons, single quotes. Config: `frontend/eslint.config.js`
- **Terraform:** `terraform fmt`, tflint, tfsec. 2-space indent.

## Branch Strategy

- `main` — production branch, deploys automatically
- `feature/*` — feature development
- `bugfix/*` — bug fixes
- Conventional commits: `feat:`, `fix:`, `chore:`, `docs:`, `test:`, `infra:`

## Important Files

| File | Purpose |
|------|---------|
| `backend/pom.xml` | Maven config with all dependencies and plugins |
| `backend/src/main/resources/application-local.yml` | Local dev config (H2, no Flyway) |
| `backend/src/main/resources/db/migration/V1__initial_schema.sql` | Database schema + seed data |
| `frontend/vite.config.ts` | Vite config with API proxy |
| `frontend/src/router/index.ts` | All frontend routes |
| `infra/main.tf` | Terraform module composition |
| `docker/docker-compose.yml` | Local dev with MySQL + LocalStack |
| `sonar-project.properties` | SonarCloud analysis config |
| `.githooks/pre-push` | Pre-push validation hook |

## Database

- **Local:** H2 in-memory with MySQL compatibility mode. DDL auto-generated (`create-drop`). Flyway disabled.
- **Production:** Aurora Serverless v2 MySQL. Flyway migrations enabled. DDL validation only.

## Environment Variables (Production)

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` — Aurora connection
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` — OAuth2
- Secrets Manager entries: `claude_api_key`, `telegram_bot_token`, `whatsapp_api_token`
