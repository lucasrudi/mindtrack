# Changelog

## [0.3.1](https://github.com/lucasrudi/mindtrack/compare/backend-v0.3.0...backend-v0.3.1) (2026-04-08)


### Miscellaneous

* **main:** release backend 0.3.1-SNAPSHOT ([#421](https://github.com/lucasrudi/mindtrack/issues/421)) ([96a218d](https://github.com/lucasrudi/mindtrack/commit/96a218d6cfb8c7abcbc44415e1955d564bca5254))

## [0.3.0](https://github.com/lucasrudi/mindtrack/compare/backend-v0.2.0...backend-v0.3.0) (2026-04-08)


### Features

* **activity:** add goal linking to activity create and edit ([#280](https://github.com/lucasrudi/mindtrack/issues/280)) ([370cdf5](https://github.com/lucasrudi/mindtrack/commit/370cdf589d83d98310358cd9a58105cc8d22876a)), closes [#272](https://github.com/lucasrudi/mindtrack/issues/272)
* **analytics:** refresh video catalog with availability fallback ([#305](https://github.com/lucasrudi/mindtrack/issues/305)) ([2aeeb49](https://github.com/lucasrudi/mindtrack/commit/2aeeb49db452355c7ab30a00d8688f15dbe847a8))
* **appointment:** add therapist calendar booking ([#333](https://github.com/lucasrudi/mindtrack/issues/333)) ([73b130a](https://github.com/lucasrudi/mindtrack/commit/73b130a0aa854a534ea721ea5c60f661474e912e)), closes [#322](https://github.com/lucasrudi/mindtrack/issues/322)
* **appointments:** add recurring appointments with duration-based scheduling ([#349](https://github.com/lucasrudi/mindtrack/issues/349)) ([3c08c02](https://github.com/lucasrudi/mindtrack/commit/3c08c02e22b40a5089584ff2a9519d68e6c74617))
* **dashboard:** add embedded YouTube health videos widget ([#288](https://github.com/lucasrudi/mindtrack/issues/288)) ([a28b41f](https://github.com/lucasrudi/mindtrack/commit/a28b41fb3d77b34af74fba113b70df80009aae59))
* **dashboard:** add personalized content widgets with tips, resources, and well-being indicators ([#253](https://github.com/lucasrudi/mindtrack/issues/253)) ([ef5965a](https://github.com/lucasrudi/mindtrack/commit/ef5965af95171da8ea8b419853ac8f7a1955cb4e)), closes [#249](https://github.com/lucasrudi/mindtrack/issues/249)
* **goals:** auto-generate suggested milestones during onboarding ([#258](https://github.com/lucasrudi/mindtrack/issues/258)) ([1f73f22](https://github.com/lucasrudi/mindtrack/commit/1f73f220737e8e95af5dfb8cde55f1dccf4f7a11))
* **goals:** infer started goals ([#335](https://github.com/lucasrudi/mindtrack/issues/335)) ([50739ad](https://github.com/lucasrudi/mindtrack/commit/50739ad417230d09730f785fe4f3f1bb1d679c7e)), closes [#328](https://github.com/lucasrudi/mindtrack/issues/328)
* **messaging:** make WhatsApp integration opt-in via ConditionalOnProperty ([#237](https://github.com/lucasrudi/mindtrack/issues/237)) ([2d67356](https://github.com/lucasrudi/mindtrack/commit/2d6735671c03a8789e9f96545d7b52c9269ae485))
* **mood:** add mood logging endpoint and dashboard widget ([#281](https://github.com/lucasrudi/mindtrack/issues/281)) ([ceea71e](https://github.com/lucasrudi/mindtrack/commit/ceea71ea3c5dabd89f3281dc6303298bf9bed947)), closes [#271](https://github.com/lucasrudi/mindtrack/issues/271)
* **notifications:** build notification delivery infrastructure ([#350](https://github.com/lucasrudi/mindtrack/issues/350)) ([b4515c2](https://github.com/lucasrudi/mindtrack/commit/b4515c2b9ba464465ae292f1ce66532cac8e967f)), closes [#340](https://github.com/lucasrudi/mindtrack/issues/340)
* **therapist:** add patient request flow ([#332](https://github.com/lucasrudi/mindtrack/issues/332)) ([f68a87d](https://github.com/lucasrudi/mindtrack/commit/f68a87d8bcb4aa4dfc5d5f36a509bd1e84ba0021)), closes [#321](https://github.com/lucasrudi/mindtrack/issues/321)
* **therapist:** implement therapist-patient connection flow via invite link ([#363](https://github.com/lucasrudi/mindtrack/issues/363)) ([c74b60a](https://github.com/lucasrudi/mindtrack/commit/c74b60a93677fd522157feb524bda2b3aeafcfdd)), closes [#354](https://github.com/lucasrudi/mindtrack/issues/354)


### Bug Fixes

* **backend:** address remaining Sonar findings ([#204](https://github.com/lucasrudi/mindtrack/issues/204)) ([230fdef](https://github.com/lucasrudi/mindtrack/commit/230fdefb0f703ef72a954cd6709f384d24aace6e))
* **backend:** resolve Sonar code smells ([#201](https://github.com/lucasrudi/mindtrack/issues/201)) ([fd31f46](https://github.com/lucasrudi/mindtrack/commit/fd31f46e43a5872d71cb18c0d637442b20fe7785)), closes [#200](https://github.com/lucasrudi/mindtrack/issues/200)
* **common:** relax API rate limits ([#317](https://github.com/lucasrudi/mindtrack/issues/317)) ([0791297](https://github.com/lucasrudi/mindtrack/commit/0791297d8604f2ee3b98160b8dfbb39ee3057e2b)), closes [#316](https://github.com/lucasrudi/mindtrack/issues/316)
* **config:** replace @Configuration with @Component on @ConfigurationProperties classes ([#241](https://github.com/lucasrudi/mindtrack/issues/241)) ([e2b948a](https://github.com/lucasrudi/mindtrack/commit/e2b948acdfa9458419dcb8ec47df30407fe5d3dc))
* **deps:** update dependency com.google.protobuf:protobuf-java to v4.34.1 ([#351](https://github.com/lucasrudi/mindtrack/issues/351)) ([56632d9](https://github.com/lucasrudi/mindtrack/commit/56632d9d5ab14d4a1590939d8bc01640af34b2bd))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v4.0.5 ([#391](https://github.com/lucasrudi/mindtrack/issues/391)) ([6324cff](https://github.com/lucasrudi/mindtrack/commit/6324cff30b456a2bab7aab80365a67b75ef3f686))
* **deps:** update netty monorepo to v4.2.11.final ([#381](https://github.com/lucasrudi/mindtrack/issues/381)) ([eca70a7](https://github.com/lucasrudi/mindtrack/commit/eca70a7f60358def79793b54b8160caba005951a))
* **deps:** update netty monorepo to v4.2.12.final ([#385](https://github.com/lucasrudi/mindtrack/issues/385)) ([4c5b2fc](https://github.com/lucasrudi/mindtrack/commit/4c5b2fccc5c6825e4089a002ae08ba7361d64d85))
* **docker:** add missing messaging config to application-docker.yml ([#243](https://github.com/lucasrudi/mindtrack/issues/243)) ([2227acd](https://github.com/lucasrudi/mindtrack/commit/2227acd40e2afc921752967f22c386f3dd72d8ad))
* **docker:** Spring Boot 4.x Flyway autoconfiguration and migration gaps ([#234](https://github.com/lucasrudi/mindtrack/issues/234)) ([b702738](https://github.com/lucasrudi/mindtrack/commit/b702738c04a1079b9874b1c3c21fbd627d6e77dd))
* **error-handling:** add comprehensive backend and frontend error handling ([#260](https://github.com/lucasrudi/mindtrack/issues/260)) ([385d867](https://github.com/lucasrudi/mindtrack/commit/385d867b5ff47d4574f1a20eb76d50e92ab0daba)), closes [#250](https://github.com/lucasrudi/mindtrack/issues/250)
* **error-handling:** extract bad request constant and use throw over Promise.reject ([#266](https://github.com/lucasrudi/mindtrack/issues/266)) ([317aeb3](https://github.com/lucasrudi/mindtrack/commit/317aeb3a328141c40e499ff0a5ee5227c2ab1a5b)), closes [#265](https://github.com/lucasrudi/mindtrack/issues/265)
* **flyway:** order new migrations after v23 ([#362](https://github.com/lucasrudi/mindtrack/issues/362)) ([bf65fe9](https://github.com/lucasrudi/mindtrack/commit/bf65fe9b0015605c36fcc8a5825600fd890bb508)), closes [#361](https://github.com/lucasrudi/mindtrack/issues/361)
* **flyway:** resolve duplicate v13 migrations ([#360](https://github.com/lucasrudi/mindtrack/issues/360)) ([65fe06e](https://github.com/lucasrudi/mindtrack/commit/65fe06e6f180a982ced2b47a1d290a45071d11dc)), closes [#359](https://github.com/lucasrudi/mindtrack/issues/359)
* **goals,ai,survey:** fix lazy load crash, AI error handling, and slider labels ([#245](https://github.com/lucasrudi/mindtrack/issues/245)) ([82825ab](https://github.com/lucasrudi/mindtrack/commit/82825ab6e0b73a222e99014a22df56ccb8cec92e))
* **onboarding:** set createdAt for suggested milestones ([#309](https://github.com/lucasrudi/mindtrack/issues/309)) ([84a652f](https://github.com/lucasrudi/mindtrack/commit/84a652f3c5901db211994956e7c4560ee0b0efcb)), closes [#308](https://github.com/lucasrudi/mindtrack/issues/308)
* **security:** exclude vulnerable jackson-core 3.x from test scope ([#412](https://github.com/lucasrudi/mindtrack/issues/412)) ([7e6ab94](https://github.com/lucasrudi/mindtrack/commit/7e6ab94a80e6c5af73afcd7cf98efe5359ef19bc)), closes [#410](https://github.com/lucasrudi/mindtrack/issues/410)
* **security:** harden whatsapp sender validation ([#299](https://github.com/lucasrudi/mindtrack/issues/299)) ([ea9ff29](https://github.com/lucasrudi/mindtrack/commit/ea9ff29602e6074428ad021e27aef8f8733f815e)), closes [#295](https://github.com/lucasrudi/mindtrack/issues/295)
* **security:** remediate current snyk findings ([#403](https://github.com/lucasrudi/mindtrack/issues/403)) ([982eb3d](https://github.com/lucasrudi/mindtrack/commit/982eb3d2a1c384c11b2f153b8ef2b6c6f789e8f4)), closes [#402](https://github.com/lucasrudi/mindtrack/issues/402)
* **security:** remediate whatsapp webhook ssrf flow ([#298](https://github.com/lucasrudi/mindtrack/issues/298)) ([a2c9ab2](https://github.com/lucasrudi/mindtrack/commit/a2c9ab2ded098e8b5521b664e876b754332f43ef)), closes [#294](https://github.com/lucasrudi/mindtrack/issues/294)
* **sonar:** resolve backend and frontend quality findings ([#297](https://github.com/lucasrudi/mindtrack/issues/297)) ([859ad11](https://github.com/lucasrudi/mindtrack/commit/859ad111fe73872efe801d3f622f7cb1cc100604)), closes [#296](https://github.com/lucasrudi/mindtrack/issues/296)
* **sonar:** resolve current unresolved findings ([#356](https://github.com/lucasrudi/mindtrack/issues/356)) ([1a0c2f4](https://github.com/lucasrudi/mindtrack/commit/1a0c2f48093c16874d28c5478e55f4875e6059ba)), closes [#355](https://github.com/lucasrudi/mindtrack/issues/355)
* **sonar:** resolve therapist and appointment findings ([#347](https://github.com/lucasrudi/mindtrack/issues/347)) ([c28f0a9](https://github.com/lucasrudi/mindtrack/commit/c28f0a9c935bfbf125ff50ea5922138b1ec346b5)), closes [#342](https://github.com/lucasrudi/mindtrack/issues/342) [#343](https://github.com/lucasrudi/mindtrack/issues/343)


### Miscellaneous

* **backend:** bump io.sentry:sentry-spring-jakarta from 8.35.0 to 8.36.0 in /backend in the backend-dependencies group ([#366](https://github.com/lucasrudi/mindtrack/issues/366)) ([8fe96b6](https://github.com/lucasrudi/mindtrack/commit/8fe96b61299012b70f08d68615b127f9146cae3e))
* **backend:** bump the backend-dependencies group in /backend with 2 updates ([#392](https://github.com/lucasrudi/mindtrack/issues/392)) ([8750763](https://github.com/lucasrudi/mindtrack/commit/87507636a58bc0a332620ccabe74a3ff3e6ef886))
* **deps:** update minor and patch dependencies ([#371](https://github.com/lucasrudi/mindtrack/issues/371)) ([2903cf1](https://github.com/lucasrudi/mindtrack/commit/2903cf1d0e465ffc2af8663c488a9665d5c3d331))
* **deps:** update pending dashboard dependencies ([9889370](https://github.com/lucasrudi/mindtrack/commit/9889370299927f206133ad2216343ce7eae8e6d3))
* **deps:** update pending dashboard dependencies ([#388](https://github.com/lucasrudi/mindtrack/issues/388)) ([9889370](https://github.com/lucasrudi/mindtrack/commit/9889370299927f206133ad2216343ce7eae8e6d3))
* **frontend:** upgrade ESLint to v10 ([#329](https://github.com/lucasrudi/mindtrack/issues/329)) ([675a6da](https://github.com/lucasrudi/mindtrack/commit/675a6dabb1f856b629644c0fbcbecbd8e0143988)), closes [#318](https://github.com/lucasrudi/mindtrack/issues/318)
* **main:** release backend 0.2.1-SNAPSHOT ([#416](https://github.com/lucasrudi/mindtrack/issues/416)) ([cad1f3d](https://github.com/lucasrudi/mindtrack/commit/cad1f3d0a71ce959b6c81592d1954a2464420901))
* **security:** fix jackson-core SNYK-JAVA-COMFASTERXMLJACKSONCORE-15365924 ([#293](https://github.com/lucasrudi/mindtrack/issues/293)) ([edf9e66](https://github.com/lucasrudi/mindtrack/commit/edf9e6650d59ce4e0f31fecde67ef46b3d4a64cd)), closes [#289](https://github.com/lucasrudi/mindtrack/issues/289) [#290](https://github.com/lucasrudi/mindtrack/issues/290)


### Tests

* **coverage:** improve sonar coverage hotspots ([#221](https://github.com/lucasrudi/mindtrack/issues/221)) ([bd1906c](https://github.com/lucasrudi/mindtrack/commit/bd1906c39d67c9c0a84f81a2692068faa6e37b92)), closes [#216](https://github.com/lucasrudi/mindtrack/issues/216)
* **coverage:** raise frontend and backend coverage ([#226](https://github.com/lucasrudi/mindtrack/issues/226)) ([3909809](https://github.com/lucasrudi/mindtrack/commit/3909809d7d1ec766e8bc8a5e8f6c22d25f975cd0))
* **frontend:** align component folders and raise coverage ([#358](https://github.com/lucasrudi/mindtrack/issues/358)) ([73f6c63](https://github.com/lucasrudi/mindtrack/commit/73f6c63ae9b83bbc63cecb286d570f0f6ddd0883)), closes [#357](https://github.com/lucasrudi/mindtrack/issues/357)
* **messaging:** improve backend coverage ([#365](https://github.com/lucasrudi/mindtrack/issues/365)) ([01c9b0d](https://github.com/lucasrudi/mindtrack/commit/01c9b0ddb57f7afb83455bc2825a6d953d0753f4)), closes [#364](https://github.com/lucasrudi/mindtrack/issues/364)


### Refactoring

* **analytics:** clean up content registry sonar issues ([#261](https://github.com/lucasrudi/mindtrack/issues/261)) ([0d34c58](https://github.com/lucasrudi/mindtrack/commit/0d34c584e71685a5cc9ebd77606a409a6752aa9c)), closes [#259](https://github.com/lucasrudi/mindtrack/issues/259)
* **onboarding:** extract milestone creation to reduce cognitive complexity ([#267](https://github.com/lucasrudi/mindtrack/issues/267)) ([a63d213](https://github.com/lucasrudi/mindtrack/commit/a63d2137b9e520787a66249d4c735b957f7b68ff)), closes [#264](https://github.com/lucasrudi/mindtrack/issues/264)
