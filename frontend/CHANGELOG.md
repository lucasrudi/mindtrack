# Changelog

## [0.4.0](https://github.com/lucasrudi/mindtrack/compare/frontend-v0.3.0...frontend-v0.4.0) (2026-03-16)


### Features

* Apple Health-style survey with depression, stress, eating habits ([#18](https://github.com/lucasrudi/mindtrack/issues/18)) ([#23](https://github.com/lucasrudi/mindtrack/issues/23)) ([487f116](https://github.com/lucasrudi/mindtrack/commit/487f116cddceb4808bbc17047821e20ab109da19))
* **observability:** add Sentry error tracking and Google Analytics 4 ([#72](https://github.com/lucasrudi/mindtrack/issues/72)) ([ff8aab0](https://github.com/lucasrudi/mindtrack/commit/ff8aab05e96fd6f096039310ac1c849fba65fc37))
* **privacy:** GDPR/CCPA right-to-erasure and data retention ([#166](https://github.com/lucasrudi/mindtrack/issues/166)) ([#174](https://github.com/lucasrudi/mindtrack/issues/174)) ([ffb52ba](https://github.com/lucasrudi/mindtrack/commit/ffb52bacd002de02ff75dbe77edbc35e4f7595ba))
* support dual patient+therapist roles per user ([#17](https://github.com/lucasrudi/mindtrack/issues/17)) ([#24](https://github.com/lucasrudi/mindtrack/issues/24)) ([dca9005](https://github.com/lucasrudi/mindtrack/commit/dca9005a450ee3ef88078e59863e216436f5b972))


### Bug Fixes

* complete Sentry configuration ([#134](https://github.com/lucasrudi/mindtrack/issues/134)) ([#135](https://github.com/lucasrudi/mindtrack/issues/135)) ([7e5b730](https://github.com/lucasrudi/mindtrack/commit/7e5b730d72319b60c6c47b11407d03002a7535e1))
* **deps:** update dependency @vueuse/core to v14 ([#156](https://github.com/lucasrudi/mindtrack/issues/156)) ([d596a2f](https://github.com/lucasrudi/mindtrack/commit/d596a2fcf1c699a2b50369514794a02d29ac3a94))
* **frontend:** pin yauzl to 3.2.1 ([#158](https://github.com/lucasrudi/mindtrack/issues/158)) ([69f7b58](https://github.com/lucasrudi/mindtrack/commit/69f7b58421f12c589d75fd6645d2771fea9526e4)), closes [#157](https://github.com/lucasrudi/mindtrack/issues/157)
* **security:** remove localStorage token storage (M-13) ([#104](https://github.com/lucasrudi/mindtrack/issues/104)) ([ca930e1](https://github.com/lucasrudi/mindtrack/commit/ca930e1ffdf7e6bc68242c1cbbabf1513e794ed4))
* **security:** resolve all 61 SonarQube issues ([#118](https://github.com/lucasrudi/mindtrack/issues/118)) ([e5e92f4](https://github.com/lucasrudi/mindtrack/commit/e5e92f40e21069626e8849a1023bd8cb76215446))
* **security:** resolve remaining 8 SonarCloud issues ([#119](https://github.com/lucasrudi/mindtrack/issues/119)) ([9cacb73](https://github.com/lucasrudi/mindtrack/commit/9cacb7355dad67e5116d16f365a803447ed9154d)), closes [#85](https://github.com/lucasrudi/mindtrack/issues/85)


### Miscellaneous

* **deps:** update dependency @sentry/vue to v10.43.0 ([#146](https://github.com/lucasrudi/mindtrack/issues/146)) ([4465113](https://github.com/lucasrudi/mindtrack/commit/4465113df95d3ca0d6711bfdd7db1daff98d5a2f))
* **deps:** update vitest monorepo to v4.1.0 ([#151](https://github.com/lucasrudi/mindtrack/issues/151)) ([c01c3e7](https://github.com/lucasrudi/mindtrack/commit/c01c3e75c7bf406623cd64caff8a3f363f8f2d1f))
* **frontend:** bump esbuild, @vitest/coverage-v8 and vitest ([3180d18](https://github.com/lucasrudi/mindtrack/commit/3180d188e796f7abf87e2e48ce41e6d4543e0b66))
* **frontend:** bump esbuild, @vitest/coverage-v8 and vitest in /frontend ([#53](https://github.com/lucasrudi/mindtrack/issues/53)) ([3180d18](https://github.com/lucasrudi/mindtrack/commit/3180d188e796f7abf87e2e48ce41e6d4543e0b66))
* **frontend:** bump minimatch and editorconfig in /frontend ([#65](https://github.com/lucasrudi/mindtrack/issues/65)) ([b9ebb6b](https://github.com/lucasrudi/mindtrack/commit/b9ebb6b09eb35a21d9db532089decce9d8c906ce))
* **frontend:** bump minimatch from 3.1.2 to 3.1.5 in /frontend ([#51](https://github.com/lucasrudi/mindtrack/issues/51)) ([da52eaf](https://github.com/lucasrudi/mindtrack/commit/da52eaf46f2bea67d42a56dd15309b6c15158688))
* **frontend:** bump rollup from 4.57.1 to 4.59.0 in /frontend ([#52](https://github.com/lucasrudi/mindtrack/issues/52)) ([da22359](https://github.com/lucasrudi/mindtrack/commit/da223594d4ce81e88d4f284c95e14b8b4f37f2db))
* **frontend:** bump the frontend-dependencies group ([1b70ed7](https://github.com/lucasrudi/mindtrack/commit/1b70ed75f1d8aab493fa407d89ad3f391f000249))
* **frontend:** bump the frontend-dependencies group in /frontend with 3 updates ([#112](https://github.com/lucasrudi/mindtrack/issues/112)) ([964a8a9](https://github.com/lucasrudi/mindtrack/commit/964a8a9a26c27c11ec9427d3096168fafc604640))
* **frontend:** bump the frontend-dependencies group in /frontend with 6 updates ([#48](https://github.com/lucasrudi/mindtrack/issues/48)) ([1b70ed7](https://github.com/lucasrudi/mindtrack/commit/1b70ed75f1d8aab493fa407d89ad3f391f000249))

## [0.3.0](https://github.com/lucasrudi/mindtrack/compare/frontend-v0.2.0...frontend-v0.3.0) (2026-02-28)


### Features

* **activity:** implement activity tracking with daily checklist ([eac2de0](https://github.com/lucasrudi/mindtrack/commit/eac2de0efd5863a766f0b2b3854a1bc9b1988163))
* add landing page and Google OAuth authentication ([1a32aab](https://github.com/lucasrudi/mindtrack/commit/1a32aabfae6a0dd8dafab8b01d1d6a48251e8996))
* **admin:** implement admin panel with RBAC ([4f241e8](https://github.com/lucasrudi/mindtrack/commit/4f241e8a39149bc6be2d1a8c8b91885a3ee61761))
* **ai:** implement AI chat frontend with conversation management ([cbedbfa](https://github.com/lucasrudi/mindtrack/commit/cbedbfa4472d2d19a99e02be4ac19b126765a702))
* **analytics:** implement analytics dashboard with mood trends, activity stats, and goal progress ([6b1ac9e](https://github.com/lucasrudi/mindtrack/commit/6b1ac9e9ef4c27c33396a8ae0310d9416e320cca))
* **ci,frontend:** make Snyk blocking, add product icon, update Definition of Done ([325f8ee](https://github.com/lucasrudi/mindtrack/commit/325f8ee7ad4c909ad2f22139a889ea38c38a3797))
* **goals:** implement goals and milestones with progress tracking ([db2d654](https://github.com/lucasrudi/mindtrack/commit/db2d654c3c523dca36a1d0874a6ba3134571aa45))
* **interview:** add audio upload with local storage and transcription placeholder ([71ab481](https://github.com/lucasrudi/mindtrack/commit/71ab481681810327fcce0b7dbf7d428cef0e8559))
* **interview:** implement interview logging with structured notes ([a11a054](https://github.com/lucasrudi/mindtrack/commit/a11a0547331ad9a204307d1f0b8e3591c8a6fe50))
* **journal:** implement journal entries with mood tracking and tagging ([c556635](https://github.com/lucasrudi/mindtrack/commit/c556635920961999dff7e132f2d25b15a305af63))
* scaffold Spring Boot backend, Vue.js frontend, and code style configs ([704d02c](https://github.com/lucasrudi/mindtrack/commit/704d02cd0bd711616c134ae9baf4067f36a7c762))
* **therapist,profile:** implement therapist read-only view and user profile settings ([3f1ef38](https://github.com/lucasrudi/mindtrack/commit/3f1ef389c056237e6a201bbc56cde6b35a1fb61b))
* **tutorial:** add interactive onboarding tutorial for new users ([72b5fb6](https://github.com/lucasrudi/mindtrack/commit/72b5fb6f616adf08c157c30bacc71ba9fd83e7fb))


### Bug Fixes

* **infra:** fix GitHub Config Sync build failures ([#4](https://github.com/lucasrudi/mindtrack/issues/4)) ([a41ce96](https://github.com/lucasrudi/mindtrack/commit/a41ce962c40651974075849311bdbdd08917eb8b))


### Miscellaneous

* release ([f1d3bbf](https://github.com/lucasrudi/mindtrack/commit/f1d3bbf0f1c7a155daa116070cec9abc2bf40caf))


### CI/CD

* add branch naming enforcement and automated code review ([#1](https://github.com/lucasrudi/mindtrack/issues/1)) ([c891759](https://github.com/lucasrudi/mindtrack/commit/c8917596da414f13098d8297528aeb97bdc44f2e))

## [0.2.0](https://github.com/lucasrudi/mindtrack/compare/frontend-v0.1.0...frontend-v0.2.0) (2026-02-28)


### Features

* **activity:** implement activity tracking with daily checklist ([eac2de0](https://github.com/lucasrudi/mindtrack/commit/eac2de0efd5863a766f0b2b3854a1bc9b1988163))
* add landing page and Google OAuth authentication ([1a32aab](https://github.com/lucasrudi/mindtrack/commit/1a32aabfae6a0dd8dafab8b01d1d6a48251e8996))
* **admin:** implement admin panel with RBAC ([4f241e8](https://github.com/lucasrudi/mindtrack/commit/4f241e8a39149bc6be2d1a8c8b91885a3ee61761))
* **ai:** implement AI chat frontend with conversation management ([cbedbfa](https://github.com/lucasrudi/mindtrack/commit/cbedbfa4472d2d19a99e02be4ac19b126765a702))
* **analytics:** implement analytics dashboard with mood trends, activity stats, and goal progress ([6b1ac9e](https://github.com/lucasrudi/mindtrack/commit/6b1ac9e9ef4c27c33396a8ae0310d9416e320cca))
* **ci,frontend:** make Snyk blocking, add product icon, update Definition of Done ([325f8ee](https://github.com/lucasrudi/mindtrack/commit/325f8ee7ad4c909ad2f22139a889ea38c38a3797))
* **goals:** implement goals and milestones with progress tracking ([db2d654](https://github.com/lucasrudi/mindtrack/commit/db2d654c3c523dca36a1d0874a6ba3134571aa45))
* **interview:** add audio upload with local storage and transcription placeholder ([71ab481](https://github.com/lucasrudi/mindtrack/commit/71ab481681810327fcce0b7dbf7d428cef0e8559))
* **interview:** implement interview logging with structured notes ([a11a054](https://github.com/lucasrudi/mindtrack/commit/a11a0547331ad9a204307d1f0b8e3591c8a6fe50))
* **journal:** implement journal entries with mood tracking and tagging ([c556635](https://github.com/lucasrudi/mindtrack/commit/c556635920961999dff7e132f2d25b15a305af63))
* scaffold Spring Boot backend, Vue.js frontend, and code style configs ([704d02c](https://github.com/lucasrudi/mindtrack/commit/704d02cd0bd711616c134ae9baf4067f36a7c762))
* **therapist,profile:** implement therapist read-only view and user profile settings ([3f1ef38](https://github.com/lucasrudi/mindtrack/commit/3f1ef389c056237e6a201bbc56cde6b35a1fb61b))
* **tutorial:** add interactive onboarding tutorial for new users ([72b5fb6](https://github.com/lucasrudi/mindtrack/commit/72b5fb6f616adf08c157c30bacc71ba9fd83e7fb))


### Bug Fixes

* **infra:** fix GitHub Config Sync build failures ([#4](https://github.com/lucasrudi/mindtrack/issues/4)) ([a41ce96](https://github.com/lucasrudi/mindtrack/commit/a41ce962c40651974075849311bdbdd08917eb8b))


### CI/CD

* add branch naming enforcement and automated code review ([#1](https://github.com/lucasrudi/mindtrack/issues/1)) ([c891759](https://github.com/lucasrudi/mindtrack/commit/c8917596da414f13098d8297528aeb97bdc44f2e))
