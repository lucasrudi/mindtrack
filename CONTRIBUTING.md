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

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>: <description>

[optional body]
```

Types: `feat`, `fix`, `chore`, `docs`, `test`, `refactor`, `ci`, `infra`

Examples:
- `feat: add interview audio upload endpoint`
- `fix: resolve JWT token expiry race condition`
- `chore: update Spring Boot to 3.4.3`
- `infra: add CloudWatch alarms for Lambda errors`

## Pull Request Process

1. Create a feature branch from `main`
2. Make changes with tests
3. Ensure all pre-push checks pass (tests, lint, terraform validate)
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

## Pre-Push Hooks

The pre-push hook (`.githooks/pre-push`) runs automatically and blocks pushes if:
- Backend tests fail
- Frontend lint or tests fail
- Terraform format/validate checks fail

Configure hooks with: `git config core.hooksPath .githooks`
