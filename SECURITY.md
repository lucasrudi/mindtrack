# Security Policy

## Supported Versions

Only the latest release of MindTrack receives security updates.

| Version | Supported |
| ------- | --------- |
| Latest  | ✅        |
| Older   | ❌        |

## Reporting a Vulnerability

MindTrack uses **GitHub private vulnerability reporting**.

1. Navigate to the [Security Advisories](https://github.com/lucasrudi/mindtrack/security/advisories/new) page
2. Click **"Report a vulnerability"**
3. Fill in the type, affected versions, impact, and reproduction steps

Reports are kept private until a fix is released. You can expect:

- **Acknowledgment within 48 hours**
- **Status update within 7 days**
- **CVE assignment** for confirmed, exploitable vulnerabilities

Please **do not** open a public GitHub issue for security vulnerabilities.

## Security Features

| Feature | Status |
| ------- | ------ |
| Dependabot security updates | ✅ Enabled — automatic PRs for vulnerable dependencies |
| Dependabot alerts | ✅ Enabled — alerts on vulnerable dependencies |
| Secret scanning | ✅ Enabled — detects accidentally committed secrets |
| Secret push protection | ✅ Enabled — blocks pushes containing secrets |
| CodeQL scanning | ✅ Enabled — static analysis for Java and JavaScript/TypeScript |
| Dependency review | ✅ Enabled — blocks PRs introducing vulnerable dependencies |

## Scope

In-scope security issues:

- Authentication and authorization bypasses
- Injection vulnerabilities (SQL, XSS, LDAP, SSTI, command injection)
- Insecure direct object references
- Sensitive data exposure (mental health records, PII)
- SSRF, XXE, and unsafe deserialization
- Dependency vulnerabilities rated HIGH or CRITICAL

## Out of Scope

- Issues requiring physical access to a device
- Third-party dependency vulnerabilities with no upstream fix
- Rate limiting without demonstrated impact
- Self-XSS
- Issues only affecting outdated, unsupported versions
