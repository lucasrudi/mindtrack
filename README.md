# MindTrack

MindTrack is a personal mental health tracking application designed to help you monitor your wellbeing between and during psychiatrist sessions. It provides structured session logging with audio transcription, activity and habit tracking, a free-form journal, goal setting with milestones, and AI-powered coaching conversations through Claude API.

The AI component can proactively reach out via Telegram or WhatsApp to check in on your progress, suggest activities, and offer support based on your recent mood trends and goals.

### Key Features

- **Interview logging** — record psychiatrist sessions with structured notes (mood, topics, medication changes) and audio transcription (audio auto-deleted after 7 days, transcription kept permanently)
- **Activity tracking** — manage therapist-assigned homework, daily habits, and custom activities with daily completion logs and mood ratings
- **Journal** — free-form writing with mood tagging and optional sharing with your therapist
- **Goals & milestones** — set personal goals, break them into milestones, track progress
- **AI chat** — conversational coaching powered by Claude API with context from your recent data
- **Messaging** — spontaneous AI check-ins via Telegram bot and WhatsApp Business API
- **Analytics dashboard** — mood trends, activity completion rates, goal progress charts
- **Therapist view** — read-only access for your therapist to monitor shared data
- **Admin panel** — user management, role-based access control (ADMIN, USER, THERAPIST)

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

**Modular Monolith** — single Spring Boot application with clean module separation (auth, admin, interview, activity, journal, goals, ai, messaging, analytics, common), deployed as AWS Lambda with SnapStart.

## Tech Stack

| Layer          | Technology                                       |
|----------------|--------------------------------------------------|
| Backend        | Java 21, Spring Boot 3.4, Maven                  |
| Frontend       | Vue.js 3 (Composition API), TypeScript, Vite     |
| State          | Pinia (frontend), Spring Data JPA (backend)      |
| Database       | Aurora Serverless v2 MySQL (prod) / H2 (local)   |
| Infrastructure | Terraform, AWS Lambda, API Gateway, CloudFront   |
| AI             | Claude API (Anthropic)                            |
| Messaging      | Telegram Bot API, WhatsApp Business API           |
| CI/CD          | GitHub Actions, release-please, SonarCloud        |
| Code Quality   | Checkstyle, SpotBugs, ESLint, Prettier, tflint   |

## Prerequisites

### Required

