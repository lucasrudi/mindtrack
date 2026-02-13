# MindTrack DX Enhancements — Design Document

**Date:** 2026-02-13
**Status:** Approved

## Overview

Enhance developer experience with VSCode support, unified linting with pre-commit hooks, comprehensive README (local dev, deployment, secrets provisioning), and release versioning with independent deploys.

## 1. VSCode Support

Add `.vscode/` directory with workspace configuration matching existing IntelliJ code style conventions.

### Files

- `.vscode/settings.json` — workspace settings: 4-space Java indent, 2-space TS/Vue/TF indent, format-on-save via ESLint/Prettier, Checkstyle config path, Terraform formatting
- `.vscode/extensions.json` — recommended extensions: Java Extension Pack, Volar (Vue), ESLint, Prettier, Terraform, Checkstyle for Java, EditorConfig, Spring Boot Tools
- `.vscode/launch.json` — debug configurations: Spring Boot (local profile), Vue dev server attach, Jest/Vitest debug
- `.vscode/tasks.json` — common tasks: mvn compile, mvn verify, npm build, npm lint, terraform validate

### .gitignore Update

Track `.vscode/` config files but exclude personal workspace files:
```
!.vscode/
.vscode/*.code-workspace
.vscode/.browse.c_cpp.db*
.vscode/.ropeproject
```

## 2. Linters + Pre-commit Hooks

### Current State

- Backend: Checkstyle + SpotBugs via Maven (runs on `mvn verify`)
- Frontend: ESLint + Prettier (runs via `npm run lint`)
- Infra: tflint + tfsec + `terraform fmt` (runs in CI and pre-push)
- Git hook: pre-push only (runs full tests — slow)

### Design

Add a **pre-commit hook** (`.githooks/pre-commit`) for fast lint-only checks:

- **Backend:** `mvn checkstyle:check -q` — lint only, no compilation or tests
- **Frontend:** `npx eslint . --max-warnings 0` (check mode, no --fix) + `npx prettier --check src/`
- **Infra:** `terraform fmt -check -recursive`

Keep the existing **pre-push hook** for heavier checks (tests, tflint, tfsec).

No additional dependencies (no husky/lint-staged). Uses the existing `.githooks/` + `setup.sh` pattern.

Update `setup.sh` to `chmod +x` both hooks.

## 3. Expanded README

### Structure

1. **Project description** — what MindTrack is, features, target users
2. **Architecture** — existing diagram (kept)
3. **Tech stack** — existing table (kept)
4. **Prerequisites** — expanded with install instructions per tool
5. **Local development** — two modes:
   - Minimal: H2 + no external services
   - Full stack: Docker Compose (MySQL, LocalStack)
6. **Testing** — existing section (kept)
7. **Deployment guide** — step-by-step first-time AWS setup
8. **Secrets provisioning** — per-system setup:
   - AWS (IAM, S3 state bucket, DynamoDB lock table)
   - Google OAuth2 (Cloud Console, consent screen, credentials)
   - GitHub (repository secrets)
   - Claude API (Anthropic Console, Secrets Manager)
   - Telegram (BotFather, bot token)
   - WhatsApp (Meta Business Suite, API setup)
9. **Release & versioning** — how releases work
10. **Contributing** — link to CONTRIBUTING.md

## 4. Release Versioning + Independent Deploys

### Approach: release-please (by Google)

Conventional commits drive automatic version bumps. `feat:` = minor, `fix:` = patch, `feat!:` / `BREAKING CHANGE` = major.

### Monorepo Configuration

`release-please-config.json` at project root with three components:
- `backend` — Java/Maven, updates `pom.xml` version
- `frontend` — Node, updates `package.json` version
- `infra` — Simple (version.txt marker file)

Each component has its own independent version number (e.g., backend v1.2.0, frontend v0.5.1, infra v1.0.0).

`.release-please-manifest.json` tracks current versions.

### Pipeline Restructure

**Remove:** `main.yml` (monolithic deploy-everything pipeline)

**Add:**

1. **`release.yml`** — triggered on push to main. Runs release-please action to create/update a release PR with changelog. When the release PR is merged, creates GitHub Releases with tags.

2. **`deploy.yml`** — triggered on GitHub Release published. Uses path-based detection to deploy only changed components:
   - `backend/**` changed → build JAR, update Lambda
   - `frontend/**` changed → build dist, sync S3, invalidate CloudFront
   - `infra/**` changed → terraform plan + apply

3. **`feature.yml`** — kept as-is (CI on feature branches + PRs)

### Version Artifacts

- Backend: `pom.xml` `<version>` tag bumped automatically
- Frontend: `package.json` `"version"` field bumped automatically
- Infra: `infra/version.txt` bumped automatically
- GitHub Releases: auto-generated changelogs per component
