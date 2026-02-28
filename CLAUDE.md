# CLAUDE.md — MindTrack

## Project Overview

MindTrack is a personal mental health tracking application. It tracks psychiatrist interviews (structured notes + audio transcription), activities (homework, habits, custom), journal entries, goals with milestones, and provides AI coaching via Claude API. Messaging integration with Telegram and WhatsApp for spontaneous check-ins.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.5.3, Maven, Spring Security (OAuth2 + JWT), Spring Data JPA, Flyway
- **Frontend:** Vue.js 3 (Composition API), TypeScript, Vite, Pinia, Vue Router, Chart.js
- **Database:** Aurora Serverless v2 MySQL (prod), H2 in-memory (local)
- **Infrastructure:** Terraform, AWS Lambda (SnapStart), API Gateway, CloudFront, S3, EventBridge, Secrets Manager
- **CI/CD:** GitHub Actions, release-please, SonarCloud, Renovate

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

Note: Maven uses OpenJDK 25 (Homebrew) with `JAVA_HOME` set in `~/.zshrc`.

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

## Git Hooks

- **Pre-commit** (`.githooks/pre-commit`): Runs Checkstyle (Java), ESLint + Prettier (frontend), terraform fmt (infra) on staged files. Fast, lint-only.
- **Pre-push** (`.githooks/pre-push`): Runs full test suites (mvn test, npm test:unit, tflint, tfsec) + Snyk vulnerability scan. Slower but comprehensive.
- Setup: `git config core.hooksPath .githooks` (done by `./setup.sh`)

## IDE Support

Both VSCode (`.vscode/`) and IntelliJ (`.idea/codeStyles/`) are configured with matching conventions.
- **VSCode:** settings.json, extensions.json, launch.json, tasks.json
- Eclipse formatter: `config/checkstyle/eclipse-formatter.xml` (used by VSCode Java extension)

## Branch Strategy

- `main` — production branch, deploys automatically
- `feature/{issue-id}-{description}` — feature development (e.g., `feature/42-add-user-auth`)
- `bugfix/{issue-id}-{description}` — bug fixes (e.g., `bugfix/17-fix-login`)
- Other types: `chore/`, `docs/`, `test/`, `refactor/`, `ci/`, `infra/`, etc.
- Every branch and commit requires a real GitHub issue — always create one before starting work. Never use `#0` as a placeholder.

Branch name format: `{type}/{issue-id}-{description}` — enforced by the "Branch Name Check" CI gate.
Conventional commits: `feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`, `ci:`, `infra:`

## CI/CD Workflows

| Workflow | File | Trigger | Purpose |
|----------|------|---------|---------|
| Feature CI | `feature.yml` | Push to non-main branches | Build, test, lint, tflint, tfsec, SonarCloud, Snyk |
| Verify | `verify.yml` | Push to `main` | Same as feature CI but on main |
| Branch Name Check | `branch-check.yml` | PR to `main` | Enforces `{type}/{issue-id}-{description}` — required gate |
| Code Review | `code-review.yml` | PR to `main` | Claude posts a structured review comment — required gate |
| Auto Merge | `auto-merge.yml` | PR opened to `main` | Enables squash auto-merge automatically |
| GitHub Config Sync | `github-config-sync.yml` | Manual / push to `infra/github-settings/` | Applies Terraform for repo settings (branch protection, required checks) |
| Release | `release.yml` | Push to `main` | release-please creates/updates release PRs |
| Deploy | `deploy.yml` | GitHub Release published | Deploys backend + frontend to AWS |

**Required secrets:** `ANTHROPIC_API_KEY` (code review), `AWS_ACCESS_KEY_ID` + `AWS_SECRET_ACCESS_KEY` (GitHub config sync), `SNYK_TOKEN`, `SONAR_TOKEN`.

## Versioning

Uses release-please with Conventional Commits. Backend, frontend, and infra versioned independently.
- Config: `release-please-config.json`, `.release-please-manifest.json`
- Pipeline: `.github/workflows/release.yml` creates release PRs, `.github/workflows/deploy.yml` deploys on release publish.

## Important Files

| File | Purpose |
|------|---------|
| `backend/pom.xml` | Maven config with all dependencies and plugins |
| `backend/src/main/resources/application-local.yml` | Local dev config (H2, no Flyway) |
| `backend/src/main/resources/db/migration/V1__initial_schema.sql` | Database schema + seed data |
| `backend/src/main/resources/db/migration/V2__seed_admin_user.sql` | Default admin user seed |
| `frontend/vite.config.ts` | Vite config with API proxy |
| `frontend/src/router/index.ts` | All frontend routes |
| `infra/main.tf` | Terraform module composition |
| `infra/modules/github/` | GitHub repo config (Terraform) |
| `docker/docker-compose.yml` | Local dev with MySQL + LocalStack |
| `sonar-project.properties` | SonarCloud analysis config |
| `release-please-config.json` | Release-please monorepo config |
| `renovate.json` | Renovate dependency update config |
| `.snyk` | Snyk vulnerability policy |
| `.githooks/pre-commit` | Pre-commit lint hook |
| `.githooks/pre-push` | Pre-push validation + Snyk hook |

## Database

- **Local:** H2 in-memory with MySQL compatibility mode. DDL auto-generated (`create-drop`). Flyway disabled.
- **Production:** Aurora Serverless v2 MySQL. Flyway migrations enabled. DDL validation only.
- **Default admin:** V2 migration seeds an admin user (`admin@mindtrack.app`) if none exists.

## Environment Variables (Production)

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` — Aurora connection
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` — OAuth2
- Secrets Manager entries: `claude_api_key`, `telegram_bot_token`, `whatsapp_api_token`
