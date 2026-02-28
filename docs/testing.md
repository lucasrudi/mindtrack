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
