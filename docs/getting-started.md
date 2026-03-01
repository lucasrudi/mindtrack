# Getting Started

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
GOOGLE_CLIENT_ID=your-id GOOGLE_CLIENT_SECRET=your-secret \
  mvn spring-boot:run -Dspring-boot.run.profiles=local -f backend/pom.xml

# 3. Start frontend (terminal 2)
cd frontend
npm run dev
```

- Backend: http://localhost:8080
- Frontend: http://localhost:3000 (proxies `/api` to backend)
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:mindtrack`)

> Without `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET`, the app starts but Google login will fail with "client not found".

> **Google Cloud Console:** ensure `http://localhost:3000/api/login/oauth2/code/google` is listed under **Authorized redirect URIs** on your OAuth client (APIs & Services → Credentials → your client → Edit).

### Option B: Full local stack (Docker Compose)

Uses MySQL, LocalStack (S3 + Secrets Manager). Closer to production.

```bash
# 1. Clone and setup
git clone <repo-url>
cd claude-first-test
./setup.sh

# 2. Configure credentials
cp docker/.env.example docker/.env
# Edit docker/.env and fill in your Google OAuth credentials

# 3. Start all services
cd docker
docker compose up --build
```

Services available after startup:

| Service    | URL                                            |
|------------|------------------------------------------------|
| Frontend   | http://localhost:3000                          |
| Backend    | http://localhost:8080                          |
| MySQL      | localhost:3306 (user: `mindtrack`, pass: `mindtrack`) |
| LocalStack | http://localhost:4566                          |
| Grafana    | http://localhost:3001 (user: `admin`, pass: `mindtrack`) |

To stop: `docker compose down` (add `-v` to also remove MySQL data volume).

#### Google OAuth credentials (`docker/.env`)

`docker/.env` is gitignored. Copy the example file and fill in your credentials from [Google Cloud Console](https://console.cloud.google.com/) → APIs & Services → Credentials:

```bash
cp docker/.env.example docker/.env
```

```dotenv
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret
```

Also add `http://localhost:3000/api/login/oauth2/code/google` under **Authorized redirect URIs** in your OAuth client (APIs & Services → Credentials → your client → Edit). Login will fail with `redirect_uri_mismatch` without this.
