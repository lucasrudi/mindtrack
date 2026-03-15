# SonarCloud Quality Gates — MindTrack

## Overview

MindTrack uses [SonarCloud](https://sonarcloud.io/) for continuous code quality and security analysis. The quality gate is enforced in CI via `sonar.qualitygate.wait=true` in `sonar-project.properties` — any PR or push to `main` that fails the gate will fail the CI pipeline.

## Quality Gate Thresholds

Configure these in the SonarCloud UI under **Quality Gates** for the `mindtrack` project:

| Metric | Condition | Threshold | Rationale |
|--------|-----------|-----------|-----------|
| Coverage on new code | is less than | **80%** | Prevents coverage regression on new features |
| Duplicated lines on new code | is greater than | **3%** | Avoids copy-paste patterns |
| Maintainability rating on new code | is worse than | **A** | Keeps technical debt low |
| Reliability rating on new code | is worse than | **A** | Zero new bugs policy |
| Security rating on new code | is worse than | **A** | Zero new vulnerabilities policy |
| Security hotspots reviewed on new code | is less than | **100%** | All security-sensitive code must be reviewed |

## How to Configure in SonarCloud

1. Go to [sonarcloud.io](https://sonarcloud.io/) → your organization → **Quality Gates**
2. Create a new gate named `MindTrack Gate` (or edit the existing default)
3. Add each condition from the table above
4. Navigate to the **mindtrack** project → **Project Settings** → **Quality Gate**
5. Select `MindTrack Gate`

## Overall Code Quality Targets

These are aspirational targets for the overall codebase (new code gate is the enforcement mechanism):

| Metric | Target |
|--------|--------|
| Test coverage (overall) | ≥ 75% |
| Duplicated lines | ≤ 5% |
| Technical debt ratio | ≤ 5% |
| Maintainability rating | A |
| Reliability rating | A |
| Security rating | A |

## CI Integration

The SonarCloud analysis runs in two pipelines:

- **`feature.yml`** — on every push to `feature/**`, `bugfix/**`, `chore/**`, and on PRs to `main`
- **`verify.yml`** — on every push to `main`

Both pipelines consume coverage artifacts from prior build steps before uploading to SonarCloud:

- backend JaCoCo XML: `backend/target/site/jacoco/jacoco.xml`
- frontend LCOV: `frontend/coverage/lcov.info`

The `SONAR_TOKEN` secret must be configured in GitHub Actions settings (managed via Terraform — see `infra/modules/github/`).

## Coverage Exclusions

The following are excluded from coverage requirements (see `sonar-project.properties`):

- `**/model/**` — JPA entities (data classes, no logic)
- `**/dto/**` — DTOs (data transfer objects)
- `**/config/**` — Spring configuration classes
- `**/*Application.java` — Spring Boot main class
