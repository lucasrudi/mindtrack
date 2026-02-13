# MindTrack — Design Document

**Date:** 2026-02-13
**Status:** Approved

## Purpose

Personal mental health tracking application for logging psychiatrist sessions (structured notes + audio transcription), tracking pre/post-session activities and daily habits, monitoring personal growth via dashboards, and AI-powered coaching conversations via in-app chat, Telegram, and WhatsApp.

## Users & Roles

- **ADMIN** — Full access, user management, RBAC configuration
- **USER** — Full access to own data, AI chat, activities
- **THERAPIST** — Read-only view of assigned patient's data (interviews, activities, goals, journal if shared)

Authentication via **Google OAuth2**, session management via **JWT**.

## Architecture: Modular Monolith

Single Spring Boot application with clean module separation, deployed as AWS Lambda with SnapStart.

### Module Structure

```
com.mindtrack
├── interview/       # Session logging, structured notes, transcription
├── activity/        # Habits, exercises, custom activities tracking
├── ai/              # Claude API integration, conversation management
├── messaging/       # Telegram bot + WhatsApp Business API integration
├── analytics/       # Growth dashboards, trends, insights
├── auth/            # Google OAuth2 + JWT, RBAC
├── admin/           # User management panel, roles & permissions
├── journal/         # Free-form journal entries
├── goals/           # Goals, milestones, progress tracking
└── common/          # Shared entities, utils, config
```

## Data Model

### Core Entities

- **User** — id, name, email, role, credentials
- **Role** — id, name (ADMIN/USER/THERAPIST), permissions[]
- **Permission** — id, resource, action (READ/WRITE/DELETE)
- **UserProfile** — id, userId, displayName, avatar, timezone, notificationPrefs, telegramChatId, whatsappNumber

### Domain Entities

- **Interview** — id, date, moodBefore, moodAfter, topics[], medicationChanges, recommendations, notes, transcriptionText, audioS3Key, audioExpiresAt
- **Activity** — id, type (HOMEWORK/HABIT/CUSTOM), name, description, frequency, linkedInterviewId?
- **ActivityLog** — id, activityId, date, completed, notes, moodRating
- **JournalEntry** — id, userId, date, title, content (rich text), mood, tags[], isSharedWithTherapist
- **Goal** — id, userId, title, description, category, targetDate, status (ACTIVE/COMPLETED/ABANDONED)
- **Milestone** — id, goalId, title, targetDate, completedAt, notes

### AI/Messaging Entities

- **Conversation** — id, channel (WEB/TELEGRAM/WHATSAPP), startedAt, messages[]
- **Message** — id, conversationId, role (USER/AI), content, timestamp
- **GrowthMetric** — id, date, category, value (computed daily from activity logs + mood data)

### Audio Handling

- Upload audio to S3 with lifecycle policy (auto-delete after 7 days)
- Trigger transcription via AWS Transcribe on upload
- Store transcription text in DB permanently, audio reference expires

## Tech Stack

### Backend

- **Java 21** with **Spring Boot 3.x**
- **Maven** for dependency management and build
- **Spring Security** with OAuth2 client (Google)
- **Spring Data JPA** with Hibernate
- **Flyway** for database migrations
- **Claude API** (Anthropic SDK for Java) for AI features

### Frontend

- **Vue.js 3** with Composition API
- **npm** for dependency management
- **Vite** as build tool
- **Pinia** for state management
- **Vue Router** for navigation
- **Chart.js** or **Apache ECharts** for dashboard visualizations

### Frontend Pages

- `/login` — Google OAuth2 login
- `/dashboard` — Growth metrics, mood trends, upcoming sessions
- `/interviews` — List/create/view sessions with notes + transcription
- `/activities` — Manage habits, homework, custom activities + daily log
- `/journal` — Free-form writing with mood tagging
- `/goals` — Goals & milestones with progress bars
- `/chat` — AI conversation interface
- `/profile` — User settings, notification prefs, linked accounts
- `/admin` — User management, roles, RBAC config (admin only)
- `/therapist` — Read-only patient overview (therapist only)

## Infrastructure (AWS Serverless via Terraform)

```
CloudFront → S3 (Vue.js SPA)
API Gateway (HTTP API) → Lambda (Spring Boot + SnapStart) → RDS Aurora Serverless v2 MySQL
S3 (audio) → Lambda → AWS Transcribe
EventBridge (schedules) → Lambda → Telegram/WhatsApp
Secrets Manager (API keys, OAuth creds)
```

