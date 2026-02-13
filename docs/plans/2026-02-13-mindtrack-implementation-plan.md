# MindTrack Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a complete scaffolded project for MindTrack — a mental health tracking app with Spring Boot backend, Vue.js frontend, AWS serverless infrastructure, full CI/CD, code quality tooling, and Backlog.md-compliant project management.

**Architecture:** Modular monolith — single Spring Boot app with clean module separation (interview, activity, ai, messaging, analytics, auth, admin, journal, goals, common). Deployed as AWS Lambda behind API Gateway. Vue.js SPA served from S3 + CloudFront. RDS Aurora Serverless v2 MySQL for production, H2 in-memory for local dev.

**Tech Stack:** Java 21, Spring Boot 3.x, Maven, Vue.js 3 (Composition API), npm, Vite, Terraform, Docker, GitHub Actions, SonarCloud, Checkstyle, ESLint, Prettier, tflint, tfsec, Terratest.

**Design doc:** `docs/plans/2026-02-13-mindtrack-design.md`

---

## Phase 1: Project Foundation & Tooling

### Task 1: Initialize Git Repository

**Files:**
- Create: `.gitignore`
- Create: `.editorconfig`

**Step 1: Initialize git repo**

Run: `cd /Users/lucasrudi/dev/claude-first-test && git init`
Expected: `Initialized empty Git repository`

**Step 2: Create .gitignore**

```gitignore
# Java / Maven
backend/target/
*.class
*.jar
*.war
*.ear
*.logs
*.log

# IntelliJ IDEA (keep codeStyles)
.idea/*
!.idea/codeStyles/
*.iml
*.iws
out/

# Node / npm
frontend/node_modules/
frontend/dist/
frontend/.vite/
*.tsbuildinfo

# Environment
.env
.env.local
.env.*.local
*.pem
*.key

# OS
.DS_Store
Thumbs.db

# Terraform
infra/.terraform/
infra/*.tfstate
infra/*.tfstate.backup
infra/*.tfplan
infra/.terraform.lock.hcl

# Docker
docker/data/

# Coverage
backend/coverage/
frontend/coverage/
*.lcov

# LocalStack
.localstack/
```

**Step 3: Create .editorconfig**

```editorconfig
root = true

[*]
charset = utf-8
end_of_line = lf
indent_style = space
indent_size = 4
insert_final_newline = true
trim_trailing_whitespace = true

[*.{js,ts,vue,json,yml,yaml,css,scss,html}]
indent_size = 2

[*.md]
trim_trailing_whitespace = false

[*.{tf,tfvars}]
indent_size = 2

[Makefile]
indent_style = tab
```

**Step 4: Commit**

```bash
git add .gitignore .editorconfig
git commit -m "chore: initialize repo with .gitignore and .editorconfig"
```

---

