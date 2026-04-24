# Changelog

## [0.4.7](https://github.com/lucasrudi/mindtrack/compare/infra-v0.4.6...infra-v0.4.7) (2026-04-24)


### Miscellaneous

* **deps:** update all dependencies ([#513](https://github.com/lucasrudi/mindtrack/issues/513)) ([534992a](https://github.com/lucasrudi/mindtrack/commit/534992ab05183974d38e694891890bed218db177))

## [0.4.6](https://github.com/lucasrudi/mindtrack/compare/infra-v0.4.5...infra-v0.4.6) (2026-04-16)


### Infrastructure

* **github:** update repo settings in Terraform module ([#463](https://github.com/lucasrudi/mindtrack/issues/463)) ([ea1447c](https://github.com/lucasrudi/mindtrack/commit/ea1447c37b9891e998ac03a56bc20d61bdaccd9b))

## [0.4.5](https://github.com/lucasrudi/mindtrack/compare/infra-v0.4.4...infra-v0.4.5) (2026-04-10)


### Bug Fixes

* **security:** refresh current snyk triage ([#439](https://github.com/lucasrudi/mindtrack/issues/439)) ([7fd74f7](https://github.com/lucasrudi/mindtrack/commit/7fd74f70760e99d3135515493403f31780ebf123)), closes [#438](https://github.com/lucasrudi/mindtrack/issues/438)

## [0.4.4](https://github.com/lucasrudi/mindtrack/compare/infra-v0.4.3...infra-v0.4.4) (2026-04-08)


### Bug Fixes

* **infra:** remove unsupported type argument from deployment branch policy ([#435](https://github.com/lucasrudi/mindtrack/issues/435)) ([5a755ac](https://github.com/lucasrudi/mindtrack/commit/5a755ac48a65b4c1d526b7e57e418e7af85412b8)), closes [#432](https://github.com/lucasrudi/mindtrack/issues/432)

## [0.4.3](https://github.com/lucasrudi/mindtrack/compare/infra-v0.4.2...infra-v0.4.3) (2026-04-08)


### Bug Fixes

* **infra:** correct GitHub Pages deploy policy and disable RDS Performance Insights ([#429](https://github.com/lucasrudi/mindtrack/issues/429)) ([2c8d4db](https://github.com/lucasrudi/mindtrack/commit/2c8d4dbbc4fba41cfe6da899f9ee07ea562f459c))

## [0.4.2](https://github.com/lucasrudi/mindtrack/compare/infra-v0.4.1...infra-v0.4.2) (2026-04-08)


### Bug Fixes

* **infra:** resolve RDS version drift, S3 MFA delete, and GitHub Pages deploy policy ([#423](https://github.com/lucasrudi/mindtrack/issues/423)) ([8a3fb91](https://github.com/lucasrudi/mindtrack/commit/8a3fb91c1db14d892356e3e49597b8dcf8a1cac9)), closes [#422](https://github.com/lucasrudi/mindtrack/issues/422)

## [0.4.1](https://github.com/lucasrudi/mindtrack/compare/infra-v0.4.0...infra-v0.4.1) (2026-04-08)


### Bug Fixes

* **common:** relax API rate limits ([#317](https://github.com/lucasrudi/mindtrack/issues/317)) ([0791297](https://github.com/lucasrudi/mindtrack/commit/0791297d8604f2ee3b98160b8dfbb39ee3057e2b)), closes [#316](https://github.com/lucasrudi/mindtrack/issues/316)
* **deploy:** unblock deploy workflows ([#205](https://github.com/lucasrudi/mindtrack/issues/205)) ([4a7dc2b](https://github.com/lucasrudi/mindtrack/commit/4a7dc2b7a8066c6bdfe919ae432a5b6ba7fc9ca2))
* **infra:** avoid unsupported prod updates ([#215](https://github.com/lucasrudi/mindtrack/issues/215)) ([f936120](https://github.com/lucasrudi/mindtrack/commit/f936120446912c360deb7f5b01e0bf8a2e40f9a1)), closes [#214](https://github.com/lucasrudi/mindtrack/issues/214)
* **infra:** ignore deploy-managed lambda sentry dsn ([#218](https://github.com/lucasrudi/mindtrack/issues/218)) ([97ce9dd](https://github.com/lucasrudi/mindtrack/commit/97ce9dd708f783cb5c28f52a4a930661144a45b5)), closes [#217](https://github.com/lucasrudi/mindtrack/issues/217)
* **security:** remediate current snyk findings ([#403](https://github.com/lucasrudi/mindtrack/issues/403)) ([982eb3d](https://github.com/lucasrudi/mindtrack/commit/982eb3d2a1c384c11b2f153b8ef2b6c6f789e8f4)), closes [#402](https://github.com/lucasrudi/mindtrack/issues/402)


### Miscellaneous

* **deps:** update all dependencies ([#414](https://github.com/lucasrudi/mindtrack/issues/414)) ([f258f46](https://github.com/lucasrudi/mindtrack/commit/f258f46555323f64b6495efa7d851562463dae19))
* **deps:** update minor and patch dependencies ([#371](https://github.com/lucasrudi/mindtrack/issues/371)) ([2903cf1](https://github.com/lucasrudi/mindtrack/commit/2903cf1d0e465ffc2af8663c488a9665d5c3d331))
* **frontend:** upgrade ESLint to v10 ([#329](https://github.com/lucasrudi/mindtrack/issues/329)) ([675a6da](https://github.com/lucasrudi/mindtrack/commit/675a6dabb1f856b629644c0fbcbecbd8e0143988)), closes [#318](https://github.com/lucasrudi/mindtrack/issues/318)
* **infra:** upgrade major infrastructure and CI dependencies ([#373](https://github.com/lucasrudi/mindtrack/issues/373)) ([2f3ca9e](https://github.com/lucasrudi/mindtrack/commit/2f3ca9e9daba5e57679986fdc7fc87fa146293d1))


### Infrastructure

* **iam:** expand GitHub Actions role permissions for Terraform plan/apply ([#194](https://github.com/lucasrudi/mindtrack/issues/194)) ([591525a](https://github.com/lucasrudi/mindtrack/commit/591525a34667b4a604b7d2e35fea1648833589c5))
* **rds:** upgrade MySQL from 8.0 to 8.4 LTS before AWS EOL ([#257](https://github.com/lucasrudi/mindtrack/issues/257)) ([25d1f32](https://github.com/lucasrudi/mindtrack/commit/25d1f3253fed29861dc3fd95ae8672b2f4c32bca)), closes [#251](https://github.com/lucasrudi/mindtrack/issues/251)

## [0.4.0](https://github.com/lucasrudi/mindtrack/compare/infra-v0.3.0...infra-v0.4.0) (2026-03-16)


### Features

* **privacy:** GDPR/CCPA right-to-erasure and data retention ([#166](https://github.com/lucasrudi/mindtrack/issues/166)) ([#174](https://github.com/lucasrudi/mindtrack/issues/174)) ([ffb52ba](https://github.com/lucasrudi/mindtrack/commit/ffb52bacd002de02ff75dbe77edbc35e4f7595ba))
* **security:** encrypt Telegram chat ID and WhatsApp number via AWS KMS ([#165](https://github.com/lucasrudi/mindtrack/issues/165)) ([#168](https://github.com/lucasrudi/mindtrack/issues/168)) ([05ae190](https://github.com/lucasrudi/mindtrack/commit/05ae19018126ce283b734241105ab76200af007b))


### Bug Fixes

* **ci:** remove no-op --delete-branch and switch squash to PR_BODY ([#78](https://github.com/lucasrudi/mindtrack/issues/78)) ([0577fac](https://github.com/lucasrudi/mindtrack/commit/0577fac233d9eb29e712db4fe4b772df569678c4)), closes [#76](https://github.com/lucasrudi/mindtrack/issues/76)
* complete Sentry configuration ([#134](https://github.com/lucasrudi/mindtrack/issues/134)) ([#135](https://github.com/lucasrudi/mindtrack/issues/135)) ([7e5b730](https://github.com/lucasrudi/mindtrack/commit/7e5b730d72319b60c6c47b11407d03002a7535e1))
* remediate SonarCloud security hotspots ([#138](https://github.com/lucasrudi/mindtrack/issues/138)) ([#139](https://github.com/lucasrudi/mindtrack/issues/139)) ([bf39ca6](https://github.com/lucasrudi/mindtrack/commit/bf39ca6f383f913938bd6dc544adc3805327d381))
* repair GitHub config sync authentication ([#129](https://github.com/lucasrudi/mindtrack/issues/129)) ([abec45f](https://github.com/lucasrudi/mindtrack/commit/abec45f345ba64864cbf27729fffcfac508b39b7)), closes [#128](https://github.com/lucasrudi/mindtrack/issues/128)
* resolve remaining SonarCloud hotspots ([#140](https://github.com/lucasrudi/mindtrack/issues/140)) ([#141](https://github.com/lucasrudi/mindtrack/issues/141)) ([6cb0b8f](https://github.com/lucasrudi/mindtrack/commit/6cb0b8f653495f9da8a2bf9e77accd8927a3ee85))


### Miscellaneous

* **infra:** update hashicorp/aws requirement from ~&gt; 5.0 to ~&gt; 6.34 ([2bf42c4](https://github.com/lucasrudi/mindtrack/commit/2bf42c4def9b96f4b6168b8b8591550da8e94029))
* **infra:** update hashicorp/aws requirement from ~&gt; 5.0 to ~&gt; 6.34 in /infra in the infra-dependencies group ([#46](https://github.com/lucasrudi/mindtrack/issues/46)) ([2bf42c4](https://github.com/lucasrudi/mindtrack/commit/2bf42c4def9b96f4b6168b8b8591550da8e94029))


### CI/CD

* **security:** pin GitHub Actions to SHAs and switch to OIDC (H-1, H-10, H-11, H-12) ([#106](https://github.com/lucasrudi/mindtrack/issues/106)) ([a1ad93a](https://github.com/lucasrudi/mindtrack/commit/a1ad93a0eba74758812e227f0e313cefda21007a))
* **security:** remove tfsec soft_fail and ESLint/SonarCloud continue-on-error (M-7, M-8) ([#105](https://github.com/lucasrudi/mindtrack/issues/105)) ([45e1622](https://github.com/lucasrudi/mindtrack/commit/45e162298d3e08ba0679e87806e1db6908300b77))


### Infrastructure

* document private vulnerability reporting and default CodeQL setup ([#70](https://github.com/lucasrudi/mindtrack/issues/70)) ([503a32f](https://github.com/lucasrudi/mindtrack/commit/503a32f0460db0e857c52e0d3e83f3abe38de265)), closes [#68](https://github.com/lucasrudi/mindtrack/issues/68)
* **iam:** restrict OIDC trust to main branch and production environment (H-13) ([#108](https://github.com/lucasrudi/mindtrack/issues/108)) ([b9f2ee4](https://github.com/lucasrudi/mindtrack/commit/b9f2ee43f4091ac936d1c3fd8ea4069e6d76a17e))
* **iam:** scope Transcribe IAM resource to mindtrack-* prefix ([#98](https://github.com/lucasrudi/mindtrack/issues/98)) ([3a185fd](https://github.com/lucasrudi/mindtrack/commit/3a185fd8dd6b58127ae7112349784894774bcde2))
* **security:** add CloudWatch security alarms for 401/403 spikes (M-9) ([#148](https://github.com/lucasrudi/mindtrack/issues/148)) ([bf1f6f1](https://github.com/lucasrudi/mindtrack/commit/bf1f6f1b5a67316570d4eb1ef8d7e07702f6643d))
* **security:** add S3 versioning, access logging, and CloudWatch retention (M-14, M-15, M-16) ([#100](https://github.com/lucasrudi/mindtrack/issues/100)) ([404dc76](https://github.com/lucasrudi/mindtrack/commit/404dc7603b4ed5568b2ebbd81e5af056024ebcae))
* **security:** fix critical infrastructure findings (C-3, C-4, C-6, H-7) ([#99](https://github.com/lucasrudi/mindtrack/issues/99)) ([6fbb347](https://github.com/lucasrudi/mindtrack/commit/6fbb3471def2f9944f866a0b64991a76bd7d03a7))
* **security:** migrate Aurora and Lambda to dedicated private VPC (H-4) ([#159](https://github.com/lucasrudi/mindtrack/issues/159)) ([9030b63](https://github.com/lucasrudi/mindtrack/commit/9030b63b39e8692ab0fd755fe61af78b46d78d21))

## [0.3.0](https://github.com/lucasrudi/mindtrack/compare/infra-v0.2.0...infra-v0.3.0) (2026-02-28)


### Features

* add GitHub repo Terraform module and default admin user seed ([4fb8d55](https://github.com/lucasrudi/mindtrack/commit/4fb8d55fbf746ba1366eaf53322c0255c933e6ac))
* add infrastructure, CI/CD, Docker, backlog, and documentation ([8da4aee](https://github.com/lucasrudi/mindtrack/commit/8da4aee88af6957160886351bcd0f86034af94d1))
* **infra,ci:** externalize env config, Terraform GitHub secrets, and SonarCloud quality gates ([b14c15b](https://github.com/lucasrudi/mindtrack/commit/b14c15b023a3746f6fc0a2fc6c5f7d4586be60f5))
* **infra:** add CloudWatch monitoring module with dashboards and alarms ([f87e6e3](https://github.com/lucasrudi/mindtrack/commit/f87e6e3267c67251222d61113748ae338eb37d4f))


### Bug Fixes

* **infra:** fix GitHub Config Sync build failures ([#4](https://github.com/lucasrudi/mindtrack/issues/4)) ([a41ce96](https://github.com/lucasrudi/mindtrack/commit/a41ce962c40651974075849311bdbdd08917eb8b))


### Miscellaneous

* add release-please, Renovate, and Snyk configurations ([a0dc2a3](https://github.com/lucasrudi/mindtrack/commit/a0dc2a33d9414565f676504e2b47e9b099e3bbaa))
* release ([f1d3bbf](https://github.com/lucasrudi/mindtrack/commit/f1d3bbf0f1c7a155daa116070cec9abc2bf40caf))


### CI/CD

* add branch naming enforcement and automated code review ([#1](https://github.com/lucasrudi/mindtrack/issues/1)) ([c891759](https://github.com/lucasrudi/mindtrack/commit/c8917596da414f13098d8297528aeb97bdc44f2e))

## [0.2.0](https://github.com/lucasrudi/mindtrack/compare/infra-v0.1.0...infra-v0.2.0) (2026-02-28)


### Features

* add GitHub repo Terraform module and default admin user seed ([4fb8d55](https://github.com/lucasrudi/mindtrack/commit/4fb8d55fbf746ba1366eaf53322c0255c933e6ac))
* add infrastructure, CI/CD, Docker, backlog, and documentation ([8da4aee](https://github.com/lucasrudi/mindtrack/commit/8da4aee88af6957160886351bcd0f86034af94d1))
* **infra,ci:** externalize env config, Terraform GitHub secrets, and SonarCloud quality gates ([b14c15b](https://github.com/lucasrudi/mindtrack/commit/b14c15b023a3746f6fc0a2fc6c5f7d4586be60f5))
* **infra:** add CloudWatch monitoring module with dashboards and alarms ([f87e6e3](https://github.com/lucasrudi/mindtrack/commit/f87e6e3267c67251222d61113748ae338eb37d4f))


### Bug Fixes

* **infra:** fix GitHub Config Sync build failures ([#4](https://github.com/lucasrudi/mindtrack/issues/4)) ([a41ce96](https://github.com/lucasrudi/mindtrack/commit/a41ce962c40651974075849311bdbdd08917eb8b))


### Miscellaneous

* add release-please, Renovate, and Snyk configurations ([a0dc2a3](https://github.com/lucasrudi/mindtrack/commit/a0dc2a33d9414565f676504e2b47e9b099e3bbaa))


### CI/CD

* add branch naming enforcement and automated code review ([#1](https://github.com/lucasrudi/mindtrack/issues/1)) ([c891759](https://github.com/lucasrudi/mindtrack/commit/c8917596da414f13098d8297528aeb97bdc44f2e))
