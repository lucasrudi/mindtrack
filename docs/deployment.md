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

1. **Create a CI/CD IAM user** (for GitHub Actions):

   ```bash
   aws iam create-user --user-name mindtrack-ci
   aws iam attach-user-policy --user-name mindtrack-ci \
     --policy-arn arn:aws:iam::aws:policy/AdministratorAccess  # scope down in production
   aws iam create-access-key --user-name mindtrack-ci
   ```

   Save the `AccessKeyId` and `SecretAccessKey` for GitHub secrets.

2. **Recommended: Use OIDC instead of access keys** for GitHub Actions:
   - Create an IAM Identity Provider for `token.actions.githubusercontent.com`
   - Create an IAM role with trust policy for your GitHub repo
   - Reference in workflows with `aws-actions/configure-aws-credentials@v4` using `role-to-assume`

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

### GitHub Repository Secrets

GitHub Actions secrets and variables are managed via Terraform (see `infra/modules/github/`). Pass them when applying:

```bash
terraform -chdir=infra/github-settings apply \
  -var='repository_name=<your-repo-name>' \
  -var='actions_secrets={
    "AWS_ACCESS_KEY_ID":"<key>",
    "AWS_SECRET_ACCESS_KEY":"<secret>",
    "SONAR_TOKEN":"<sonar-token>",
    "SNYK_TOKEN":"<snyk-token>",
    "ANTHROPIC_API_KEY":"<anthropic-key>"
  }'
```

Or configure manually in **Settings** > **Secrets and variables** > **Actions**:

**Secrets:**

| Secret | Source | Purpose |
|--------|--------|---------|
| `AWS_ACCESS_KEY_ID` | AWS IAM (or use OIDC) | CI/CD AWS access |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM (or use OIDC) | CI/CD AWS access |
| `SONAR_TOKEN` | [SonarCloud](https://sonarcloud.io/) > My Account > Security | Code quality analysis |
| `SNYK_TOKEN` | [Snyk](https://app.snyk.io/) > Account settings | Vulnerability scanning |
| `ANTHROPIC_API_KEY` | [Anthropic Console](https://console.anthropic.com/) > API Keys | Claude code-review in CI |
| `RENOVATE_TOKEN` | GitHub PAT (classic) with `repo` scope | Renovate dependency updates |

**Variables:**

| Variable | Example | Purpose |
|----------|---------|---------|
| `FRONTEND_BUCKET` | `mindtrack-prod-frontend` | S3 bucket for frontend deploy |
| `CLOUDFRONT_DISTRIBUTION_ID` | `E1234ABCDE` | CloudFront invalidation |

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
cd infra/envs/dev
terraform init
terraform apply -var-file=dev.tfvars
# Retrieve credentials and store in GitHub Actions secrets
terraform output -raw ci_access_key_id
terraform output -raw ci_secret_access_key
```

Add to GitHub secrets: `AWS_ACCESS_KEY_ID_DEV`, `AWS_SECRET_ACCESS_KEY_DEV`
(and same for test: `AWS_ACCESS_KEY_ID_TEST`, `AWS_SECRET_ACCESS_KEY_TEST`)

### IAM Policy Summary

Each environment's CI user (`mindtrack-{env}-ci`) can:
- Update Lambda functions prefixed `mindtrack-{env}-*`
- Read/write S3 buckets prefixed `mindtrack-{env}-*`
- Invalidate CloudFront distributions

Each environment's CI user explicitly **cannot**:
- Access prod S3, Lambda, or Secrets Manager paths
- Read secrets from `/mindtrack/prod/*`