### Terraform Module Structure

```
infra/
├── main.tf
├── variables.tf
├── outputs.tf
├── environments/
│   ├── dev.tfvars
│   └── prod.tfvars
├── modules/
│   ├── api-gateway/
│   ├── lambda/
│   ├── rds/
│   ├── s3/
│   ├── cloudfront/
│   ├── eventbridge/
│   ├── secrets/
│   └── iam/
└── tests/
    ├── unit/              # terraform validate + tflint + tfsec
    └── integration/       # Terratest (Go)
```

### Local Development

- Spring Boot `local` profile → H2 in-memory DB
- Docker Compose: backend, frontend (hot reload), MySQL 8.0, LocalStack
- `application.yml` → shared config
- `application-local.yml` → H2 datasource
- `application-prod.yml` → RDS via Secrets Manager

## CI/CD (GitHub Actions)

### Feature Branch Pipeline (on push to `feature/*`)

1. Checkout → Setup Java 21 + Node 20
2. Backend: `mvn verify` (compile, test, checkstyle, SpotBugs)
3. Frontend: `npm ci && npm run lint && npm run test:unit`
4. Infra: `terraform fmt -check`, `terraform validate`, `tflint`, `tfsec`
5. SonarQube analysis (via SonarCloud)
6. `terraform plan` (no apply)

### Main Branch Pipeline (on push/merge to `main`)

1. All feature branch steps
2. Terratest integration tests
3. Backend: Build Lambda deployment package
4. Frontend: `npm run build` → upload to S3
5. `terraform apply` (manual approval gate)
6. CloudFront cache invalidation
7. Integration tests against deployed environment

### Git Hooks (pre-push)

1. Backend: `mvn test -q`
2. Frontend: `npm run lint && npm run test:unit`
3. Infra: `terraform fmt -check -recursive && terraform validate && tflint --recursive && tfsec infra/`
4. Blocks push if any step fails

## Code Quality

| Tool | Purpose | Config |
|------|---------|--------|
| Checkstyle | Java code style | `config/checkstyle/checkstyle.xml` |
| SpotBugs | Static analysis | Maven plugin |
| ESLint | Vue.js/JS linting | `.eslintrc.js` |
| Prettier | Code formatting (frontend) | `.prettierrc` |
| SonarQube | Code quality dashboard | `sonar-project.properties` |
| IntelliJ code style | IDE formatting | `.idea/codeStyles/` |
| EditorConfig | Cross-IDE basics | `.editorconfig` |
| tflint | Terraform linting | `.tflint.hcl` |
| tfsec | Terraform security | CI pipeline |

## Testing Strategy

### Backend (JUnit 5 + Mockito + Testcontainers)

- Unit tests for services/business logic
- Integration tests with H2 for repositories
- Controller tests with `@WebMvcTest`
- API tests with `MockMvc`

### Frontend (Vitest + Vue Test Utils + Cypress)

- Unit tests for components and composables (Vitest)
- Component integration tests (Vue Test Utils)
- E2E tests (Cypress — main pipeline only)

### Infrastructure (Terratest + tflint + tfsec)

- Unit: `terraform validate`, `tflint`, `tfsec` — run locally and in CI
- Integration: Terratest deploy/destroy in test AWS account — CI only

## Docker Support

```
docker/
├── Dockerfile.backend      # Multi-stage: Maven build → Eclipse Temurin 21
├── Dockerfile.frontend     # Multi-stage: Node build → nginx
└── docker-compose.yml      # backend, frontend, mysql, localstack
```

## Project File Structure

```
/
├── README.md
├── CONTRIBUTING.md
├── CLAUDE.md
├── .editorconfig
├── .gitignore
├── .githooks/pre-push
├── setup.sh
├── sonar-project.properties
├── backlog/                     # Backlog.md spec compliant
│   ├── config.yml
│   ├── tasks/
│   ├── docs/
│   └── decisions/
├── config/checkstyle/checkstyle.xml
├── .idea/codeStyles/
├── backend/                     # Spring Boot Maven project
├── frontend/                    # Vue.js project
├── infra/                       # Terraform
├── docker/                      # Dockerfiles + compose
└── .github/workflows/
    ├── feature.yml
    └── main.yml
```
