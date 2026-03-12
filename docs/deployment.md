# Deployment

## First-Time AWS Setup

1. **Create Terraform state resources** (one-time, manual):

   ```bash
   # Create S3 bucket for Terraform state
   aws s3api create-bucket \
     --bucket mindtrack-terraform-state \
     --region us-east-1

   aws s3api put-bucket-versioning \
     --bucket mindtrack-terraform-state \
     --versioning-configuration Status=Enabled

   aws s3api put-bucket-encryption \
     --bucket mindtrack-terraform-state \
     --server-side-encryption-configuration \
       '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'

   # Create DynamoDB table for state locking
   aws dynamodb create-table \
     --table-name mindtrack-terraform-locks \
     --attribute-definitions AttributeName=LockID,AttributeType=S \
     --key-schema AttributeName=LockID,KeyType=HASH \
     --billing-mode PAY_PER_REQUEST \
     --region us-east-1
   ```

2. **Initialize Terraform**:

   ```bash
   cd infra
   terraform init
   terraform plan -var-file=environments/dev.tfvars
   terraform apply -var-file=environments/dev.tfvars
   ```

3. **Populate secrets** (see Secrets Provisioning below)

4. **Deploy frontend**:

   ```bash
   cd frontend && npm run build
   aws s3 sync dist/ s3://mindtrack-dev-frontend/ --delete
   ```

## Automated Deploys

After first-time setup, all deployments are automated via GitHub Actions:

- Push to `main` → release-please creates a release PR
- Merge the release PR → GitHub Release published → deploy pipeline runs
- Only changed components are deployed (backend, frontend, or infra)

See `.github/workflows/release.yml` and `.github/workflows/deploy.yml`.

---

## Secrets Provisioning

### AWS

GitHub Actions authenticates to AWS via **OIDC** — no long-lived access keys are stored as secrets. The IAM OIDC provider and GitHub Actions role are managed by Terraform in `infra/modules/iam/`.

1. **Apply the prod Terraform environment** (creates the OIDC provider + scoped IAM role):

   ```bash
   cd infra
   terraform init
   terraform apply -var-file=environments/prod.tfvars
   ```

2. **Get the role ARN** from Terraform output:

   ```bash
   terraform output github_actions_role_arn
   ```

3. **Set `AWS_ROLE_ARN` as a GitHub Actions variable** via the GitHub settings Terraform:

   ```bash
   terraform -chdir=infra/github-settings apply \
     -var='repository_name=mindtrack' \
     -var='actions_variables={"AWS_ROLE_ARN":"<role-arn-from-step-2>"}'
   ```

   Or manually in **Settings** > **Secrets and variables** > **Actions** > **Variables**.

The deploy workflow (`deploy.yml`) uses `role-to-assume: ${{ vars.AWS_ROLE_ARN }}` and already has `id-token: write` permissions. No key rotation needed.

> **Note:** `create_oidc_provider = true` is set only in `prod.tfvars`. The OIDC provider is AWS account-scoped; setting it to `false` in `dev.tfvars` prevents a conflict if both environments share the same account.

### Google OAuth2

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (or select existing)
3. Navigate to **APIs & Services** > **OAuth consent screen**
   - User type: External
   - App name: MindTrack
   - Authorized domains: your domain
4. Navigate to **APIs & Services** > **Credentials**
   - Click **Create Credentials** > **OAuth client ID**
   - Application type: Web application
   - Authorized redirect URIs: `https://your-domain.com/login/oauth2/code/google` and `http://localhost:8080/login/oauth2/code/google` (for local dev)
5. Copy `Client ID` and `Client Secret`
6. Store in AWS Secrets Manager:

   ```bash
   aws secretsmanager put-secret-value \
     --secret-id mindtrack-dev/google_oauth_client_id \
     --secret-string "your-client-id"
   aws secretsmanager put-secret-value \
     --secret-id mindtrack-dev/google_oauth_client_secret \
     --secret-string "your-client-secret"
   ```

### Claude API (Anthropic)

