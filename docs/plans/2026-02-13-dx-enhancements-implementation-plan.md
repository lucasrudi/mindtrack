# DX Enhancements Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add VSCode support, pre-commit linting hooks, comprehensive README (local dev, deployment, secrets provisioning), and release versioning with independent component deploys.

**Architecture:** Enhances the existing MindTrack project scaffold (Spring Boot + Vue.js + Terraform) with cross-IDE support, faster feedback loops via pre-commit linting, documentation for onboarding and operations, and a release-please pipeline that versions backend/frontend/infra independently.

**Tech Stack:** VSCode, ESLint, Prettier, Checkstyle, terraform fmt, release-please, GitHub Actions, Conventional Commits.

**Design doc:** `docs/plans/2026-02-13-dx-enhancements-design.md`

---

## Task 1: VSCode Workspace Settings

**Files:**
- Create: `.vscode/settings.json`
- Create: `.vscode/extensions.json`

**Step 1: Create `.vscode/settings.json`**

```json
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.tabSize": 4,
  "editor.insertSpaces": true,
  "files.trimTrailingWhitespace": true,
  "files.insertFinalNewline": true,
  "files.eol": "\n",

  "[java]": {
    "editor.defaultFormatter": "redhat.java",
    "editor.tabSize": 4
  },
  "[typescript]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode",
    "editor.tabSize": 2
  },
  "[vue]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode",
    "editor.tabSize": 2
  },
  "[javascript]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode",
    "editor.tabSize": 2
  },
  "[json]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode",
    "editor.tabSize": 2
  },
  "[yaml]": {
    "editor.tabSize": 2
  },
  "[terraform]": {
    "editor.defaultFormatter": "hashicorp.terraform",
    "editor.tabSize": 2,
    "editor.formatOnSave": true
  },
  "[terraform-vars]": {
    "editor.defaultFormatter": "hashicorp.terraform",
    "editor.tabSize": 2
  },
  "[markdown]": {
    "editor.wordWrap": "on",
    "files.trimTrailingWhitespace": false
  },

  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic",
  "java.format.settings.url": "config/checkstyle/eclipse-formatter.xml",
  "java.checkstyle.configuration": "${workspaceFolder}/config/checkstyle/checkstyle.xml",
  "java.checkstyle.version": "10.12.5",

  "eslint.workingDirectories": ["frontend"],
  "prettier.configPath": "frontend/.prettierrc",

  "terraform.languageServer.enable": true,

  "files.exclude": {
    "**/.DS_Store": true,
    "**/Thumbs.db": true,
    "backend/target": true,
    "frontend/node_modules": true,
    "infra/.terraform": true
  },

  "search.exclude": {
    "backend/target": true,
    "frontend/node_modules": true,
    "frontend/dist": true,
    "infra/.terraform": true
  }
}
```

**Step 2: Create `.vscode/extensions.json`**

```json
{
  "recommendations": [
    "redhat.java",
    "vscjava.vscode-java-pack",
    "vscjava.vscode-spring-boot-dashboard",
    "vmware.vscode-spring-boot",
    "shengchen.vscode-checkstyle",
    "vue.volar",
    "dbaeumer.vscode-eslint",
    "esbenp.prettier-vscode",
    "hashicorp.terraform",
    "editorconfig.editorconfig",
    "eamodio.gitlens",
    "ms-azuretools.vscode-docker"
  ]
}
```

**Step 3: Verify files are valid JSON**

Run: `python3 -c "import json; json.load(open('.vscode/settings.json')); json.load(open('.vscode/extensions.json')); print('OK')"`
Expected: `OK`

---

## Task 2: VSCode Launch & Tasks Configurations

**Files:**
- Create: `.vscode/launch.json`
- Create: `.vscode/tasks.json`