### Task 2: Create Spring Boot Backend Scaffold

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/mindtrack/MindTrackApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/application-local.yml`
- Create: `backend/src/main/resources/application-prod.yml`

**Step 1: Create backend directory structure**

Run:
```bash
mkdir -p backend/src/main/java/com/mindtrack/{interview,activity,ai,messaging,analytics,auth,admin,journal,goals,common}/{controller,service,repository,model,dto,config}
mkdir -p backend/src/main/resources/db/migration
mkdir -p backend/src/test/java/com/mindtrack
```

**Step 2: Create pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.2</version>
        <relativePath/>
    </parent>

    <groupId>com.mindtrack</groupId>
    <artifactId>mindtrack</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    <name>MindTrack</name>
    <description>Personal mental health tracking application</description>

    <properties>
        <java.version>21</java.version>
        <spring-cloud-aws.version>3.2.1</spring-cloud-aws.version>
        <flyway.version>10.6.0</flyway.version>
        <spotbugs-maven-plugin.version>4.8.3.1</spotbugs-maven-plugin.version>
        <checkstyle-maven-plugin.version>3.3.1</checkstyle-maven-plugin.version>
        <jacoco-maven-plugin.version>0.8.11</jacoco-maven-plugin.version>
        <sonar.organization>mindtrack</sonar.organization>
        <sonar.host.url>https://sonarcloud.io</sonar.host.url>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-oauth2-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>

        <!-- AWS -->
        <dependency>
            <groupId>io.awspring.cloud</groupId>
            <artifactId>spring-cloud-aws-starter-s3</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.awspring.cloud</groupId>
                <artifactId>spring-cloud-aws-dependencies</artifactId>
                <version>${spring-cloud-aws.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <!-- Checkstyle -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-checkstyle-plugin</artifactId>
                <version>${checkstyle-maven-plugin.version}</version>
                <configuration>
                    <configLocation>../config/checkstyle/checkstyle.xml</configLocation>
                    <consoleOutput>true</consoleOutput>
                    <failsOnError>true</failsOnError>
                    <includeTestSourceRoots>true</includeTestSourceRoots>
                </configuration>
                <executions>
                    <execution>
                        <id>validate</id>
                        <phase>validate</phase>
                        <goals>
                            <goal>check</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- SpotBugs -->
            <plugin>
                <groupId>com.github.spotbugs</groupId>
                <artifactId>spotbugs-maven-plugin</artifactId>
                <version>${spotbugs-maven-plugin.version}</version>
                <executions>
                    <execution>
                        <phase>verify</phase>
                        <goals>
                            <goal>check</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- JaCoCo -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>${jacoco-maven-plugin.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>verify</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Surefire -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <includes>
                        <include>**/*Test.java</include>
                    </includes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**Step 3: Create MindTrackApplication.java**

```java
package com.mindtrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MindTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(MindTrackApplication.class, args);
    }
}
```

**Step 4: Create application.yml (shared config)**

```yaml
spring:
  application:
    name: mindtrack
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

**Step 5: Create application-local.yml (H2 for local dev)**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:mindtrack;DB_CLOSE_DELAY=-1;MODE=MYSQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
  flyway:
    enabled: false
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: local-dev-client-id
            client-secret: local-dev-client-secret

logging:
  level:
    com.mindtrack: DEBUG
    org.hibernate.SQL: DEBUG
```

**Step 6: Create application-prod.yml**

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}

logging:
  level:
    com.mindtrack: INFO
```

**Step 7: Create initial Flyway migration**

Create `backend/src/main/resources/db/migration/V1__initial_schema.sql`:

```sql
-- Users and Auth
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    UNIQUE (resource, action)
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    google_id VARCHAR(255) UNIQUE,
    role_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    display_name VARCHAR(255),
    avatar_url VARCHAR(500),
    timezone VARCHAR(50) DEFAULT 'UTC',
    notification_prefs JSON,
    telegram_chat_id VARCHAR(100),
    whatsapp_number VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Interviews
CREATE TABLE interviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    interview_date DATE NOT NULL,
    mood_before INT,
    mood_after INT,
    topics JSON,
    medication_changes TEXT,
    recommendations TEXT,
    notes TEXT,
    transcription_text LONGTEXT,
    audio_s3_key VARCHAR(500),
    audio_expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Activities
CREATE TABLE activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type ENUM('HOMEWORK', 'HABIT', 'CUSTOM') NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    frequency VARCHAR(50),
    linked_interview_id BIGINT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (linked_interview_id) REFERENCES interviews(id)
);

CREATE TABLE activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    log_date DATE NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    notes TEXT,
    mood_rating INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (activity_id) REFERENCES activities(id)
);

-- Journal
CREATE TABLE journal_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    entry_date DATE NOT NULL,
    title VARCHAR(255),
    content LONGTEXT,
    mood INT,
    tags JSON,
    shared_with_therapist BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Goals
CREATE TABLE goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    target_date DATE,
    status ENUM('ACTIVE', 'COMPLETED', 'ABANDONED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE milestones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    target_date DATE,
    completed_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goal_id) REFERENCES goals(id)
);

-- AI Conversations
CREATE TABLE conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    channel ENUM('WEB', 'TELEGRAM', 'WHATSAPP') NOT NULL,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role ENUM('USER', 'AI') NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id)
);

-- Analytics
CREATE TABLE growth_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    metric_date DATE NOT NULL,
    category VARCHAR(100) NOT NULL,
    value DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Seed default roles
INSERT INTO roles (name) VALUES ('ADMIN'), ('USER'), ('THERAPIST');

-- Seed default permissions
INSERT INTO permissions (resource, action) VALUES
    ('users', 'READ'), ('users', 'WRITE'), ('users', 'DELETE'),
    ('interviews', 'READ'), ('interviews', 'WRITE'), ('interviews', 'DELETE'),
    ('activities', 'READ'), ('activities', 'WRITE'), ('activities', 'DELETE'),
    ('journal', 'READ'), ('journal', 'WRITE'), ('journal', 'DELETE'),
    ('goals', 'READ'), ('goals', 'WRITE'), ('goals', 'DELETE'),
    ('conversations', 'READ'), ('conversations', 'WRITE'),
    ('analytics', 'READ'),
    ('admin', 'READ'), ('admin', 'WRITE');

-- ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
    SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ADMIN';

-- USER gets own-data permissions (enforced at service layer)
INSERT INTO role_permissions (role_id, permission_id)
    SELECT r.id, p.id FROM roles r, permissions p
    WHERE r.name = 'USER' AND p.resource NOT IN ('admin', 'users');

-- THERAPIST gets read-only on patient data
INSERT INTO role_permissions (role_id, permission_id)
    SELECT r.id, p.id FROM roles r, permissions p
    WHERE r.name = 'THERAPIST' AND p.action = 'READ' AND p.resource NOT IN ('admin', 'users', 'conversations');
```

