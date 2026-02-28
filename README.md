# MindTrack

MindTrack is a personal mental health tracking application designed to help you monitor your wellbeing between and during psychiatrist sessions. It provides structured session logging with audio transcription, activity and habit tracking, a free-form journal, goal setting with milestones, and AI-powered coaching conversations through Claude API.

The AI component can proactively reach out via Telegram or WhatsApp to check in on your progress, suggest activities, and offer support based on your recent mood trends and goals.

### Key Features

- **Interview logging** — record psychiatrist sessions with structured notes (mood, topics, medication changes) and audio transcription
- **Activity tracking** — manage therapist-assigned homework, daily habits, and custom activities with daily completion logs and mood ratings
- **Journal** — free-form writing with mood tagging and optional sharing with your therapist
- **Goals & milestones** — set personal goals, break them into milestones, track progress
- **AI chat** — conversational coaching powered by Claude API with context from your recent data
- **Messaging** — spontaneous AI check-ins via Telegram bot and WhatsApp Business API
- **Analytics dashboard** — mood trends, activity completion rates, goal progress charts
- **Therapist view** — read-only access for your therapist to monitor shared data
- **Admin panel** — user management, role-based access control (ADMIN, USER, THERAPIST)

## Documentation

| Guide | Description |
|-------|-------------|
| [Getting Started](docs/getting-started.md) | Prerequisites and local development setup |
| [Architecture](docs/architecture.md) | System diagram, tech stack, module overview |
| [Testing](docs/testing.md) | Running backend, frontend, and infrastructure tests |
| [Deployment](docs/deployment.md) | AWS setup, Terraform, and secrets provisioning |
| [Release & Versioning](docs/release.md) | release-please and Conventional Commits workflow |
| [Environment Variables](docs/environment-variables.md) | All env vars for local dev, CI, and production |
| [Contributing](CONTRIBUTING.md) | Branch naming, commit conventions, PR process |

## Project Structure

```
.
├── backend/                  # Spring Boot application (Java 21, Maven)
│   ├── src/main/java/com/mindtrack/
│   │   ├── auth/             # Google OAuth2, JWT, RBAC
│   │   ├── admin/            # User management panel
│   │   ├── interview/        # Session logging, audio, transcription
│   │   ├── activity/         # Habits, exercises, homework tracking
│   │   ├── journal/          # Free-form journal entries
│   │   ├── goals/            # Goals & milestones
│   │   ├── ai/               # Claude API integration
│   │   ├── messaging/        # Telegram + WhatsApp bots
│   │   ├── analytics/        # Dashboard metrics computation
│   │   └── common/           # Shared entities, config, utils
│   └── src/main/resources/
│       ├── application.yml            # Shared config
│       ├── application-local.yml      # Local dev (H2)
│       ├── application-prod.yml       # Production (Aurora)
│       └── db/migration/             # Flyway SQL migrations
├── frontend/                 # Vue.js 3 SPA (TypeScript, Vite)
│   └── src/
│       ├── views/            # Page components (Login, Dashboard, etc.)
│       ├── stores/           # Pinia state management
│       ├── router/           # Vue Router configuration
│       └── api/              # Axios HTTP client with JWT interceptor
├── infra/                    # Terraform (AWS infrastructure)
│   ├── main.tf              # Module composition
│   ├── providers.tf         # AWS provider + S3 backend
│   ├── modules/              # IAM, S3, RDS, Lambda, API GW, CloudFront, EventBridge, Secrets
│   ├── environments/         # dev.tfvars, prod.tfvars
│   └── tests/                # Unit (validate.sh) + integration (Terratest)
├── docs/                     # Extended documentation
├── docker/                   # Docker Compose + Dockerfiles + nginx
├── config/                   # Checkstyle, Eclipse formatter
├── backlog/                  # Project backlog
├── .github/workflows/        # CI/CD pipelines
├── .githooks/                # Pre-commit (lint) + pre-push (tests)
└── .vscode/                  # VSCode workspace settings
```
