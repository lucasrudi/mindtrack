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

## Sentry (optional)

Error tracking and performance monitoring. SDKs activate only when the DSN is set — leave empty to disable.

### Backend

| Variable | Description | Default | Storage |
|----------|-------------|---------|---------|
| `SENTRY_DSN` | Backend Sentry project DSN | `""` (disabled) | AWS Secrets Manager: `mindtrack-prod/sentry_dsn` → Lambda env var |
| `SENTRY_TRACES_SAMPLE_RATE` | Performance tracing sample rate (0–1) | `0.05` (prod), `0.1` (base) | Lambda env var or YAML default |
| `SENTRY_ENVIRONMENT` | Environment label shown in Sentry | `local` | Set per profile via `application-{env}.yml` |

### Frontend (build-time, `VITE_` prefix — embedded in JS bundle, not secrets)

| Variable | Description | Default | Storage |
|----------|-------------|---------|---------|
| `VITE_SENTRY_DSN` | Frontend Sentry project DSN | `""` (disabled) | GitHub Actions Variable (`vars.*`) |
| `VITE_SENTRY_RELEASE` | Frontend release identifier attached to Sentry events | `""` | Derived in GitHub Actions from the release tag |
| `VITE_SENTRY_TRACES_SAMPLE_RATE` | Performance tracing sample rate (0–1) | `0.1` | GitHub Actions Variable |
| `VITE_APP_ENV` | Environment label shown in Sentry | `local` | Hardcoded `production` in `deploy.yml` |

### Frontend Sentry Build Upload (CI only)

| Variable | Description | Default | Storage |
|----------|-------------|---------|---------|
| `SENTRY_AUTH_TOKEN` | Sentry auth token used to create releases and upload sourcemaps | unset | GitHub Actions Secret |
| `SENTRY_ORG` | Sentry organization slug | unset | GitHub Actions Variable |
| `SENTRY_PROJECT_FRONTEND` | Frontend Sentry project slug | unset | GitHub Actions Variable |
| `SENTRY_RELEASE` | Build-time release name used by the Sentry Vite plugin | unset | Derived in GitHub Actions from the release tag |

## Google Analytics 4 (optional)

Page view and usage analytics with privacy guards (route patterns only, IP anonymized).

| Variable | Description | Default | Storage |
|----------|-------------|---------|---------|
| `VITE_GA_MEASUREMENT_ID` | GA4 Measurement ID (format: `G-XXXXXXXXXX`) | `""` (disabled) | GitHub Actions Variable (`vars.*`) |

## CI/CD — GitHub Actions Secrets

| Secret | Description | Source |
|--------|-------------|--------|
| `AWS_ACCESS_KEY_ID` | AWS CI/CD credentials | AWS IAM user `mindtrack-ci` |
| `AWS_SECRET_ACCESS_KEY` | AWS CI/CD credentials | AWS IAM user `mindtrack-ci` |
| `SONAR_TOKEN` | SonarCloud analysis token | SonarCloud > My Account > Security |
| `SNYK_TOKEN` | Snyk vulnerability scanning token | Snyk > Account settings |
| `ANTHROPIC_API_KEY` | Claude code-review in CI | Anthropic Console > API Keys |
| `SENTRY_AUTH_TOKEN` | Sentry auth token for frontend release/source-map upload | Sentry > Settings > Auth Tokens |
| `RENOVATE_TOKEN` | GitHub PAT for Renovate | GitHub Settings > Developer settings > PAT (classic), `repo` scope |

## CI/CD — GitHub Actions Variables

| Variable | Example | Purpose |
|----------|---------|---------|
| `AWS_ROLE_ARN` | `arn:aws:iam::123456789012:role/mindtrack-prod-github-actions` | OIDC role for deploy workflow |
| `FRONTEND_BUCKET` | `mindtrack-prod-frontend` | S3 bucket name for frontend deploy |
| `CLOUDFRONT_DISTRIBUTION_ID` | `E1234ABCDE` | CloudFront distribution for cache invalidation |
| `SENTRY_ORG` | `rudilucas` | Sentry organization slug for frontend release uploads |
| `SENTRY_PROJECT_FRONTEND` | `mindtrack-frontend` | Frontend Sentry project slug for source-map upload |
| `VITE_SENTRY_DSN` | `https://abc123@o0.ingest.sentry.io/0` | Frontend Sentry DSN (injected at build time) |
| `VITE_SENTRY_RELEASE` | `frontend-v1.2.3` | Frontend release identifier (derived in deploy workflow) |
| `VITE_SENTRY_TRACES_SAMPLE_RATE` | `0.1` | Frontend Sentry tracing rate |
| `VITE_GA_MEASUREMENT_ID` | `G-XXXXXXXXXX` | GA4 Measurement ID (injected at build time) |