**Step 8: Create a basic test**

Create `backend/src/test/java/com/mindtrack/MindTrackApplicationTest.java`:

```java
package com.mindtrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class MindTrackApplicationTest {

    @Test
    void contextLoads() {
    }
}
```

**Step 9: Verify it compiles**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

**Step 10: Commit**

```bash
git add backend/
git commit -m "feat: scaffold Spring Boot backend with Maven, modules, and initial schema"
```

---

### Task 3: Create Vue.js Frontend Scaffold

**Files:**
- Create: `frontend/` (via `npm create vue@latest`)
- Modify: `frontend/package.json` (add dependencies)
- Create: `frontend/.eslintrc.js`
- Create: `frontend/.prettierrc`

**Step 1: Scaffold Vue.js project**

Run:
```bash
cd /Users/lucasrudi/dev/claude-first-test
npm create vue@latest frontend -- --typescript --router --pinia --vitest --eslint-with-prettier
```

Select: TypeScript Yes, JSX No, Router Yes, Pinia Yes, Vitest Yes, E2E Cypress, ESLint+Prettier Yes

**Step 2: Install dependencies**

Run:
```bash
cd frontend && npm install
npm install axios chart.js vue-chartjs @vueuse/core
npm install -D cypress @types/node
```

**Step 3: Create .prettierrc**

```json
{
  "semi": false,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "all",
  "printWidth": 100,
  "bracketSpacing": true,
  "arrowParens": "always",
  "endOfLine": "lf"
}
```

**Step 4: Configure ESLint** — update `.eslintrc.cjs` (or equivalent generated config) to extend recommended Vue 3 + TypeScript rules. Ensure `"plugin:vue/vue3-recommended"` and `"@typescript-eslint/recommended"` are included.

**Step 5: Add proxy to Vite config**

Update `frontend/vite.config.ts`:

```typescript
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

**Step 6: Create basic page structure**

Create stub view files in `frontend/src/views/`:
- `LoginView.vue`
- `DashboardView.vue`
- `InterviewsView.vue`
- `ActivitiesView.vue`
- `JournalView.vue`
- `GoalsView.vue`
- `ChatView.vue`
- `ProfileView.vue`
- `AdminView.vue`
- `TherapistView.vue`

Each is a minimal `<template><div><h1>PageName</h1></div></template>` stub.

**Step 7: Set up Vue Router**

Update `frontend/src/router/index.ts` with routes for all views.

**Step 8: Verify frontend builds**

Run: `cd frontend && npm run build`
Expected: Build succeeds

**Step 9: Run lint**

Run: `npm run lint`
Expected: No errors

**Step 10: Commit**

```bash
git add frontend/
git commit -m "feat: scaffold Vue.js frontend with router, Pinia, and page stubs"
```

---

### Task 4: Checkstyle & IntelliJ Code Style

**Files:**
- Create: `config/checkstyle/checkstyle.xml`
- Create: `.idea/codeStyles/codeStyleConfig.xml`
- Create: `.idea/codeStyles/Project.xml`

**Step 1: Create Checkstyle configuration**

Create `config/checkstyle/checkstyle.xml` — based on Google Java Style with project customizations:
- 4-space indentation
- 120 char line width
- Require Javadoc on public classes/methods (warning, not error)
- Import ordering: static imports last, alphabetical

**Step 2: Create IntelliJ code style**

Create `.idea/codeStyles/codeStyleConfig.xml`:
```xml
<component name="ProjectCodeStyleConfiguration">
  <state>
    <option name="USE_PER_PROJECT_SETTINGS" value="true" />
  </state>