**Step 1: Create `.vscode/launch.json`**

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Spring Boot (local)",
      "request": "launch",
      "mainClass": "com.mindtrack.MindTrackApplication",
      "projectName": "mindtrack",
      "args": "--spring.profiles.active=local",
      "cwd": "${workspaceFolder}/backend",
      "envFile": "${workspaceFolder}/.env.local"
    },
    {
      "type": "java",
      "name": "Spring Boot (debug)",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005
    },
    {
      "type": "chrome",
      "name": "Vue Dev (Chrome)",
      "request": "launch",
      "url": "http://localhost:3000",
      "webRoot": "${workspaceFolder}/frontend/src"
    }
  ]
}
```

**Step 2: Create `.vscode/tasks.json`**

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Backend: Compile",
      "type": "shell",
      "command": "mvn compile -q",
      "options": { "cwd": "${workspaceFolder}/backend" },
      "group": "build",
      "problemMatcher": "$javac"
    },
    {
      "label": "Backend: Test",
      "type": "shell",
      "command": "mvn verify -B",
      "options": { "cwd": "${workspaceFolder}/backend" },
      "group": "test",
      "problemMatcher": "$javac"
    },
    {
      "label": "Backend: Checkstyle",
      "type": "shell",
      "command": "mvn checkstyle:check -q",
      "options": { "cwd": "${workspaceFolder}/backend" },
      "group": "test",
      "problemMatcher": []
    },
    {
      "label": "Frontend: Dev Server",
      "type": "shell",
      "command": "npm run dev",
      "options": { "cwd": "${workspaceFolder}/frontend" },
      "isBackground": true,
      "problemMatcher": []
    },
    {
      "label": "Frontend: Lint",
      "type": "shell",
      "command": "npm run lint",
      "options": { "cwd": "${workspaceFolder}/frontend" },
      "group": "test",
      "problemMatcher": ["$eslint-stylish"]
    },
    {
      "label": "Frontend: Test",
      "type": "shell",
      "command": "npm run test:unit -- --run",
      "options": { "cwd": "${workspaceFolder}/frontend" },
      "group": "test",
      "problemMatcher": []
    },
    {
      "label": "Frontend: Build",
      "type": "shell",
      "command": "npm run build",
      "options": { "cwd": "${workspaceFolder}/frontend" },
      "group": "build",
      "problemMatcher": []
    },
    {
      "label": "Infra: Validate",
      "type": "shell",
      "command": "terraform fmt -check -recursive && terraform init -backend=false && terraform validate",
      "options": { "cwd": "${workspaceFolder}/infra" },
      "group": "test",
      "problemMatcher": []
    },
    {
      "label": "Docker: Up",
      "type": "shell",
      "command": "docker compose up --build -d",
      "options": { "cwd": "${workspaceFolder}/docker" },
      "problemMatcher": []
    },
    {
      "label": "Docker: Down",
      "type": "shell",
      "command": "docker compose down",
      "options": { "cwd": "${workspaceFolder}/docker" },
      "problemMatcher": []
    }
  ]
}
```

**Step 3: Verify JSON**

Run: `python3 -c "import json; json.load(open('.vscode/launch.json')); json.load(open('.vscode/tasks.json')); print('OK')"`
Expected: `OK`

---

## Task 3: Eclipse Formatter for VSCode Java

**Files:**
- Create: `config/checkstyle/eclipse-formatter.xml`

The VSCode Java extension uses Eclipse formatter. Create a formatter profile that matches our Checkstyle conventions (4-space indent, 120 char width, braces on same line).