| Tool | Version | Install |
|------|---------|---------|
| Java (OpenJDK) | 21+ | `brew install openjdk@21` (macOS) / `sudo apt install openjdk-21-jdk` (Ubuntu) |
| Maven | 3.9+ | `brew install maven` (macOS) / `sudo apt install maven` (Ubuntu) |
| Node.js | 20+ | `brew install node@20` (macOS) / via [nvm](https://github.com/nvm-sh/nvm) |
| npm | 10+ | Comes with Node.js |
| Git | 2.40+ | `brew install git` (macOS) / `sudo apt install git` (Ubuntu) |

### Optional (for infrastructure & Docker)

| Tool | Version | Install |
|------|---------|---------|
| Terraform | 1.7+ | `brew install terraform` (macOS) / [tfenv](https://github.com/tfutils/tfenv) |
| Docker | 24+ | [Docker Desktop](https://www.docker.com/products/docker-desktop/) |
| AWS CLI | 2.x | `brew install awscli` (macOS) / [AWS docs](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) |
| tflint | 0.50+ | `brew install tflint` |
| tfsec | latest | `brew install tfsec` |
| Snyk CLI | latest | `npm install -g snyk` / `brew install snyk` |

## Local Development

### Option A: Minimal (no external dependencies)

Uses H2 in-memory database. No Docker, no AWS, no external services needed.

```bash
# 1. Clone and setup
git clone <repo-url>
cd claude-first-test
./setup.sh      # installs deps, configures git hooks

# 2. Start backend (terminal 1)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 3. Start frontend (terminal 2)
cd frontend
npm run dev
```

- Backend: http://localhost:8080
- Frontend: http://localhost:3000 (proxies `/api` to backend)
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:mindtrack`)

### Option B: Full local stack (Docker Compose)

Uses MySQL, LocalStack (S3 + Secrets Manager). Closer to production.

```bash
# 1. Clone and setup
git clone <repo-url>
cd claude-first-test
./setup.sh

# 2. Start all services
cd docker
docker compose up --build

# 3. (Optional) Access services
#    Backend:    http://localhost:8080
#    Frontend:   http://localhost:3000
#    MySQL:      localhost:3306 (user: mindtrack, pass: mindtrack)
#    LocalStack: http://localhost:4566
```

To stop: `docker compose down` (add `-v` to also remove MySQL data volume).

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
├── docker/                   # Docker Compose + Dockerfiles + nginx
├── config/                   # Checkstyle, Eclipse formatter
├── backlog/                  # Backlog.md project management
├── .github/workflows/        # CI/CD pipelines
├── .githooks/                # Pre-commit (lint) + pre-push (tests)
└── .vscode/                  # VSCode workspace settings
```

## Testing

```bash
# Backend — compile, checkstyle, spotbugs, unit tests, jacoco coverage
cd backend && mvn verify

# Frontend — ESLint + Prettier
cd frontend && npm run lint

# Frontend — Vitest unit tests
cd frontend && npm run test:unit

# Infrastructure — format, validate, tflint, tfsec
bash infra/tests/unit/validate.sh
```

## Deployment

### First-Time AWS Setup

1. **Create Terraform state resources** (one-time, manual):

   ```bash
   # Create S3 bucket for Terraform state
   aws s3api create-bucket \
     --bucket mindtrack-terraform-state \
     --region us-east-1

   aws s3api put-bucket-versioning \
     --bucket mindtrack-terraform-state \
     --versioning-configuration Status=Enabled

   aws s3api put-bucket-encryption \
     --bucket mindtrack-terraform-state \
     --server-side-encryption-configuration \
       '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'

   # Create DynamoDB table for state locking
   aws dynamodb create-table \
     --table-name mindtrack-terraform-locks \
     --attribute-definitions AttributeName=LockID,AttributeType=S \
     --key-schema AttributeName=LockID,KeyType=HASH \
     --billing-mode PAY_PER_REQUEST \
     --region us-east-1
   ```

2. **Initialize Terraform**:

   ```bash
   cd infra
   terraform init
   terraform plan -var-file=environments/dev.tfvars
   terraform apply -var-file=environments/dev.tfvars
   ```

3. **Populate secrets** (see Secrets Provisioning below)

4. **Deploy frontend**:

   ```bash
   cd frontend && npm run build
   aws s3 sync dist/ s3://mindtrack-dev-frontend/ --delete
   ```

### Automated Deploys

After first-time setup, all deployments are automated via GitHub Actions:

- Push to `main` → release-please creates a release PR
- Merge the release PR → GitHub Release published → deploy pipeline runs
- Only changed components are deployed (backend, frontend, or infra)

See `.github/workflows/release.yml` and `.github/workflows/deploy.yml`.

## Secrets Provisioning

### AWS

1. **Create a CI/CD IAM user** (for GitHub Actions):

   ```bash
   aws iam create-user --user-name mindtrack-ci
   aws iam attach-user-policy --user-name mindtrack-ci \
     --policy-arn arn:aws:iam::aws:policy/AdministratorAccess  # scope down in production
   aws iam create-access-key --user-name mindtrack-ci
   ```

   Save the `AccessKeyId` and `SecretAccessKey` for GitHub secrets.

2. **Recommended: Use OIDC instead of access keys** for GitHub Actions:
   - Create an IAM Identity Provider for `token.actions.githubusercontent.com`
   - Create an IAM role with trust policy for your GitHub repo
   - Reference in workflows with `aws-actions/configure-aws-credentials@v4` using `role-to-assume`

### Google OAuth2

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or select existing)
3. Navigate to **APIs & Services** > **OAuth consent screen**
   - User type: External
   - App name: MindTrack
   - Authorized domains: your domain
4. Navigate to **APIs & Services** > **Credentials**
   - Click **Create Credentials** > **OAuth client ID**
   - Application type: Web application
   - Authorized redirect URIs: `https://your-domain.com/login/oauth2/code/google` and `http://localhost:8080/login/oauth2/code/google` (for local dev)
5. Copy `Client ID` and `Client Secret`
6. Store in AWS Secrets Manager:

   ```bash
   aws secretsmanager put-secret-value \
     --secret-id mindtrack-dev/google_oauth_client_id \
     --secret-string "your-client-id"
   aws secretsmanager put-secret-value \
     --secret-id mindtrack-dev/google_oauth_client_secret \
     --secret-string "your-client-secret"
   ```

### Claude API (Anthropic)

1. Go to [Anthropic Console](https://console.anthropic.com/)
2. Navigate to **API Keys**
3. Create a new API key
4. Store in AWS Secrets Manager:

   ```bash
   aws secretsmanager put-secret-value \
     --secret-id mindtrack-dev/claude_api_key \
     --secret-string "sk-ant-..."
   ```

### Telegram Bot

1. Open Telegram and message [@BotFather](https://t.me/BotFather)
2. Send `/newbot`, follow prompts to name your bot
3. Copy the bot token (format: `123456789:ABCdefGhIjKlMnOpQrStUvWxYz`)
4. Store in AWS Secrets Manager:

   ```bash
   aws secretsmanager put-secret-value \
     --secret-id mindtrack-dev/telegram_bot_token \
     --secret-string "123456789:ABCdef..."
   ```

5. Set up webhook (after backend is deployed):

   ```bash
   curl "https://api.telegram.org/bot<TOKEN>/setWebhook?url=https://your-api-domain.com/api/messaging/telegram/webhook"
   ```

### WhatsApp Business API

1. Go to [Meta Business Suite](https://business.facebook.com/)
2. Create or select a Business Account
3. Navigate to **WhatsApp** > **Getting Started**
4. Create a WhatsApp Business App in [Meta Developers](https://developers.facebook.com/)
5. Navigate to **WhatsApp** > **API Setup**
6. Copy the permanent access token
7. Store in AWS Secrets Manager:

   ```bash
   aws secretsmanager put-secret-value \
     --secret-id mindtrack-dev/whatsapp_api_token \
     --secret-string "EAAx..."
   ```

8. Configure webhook URL in Meta Developer Dashboard pointing to `https://your-api-domain.com/api/messaging/whatsapp/webhook`

### GitHub Repository Secrets

Configure these in your repo's **Settings** > **Secrets and variables** > **Actions**:

| Secret | Source | Purpose |
|--------|--------|---------|
| `AWS_ACCESS_KEY_ID` | AWS IAM (or use OIDC) | CI/CD AWS access |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM (or use OIDC) | CI/CD AWS access |
| `SONAR_TOKEN` | [SonarCloud](https://sonarcloud.io/) > My Account > Security | Code quality analysis |

Configure these as **Variables** (not secrets):

| Variable | Example | Purpose |
|----------|---------|---------|
| `FRONTEND_BUCKET` | `mindtrack-prod-frontend` | S3 bucket for frontend deploy |
| `CLOUDFRONT_DISTRIBUTION_ID` | `E1234ABCDE` | CloudFront invalidation |

## Release & Versioning

This project uses [release-please](https://github.com/googleapis/release-please) with [Conventional Commits](https://www.conventionalcommits.org/) for automatic versioning.

- `feat:` commits → minor version bump (0.1.0 → 0.2.0)
- `fix:` commits → patch version bump (0.1.0 → 0.1.1)
- `feat!:` or `BREAKING CHANGE:` → major version bump (0.1.0 → 1.0.0)

Backend, frontend, and infrastructure are versioned independently. When you push to `main`, release-please creates a release PR. Merging it publishes GitHub Releases and triggers deploys for changed components only.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for branch naming, commit conventions, code style, and PR process.