1. Go to [Anthropic Console](https://console.anthropic.com/)
2. Navigate to **API Keys**
3. Create a new API key
4. Store in AWS Secrets Manager:

   ```bash
   aws secretsmanager put-secret-value \
     --secret-id mindtrack-dev/claude_api_key \
     --secret-string "sk-ant-..."
   ```

### Telegram Bot

1. Open Telegram and message [@BotFather](https://t.me/BotFather)
2. Send `/newbot`, follow prompts to name your bot
3. Copy the bot token (format: `123456789:ABCdefGhIjKlMnOpQrStUvWxYz`)
4. Store in AWS Secrets Manager:

   ```bash
   aws secretsmanager put-secret-value \
     --secret-id mindtrack-dev/telegram_bot_token \
     --secret-string "123456789:ABCdef..."
   ```

5. Set up webhook (after backend is deployed):

   ```bash
   curl "https://api.telegram.org/bot<TOKEN>/setWebhook?url=https://your-api-domain.com/api/messaging/telegram/webhook"
   ```

### WhatsApp Business API

1. Go to [Meta Business Suite](https://business.facebook.com/)
2. Create or select a Business Account
3. Navigate to **WhatsApp** > **Getting Started**
4. Create a WhatsApp Business App in [Meta Developers](https://developers.facebook.com/)
5. Navigate to **WhatsApp** > **API Setup**
6. Copy the permanent access token
7. Store in AWS Secrets Manager:

   ```bash
   aws secretsmanager put-secret-value \
     --secret-id mindtrack-dev/whatsapp_api_token \
     --secret-string "EAAx..."
   ```

8. Configure webhook URL in Meta Developer Dashboard pointing to `https://your-api-domain.com/api/messaging/whatsapp/webhook`

### Sentry

1. Go to [sentry.io](https://sentry.io/) and create or sign in to your organization.
2. Create two projects:
   - **Java/Spring Boot** — for the backend
   - **JavaScript/Vue** — for the frontend
3. Store the backend DSN in AWS Secrets Manager:

   ```bash
   aws secretsmanager put-secret-value \
     --secret-id mindtrack-prod/sentry_dsn \
     --secret-string "https://abc123@o0.ingest.sentry.io/0"
   ```

   The backend release workflow reads this secret and merges `SENTRY_DSN` into
   the production Lambda environment automatically during deploy.

4. Set the frontend Sentry values in GitHub Actions. The DSN is embedded in the
   JS bundle, so keep it as a variable; the upload token must be a secret:

   In **Settings** > **Secrets and variables** > **Actions** > **Variables**:
   - `SENTRY_ORG` → your Sentry organization slug
   - `SENTRY_PROJECT_FRONTEND` → your frontend Sentry project slug
   - `VITE_SENTRY_DSN` → `https://xyz789@o0.ingest.sentry.io/1` (frontend project DSN)
   - `VITE_SENTRY_TRACES_SAMPLE_RATE` → `0.1` (optional, defaults to 0.1)

   In **Settings** > **Secrets and variables** > **Actions** > **Secrets**:
   - `SENTRY_AUTH_TOKEN` → Sentry auth token with release/source-map upload access

### Google Analytics 4

1. Go to [Google Analytics](https://analytics.google.com/) and create a new **GA4 property**.
2. In **Admin** > **Data Streams**, add a Web stream for your app's domain.
3. Copy the **Measurement ID** (format: `G-XXXXXXXXXX`).
4. Set it as a GitHub Actions Variable:

   In **Settings** > **Secrets and variables** > **Actions** > **Variables**:
   - `VITE_GA_MEASUREMENT_ID` → `G-XXXXXXXXXX`

   Leave unset to disable analytics in all environments.

### GitHub Repository Secrets

GitHub Actions secrets and variables are managed via Terraform (see `infra/modules/github/`). Pass them when applying:

```bash
terraform -chdir=infra/github-settings apply \
  -var='repository_name=<your-repo-name>' \
  -var='actions_secrets={
    "GH_CONFIG_TOKEN":"<github-pat>",
    "SONAR_TOKEN":"<sonar-token>",
    "SNYK_TOKEN":"<snyk-token>",
    "ANTHROPIC_API_KEY":"<anthropic-key>",
    "SENTRY_AUTH_TOKEN":"<sentry-auth-token>"
  }' \
  -var='actions_variables={
    "AWS_ROLE_ARN":"<aws-role-arn>",
    "SENTRY_ORG":"<sentry-org-slug>",
    "SENTRY_PROJECT_FRONTEND":"<frontend-project-slug>",
    "VITE_SENTRY_DSN":"<frontend-dsn>",
    "VITE_SENTRY_TRACES_SAMPLE_RATE":"0.1"
  }'
```

Or configure manually in **Settings** > **Secrets and variables** > **Actions**:

**Secrets:**

| Secret | Source | Purpose |
|--------|--------|---------|
| `SONAR_TOKEN` | [SonarCloud](https://sonarcloud.io/) > My Account > Security | Code quality analysis |
| `SNYK_TOKEN` | [Snyk](https://app.snyk.io/) > Account settings | Vulnerability scanning |
| `ANTHROPIC_API_KEY` | [Anthropic Console](https://console.anthropic.com/) > API Keys | Claude code-review in CI |
| `SENTRY_AUTH_TOKEN` | [Sentry](https://sentry.io/settings/auth-tokens/) | Frontend release/source-map upload during deploy |
| `RENOVATE_TOKEN` | GitHub PAT (classic) with `repo` scope | Renovate dependency updates |

**Variables:**

| Variable | Example | Purpose |
|----------|---------|---------|
| `AWS_ROLE_ARN` | `arn:aws:iam::123456789012:role/mindtrack-prod-github-actions` | OIDC role for deploy workflow |
| `FRONTEND_BUCKET` | `mindtrack-prod-frontend` | S3 bucket for frontend deploy |
| `CLOUDFRONT_DISTRIBUTION_ID` | `E1234ABCDE` | CloudFront invalidation |
| `SENTRY_ORG` | `rudilucas` | Sentry organization slug for frontend uploads |
| `SENTRY_PROJECT_FRONTEND` | `mindtrack-frontend` | Frontend Sentry project slug for source-map upload |
| `VITE_SENTRY_DSN` | `https://xyz789@o0.ingest.sentry.io/1` | Frontend Sentry DSN (injected at build time) |
| `VITE_SENTRY_TRACES_SAMPLE_RATE` | `0.1` | Frontend Sentry tracing sample rate |
| `VITE_APP_ENV` | `production` | Environment label in Sentry (hardcoded in deploy.yml) |
| `VITE_GA_MEASUREMENT_ID` | `G-XXXXXXXXXX` | GA4 Measurement ID (injected at build time) |

> See [Environment Variables](environment-variables.md) for the complete reference of all application and CI environment variables.

---

## Environment Matrix

| Environment | Purpose | State Key | IAM User | Lambda Role |
|-------------|---------|-----------|----------|-------------|
| `dev` | Developer manual testing | `mindtrack/dev/terraform.tfstate` | `mindtrack-dev-ci` | `mindtrack-dev-app` |
| `test` | CI integration tests | `mindtrack/test/terraform.tfstate` | `mindtrack-test-ci` | `mindtrack-test-app` |
| `prod` | Production | `terraform.tfstate` | `mindtrack-ci` | `mindtrack-prod-lambda-role` |

### Provisioning a new environment

```bash
cd infra
terraform init
terraform apply -var-file=environments/dev.tfvars
# Retrieve the GitHub Actions role ARN and set it as an Actions variable
terraform output github_actions_role_arn
```

Set the output as the `AWS_ROLE_ARN` Actions variable for the environment.

### IAM Policy Summary

Each environment's CI user (`mindtrack-{env}-ci`) can:
- Update Lambda functions prefixed `mindtrack-{env}-*`
- Read/write S3 buckets prefixed `mindtrack-{env}-*`
- Invalidate CloudFront distributions

Each environment's CI user explicitly **cannot**:
- Access prod S3, Lambda, or Secrets Manager paths
- Read secrets from `/mindtrack/prod/*`