**Step 1: Create `config/checkstyle/eclipse-formatter.xml`**

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<profiles version="21">
    <profile kind="CodeFormatterProfile" name="MindTrack" version="21">
        <setting id="org.eclipse.jdt.core.formatter.tabulation.char" value="space"/>
        <setting id="org.eclipse.jdt.core.formatter.tabulation.size" value="4"/>
        <setting id="org.eclipse.jdt.core.formatter.indentation.size" value="4"/>
        <setting id="org.eclipse.jdt.core.formatter.lineSplit" value="120"/>
        <setting id="org.eclipse.jdt.core.formatter.comment.line_length" value="120"/>
        <setting id="org.eclipse.jdt.core.formatter.continuation_indentation" value="2"/>
        <setting id="org.eclipse.jdt.core.formatter.continuation_indentation_for_array_initializer" value="1"/>
        <setting id="org.eclipse.jdt.core.formatter.brace_position_for_type_declaration" value="end_of_line"/>
        <setting id="org.eclipse.jdt.core.formatter.brace_position_for_method_declaration" value="end_of_line"/>
        <setting id="org.eclipse.jdt.core.formatter.brace_position_for_constructor_declaration" value="end_of_line"/>
        <setting id="org.eclipse.jdt.core.formatter.brace_position_for_block" value="end_of_line"/>
        <setting id="org.eclipse.jdt.core.formatter.brace_position_for_switch" value="end_of_line"/>
        <setting id="org.eclipse.jdt.core.formatter.brace_position_for_enum_declaration" value="end_of_line"/>
        <setting id="org.eclipse.jdt.core.formatter.brace_position_for_enum_constant" value="end_of_line"/>
        <setting id="org.eclipse.jdt.core.formatter.brace_position_for_annotation_type_declaration" value="end_of_line"/>
        <setting id="org.eclipse.jdt.core.formatter.brace_position_for_record_declaration" value="end_of_line"/>
        <setting id="org.eclipse.jdt.core.formatter.brace_position_for_record_constructor" value="end_of_line"/>
        <setting id="org.eclipse.jdt.core.formatter.brace_position_for_lambda_body" value="end_of_line"/>
        <setting id="org.eclipse.jdt.core.formatter.brace_position_for_array_initializer" value="end_of_line"/>
        <setting id="org.eclipse.jdt.core.formatter.insert_new_line_before_else_in_if_statement" value="do not insert"/>
        <setting id="org.eclipse.jdt.core.formatter.insert_new_line_before_catch_in_try_statement" value="do not insert"/>
        <setting id="org.eclipse.jdt.core.formatter.insert_new_line_before_finally_in_try_statement" value="do not insert"/>
        <setting id="org.eclipse.jdt.core.formatter.insert_new_line_before_while_in_do_statement" value="do not insert"/>
        <setting id="org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_type_declaration" value="insert"/>
        <setting id="org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_method_declaration" value="insert"/>
        <setting id="org.eclipse.jdt.core.formatter.insert_space_before_opening_brace_in_block" value="insert"/>
        <setting id="org.eclipse.jdt.core.formatter.blank_lines_before_package" value="0"/>
        <setting id="org.eclipse.jdt.core.formatter.blank_lines_after_package" value="1"/>
        <setting id="org.eclipse.jdt.core.formatter.blank_lines_before_imports" value="1"/>
        <setting id="org.eclipse.jdt.core.formatter.blank_lines_after_imports" value="1"/>
        <setting id="org.eclipse.jdt.core.formatter.blank_lines_before_member_type" value="1"/>
        <setting id="org.eclipse.jdt.core.formatter.blank_lines_before_field" value="0"/>
        <setting id="org.eclipse.jdt.core.formatter.blank_lines_before_method" value="1"/>
    </profile>
</profiles>
```

---

## Task 4: Update .gitignore for VSCode

**Files:**
- Modify: `.gitignore`

**Step 1: Add VSCode tracking rules to `.gitignore`**

Append after the IntelliJ section:

```gitignore
# VSCode (keep shared configs)
.vscode/*
!.vscode/settings.json
!.vscode/extensions.json
!.vscode/launch.json
!.vscode/tasks.json
```

**Step 2: Verify .gitignore works**

Run: `git status`
Expected: `.vscode/` files show as untracked (ready to add)

**Step 3: Commit Tasks 1-4**

```bash
git add .vscode/ config/checkstyle/eclipse-formatter.xml .gitignore
git commit -m "chore: add VSCode workspace configuration and Java formatter"
```

---

## Task 5: Pre-commit Hook

**Files:**
- Create: `.githooks/pre-commit`
- Modify: `setup.sh`

**Step 1: Create `.githooks/pre-commit`**

```bash
#!/usr/bin/env bash
set -e

echo "=== Pre-commit checks ==="

# Determine repo root
REPO_ROOT="$(git rev-parse --show-toplevel)"

# Get list of staged files
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACM)

# --- Backend lint (Checkstyle) ---
STAGED_JAVA=$(echo "$STAGED_FILES" | grep '\.java$' || true)
if [ -n "$STAGED_JAVA" ]; then
    echo ">> Checking Java code style..."
    cd "$REPO_ROOT/backend"
    mvn checkstyle:check -q 2>&1 || {
        echo "!! Checkstyle violations found. Fix them before committing."
        exit 1
    }
    cd "$REPO_ROOT"
    echo ">> Java code style OK."
fi

# --- Frontend lint (ESLint + Prettier) ---
STAGED_FRONTEND=$(echo "$STAGED_FILES" | grep '^frontend/src/.*\.\(ts\|vue\|js\|tsx\)$' || true)
if [ -n "$STAGED_FRONTEND" ]; then
    echo ">> Checking frontend code style..."
    cd "$REPO_ROOT/frontend"
    npx eslint . --max-warnings 0 2>&1 || {
        echo "!! ESLint errors found. Fix them before committing."
        exit 1
    }
    npx prettier --check "src/**/*.{ts,vue,js,tsx,css,scss}" 2>&1 || {
        echo "!! Prettier formatting issues found. Run 'npm run format' to fix."
        exit 1
    }
    cd "$REPO_ROOT"
    echo ">> Frontend code style OK."
