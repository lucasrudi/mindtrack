# Changelog

## [0.5.0](https://github.com/lucasrudi/mindtrack/compare/frontend-v0.4.0...frontend-v0.5.0) (2026-04-08)


### Features

* **activity:** add goal linking to activity create and edit ([#280](https://github.com/lucasrudi/mindtrack/issues/280)) ([370cdf5](https://github.com/lucasrudi/mindtrack/commit/370cdf589d83d98310358cd9a58105cc8d22876a)), closes [#272](https://github.com/lucasrudi/mindtrack/issues/272)
* **appointment:** add therapist calendar booking ([#333](https://github.com/lucasrudi/mindtrack/issues/333)) ([73b130a](https://github.com/lucasrudi/mindtrack/commit/73b130a0aa854a534ea721ea5c60f661474e912e)), closes [#322](https://github.com/lucasrudi/mindtrack/issues/322)
* **appointments:** add recurring appointments with duration-based scheduling ([#349](https://github.com/lucasrudi/mindtrack/issues/349)) ([3c08c02](https://github.com/lucasrudi/mindtrack/commit/3c08c02e22b40a5089584ff2a9519d68e6c74617))
* **dashboard:** add embedded YouTube health videos widget ([#288](https://github.com/lucasrudi/mindtrack/issues/288)) ([a28b41f](https://github.com/lucasrudi/mindtrack/commit/a28b41fb3d77b34af74fba113b70df80009aae59))
* **dashboard:** add pending activities checklist widget ([#282](https://github.com/lucasrudi/mindtrack/issues/282)) ([a71e56c](https://github.com/lucasrudi/mindtrack/commit/a71e56cb18f68131c38d72ff645987741cb84737)), closes [#273](https://github.com/lucasrudi/mindtrack/issues/273)
* **dashboard:** add personalized content widgets with tips, resources, and well-being indicators ([#253](https://github.com/lucasrudi/mindtrack/issues/253)) ([ef5965a](https://github.com/lucasrudi/mindtrack/commit/ef5965af95171da8ea8b419853ac8f7a1955cb4e)), closes [#249](https://github.com/lucasrudi/mindtrack/issues/249)
* **dashboard:** align top row cards and normalize spacing ([#307](https://github.com/lucasrudi/mindtrack/issues/307)) ([6be32b0](https://github.com/lucasrudi/mindtrack/commit/6be32b07d3ce64df4e02ad30f706944e77fc12a8))
* **dashboard:** display individual active goals with progress on dashboard ([#254](https://github.com/lucasrudi/mindtrack/issues/254)) ([1f619d1](https://github.com/lucasrudi/mindtrack/commit/1f619d1eb9a633e73168bddba7c15dc1b1c18fe7)), closes [#248](https://github.com/lucasrudi/mindtrack/issues/248)
* **dashboard:** reorder widgets for better UX flow ([#291](https://github.com/lucasrudi/mindtrack/issues/291)) ([0a8a0de](https://github.com/lucasrudi/mindtrack/commit/0a8a0def208b2460b02141d5309598438bafc223)), closes [#275](https://github.com/lucasrudi/mindtrack/issues/275)
* **frontend:** add therapist view toggle ([#330](https://github.com/lucasrudi/mindtrack/issues/330)) ([642ecb2](https://github.com/lucasrudi/mindtrack/commit/642ecb2cba793cdda354611053bc5073f8bf8170)), closes [#319](https://github.com/lucasrudi/mindtrack/issues/319)
* **goals:** add category-based icons to goal cards ([#252](https://github.com/lucasrudi/mindtrack/issues/252)) ([0225495](https://github.com/lucasrudi/mindtrack/commit/0225495ac6801ebd70f59447e29da55460cac87d)), closes [#246](https://github.com/lucasrudi/mindtrack/issues/246)
* **goals:** auto-generate suggested milestones during onboarding ([#258](https://github.com/lucasrudi/mindtrack/issues/258)) ([1f73f22](https://github.com/lucasrudi/mindtrack/commit/1f73f220737e8e95af5dfb8cde55f1dccf4f7a11))
* **goals:** infer started goals ([#335](https://github.com/lucasrudi/mindtrack/issues/335)) ([50739ad](https://github.com/lucasrudi/mindtrack/commit/50739ad417230d09730f785fe4f3f1bb1d679c7e)), closes [#328](https://github.com/lucasrudi/mindtrack/issues/328)
* **mood:** add mood logging endpoint and dashboard widget ([#281](https://github.com/lucasrudi/mindtrack/issues/281)) ([ceea71e](https://github.com/lucasrudi/mindtrack/commit/ceea71ea3c5dabd89f3281dc6303298bf9bed947)), closes [#271](https://github.com/lucasrudi/mindtrack/issues/271)
* **notifications:** build notification delivery infrastructure ([#350](https://github.com/lucasrudi/mindtrack/issues/350)) ([b4515c2](https://github.com/lucasrudi/mindtrack/commit/b4515c2b9ba464465ae292f1ce66532cac8e967f)), closes [#340](https://github.com/lucasrudi/mindtrack/issues/340)
* **therapist:** add patient overview dashboard ([#331](https://github.com/lucasrudi/mindtrack/issues/331)) ([f038dc8](https://github.com/lucasrudi/mindtrack/commit/f038dc8a6680c1c42be848be879e8cb292bffc3c)), closes [#320](https://github.com/lucasrudi/mindtrack/issues/320)
* **therapist:** add patient request flow ([#332](https://github.com/lucasrudi/mindtrack/issues/332)) ([f68a87d](https://github.com/lucasrudi/mindtrack/commit/f68a87d8bcb4aa4dfc5d5f36a509bd1e84ba0021)), closes [#321](https://github.com/lucasrudi/mindtrack/issues/321)
* **therapist:** implement therapist-patient connection flow via invite link ([#363](https://github.com/lucasrudi/mindtrack/issues/363)) ([c74b60a](https://github.com/lucasrudi/mindtrack/commit/c74b60a93677fd522157feb524bda2b3aeafcfdd)), closes [#354](https://github.com/lucasrudi/mindtrack/issues/354)


### Bug Fixes

* **auth:** persist session across page refresh ([#306](https://github.com/lucasrudi/mindtrack/issues/306)) ([be9170c](https://github.com/lucasrudi/mindtrack/commit/be9170c846ec5c72a83e4ebdbdea61a304be1ed9))
* **dashboard:** replace unintuitive pending goals icon with hourglass ([#278](https://github.com/lucasrudi/mindtrack/issues/278)) ([f523e96](https://github.com/lucasrudi/mindtrack/commit/f523e967c3d4f4b692afbdcb362988b9cad36e38)), closes [#269](https://github.com/lucasrudi/mindtrack/issues/269)
* **dashboard:** show goals loading/error state in active goals widget ([#279](https://github.com/lucasrudi/mindtrack/issues/279)) ([4316878](https://github.com/lucasrudi/mindtrack/commit/4316878f8948338e5063797c80d845db7a477880)), closes [#270](https://github.com/lucasrudi/mindtrack/issues/270)
* **dashboard:** use native progress element in active goals widget ([#256](https://github.com/lucasrudi/mindtrack/issues/256)) ([f495432](https://github.com/lucasrudi/mindtrack/commit/f495432976c077b04f7960b1826dbcb26e5ec776)), closes [#255](https://github.com/lucasrudi/mindtrack/issues/255)
* **error-handling:** add comprehensive backend and frontend error handling ([#260](https://github.com/lucasrudi/mindtrack/issues/260)) ([385d867](https://github.com/lucasrudi/mindtrack/commit/385d867b5ff47d4574f1a20eb76d50e92ab0daba)), closes [#250](https://github.com/lucasrudi/mindtrack/issues/250)
* **error-handling:** extract bad request constant and use throw over Promise.reject ([#266](https://github.com/lucasrudi/mindtrack/issues/266)) ([317aeb3](https://github.com/lucasrudi/mindtrack/commit/317aeb3a328141c40e499ff0a5ee5227c2ab1a5b)), closes [#265](https://github.com/lucasrudi/mindtrack/issues/265)
* **frontend:** clarify secure random video selection ([#346](https://github.com/lucasrudi/mindtrack/issues/346)) ([f30b414](https://github.com/lucasrudi/mindtrack/commit/f30b414e47d29a14805f53439dc164851c6081a3)), closes [#345](https://github.com/lucasrudi/mindtrack/issues/345)
* **frontend:** resolve Sonar findings in dashboard auth ([#315](https://github.com/lucasrudi/mindtrack/issues/315)) ([78fcaab](https://github.com/lucasrudi/mindtrack/commit/78fcaab940d23207535dbf9c31af9016aa9eb516)), closes [#314](https://github.com/lucasrudi/mindtrack/issues/314)
* **goals,ai,survey:** fix lazy load crash, AI error handling, and slider labels ([#245](https://github.com/lucasrudi/mindtrack/issues/245)) ([82825ab](https://github.com/lucasrudi/mindtrack/commit/82825ab6e0b73a222e99014a22df56ccb8cec92e))
* **security:** remediate current snyk findings ([#403](https://github.com/lucasrudi/mindtrack/issues/403)) ([982eb3d](https://github.com/lucasrudi/mindtrack/commit/982eb3d2a1c384c11b2f153b8ef2b6c6f789e8f4)), closes [#402](https://github.com/lucasrudi/mindtrack/issues/402)
* **sonar:** resolve backend and frontend quality findings ([#297](https://github.com/lucasrudi/mindtrack/issues/297)) ([859ad11](https://github.com/lucasrudi/mindtrack/commit/859ad111fe73872efe801d3f622f7cb1cc100604)), closes [#296](https://github.com/lucasrudi/mindtrack/issues/296)
* **sonar:** resolve current unresolved findings ([#356](https://github.com/lucasrudi/mindtrack/issues/356)) ([1a0c2f4](https://github.com/lucasrudi/mindtrack/commit/1a0c2f48093c16874d28c5478e55f4875e6059ba)), closes [#355](https://github.com/lucasrudi/mindtrack/issues/355)
* **sonar:** resolve therapist and appointment findings ([#347](https://github.com/lucasrudi/mindtrack/issues/347)) ([c28f0a9](https://github.com/lucasrudi/mindtrack/commit/c28f0a9c935bfbf125ff50ea5922138b1ec346b5)), closes [#342](https://github.com/lucasrudi/mindtrack/issues/342) [#343](https://github.com/lucasrudi/mindtrack/issues/343)
* **survey:** add scale labels to wellness sliders in profile view ([#277](https://github.com/lucasrudi/mindtrack/issues/277)) ([41cd02e](https://github.com/lucasrudi/mindtrack/commit/41cd02ef575bc985fb1c26ea2f02541a2b30b776)), closes [#268](https://github.com/lucasrudi/mindtrack/issues/268)
* **tutorial:** preserve navbar link clicks through overlay ([#311](https://github.com/lucasrudi/mindtrack/issues/311)) ([8ecb649](https://github.com/lucasrudi/mindtrack/commit/8ecb6490fff077f647024d901342bdb423427ef3)), closes [#310](https://github.com/lucasrudi/mindtrack/issues/310)


### Miscellaneous

* **deps:** update all dependencies ([#414](https://github.com/lucasrudi/mindtrack/issues/414)) ([f258f46](https://github.com/lucasrudi/mindtrack/commit/f258f46555323f64b6495efa7d851562463dae19))
* **deps:** update dependency @sentry/vue to v10.45.0 ([#312](https://github.com/lucasrudi/mindtrack/issues/312)) ([45eab76](https://github.com/lucasrudi/mindtrack/commit/45eab76ec5946dad4c1cae8918db1532624f9863))
* **deps:** update dependency @sentry/vue to v10.46.0 ([#386](https://github.com/lucasrudi/mindtrack/issues/386)) ([7e7ea44](https://github.com/lucasrudi/mindtrack/commit/7e7ea448ed0b9912d1d1fbd79ce24bf07ba7efd9))
* **deps:** update dependency @sentry/vue to v10.47.0 ([#401](https://github.com/lucasrudi/mindtrack/issues/401)) ([a6a36ee](https://github.com/lucasrudi/mindtrack/commit/a6a36ee2c3a514be13e6f8d006c8d00584c24ce6))
* **deps:** update dependency eslint to v10.1.0 ([#352](https://github.com/lucasrudi/mindtrack/issues/352)) ([e3f1f50](https://github.com/lucasrudi/mindtrack/commit/e3f1f50075bd2e51be4e39c482820d84a4ee7ef2))
* **deps:** update dependency eslint to v10.2.0 ([#406](https://github.com/lucasrudi/mindtrack/issues/406)) ([6ec3881](https://github.com/lucasrudi/mindtrack/commit/6ec388138e07fcd2e06cf05c3f2e6ba115336da2))
* **deps:** update dependency typescript to v6 ([#378](https://github.com/lucasrudi/mindtrack/issues/378)) ([fa5b969](https://github.com/lucasrudi/mindtrack/commit/fa5b96900bf0da81b862f7aab8f02ba45c4654c0))
* **deps:** update dependency vue to v3.5.31 ([#382](https://github.com/lucasrudi/mindtrack/issues/382)) ([f02be10](https://github.com/lucasrudi/mindtrack/commit/f02be10961173a145d51832dea435f67ebd7486f))
* **deps:** update dependency vue to v3.5.32 ([#405](https://github.com/lucasrudi/mindtrack/issues/405)) ([22292cf](https://github.com/lucasrudi/mindtrack/commit/22292cfb2a885dbde37cdf28783a3cb6e59861d7))
* **deps:** update pending dashboard dependencies ([#404](https://github.com/lucasrudi/mindtrack/issues/404)) ([a260f1a](https://github.com/lucasrudi/mindtrack/commit/a260f1ad18b8a006ee124cbbe61cd0a508e3db63)), closes [#71](https://github.com/lucasrudi/mindtrack/issues/71)
* **deps:** update renovate dashboard dependencies ([#379](https://github.com/lucasrudi/mindtrack/issues/379)) ([c6dc4be](https://github.com/lucasrudi/mindtrack/commit/c6dc4beaee8cb015ea6bb395a728f5935fe9ce08)), closes [#71](https://github.com/lucasrudi/mindtrack/issues/71)
* **deps:** update vitest monorepo to v4.1.1 ([#376](https://github.com/lucasrudi/mindtrack/issues/376)) ([308306d](https://github.com/lucasrudi/mindtrack/commit/308306dd2f90061baf78d21e0d1cfe374b5f2592))
* **deps:** update vitest monorepo to v4.1.2 ([#390](https://github.com/lucasrudi/mindtrack/issues/390)) ([80ab6fe](https://github.com/lucasrudi/mindtrack/commit/80ab6fe02dcad3507773a70e5b0c58fc57984bfb))
* **deps:** update vitest monorepo to v4.1.3 ([#415](https://github.com/lucasrudi/mindtrack/issues/415)) ([790aa99](https://github.com/lucasrudi/mindtrack/commit/790aa99f2cbc61cbb4ba7a0cfd954d2e9fd58f5f))
* **frontend:** bump flatted from 3.3.3 to 3.4.2 in /frontend ([#313](https://github.com/lucasrudi/mindtrack/issues/313)) ([d6d1715](https://github.com/lucasrudi/mindtrack/commit/d6d17157974dd553d038ee7e0d484ad02653a9c0))
* **frontend:** bump picomatch in /frontend ([#384](https://github.com/lucasrudi/mindtrack/issues/384)) ([0abe855](https://github.com/lucasrudi/mindtrack/commit/0abe855dd65f07e661cb94e8b1de13c5161656a3))
* **frontend:** bump the frontend-dependencies group in /frontend with 2 updates ([#407](https://github.com/lucasrudi/mindtrack/issues/407)) ([efcb16c](https://github.com/lucasrudi/mindtrack/commit/efcb16c367f84712f18c27553404d5846e91e337))
* **frontend:** bump the frontend-dependencies group in /frontend with 3 updates ([#393](https://github.com/lucasrudi/mindtrack/issues/393)) ([1a2de14](https://github.com/lucasrudi/mindtrack/commit/1a2de14f1b8dca8011db909d3295a013da9fa635))
* **frontend:** upgrade ESLint to v10 ([#329](https://github.com/lucasrudi/mindtrack/issues/329)) ([675a6da](https://github.com/lucasrudi/mindtrack/commit/675a6dabb1f856b629644c0fbcbecbd8e0143988)), closes [#318](https://github.com/lucasrudi/mindtrack/issues/318)
* **frontend:** upgrade major frontend dependencies ([#372](https://github.com/lucasrudi/mindtrack/issues/372)) ([09193c0](https://github.com/lucasrudi/mindtrack/commit/09193c079bec206cc074119bf270ff09888f64c8))


### Tests

* **coverage:** improve sonar coverage hotspots ([#221](https://github.com/lucasrudi/mindtrack/issues/221)) ([bd1906c](https://github.com/lucasrudi/mindtrack/commit/bd1906c39d67c9c0a84f81a2692068faa6e37b92)), closes [#216](https://github.com/lucasrudi/mindtrack/issues/216)
* **coverage:** raise frontend and backend coverage ([#226](https://github.com/lucasrudi/mindtrack/issues/226)) ([3909809](https://github.com/lucasrudi/mindtrack/commit/3909809d7d1ec766e8bc8a5e8f6c22d25f975cd0))
* **frontend:** align component folders and raise coverage ([#358](https://github.com/lucasrudi/mindtrack/issues/358)) ([73f6c63](https://github.com/lucasrudi/mindtrack/commit/73f6c63ae9b83bbc63cecb286d570f0f6ddd0883)), closes [#357](https://github.com/lucasrudi/mindtrack/issues/357)


### Refactoring

* **frontend:** move goal icon helper to outer scope ([#263](https://github.com/lucasrudi/mindtrack/issues/263)) ([afe6087](https://github.com/lucasrudi/mindtrack/commit/afe6087905643d5e358239c6ab41beaa487affa9)), closes [#262](https://github.com/lucasrudi/mindtrack/issues/262)

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
