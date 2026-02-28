---
id: 26
title: Generate code documentation site and publish on release
status: To Do
priority: medium
labels:
  - docs
  - ci
  - backend
  - frontend
created: 2026-02-28 00:00
type: feature
dependencies: []
---

## Description

Generate API and code documentation from source (Javadoc for backend, TypeDoc or VitePress for frontend) and publish it automatically on every GitHub Release. The documentation site should be hosted on GitHub Pages or S3/CloudFront alongside the frontend.

## Plan

1. Configure Javadoc generation in `backend/pom.xml` (`maven-javadoc-plugin`)
2. Add TypeDoc or VitePress config for frontend (`frontend/`) to generate docs from JSDoc/TSDoc comments
3. Add a `docs` job to `.github/workflows/deploy.yml` triggered on GitHub Release published
4. Build and publish backend Javadoc and frontend docs to GitHub Pages (or S3 bucket)
5. Add documentation site URL to README.md and release notes
6. Update `docs/release.md` with documentation publishing steps

## Acceptance Criteria

- [ ] `mvn javadoc:javadoc` generates backend API docs without errors
- [ ] Frontend doc generation command configured and working
- [ ] Documentation site published automatically on GitHub Release
- [ ] Documentation URL accessible publicly
- [ ] Link to docs site added to README.md
- [ ] Deploy workflow updated and tested
