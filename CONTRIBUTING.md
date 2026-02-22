# Contributing to MindTrack

## Getting Started

1. Clone the repository
2. Run `./setup.sh` to configure git hooks and install dependencies
3. Start the backend with `cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local`
4. Start the frontend with `cd frontend && npm run dev`

## Branch Naming

- `feature/<description>` — new features
- `bugfix/<description>` — bug fixes
- `hotfix/<description>` — urgent production fixes
- `chore/<description>` — maintenance tasks

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/) **prefixed with the GitHub issue ID**:

```
#<issue-id> <type>(<scope>): <description>

[optional body]
```

Types: `feat`, `fix`, `chore`, `docs`, `test`, `refactor`, `ci`, `infra`

Examples:
- `#17 chore: review and fix TODOs across codebase`
- `#18 test: add integration tests for interview module`
- `#4 feat(interview): add audio upload endpoint`
- `#1 fix(auth): resolve JWT token expiry race condition`

The `#<issue-id>` prefix is validated by the `commit-msg` hook (`.githooks/commit-msg`).
Commits without an issue prefix will be rejected — use `#0` only for housekeeping with no associated issue.

## Pull Request Process

1. Create a feature branch from `main`
2. Make changes with tests
3. Ensure all pre-commit and pre-push checks pass (lint, tests, terraform validate)
4. Open a PR targeting `main`
5. Wait for CI to pass and get code review
6. Squash merge into `main`

## Code Style

### Java (Backend)

- **Checkstyle** enforced via Maven plugin (see `config/checkstyle/checkstyle.xml`)
- 4-space indentation, 120 character line width
- No wildcard imports
- Javadoc on public classes and methods (warning level)
- Run: `cd backend && mvn checkstyle:check`

### TypeScript/Vue (Frontend)

- **ESLint** + **Prettier** enforced (see `frontend/eslint.config.js`, `frontend/.prettierrc`)
- 2-space indentation, no semicolons, single quotes
- Vue 3 Composition API (`<script setup lang="ts">`)
- Run: `cd frontend && npm run lint`

### Terraform (Infrastructure)

- `terraform fmt` for formatting
- `tflint` for linting
- `tfsec` for security scanning
- 2-space indentation

## Testing Requirements

### Backend
- JUnit 5 + Mockito for unit tests
- Spring Boot Test for integration tests
- `@ActiveProfiles("local")` for test profile (H2 in-memory)
- Run: `cd backend && mvn verify`

### Frontend
- Vitest + Vue Test Utils for unit tests
- Cypress for E2E tests (future)
- Run: `cd frontend && npm run test:unit`

### Infrastructure
- Shell script validation (`infra/tests/unit/validate.sh`)
- Terratest for integration tests (`infra/tests/integration/`)

## Pre-Commit Hooks

The pre-commit hook (`.githooks/pre-commit`) runs fast lint checks on every commit:
- **Java:** Checkstyle (only if `.java` files staged)
- **Frontend:** ESLint + Prettier (only if `frontend/src/` files staged)
- **Terraform:** `terraform fmt -check` (only if `.tf` files staged)

These are much faster than the pre-push checks and catch formatting issues early.

## Pre-Push Hooks

The pre-push hook (`.githooks/pre-push`) runs automatically and blocks pushes if:
- Backend tests fail
- Frontend lint or tests fail
- Terraform format/validate checks fail
- Snyk finds high/critical vulnerabilities (requires Snyk CLI installed)

Configure hooks with: `git config core.hooksPath .githooks`

## Security Scanning

[Snyk](https://snyk.io/) is integrated into the pre-push hook to scan for dependency vulnerabilities:

```bash
# Install Snyk CLI
npm install -g snyk
snyk auth  # authenticate with your Snyk account

# Manual scan
snyk test --file=backend/pom.xml
snyk test --file=frontend/package.json
```

The pre-push hook only blocks on high/critical severity vulnerabilities. If Snyk CLI is not installed, the scan is skipped with a warning.

## Releases

Releases are automated via [release-please](https://github.com/googleapis/release-please). Your commit messages directly drive versioning:

| Commit prefix | Version bump | Example |
|---------------|-------------|---------|
| `feat:` | minor (0.1.0 → 0.2.0) | `feat: add interview audio upload` |
| `fix:` | patch (0.1.0 → 0.1.1) | `fix: resolve JWT expiry bug` |
| `feat!:` | major (0.1.0 → 1.0.0) | `feat!: redesign auth flow` |
| `chore:`, `docs:`, etc. | no release | `chore: update dependencies` |

Backend, frontend, and infra are versioned independently based on which files changed.

## Dependency Updates

[Renovate](https://www.mend.io/renovate/) automatically creates PRs for dependency updates:
- **Minor/patch updates** are auto-merged (runs on weekends)
- **Major updates** require manual review (labeled `breaking`)
- Dependencies are grouped by layer (backend, frontend, infra, github-actions)
- Vulnerability alerts create immediate PRs (labeled `security`)
