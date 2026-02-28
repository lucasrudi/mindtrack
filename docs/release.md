# Release & Versioning

This project uses [release-please](https://github.com/googleapis/release-please) with [Conventional Commits](https://www.conventionalcommits.org/) for automatic versioning.

- `feat:` commits → minor version bump (0.1.0 → 0.2.0)
- `fix:` commits → patch version bump (0.1.0 → 0.1.1)
- `feat!:` or `BREAKING CHANGE:` → major version bump (0.1.0 → 1.0.0)

Backend, frontend, and infrastructure are versioned independently. When you push to `main`, release-please creates a release PR. Merging it publishes GitHub Releases and triggers deploys for changed components only.

Config: `release-please-config.json`, `.release-please-manifest.json`.