fi

# --- Infra lint (terraform fmt) ---
STAGED_TF=$(echo "$STAGED_FILES" | grep '^infra/.*\.tf$' || true)
if [ -n "$STAGED_TF" ]; then
    echo ">> Checking Terraform formatting..."
    terraform -chdir="$REPO_ROOT/infra" fmt -check -recursive 2>&1 || {
        echo "!! Terraform formatting issues found. Run 'terraform fmt -recursive' in infra/."
        exit 1
    }
    echo ">> Terraform formatting OK."
fi

echo "=== Pre-commit checks passed ==="
```

**Step 2: Make it executable**

Run: `chmod +x .githooks/pre-commit`

**Step 3: Update `setup.sh`** to mention both hooks

Replace the git hooks section. The existing `chmod +x .githooks/*` already covers both files, so no code change needed for setup.sh. But we should add a message about the pre-commit hook.

In `setup.sh`, after line 8 (`echo ">> Git hooks configured."`), the existing `chmod +x .githooks/*` already handles all hooks. No change needed.

**Step 4: Verify pre-commit hook works**

Run: `bash -n .githooks/pre-commit` (syntax check)
Expected: No output (valid bash)

**Step 5: Commit**

```bash
git add .githooks/pre-commit
git commit -m "chore: add pre-commit hook for fast lint checks (Checkstyle, ESLint, terraform fmt)"
```

---

## Task 6: Expanded README — Project Description & Prerequisites

**Files:**
- Modify: `README.md` (full rewrite)

**Step 1: Rewrite README.md**

Replace the entire file with:

````markdown
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
````

---

## Task 7: Release-Please Configuration

**Files:**
- Create: `release-please-config.json`
- Create: `.release-please-manifest.json`
- Create: `infra/version.txt`

**Step 1: Create `release-please-config.json`**

```json
{
  "$schema": "https://raw.githubusercontent.com/googleapis/release-please/main/schemas/config.json",
  "packages": {
    "backend": {
      "release-type": "maven",
      "component": "backend",
      "extra-files": []
    },
    "frontend": {
      "release-type": "node",
      "component": "frontend",
      "extra-files": []
    },
    "infra": {
      "release-type": "simple",
      "component": "infra",
      "extra-files": []
    }
  },
  "separate-pull-requests": false,
  "group-pull-request-title-pattern": "chore: release ${version}",
  "changelog-sections": [
    { "type": "feat", "section": "Features" },
    { "type": "fix", "section": "Bug Fixes" },
    { "type": "chore", "section": "Miscellaneous" },
    { "type": "docs", "section": "Documentation" },
    { "type": "test", "section": "Tests" },
    { "type": "ci", "section": "CI/CD" },
    { "type": "infra", "section": "Infrastructure" },
    { "type": "refactor", "section": "Refactoring" }
  ]
}
```

**Step 2: Create `.release-please-manifest.json`**

```json
{
  "backend": "0.1.0",
  "frontend": "0.1.0",
  "infra": "0.1.0"
}
```

**Step 3: Create `infra/version.txt`**

```
0.1.0
```

**Step 4: Commit**

```bash
git add release-please-config.json .release-please-manifest.json infra/version.txt
git commit -m "chore: add release-please configuration for monorepo versioning"
```

---

## Task 8: Release Pipeline (`release.yml`)

**Files:**
- Create: `.github/workflows/release.yml`

**Step 1: Create `.github/workflows/release.yml`**

```yaml
name: Release

on:
  push:
    branches:
      - main

permissions:
  contents: write
  pull-requests: write

jobs:
  release-please:
    name: Release Please
    runs-on: ubuntu-latest
    outputs:
      releases_created: ${{ steps.release.outputs.releases_created }}
      backend--release_created: ${{ steps.release.outputs['backend--release_created'] }}
      frontend--release_created: ${{ steps.release.outputs['frontend--release_created'] }}
      infra--release_created: ${{ steps.release.outputs['infra--release_created'] }}
      backend--tag_name: ${{ steps.release.outputs['backend--tag_name'] }}
      frontend--tag_name: ${{ steps.release.outputs['frontend--tag_name'] }}
      infra--tag_name: ${{ steps.release.outputs['infra--tag_name'] }}
    steps:
      - uses: googleapis/release-please-action@v4
        id: release
        with:
          token: ${{ secrets.GITHUB_TOKEN }}
```

---

## Task 9: Deploy Pipeline (`deploy.yml`)

**Files:**
- Create: `.github/workflows/deploy.yml`

**Step 1: Create `.github/workflows/deploy.yml`**

```yaml
name: Deploy

on:
  release:
    types: [published]

permissions:
  contents: read
  id-token: write

jobs:
  detect-component:
    name: Detect Component
    runs-on: ubuntu-latest
    outputs:
      component: ${{ steps.detect.outputs.component }}
    steps:
      - name: Detect component from release tag
        id: detect
        run: |
          TAG="${{ github.event.release.tag_name }}"
          if [[ "$TAG" == backend-* ]]; then
            echo "component=backend" >> "$GITHUB_OUTPUT"
          elif [[ "$TAG" == frontend-* ]]; then
            echo "component=frontend" >> "$GITHUB_OUTPUT"
          elif [[ "$TAG" == infra-* ]]; then
            echo "component=infra" >> "$GITHUB_OUTPUT"
          else
            echo "component=unknown" >> "$GITHUB_OUTPUT"
          fi

  deploy-backend:
    name: Deploy Backend
    runs-on: ubuntu-latest
    needs: detect-component
    if: needs.detect-component.outputs.component == 'backend'
    environment: production
    steps:
      - uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Build & Test
        working-directory: backend
        run: mvn verify -B

      - name: Package
        working-directory: backend
        run: mvn package -DskipTests -q

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1

      - name: Deploy to Lambda
        run: |
          aws lambda update-function-code \
            --function-name mindtrack-prod-api \
            --zip-file fileb://backend/target/mindtrack-*.jar

  deploy-frontend:
    name: Deploy Frontend
    runs-on: ubuntu-latest
    needs: detect-component
    if: needs.detect-component.outputs.component == 'frontend'
    environment: production
    steps:
      - uses: actions/checkout@v4

      - name: Set up Node 20
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Install dependencies
        working-directory: frontend
        run: npm ci

      - name: Lint
        working-directory: frontend
        run: npm run lint

      - name: Unit tests
        working-directory: frontend
        run: npm run test:unit -- --run

      - name: Build
        working-directory: frontend
        run: npm run build

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1

      - name: Deploy to S3
        run: aws s3 sync frontend/dist/ s3://${{ vars.FRONTEND_BUCKET }}/ --delete

      - name: Invalidate CloudFront
        run: |
          aws cloudfront create-invalidation \
            --distribution-id ${{ vars.CLOUDFRONT_DISTRIBUTION_ID }} \
            --paths "/*"

  deploy-infra:
    name: Deploy Infrastructure
    runs-on: ubuntu-latest
    needs: detect-component
    if: needs.detect-component.outputs.component == 'infra'
    environment: production
    steps:
      - uses: actions/checkout@v4

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1

      - name: Setup Terraform
        uses: hashicorp/setup-terraform@v3
        with:
          terraform_version: '1.7.0'

      - name: Terraform Init
        working-directory: infra
        run: terraform init

      - name: Terraform Plan
        working-directory: infra
        run: terraform plan -var-file=environments/prod.tfvars -out=tfplan

      - name: Terraform Apply
        working-directory: infra
        run: terraform apply tfplan
```

---

## Task 10: Remove Old Main Pipeline + Update Feature Pipeline

**Files:**
- Delete: `.github/workflows/main.yml`
- Modify: `.github/workflows/feature.yml`

**Step 1: Delete `.github/workflows/main.yml`**

This is replaced by `release.yml` + `deploy.yml`.

**Step 2: Update `.github/workflows/feature.yml`**

Add path filters so jobs only run when their files change. Also add `bugfix/**` and `chore/**` branch patterns.

Replace the entire file:

```yaml
name: Feature Branch CI

on:
  push:
    branches:
      - 'feature/**'
      - 'bugfix/**'
      - 'chore/**'
  pull_request:
    branches:
      - main

jobs:
  backend:
    name: Backend Build & Test
    runs-on: ubuntu-latest
    if: >
      github.event_name == 'pull_request' ||
      contains(github.event.head_commit.modified, 'backend/') ||
      contains(github.event.head_commit.added, 'backend/')
    steps:
      - uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Build & Test
        working-directory: backend
        run: mvn verify -B

      - name: Upload coverage
        uses: actions/upload-artifact@v4
        with:
          name: backend-coverage
          path: backend/target/site/jacoco/

  frontend:
    name: Frontend Build & Test
    runs-on: ubuntu-latest
    if: >
      github.event_name == 'pull_request' ||
      contains(github.event.head_commit.modified, 'frontend/') ||
      contains(github.event.head_commit.added, 'frontend/')
    steps:
      - uses: actions/checkout@v4

      - name: Set up Node 20
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Install dependencies
        working-directory: frontend
        run: npm ci

      - name: Lint
        working-directory: frontend
        run: npm run lint

      - name: Unit tests
        working-directory: frontend
        run: npm run test:unit -- --run --coverage

      - name: Build
        working-directory: frontend
        run: npm run build

      - name: Upload coverage
        uses: actions/upload-artifact@v4
        with:
          name: frontend-coverage
          path: frontend/coverage/

  infra:
    name: Terraform Validate
    runs-on: ubuntu-latest
    if: >
      github.event_name == 'pull_request' ||
      contains(github.event.head_commit.modified, 'infra/') ||
      contains(github.event.head_commit.added, 'infra/')
    steps:
      - uses: actions/checkout@v4

      - name: Setup Terraform
        uses: hashicorp/setup-terraform@v3
        with:
          terraform_version: '1.7.0'

      - name: Terraform Format Check
        working-directory: infra
        run: terraform fmt -check -recursive

      - name: Terraform Init
        working-directory: infra
        run: terraform init -backend=false

      - name: Terraform Validate
        working-directory: infra
        run: terraform validate

      - name: Setup tflint
        uses: terraform-linters/setup-tflint@v4

      - name: Run tflint
        working-directory: infra
        run: tflint --recursive

      - name: Run tfsec
        uses: aquasecurity/tfsec-action@v1.0.3
        with:
          working_directory: infra

  sonar:
    name: SonarCloud Analysis
    runs-on: ubuntu-latest
    needs: [backend, frontend]
    if: always() && (needs.backend.result == 'success' || needs.frontend.result == 'success')
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Download backend coverage
        uses: actions/download-artifact@v4
        if: needs.backend.result == 'success'
        with:
          name: backend-coverage
          path: backend/target/site/jacoco/

      - name: Download frontend coverage
        uses: actions/download-artifact@v4
        if: needs.frontend.result == 'success'
        with:
          name: frontend-coverage
          path: frontend/coverage/

      - name: SonarCloud Scan
        uses: SonarSource/sonarcloud-github-action@v2
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

**Step 3: Commit**

```bash
git rm .github/workflows/main.yml
git add .github/workflows/feature.yml .github/workflows/release.yml .github/workflows/deploy.yml
git commit -m "ci: replace monolithic deploy with release-please + independent component deploys"
```

---

## Task 11: Update CLAUDE.md and CONTRIBUTING.md

**Files:**
- Modify: `CLAUDE.md`
- Modify: `CONTRIBUTING.md`

**Step 1: Update CLAUDE.md**

Add to the "Running Locally" section that both VSCode and IntelliJ are supported. Add note about pre-commit hooks. Add versioning section.

Append before the "Important Files" section:

```markdown
## Git Hooks

- **Pre-commit** (`.githooks/pre-commit`): Runs Checkstyle (Java), ESLint + Prettier (frontend), terraform fmt (infra) on staged files. Fast, lint-only.
- **Pre-push** (`.githooks/pre-push`): Runs full test suites (mvn test, npm test:unit, tflint, tfsec). Slower but comprehensive.
- Setup: `git config core.hooksPath .githooks` (done by `./setup.sh`)

## IDE Support

Both VSCode (`.vscode/`) and IntelliJ (`.idea/codeStyles/`) are configured with matching conventions.

## Versioning

Uses release-please with Conventional Commits. Backend, frontend, and infra versioned independently.
- Config: `release-please-config.json`, `.release-please-manifest.json`
- Pipeline: `.github/workflows/release.yml` creates release PRs, `.github/workflows/deploy.yml` deploys on release publish.
```

**Step 2: Update CONTRIBUTING.md**

Add pre-commit hook section and note about conventional commits driving releases.

Append after the "Pre-Push Hooks" section:

```markdown
## Pre-Commit Hooks

The pre-commit hook (`.githooks/pre-commit`) runs fast lint checks on every commit:
- **Java:** Checkstyle (only if `.java` files staged)
- **Frontend:** ESLint + Prettier (only if `frontend/src/` files staged)
- **Terraform:** `terraform fmt -check` (only if `.tf` files staged)

These are much faster than the pre-push checks and catch formatting issues early.

## Releases

Releases are automated via [release-please](https://github.com/googleapis/release-please). Your commit messages directly drive versioning:

| Commit prefix | Version bump | Example |
|---------------|-------------|---------|
| `feat:` | minor (0.1.0 → 0.2.0) | `feat: add interview audio upload` |
| `fix:` | patch (0.1.0 → 0.1.1) | `fix: resolve JWT expiry bug` |
| `feat!:` | major (0.1.0 → 1.0.0) | `feat!: redesign auth flow` |
| `chore:`, `docs:`, etc. | no release | `chore: update dependencies` |

Backend, frontend, and infra are versioned independently based on which files changed.
```

**Step 3: Commit**

```bash
git add CLAUDE.md CONTRIBUTING.md
git commit -m "docs: update CLAUDE.md and CONTRIBUTING.md with hooks, IDE, and versioning info"
```

---

## Summary of All Tasks

| # | Task | Files |
|---|------|-------|
| 1 | VSCode settings + extensions | `.vscode/settings.json`, `.vscode/extensions.json` |
| 2 | VSCode launch + tasks | `.vscode/launch.json`, `.vscode/tasks.json` |
| 3 | Eclipse Java formatter | `config/checkstyle/eclipse-formatter.xml` |
| 4 | Update .gitignore for VSCode | `.gitignore` |
| 5 | Pre-commit hook | `.githooks/pre-commit` |
| 6 | Expanded README | `README.md` (full rewrite) |
| 7 | Release-please config | `release-please-config.json`, `.release-please-manifest.json`, `infra/version.txt` |
| 8 | Release pipeline | `.github/workflows/release.yml` |
| 9 | Deploy pipeline | `.github/workflows/deploy.yml` |
| 10 | Remove old main.yml + update feature.yml | `.github/workflows/main.yml` (delete), `.github/workflows/feature.yml` (update) |
| 11 | Update CLAUDE.md + CONTRIBUTING.md | `CLAUDE.md`, `CONTRIBUTING.md` |
