# Architecture

## System Diagram

```
                           +------------------+
                           |   CloudFront     |
                           |   (CDN + SPA)    |
                           +--------+---------+
                                    |
                    +---------------+---------------+
                    |                               |
             +------+------+              +---------+---------+
             |  S3 Bucket  |              |   API Gateway     |
             |  (Frontend) |              |   (HTTP API)      |
             +-------------+              +---------+---------+
                                                    |
                                          +---------+---------+
                                          |    AWS Lambda     |
                                          |  (Spring Boot +   |
                                          |   SnapStart)      |
                                          +---------+---------+
                                                    |
                                  +-----------------+-----------------+
                                  |                 |                 |
                          +-------+-------+ +-------+------+ +------+-------+
                          | Aurora        | | S3 (Audio)   | | Secrets      |
                          | Serverless v2 | | 7-day expiry | | Manager      |
                          | (MySQL)       | +-------+------+ +--------------+
                          +---------------+         |
                                            +-------+-------+
                                            | AWS Transcribe|
                                            +---------------+
```

**Modular Monolith** — single Spring Boot application with clean module separation, deployed as AWS Lambda with SnapStart.

### Backend Modules

| Module | Responsibility |
|--------|---------------|
| `auth` | Google OAuth2, JWT, RBAC |
| `admin` | User management panel |
| `interview` | Session logging, audio, transcription |
| `activity` | Habits, exercises, homework tracking |
| `journal` | Free-form journal entries |
| `goals` | Goals & milestones |
| `ai` | Claude API integration |
| `messaging` | Telegram + WhatsApp bots |
| `analytics` | Dashboard metrics computation |
| `common` | Shared entities, config, utils |

## Tech Stack

| Layer          | Technology                                        |
|----------------|---------------------------------------------------|
| Backend        | Java 21, Spring Boot 3.5.3, Maven                 |
| Frontend       | Vue.js 3 (Composition API), TypeScript, Vite      |
| State          | Pinia (frontend), Spring Data JPA (backend)       |
| Database       | Aurora Serverless v2 MySQL (prod) / H2 (local)    |
| Infrastructure | Terraform, AWS Lambda, API Gateway, CloudFront    |
| AI             | Claude API (Anthropic)                            |
| Messaging      | Telegram Bot API, WhatsApp Business API           |
| CI/CD          | GitHub Actions, release-please, SonarCloud        |
| Code Quality   | Checkstyle, SpotBugs, ESLint, Prettier, tflint    |