</component>
```

Create `.idea/codeStyles/Project.xml` with Java settings matching Checkstyle:
- 4-space indent
- 120 char line width
- Braces on same line
- Import layout matching Checkstyle rules

**Step 3: Verify Checkstyle runs**

Run: `cd backend && mvn checkstyle:check`
Expected: BUILD SUCCESS (no violations on scaffold code)

**Step 4: Commit**

```bash
git add config/ .idea/
git commit -m "chore: add Checkstyle config and IntelliJ code style conventions"
```

---

### Task 5: SonarQube Configuration

**Files:**
- Create: `sonar-project.properties`

**Step 1: Create SonarQube config**

```properties
sonar.projectKey=mindtrack
sonar.projectName=MindTrack
sonar.organization=mindtrack

# Sources
sonar.sources=backend/src/main/java,frontend/src
sonar.tests=backend/src/test/java,frontend/src
sonar.test.inclusions=**/*Test.java,**/*.spec.ts,**/*.test.ts

# Java
sonar.java.binaries=backend/target/classes
sonar.java.source=21
sonar.java.coveragePlugin=jacoco
sonar.coverage.jacoco.xmlReportPaths=backend/target/site/jacoco/jacoco.xml

# TypeScript/Vue
sonar.typescript.lcov.reportPaths=frontend/coverage/lcov.info

# Exclusions
sonar.exclusions=**/node_modules/**,**/target/**,**/*.config.*,**/db/migration/**
sonar.coverage.exclusions=**/model/**,**/dto/**,**/config/**,**/*Application.java

# Quality Gate
sonar.qualitygate.wait=true
```

**Step 2: Commit**

```bash
git add sonar-project.properties
git commit -m "chore: add SonarCloud configuration"
```

---

### Task 6: Docker Support

**Files:**
- Create: `docker/Dockerfile.backend`
- Create: `docker/Dockerfile.frontend`
- Create: `docker/docker-compose.yml`

**Step 1: Create Dockerfile.backend**

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B
COPY backend/src ./src
COPY config ../config
RUN mvn package -DskipTests -q

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=local"]
```

**Step 2: Create Dockerfile.frontend**

```dockerfile
# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build

# Serve stage
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

**Step 3: Create docker/nginx.conf**

```nginx
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

**Step 4: Create docker-compose.yml**

```yaml
version: '3.8'

services:
  backend:
    build:
      context: ..
      dockerfile: docker/Dockerfile.backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=local
    depends_on:
      - mysql
    networks:
      - mindtrack

  frontend:
    build:
      context: ..
      dockerfile: docker/Dockerfile.frontend
    ports:
      - "3000:80"
    depends_on:
      - backend
    networks:
      - mindtrack

  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: mindtrack
      MYSQL_DATABASE: mindtrack
      MYSQL_USER: mindtrack
      MYSQL_PASSWORD: mindtrack
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - mindtrack

  localstack:
    image: localstack/localstack:3
    ports:
      - "4566:4566"
    environment:
      - SERVICES=s3,secretsmanager
      - DEFAULT_REGION=us-east-1
    networks:
      - mindtrack

volumes:
  mysql_data:

networks:
  mindtrack:
    driver: bridge
```

**Step 5: Commit**

```bash
git add docker/
git commit -m "chore: add Docker support with compose for local development"
```

---

### Task 7: Git Hooks (pre-push)

**Files:**
- Create: `.githooks/pre-push`
- Create: `setup.sh`

**Step 1: Create pre-push hook**

Create `.githooks/pre-push`:

```bash
#!/usr/bin/env bash
set -e

echo "=== Pre-push checks ==="

# Backend tests
if [ -d "backend" ]; then
    echo ">> Running backend tests..."
    cd backend
    mvn test -q
    cd ..
    echo ">> Backend tests passed."
fi

# Frontend lint + tests
if [ -d "frontend" ] && [ -f "frontend/package.json" ]; then
    echo ">> Running frontend lint..."
    cd frontend
    npm run lint
    echo ">> Running frontend tests..."
    npm run test:unit -- --run
    cd ..
    echo ">> Frontend checks passed."
fi

# Terraform checks
if [ -d "infra" ]; then
    echo ">> Running Terraform checks..."
    terraform -chdir=infra fmt -check -recursive
    terraform -chdir=infra validate
    if command -v tflint &> /dev/null; then
        tflint --recursive --chdir=infra
    fi
    if command -v tfsec &> /dev/null; then
        tfsec infra/
    fi
    echo ">> Terraform checks passed."
fi

echo "=== All pre-push checks passed ==="
```

**Step 2: Create setup.sh**

```bash
#!/usr/bin/env bash
set -e

echo "=== MindTrack Project Setup ==="

# Configure git hooks
git config core.hooksPath .githooks
chmod +x .githooks/*
echo ">> Git hooks configured."

# Backend setup
if [ -d "backend" ]; then
    echo ">> Building backend..."
    cd backend
    mvn compile -q
    cd ..
    echo ">> Backend ready."
fi

# Frontend setup
if [ -d "frontend" ]; then
    echo ">> Installing frontend dependencies..."
    cd frontend
    npm ci
    cd ..
    echo ">> Frontend ready."
fi

echo "=== Setup complete ==="
echo "Run 'cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local' to start backend"
echo "Run 'cd frontend && npm run dev' to start frontend"
```

**Step 3: Make scripts executable**

Run: `chmod +x .githooks/pre-push setup.sh`

**Step 4: Commit**

```bash
git add .githooks/ setup.sh
git commit -m "chore: add pre-push git hooks and project setup script"
```

---

### Task 8: GitHub Actions Pipelines

**Files:**
- Create: `.github/workflows/feature.yml`
- Create: `.github/workflows/main.yml`

**Step 1: Create feature branch pipeline**

Create `.github/workflows/feature.yml`:

```yaml
name: Feature Branch CI

on:
  push:
    branches:
      - 'feature/**'
  pull_request:
    branches:
      - main

jobs:
  backend:
    name: Backend Build & Test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Build & Test
        working-directory: backend
        run: mvn verify -B

      - name: Upload coverage
        uses: actions/upload-artifact@v4
        with:
          name: backend-coverage
          path: backend/target/site/jacoco/

  frontend:
    name: Frontend Build & Test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Node 20
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Install dependencies
        working-directory: frontend
        run: npm ci

      - name: Lint
        working-directory: frontend
        run: npm run lint

      - name: Unit tests
        working-directory: frontend
        run: npm run test:unit -- --run --coverage

      - name: Build
        working-directory: frontend
        run: npm run build

      - name: Upload coverage
        uses: actions/upload-artifact@v4
        with:
          name: frontend-coverage
          path: frontend/coverage/

  infra:
    name: Terraform Validate
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Terraform
        uses: hashicorp/setup-terraform@v3
        with:
          terraform_version: 1.7.0

      - name: Terraform Format Check
        working-directory: infra
        run: terraform fmt -check -recursive

      - name: Terraform Init
        working-directory: infra
        run: terraform init -backend=false

      - name: Terraform Validate
        working-directory: infra
        run: terraform validate

      - name: Setup tflint
        uses: terraform-linters/setup-tflint@v4

      - name: Run tflint
        working-directory: infra
        run: tflint --recursive

      - name: Run tfsec
        uses: aquasecurity/tfsec-action@v1.0.3
        with:
          working_directory: infra

  sonar:
    name: SonarCloud Analysis
    runs-on: ubuntu-latest
    needs: [backend, frontend]
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Download backend coverage
        uses: actions/download-artifact@v4
        with:
          name: backend-coverage
          path: backend/target/site/jacoco/

      - name: Download frontend coverage
        uses: actions/download-artifact@v4
        with:
          name: frontend-coverage
          path: frontend/coverage/

      - name: SonarCloud Scan
        uses: SonarSource/sonarcloud-github-action@v2
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

**Step 2: Create main branch pipeline**

Create `.github/workflows/main.yml`:

```yaml
name: Main Branch CI/CD

