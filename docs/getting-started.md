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
