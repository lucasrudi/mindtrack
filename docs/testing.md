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
