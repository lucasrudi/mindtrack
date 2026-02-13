# MindTrack

Personal mental health tracking application for logging psychiatrist sessions, tracking activities and habits, monitoring personal growth, and AI-powered coaching conversations.

## Architecture

```
                           +------------------+
                           |   CloudFront     |
                           |   (CDN + SPA)    |
                           +--------+---------+
                                    |
                    +---------------+---------------+
                    |                               |
             +------+------+              +---------+---------+
             |  S3 Bucket  |              |   API Gateway     |
             |  (Frontend) |              |   (HTTP API)      |
             +-------------+              +---------+---------+
                                                    |
                                          +---------+---------+
                                          |    AWS Lambda     |
                                          |  (Spring Boot +   |
                                          |   SnapStart)      |
                                          +---------+---------+
                                                    |
                                  +-----------------+-----------------+
                                  |                 |                 |
                          +-------+-------+ +-------+------+ +------+-------+
                          | Aurora        | | S3 (Audio)   | | Secrets      |
                          | Serverless v2 | | 7-day expiry | | Manager      |
                          | (MySQL)       | +-------+------+ +--------------+
                          +---------------+         |
                                            +-------+-------+
                                            | AWS Transcribe|
                                            +---------------+
```

**Modular Monolith** — single Spring Boot application with clean module separation, deployed as AWS Lambda.

## Tech Stack

| Layer          | Technology                                    |
|----------------|-----------------------------------------------|
| Backend        | Java 21, Spring Boot 3.4, Maven               |
| Frontend       | Vue.js 3, TypeScript, Vite, Pinia             |
| Database       | Aurora Serverless v2 (MySQL) / H2 (local)     |
| Infrastructure | Terraform, AWS Lambda, API Gateway, CloudFront |
| AI             | Claude API (Anthropic)                         |
| Messaging      | Telegram Bot API, WhatsApp Business API        |
| CI/CD          | GitHub Actions, SonarCloud                     |
| Code Quality   | Checkstyle, SpotBugs, ESLint, Prettier         |

## Prerequisites

- Java 21 (OpenJDK)
- Maven 3.9+
- Node.js 20+
- npm 10+
- Terraform 1.7+ (for infrastructure)
- Docker & Docker Compose (for local services)

## Quick Start

```bash
# Clone and setup
git clone <repo-url>
cd claude-first-test
./setup.sh

# Start backend (H2 in-memory, no external deps)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Start frontend (in another terminal)
cd frontend
npm run dev
```

Frontend runs at `http://localhost:3000`, backend at `http://localhost:8080`.

## Docker Development

```bash
# Start all services (backend, frontend, MySQL, LocalStack)
cd docker
docker compose up --build
```

## Project Structure

```
.
├── backend/                  # Spring Boot application
│   ├── src/main/java/com/mindtrack/
│   │   ├── auth/             # Google OAuth2, JWT, RBAC
│   │   ├── admin/            # User management panel
│   │   ├── interview/        # Session logging, transcription
│   │   ├── activity/         # Habits, exercises tracking
│   │   ├── journal/          # Free-form entries
│   │   ├── goals/            # Goals & milestones
│   │   ├── ai/               # Claude API integration
│   │   ├── messaging/        # Telegram + WhatsApp bots
│   │   ├── analytics/        # Dashboard metrics
│   │   └── common/           # Shared entities, config
│   └── src/main/resources/
│       └── db/migration/     # Flyway SQL migrations
├── frontend/                 # Vue.js SPA
│   └── src/
│       ├── views/            # Page components
│       ├── stores/           # Pinia state management
│       ├── router/           # Vue Router config
│       └── api/              # Axios HTTP client
├── infra/                    # Terraform infrastructure
│   ├── modules/              # IAM, S3, RDS, Lambda, etc.
│   ├── environments/         # dev.tfvars, prod.tfvars
│   └── tests/                # Unit + integration tests
├── docker/                   # Docker configs
├── config/                   # Checkstyle, code quality
├── backlog/                  # Backlog.md project management
│   ├── tasks/                # Feature task files
│   ├── decisions/            # Architecture decisions
│   └── docs/                 # Project documentation
├── .github/workflows/        # CI/CD pipelines
└── .githooks/                # Pre-push hooks
```

## Testing

```bash
# Backend tests
cd backend && mvn verify

# Frontend tests
cd frontend && npm run test:unit

# Frontend lint
cd frontend && npm run lint

# Terraform validation
cd infra/tests/unit && bash validate.sh
```

## Deployment

Deployment is automated via GitHub Actions on push to `main`:

1. Backend and frontend are built and tested
2. Terraform validates and applies infrastructure changes
3. Frontend is deployed to S3 and CloudFront cache is invalidated

See `.github/workflows/main.yml` for the full pipeline.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines.
