# Environment Variables

All environment variables required to run MindTrack, organized by context.

## Backend Application

| Variable | Description | Example | Source | Required |
|----------|-------------|---------|--------|----------|
| `DB_URL` | Aurora/MySQL JDBC URL | `jdbc:mysql://host:3306/mindtrack` | AWS Secrets Manager | Production only |
| `DB_USERNAME` | Database username | `mindtrack` | AWS Secrets Manager | Production only |
| `DB_PASSWORD` | Database password | `s3cr3t` | AWS Secrets Manager | Production only |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID | `1234...apps.googleusercontent.com` | Google Cloud Console | Production only |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | `GOCSPX-...` | Google Cloud Console | Production only |
| `JWT_SECRET` | 256-bit JWT signing key | `my-secret-key-256-bits-long-pad!!` | Secrets Manager or `.env` | All envs (has dev default) |
| `JWT_EXPIRATION_MS` | JWT token TTL in milliseconds | `86400000` | Config | Optional — default: 86400000 (24h) |
| `FRONTEND_URL` | Allowed CORS origin | `https://app.mindtrack.io` | Config | Optional — default: `http://localhost:3000` |
| `CLAUDE_API_KEY` | Anthropic Claude API key | `sk-ant-...` | AWS Secrets Manager | All envs |

## Observability (optional)

All default to disabled/localhost. Only set these if you have an OTLP-compatible collector running.

| Variable | Description | Default |
|----------|-------------|---------|
| `OTEL_SAMPLING_PROBABILITY` | Trace sampling rate (0–1) | `1.0` |
| `OTEL_EXPORTER_OTLP_METRICS_ENDPOINT` | OTLP metrics push URL | `http://localhost:4318/v1/metrics` |
| `OTEL_METRICS_EXPORT_STEP` | Metrics export interval | `30s` |
| `OTEL_METRICS_ENABLED` | Enable OTLP metrics export | `false` |
| `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT` | OTLP traces push URL | `http://localhost:4318/v1/traces` |
| `OTEL_TRACING_ENABLED` | Enable OTLP traces export | `false` |

## CI/CD — GitHub Actions Secrets

| Secret | Description | Source |
|--------|-------------|--------|
| `AWS_ACCESS_KEY_ID` | AWS CI/CD credentials | AWS IAM user `mindtrack-ci` |
| `AWS_SECRET_ACCESS_KEY` | AWS CI/CD credentials | AWS IAM user `mindtrack-ci` |
| `SONAR_TOKEN` | SonarCloud analysis token | SonarCloud > My Account > Security |
| `SNYK_TOKEN` | Snyk vulnerability scanning token | Snyk > Account settings |
| `ANTHROPIC_API_KEY` | Claude code-review in CI | Anthropic Console > API Keys |
| `RENOVATE_TOKEN` | GitHub PAT for Renovate | GitHub Settings > Developer settings > PAT (classic), `repo` scope |

## CI/CD — GitHub Actions Variables

| Variable | Example | Purpose |
|----------|---------|---------|
| `FRONTEND_BUCKET` | `mindtrack-prod-frontend` | S3 bucket name for frontend deploy |
| `CLOUDFRONT_DISTRIBUTION_ID` | `E1234ABCDE` | CloudFront distribution for cache invalidation |
