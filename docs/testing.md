# Testing

## Backend

```bash
# Compile, checkstyle, spotbugs, unit tests
cd backend && mvn verify
```

## Frontend

```bash
# ESLint + Prettier (auto-fix)
cd frontend && npm run lint

# Vitest unit tests
cd frontend && npm run test:unit

# Vitest unit tests with coverage
cd frontend && npm run test:unit -- --run --coverage
```

## Infrastructure

```bash
# Format, validate, tflint, tfsec
bash infra/tests/unit/validate.sh
```

## Soft-Fail Policy

Snyk vulnerability scans and ESLint lint checks use `continue-on-error: true` in CI. Failed steps appear as warnings (orange) in GitHub Actions rather than failing the build. Issues remain visible in the job logs.

The pre-push git hook still enforces Snyk locally — `snyk test` must pass before pushing.

## Snyk Monitoring

A scheduled `snyk-monitor` workflow runs daily at 03:00 UTC. It sends dependency snapshots to the Snyk dashboard via `snyk monitor` (non-blocking). This gives continuous visibility into new CVEs that appear after a release.

The daily monitor complements the `snyk test` step in feature CI (which gates on high-severity issues at PR time).

Trigger manually: **GitHub Actions > Snyk Monitor > Run workflow**.