on:
  push:
    branches:
      - main

jobs:
  backend:
    name: Backend Build & Test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Build & Test
        working-directory: backend
        run: mvn verify -B

      - name: Build deployment package
        working-directory: backend
        run: mvn package -DskipTests -q

      - name: Upload artifact
        uses: actions/upload-artifact@v4
        with:
          name: backend-jar
          path: backend/target/*.jar

  frontend:
    name: Frontend Build & Test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Node 20
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Install dependencies
        working-directory: frontend
        run: npm ci

      - name: Lint
        working-directory: frontend
        run: npm run lint

      - name: Unit tests
        working-directory: frontend
        run: npm run test:unit -- --run --coverage

      - name: Build
        working-directory: frontend
        run: npm run build

      - name: Upload build
        uses: actions/upload-artifact@v4
        with:
          name: frontend-dist
          path: frontend/dist/

  infra-validate:
    name: Terraform Validate
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Terraform
        uses: hashicorp/setup-terraform@v3

      - name: Terraform Format Check
        working-directory: infra
        run: terraform fmt -check -recursive

      - name: Terraform Init
        working-directory: infra
        run: terraform init -backend=false

      - name: Terraform Validate
        working-directory: infra
        run: terraform validate

      - name: Setup tflint
        uses: terraform-linters/setup-tflint@v4

      - name: Run tflint
        working-directory: infra
        run: tflint --recursive

      - name: Run tfsec
        uses: aquasecurity/tfsec-action@v1.0.3
        with:
          working_directory: infra

  deploy:
    name: Deploy
    runs-on: ubuntu-latest
    needs: [backend, frontend, infra-validate]
    environment: production
    steps:
      - uses: actions/checkout@v4

      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1

      - name: Setup Terraform
        uses: hashicorp/setup-terraform@v3

      - name: Terraform Init
        working-directory: infra
        run: terraform init

      - name: Terraform Plan
        working-directory: infra
        run: terraform plan -var-file=environments/prod.tfvars -out=tfplan

      - name: Terraform Apply
        working-directory: infra
        run: terraform apply tfplan

      - name: Download frontend build
        uses: actions/download-artifact@v4
        with:
          name: frontend-dist
          path: frontend/dist/

      - name: Deploy frontend to S3
        run: aws s3 sync frontend/dist/ s3://${{ vars.FRONTEND_BUCKET }}/ --delete

      - name: Invalidate CloudFront
        run: |
          aws cloudfront create-invalidation \
            --distribution-id ${{ vars.CLOUDFRONT_DISTRIBUTION_ID }} \
            --paths "/*"
```

**Step 3: Commit**

```bash
git add .github/
git commit -m "ci: add GitHub Actions pipelines for feature branches and main"
```

---

## Phase 2: Terraform Infrastructure

### Task 9: Terraform Base Configuration

**Files:**
- Create: `infra/main.tf`
- Create: `infra/variables.tf`
- Create: `infra/outputs.tf`
- Create: `infra/providers.tf`
- Create: `infra/environments/dev.tfvars`
- Create: `infra/environments/prod.tfvars`
- Create: `infra/.tflint.hcl`

**Step 1: Create providers.tf**

```hcl
terraform {
  required_version = ">= 1.7.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "mindtrack-terraform-state"
    key            = "terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "mindtrack-terraform-locks"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "mindtrack"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}
```

**Step 2: Create variables.tf**

```hcl
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "db_min_capacity" {
  description = "Aurora Serverless v2 minimum ACU"
  type        = number
  default     = 0.5
}

variable "db_max_capacity" {
  description = "Aurora Serverless v2 maximum ACU"
  type        = number
  default     = 2
}

variable "lambda_memory_size" {
  description = "Lambda function memory in MB"
  type        = number
  default     = 1024
}

variable "domain_name" {
  description = "Domain name for the application"
  type        = string
  default     = ""
}
```

**Step 3: Create main.tf** with module calls for all infra components.

**Step 4: Create environment tfvars files.**

**Step 5: Create .tflint.hcl**

```hcl
config {
  module = true
}

plugin "aws" {
  enabled = true
  version = "0.31.0"
  source  = "github.com/terraform-linters/tflint-ruleset-aws"
}

rule "terraform_naming_convention" {
  enabled = true
}

rule "terraform_documented_variables" {
  enabled = true
}
```

**Step 6: Run terraform fmt and validate**

Run: `cd infra && terraform fmt -recursive && terraform validate`
Expected: Success

**Step 7: Commit**

```bash
git add infra/
git commit -m "infra: add Terraform base configuration with modules"
```

---

### Task 10: Terraform Modules (IAM, S3, RDS, Lambda, API Gateway, CloudFront, EventBridge, Secrets)

Create each module in `infra/modules/<name>/` with `main.tf`, `variables.tf`, `outputs.tf`.

**Modules to create:**
1. `iam/` — Lambda execution role, S3 access, RDS access, Secrets Manager access
2. `s3/` — Audio bucket with 7-day lifecycle, frontend bucket
3. `rds/` — Aurora Serverless v2 MySQL cluster
4. `lambda/` — Spring Boot Lambda with SnapStart, VPC config for RDS access
5. `api-gateway/` — HTTP API with Lambda integration
6. `cloudfront/` — Distribution for frontend S3 bucket
7. `eventbridge/` — Scheduled rules for AI check-ins
8. `secrets/` — Secrets Manager entries for API keys

Each module is a separate step with its own commit.

---

### Task 11: Terraform Tests

**Files:**
- Create: `infra/tests/unit/validate.sh`
- Create: `infra/tests/integration/` (Terratest Go files)

**Step 1: Create unit test script**

```bash
#!/usr/bin/env bash
set -e
echo "=== Terraform Unit Tests ==="
terraform -chdir=infra fmt -check -recursive
terraform -chdir=infra init -backend=false
terraform -chdir=infra validate
tflint --recursive --chdir=infra
tfsec infra/
echo "=== All Terraform unit tests passed ==="
```

**Step 2: Create Terratest integration test**

Create `infra/tests/integration/main_test.go` with basic infrastructure validation tests.

**Step 3: Commit**

```bash
git add infra/tests/
git commit -m "test: add Terraform unit and integration test scaffolding"
```

---

## Phase 3: Backend Domain Implementation

### Task 12: Common Module — Base Entities & Config

**Files:**
- Create: `backend/src/main/java/com/mindtrack/common/model/BaseEntity.java`
- Create: `backend/src/main/java/com/mindtrack/common/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/mindtrack/common/config/CorsConfig.java`
- Test: `backend/src/test/java/com/mindtrack/common/`

TDD: Write test for BaseEntity → implement → verify.

### Task 13: Auth Module — User, Role, JWT, Google OAuth

TDD for each:
- User entity + repository + tests
- Role/Permission entities + repository + tests
- JWT token service + tests
- Google OAuth2 handler + tests
- Auth controller (login, token refresh) + tests

### Task 14: Interview Module

TDD for each:
- Interview entity + repository + tests
- Interview service (CRUD) + tests
- Interview controller + MockMvc tests
- Audio upload endpoint (S3 integration) + tests

### Task 15: Activity Module

TDD for each:
- Activity + ActivityLog entities + repositories + tests
- Activity service (CRUD + logging) + tests
- Activity controller + tests

### Task 16: Journal Module

TDD: JournalEntry entity → repository → service → controller, each with tests.

### Task 17: Goals Module

TDD: Goal + Milestone entities → repositories → services → controllers, each with tests.

### Task 18: AI Module (Claude API Integration)

- Claude API client service + tests (mocked HTTP)
- Conversation/Message entities + repositories + tests
- Chat controller (WebSocket or SSE for streaming) + tests
- Context builder (pulls user's recent data for AI context) + tests

### Task 19: Messaging Module (Telegram + WhatsApp)

- Telegram Bot service + tests
- WhatsApp Business API client + tests
- Scheduled check-in service (EventBridge trigger handler) + tests
- Message routing (channel-agnostic interface) + tests

### Task 20: Analytics Module

- GrowthMetric entity + repository + tests
- Metrics computation service (daily aggregation) + tests
- Analytics controller (dashboard data endpoints) + tests

### Task 21: Admin Module

- User management service + tests
- RBAC configuration service + tests
- Admin controller + tests

---

## Phase 4: Frontend Implementation

### Task 22: Frontend Core Setup

- API client (Axios instance with JWT interceptor)
- Auth store (Pinia) — login state, token management
- Router guards (auth required, role-based access)
- Layout components (AppHeader, AppSidebar, AppFooter)
- Unit tests for stores and composables

### Task 23-31: Page Implementations

One task per page (Login, Dashboard, Interviews, Activities, Journal, Goals, Chat, Profile, Admin, Therapist), each with:
- Component implementation
- Pinia store for data
- Unit tests (Vitest + Vue Test Utils)
- Integration with backend API

---

## Phase 5: Documentation & Backlog

### Task 32: Backlog.md Setup

**Files:**
- Create: `backlog/config.yml`
- Create: 13 task files in `backlog/tasks/`
- Create: `backlog/docs/`
- Create: `backlog/decisions/`

**Step 1: Create config.yml**

```yaml
project:
  name: MindTrack
  description: Personal mental health tracking application

board:
  columns:
    - To Do
    - In Progress
    - Review
    - Done

labels:
  - backend
  - frontend
  - infra
  - ai
  - messaging
  - auth
  - testing

priorities:
  - critical
  - high
  - medium
  - low
```

**Step 2: Create task files** (one per feature from design doc)

Format: `task-<N> - <Title>.md` with metadata, description, acceptance criteria, and implementation plan sections.

Tasks:
1. `task-1 - User authentication with Google OAuth2.md`
2. `task-2 - Admin panel with RBAC.md`
3. `task-3 - Interview logging with structured notes.md`
4. `task-4 - Audio upload and transcription.md`
5. `task-5 - Activity tracking.md`
6. `task-6 - Journal entries.md`
7. `task-7 - Goals and milestones.md`
8. `task-8 - AI chat with Claude API.md`
9. `task-9 - Telegram bot integration.md`
10. `task-10 - WhatsApp Business API integration.md`
11. `task-11 - Analytics dashboard.md`
12. `task-12 - Therapist read-only view.md`
13. `task-13 - User profile configuration.md`

**Step 3: Create architecture decision record**

Create `backlog/decisions/decision-1 - Modular monolith architecture.md`

**Step 4: Commit**

```bash
git add backlog/
git commit -m "docs: add Backlog.md project backlog with initial tasks"
```

---

### Task 33: Project Documentation

**Files:**
- Create: `README.md`
- Create: `CONTRIBUTING.md`
- Create: `CLAUDE.md`

**Step 1: Create README.md**

Sections: Project overview, architecture diagram (ASCII), tech stack, prerequisites, quick start (setup.sh), Docker development, project structure, API documentation reference, testing, deployment, contributing link.

**Step 2: Create CONTRIBUTING.md**

Sections: Getting started, branch naming convention (`feature/`, `bugfix/`, `hotfix/`), commit message format (conventional commits), PR process, code review checklist, code style (link to Checkstyle + ESLint configs), testing requirements (all tests must pass, coverage thresholds).

**Step 3: Create CLAUDE.md**

Sections: Project context (what MindTrack is), tech stack summary, module structure, how to run locally, how to run tests (backend: `mvn verify`, frontend: `npm run test:unit`), code conventions (Checkstyle for Java, ESLint/Prettier for TypeScript), branch strategy, important files and their purposes, database (H2 local, Aurora prod), environment variables.

**Step 4: Commit**

```bash
git add README.md CONTRIBUTING.md CLAUDE.md
git commit -m "docs: add README, CONTRIBUTING, and CLAUDE.md"
```

---

## Summary of All Tasks

| # | Task | Phase |
|---|------|-------|
| 1 | Git init + .gitignore + .editorconfig | 1 |
| 2 | Spring Boot backend scaffold | 1 |
| 3 | Vue.js frontend scaffold | 1 |
| 4 | Checkstyle + IntelliJ code style | 1 |
| 5 | SonarQube configuration | 1 |
| 6 | Docker support | 1 |
| 7 | Git hooks (pre-push) | 1 |
| 8 | GitHub Actions pipelines | 1 |
| 9 | Terraform base config | 2 |
| 10 | Terraform modules | 2 |
| 11 | Terraform tests | 2 |
| 12-21 | Backend domain modules (TDD) | 3 |
| 22-31 | Frontend pages + stores | 4 |
| 32 | Backlog.md setup | 5 |
| 33 | README, CONTRIBUTING, CLAUDE.md | 5 |
