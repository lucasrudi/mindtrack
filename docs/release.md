# Release & Versioning

This project uses [release-please](https://github.com/googleapis/release-please) with [Conventional Commits](https://www.conventionalcommits.org/) for automatic versioning.

- `feat:` commits → minor version bump (0.1.0 → 0.2.0)
- `fix:` commits → patch version bump (0.1.0 → 0.1.1)
- `feat!:` or `BREAKING CHANGE:` → major version bump (0.1.0 → 1.0.0)

**Commit/PR title format:** `type(scope): description` — no issue number prefix. Link issues in the PR body with `Closes #N` on its own line. release-please uses the commit title to detect version bumps and build changelogs; a `#N` prefix breaks parsing.

Backend, frontend, and infrastructure are versioned independently. Each component gets its own release PR (`separate-pull-requests: true`). Merging a release PR publishes the GitHub Release for that component and triggers its deploy.

Config: `release-please-config.json`, `.release-please-manifest.json`.

## Documentation Publishing

On every backend release, the deploy workflow automatically generates Javadoc and publishes it to GitHub Pages at `https://lucasrudi.github.io/claude-first-test/`. No manual step is needed — merging the release-please PR triggers the release, which triggers the deploy.
