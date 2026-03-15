# Testing

## Backend

```bash
# Compile, checkstyle, spotbugs, unit tests, and JaCoCo coverage
cd backend && mvn clean verify
```

## Frontend

```bash
# ESLint + Prettier (auto-fix)
cd frontend && npm run lint

# Vitest unit tests
cd frontend && npm run test:unit

# Vitest unit tests with coverage
cd frontend && npm run test:coverage
```

Coverage reports generated for SonarCloud:

- Backend: `backend/target/site/jacoco/jacoco.xml`
- Frontend: `frontend/coverage/lcov.info`

## Infrastructure

```bash
# Format, validate, tflint, tfsec
bash infra/tests/unit/validate.sh
```

## Soft-Fail Policy

ESLint lint checks use `continue-on-error: true` where configured in CI. Failed steps appear as warnings (orange) in GitHub Actions rather than failing the build.

## Snyk Monitoring

A scheduled `snyk-monitor` workflow runs weekly on Mondays at 03:00 UTC. It runs `snyk test` for backend and frontend dependencies, then sends dependency snapshots to the Snyk dashboard via `snyk monitor`.

This repository does not run Snyk in the PR workflows. If Snyk checks appear on pull requests, they are coming from the Snyk GitHub integration rather than from GitHub Actions in this repo.

Trigger manually: **GitHub Actions > Snyk Weekly Security > Run workflow**.
