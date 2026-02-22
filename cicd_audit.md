# BACKEND – RS6840 CI/CD Audit (C1–C4)

## A. Service identity

- Repo name: `skillshub`
- Modules/services inside (course-service, user-service, etc.):
    - Monolith backend with bounded contexts (via packages), not separate deployable services. Evidence:
        - `pom.xml` – single Maven module with artifactId `skills-hub`.
        - Package structure in tests indicates logical modules:
            - `com.simplon_project.skillhub.skillhub.course...`
            - `com.simplon_project.skillhub.skillhub.user...`
            - `com.simplon_project.skillhub.skillhub.storage...`
        - ArchUnit test enforcing modulith boundaries:
            - `src/test/java/com/simplon_project/skillhub/skillhub/course/archunit/SkillshubArchitectureRulesTest.java`.
- Ports:
    - Backend HTTP port:
        - `src/main/resources/application.yml` – `server.port: ${SERVER_PORT:10020}` (default 10020).
        - `src/main/resources/application-docker.yml` – `server.port: ${SERVER_PORT:10020}`.
        - `docker-compose.yml` – `backend-service` exposes `10020:10020`.
    - Observability stack:
        - `docker-compose.yml`:
            - Prometheus: `9090:9090` (service `prometheus`).
            - Grafana: `3000:3000` (service `grafana`).
            - Loki: `3100:3100` (service `loki`).
            - Promtail: `9080:9080` (service `promtail`).
    - Data & messaging:
        - PostgreSQL instances:
            - `postgres-course`: `5433:5432` (course DB).
            - `postgres-storage`: `5434:5432` (storage DB).
            - `postgres-user`: `5435:5432` (user DB).
        - MinIO:
            - `9000:9000` (S3 API), `9001:9001` (console web).
        - RabbitMQ:
            - `5672:5672` (AMQP), `15672:15672` (management UI).
- External dependencies (DBs, MQ, MinIO):
    - PostgreSQL – three logical databases, each mapped to a dedicated container:
        - `docker-compose.yml` services `postgres-course`, `postgres-storage`, `postgres-user` with `POSTGRES_DB` set to
          `course_service`, `storage_service`, `user_service`.
        - `src/main/resources/application.yml` sources for each:
            - `spring.datasource.course.url: jdbc:postgresql://localhost:5433/course_service`.
            - `spring.datasource.storage.url: jdbc:postgresql://localhost:5434/storage_service`.
            - `spring.datasource.user.url: jdbc:postgresql://localhost:5435/user_service`.
    - RabbitMQ – asynchronous messaging:
        - `docker-compose.yml` service `rabbitmq` using `rabbitmq:3.13-management`.
        - Messaging configuration in `application.yml` / `application-docker.yml` under `rabbitmq`, `storage.rabbitmq`,
          `course.rabbitmq`.
    - MinIO – S3-compatible storage:
        - `docker-compose.yml` service `minio` with `minio/minio:latest`.
        - `application.yml` and `application-docker.yml` `minio` block (URL, access key, secret key, bucket).
    - Observability: Prometheus, Grafana, Loki, Promtail:
        - `docker-compose.yml` services `prometheus`, `grafana`, `loki`, `promtail`.
        - Prometheus configuration mounted from `prometheus.yml`.
        - Loki configuration from `loki-config.yml` and logs directory `logs/` mounted into `promtail`.

---

## B. C1 – Containerized dev/test environment evidence

Status summary (per criterion):

1) Each service isolated in its own container – **OK**.
2) Resource allocation defined per container (cpu/mem/limits) – **MISSING**.
3) Dependency links/interconnections function (networks, hostnames, healthchecks) – **OK**.
4) Developer productivity (logs, console, volume sync, profiles) – **PARTIAL**.
5) Environment documented for replication – **PARTIAL**.

Detailed evidence:

1) Each service isolated in its own container (api, db, mq, storage, observability)
    - Status: **OK**.
    - Evidence:
        - `docker-compose.yml` defines distinct services:
            - `backend-service` – Spring Boot backend (`build: .`, `container_name: backend-service`,
              `ports: "10020:10020"`).
            - Databases:
                - `postgres-course` – course database.
                - `postgres-storage` – storage database.
                - `postgres-user` – user database.
            - Storage:
                - `minio` – S3-compatible object store.
            - Messaging:
                - `rabbitmq` – AMQP broker.
            - Observability:
                - `prometheus`, `grafana`, `loki`, `promtail`.
        - Each database has its own Docker volume (`postgres-course-data`, `postgres-storage-data`,
          `postgres-user-data`).
    - Comment: One backend container plus multiple infra containers matches RS6840 requirement for isolation per
      technical service.

2) Resource allocation defined per container (cpu/mem/limits)
    - Status: **MISSING**.
    - Evidence:
        - `docker-compose.yml` – no `deploy.resources`, `mem_limit`, `cpus` or similar constraints defined for any
          service.
    - Conclusion:
        - **MISSING – No evidence found in repository** of container-level CPU or memory limits.

3) Dependency links/interconnections function (networks, hostnames, healthchecks)
    - Status: **OK**.
    - Evidence – networks & hostnames:
        - `docker-compose.yml`:
            - Uses a single shared network `skillshub-net` (external) for all services.
            - Containers named and referred to by hostname:
                - PostgreSQL: `postgres-course`, `postgres-storage`, `postgres-user`.
                - RabbitMQ: `rabbitmq`.
                - MinIO: `minio`.
            - `backend-service` depends on:
                - `postgres-course`, `postgres-storage`, `rabbitmq`, `minio` via `depends_on`.
            - `application-docker.yml` uses these container hostnames in defaults:
                - `SPRING_DATASOURCE_COURSE_URL: jdbc:postgresql://postgres-course:5432/course_service`.
                - `SPRING_DATASOURCE_STORAGE_URL: jdbc:postgresql://postgres-storage:5432/storage_service`.
                - `rabbitmq.host: ${RABBITMQ_HOST:rabbitmq}`.
                - `minio.url: ${MINIO_URL:http://minio:9000}`.
    - Evidence – healthchecks:
        - `docker-compose.yml`:
            - `postgres-course`, `postgres-storage`, `postgres-user` have `healthcheck` sections using `pg_isready` with
              `interval: 3s` and `retries: 10`.
    - Evidence – observability stack dependencies:
        - `prometheus` depends on `postgres-course`, `postgres-storage`, `rabbitmq`, `minio` (`depends_on` clause).
        - `promtail` depends on `loki`.
    - Conclusion:
        - Network and service discovery via container names + healthchecks for DBs are clearly modeled.

4) Developer productivity (logs, console, volume sync, profiles)
    - Status: **PARTIAL**.
    - Evidence – logs and log aggregation:
        - `docker-compose.yml` for `promtail` mounts Docker host logs and the project `logs` directory:
            - `/var/lib/docker/containers:/var/lib/docker/containers:ro`.
            - `/var/run/docker.sock:/var/run/docker.sock`.
            - `./promtail-config.yml:/etc/promtail/config.yml`.
            - `/c/Users/darde/Documents/project/skillshub/logs:/logs:ro` (Windows-path specific).
        - `loki-config.yml` and `logs/` directory exist, with multiple backend and skills-hub log files, indicating log
          file rotation and Promtail compatibility.
    - Evidence – profiles and configuration switching:
        - `application.yml`:
            - `spring.profiles.active: ${SPRING_PROFILES_ACTIVE:dev}`.
        - `application-docker.yml`:
            - `spring.profiles.active: ${SPRING_PROFILES_ACTIVE:docker-all}`.
        - `docker-compose.yml`:
            - `backend-service` uses `env_file: - ${ENV_FILE:-./envs/dev.env}` to externalize configuration.
    - Evidence – helper commands:
        - `Makefile` provides local commands (`run`, `install_and_run`, liquibase tasks, observability commands
          `prometheus_up`, `grafana_up`, `loki_up`).
    - Gaps:
        - No explicit `docker-compose` target in `Makefile` for full stack (must be run manually with
          `docker-compose up`).
        - No documented shared code volume for hot-reload into the container (no `volumes:` mapping of source tree to
          `backend-service`).
    - Conclusion:
        - Good support for observability and migrations, but missing explicit containerized dev loop optimization (
          volume sync, dedicated `make` targets) – hence **PARTIAL**.

5) Environment documented for replication
    - Status: **PARTIAL**.
    - Evidence – documentation:
        - `README.md` – *Environment & Deployment Modes* section:
            - Describes **Local**, **Hybrid**, and **Full Docker** modes.
            - States that ports, credentials, and endpoints are **driven by environment variables**, with no hard-coded
              configuration.
        - `README.md` – technology stack and observability sections document PostgreSQL, RabbitMQ, MinIO, Prometheus,
          Grafana, Loki & Promtail.
    - Evidence – configuration:
        - `application.yml` & `application-docker.yml` show clear separation of profiles and environment-based
          configuration.
        - `docker-compose.yml` fully describes the stack.
    - Gaps:
        - `envs/` directory is `.gitignore`’d (`.gitignore` line `/envs`), and although files exist locally (
          `envs/dev.env`, `envs/docker-all.env`, etc.), they are not versioned; no `*.env.example` template is present
          in the repo.
        - No step-by-step “docker-only” quick-start documented in `README.md` (commands, required network, env files,
          etc.).
    - Conclusion:
        - Config is mostly self-describing but missing committed env templates and explicit replication runbook – marked
          **PARTIAL**.

Additional notes:

- Docker compose modes (local/hybrid/all):
    - `application.yml` vs `application-docker.yml` and `SPRING_PROFILES_ACTIVE` default values (`dev` vs `docker-all`)
      imply multiple execution modes.
    - `README.md` explicitly mentions **Local mode**, **Hybrid mode**, **Full Docker mode**, but there is no dedicated
      second/third compose file.
- DB separation and ports:
    - Clearly documented in `docker-compose.yml` and `application.yml` as described above.

---

## C. C2 – Git workflow & secrets

### 1) Branch model and merges to master

- Status: **PARTIAL** (due to divergence with required delivery branch `master`).
- Evidence – documented Git workflow:
    - `README.md` – *Git Workflow* section:
        - `dev` is the **main development branch**.
        - `main` represents **stable, deployable releases**.
        - Feature branches are created from `dev`.
        - Integrations into `dev` use **squash merge**.
    - There is **no mention of a `master` branch** in the repo documentation.
- RS6840 requirement:
    - Official delivery branch is **`master`** (provided constraint).
- Divergence risk:
    - The documented workflow uses `dev` → `main` instead of targeting `master`.
    - This can break alignment with RS6840 governance if tooling and processes expect delivery from `master`.
- Conclusion:
    - **PARTIAL** – workflow is well-defined but not aligned to the required delivery branch name.

### 2) Commit conventions evidence

- Status: **MISSING**.
- Evidence:
    - No `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, or documented commit style (e.g., Conventional Commits) in the
      repository root or docs.
    - `README.md` mentions “clean and readable history” but does not define a specific commit message format.
- Conclusion:
    - **MISSING – No evidence found in repository** of an enforced or documented commit convention.

### 3) Secrets strategy evidence (.env ignored, example templates)

- Status: **PARTIAL**.
- Evidence – ignoring secret files:
    - `.gitignore`:
        - Ignores `*.env`, `.env`, and `/envs`.
    - Local `envs/` directory exists (per workspace listing) with `dev.env`, `docker-all.env`, `docker-hybrid.env`,
      `prod.env`, but these are not committed.
- Evidence – configuration driven by environment variables:
    - `application.yml` and `application-docker.yml` use environment-variable based configuration for DBs, RabbitMQ,
      MinIO, and external APIs.
    - Example:
        - `SPRING_DATASOURCE_*_URL`, `RABBITMQ_HOST`, `MINIO_URL`, `VIMEO_ACCESS_TOKEN`, etc.
- Evidence – hard-coded secrets:
    - `application.yml`:
        - `minio.access-key: ${MINIO_ACCESS_KEY:minioadmin}`.
        - `minio.secret-key: ${MINIO_SECRET_KEY:minioadmin123}`.
    - `application-docker.yml`:
        - Same pattern for MinIO secrets.
    - `docker-compose.yml`:
        - `minio` environment:
            - `MINIO_ROOT_USER: minioadmin`.
            - `MINIO_ROOT_PASSWORD: minioadmin123`.
        - `rabbitmq` environment:
            - `RABBITMQ_DEFAULT_USER: admin`.
            - `RABBITMQ_DEFAULT_PASS: admin`.
    - `application.yml`:
        - `vimeo.access-token: ${VIMEO_ACCESS_TOKEN}` (no default hardcoded key, but value must come from env).
- Evaluation according to rules:
    - For MinIO and RabbitMQ, default credentials (`minioadmin` / `admin`) are present directly in repo configs.
    - They are low-entropy but commonly used development defaults.
    - However, the **rule 6** states: “If secrets appear hardcoded in repo → flag as BLOCKER.”
- Conclusion:
    - Secret strategy is **PARTIAL**:
        - Positives: `.env` and `envs/` are ignored; configuration is environment-based; SonarCloud configuration
          expects tokens via CLI/CI.
        - Negatives: default credentials for MinIO and RabbitMQ are hardcoded in `docker-compose.yml` and
          `application*.yml`.
    - This will be listed as a **BLOCKER** in section G.

### 4) Any git hooks / tooling

- Status: **MISSING**.
- Evidence:
    - No `.husky`, `.githooks`, or documented use of Git hooks in the repo.
    - No scripts in `Makefile` referencing Git hooks.
- Conclusion:
    - **MISSING – No evidence found in repository** of Git hooks or pre-commit tooling.

---

## D. C3 – Tests + linter + coverage

### 1) Unit test suites

- Status: **OK** (multiple unit and some integration tests present).

- Evidence – user module tests:
    - `src/test/java/com/simplon_project/skillhub/skillhub/user/application/usecase/EnrollInCourseUseCaseTest.java` –
      unit tests for user-course enrollment use case.
    - `src/test/java/com/simplon_project/skillhub/skillhub/user/application/usecase/UserUseCasesTest.java` – unit tests
      around user use cases.
    - `src/test/java/com/simplon_project/skillhub/skillhub/user/integrationTests/UserControllerIntegrationTest.java` –
      integration tests for user controller.

- Evidence – course module tests:
    - Use case tests:
        -
        `src/test/java/com/simplon_project/skillhub/skillhub/course/application/usecase/SearchCoursesByIdsUseCaseTest.java`.
        - `src/test/java/com/simplon_project/skillhub/skillhub/course/application/usecase/VideoUseCasesTest.java`.
        -
        `src/test/java/com/simplon_project/skillhub/skillhub/course/application/usecase/RetryVideoExternalDeletionUseCaseTest.java`.
        - `src/test/java/com/simplon_project/skillhub/skillhub/course/application/usecase/CourseUseCasesTest.java`.
    - Adapter tests (messaging, persistence, web):
        - `course.adapter.messaging.*` (e.g., `VideoDeletionMessageTest`, `OutboxEventProcessorTest`,
          `VideoPollingListenerTest`, `VideoDeletionListenerTest`).
        - `course.adapter.out.persistence.*` (e.g., `CourseAdapterTest`, `CourseSummaryEntityMapperTest`,
          `SoftDeleteSafetyTest`, `VideoInFlightCheckAdapterTest`).
        - Web controllers:
            - `course.adapter.in.web.controller.StudentCourseControllerTest`.
            - `course.adapter.in.web.controller.CourseSearchControllerTest`.
            - `course.adapter.in.web.controller.CourseSearchControllerIntegrationTest`.

- Evidence – architecture rules:
    - `src/test/java/com/simplon_project/skillhub/skillhub/course/archunit/SkillshubArchitectureRulesTest.java` –
      ArchUnit test suite enforcing hexagonal/modulith rules.

- Conclusion:
    - At least **two distinct test suites** clearly exist (user + course; unit + integration; plus ArchUnit).
    - Status **OK**.

### 2) Linter / static analysis (Checkstyle/SpotBugs/Sonar, etc.)

- Status: **OK** (for static analysis integration via Sonar; no evidence of Checkstyle/SpotBugs plugins, but RS6840
  requires at least one static analysis tool).

- Evidence – SonarCloud configuration:
    - `pom.xml` properties:
        - `sonar.organization>mrd-fox</sonar.organization>`.
        - `sonar.projectVersion>${project.version}</sonar.projectVersion>`.
        - `sonar.sources>src/main/java</sonar.sources>`.
        - `sonar.tests>src/test/java</sonar.tests>`.
        - `sonar.sourceEncoding>UTF-8</sonar.sourceEncoding>`.
        - `sonar.coverage.jacoco.xmlReportPaths>target/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>`.
    - `sonar-project.properties`:
        - Defines `sonar.sources`, `sonar.tests`, `sonar.java.binaries`, `sonar.java.test.binaries`.
        - Configures `sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml`.
        - Provides coverage and analysis exclusions.
- Evidence – additional linters:
    - Partial `pom.xml` (first 260 lines) does not show Checkstyle/SpotBugs plugins; the remaining section (not fully
      shown) may contain them, but **no direct evidence** is visible in the provided excerpt.
- Conclusion:
    - Static analysis through **SonarCloud** is clearly configured – **OK** for RS6840.
    - No further tools can be claimed without additional POM lines, so only Sonar is counted.

### 3) Coverage measurement (JaCoCo or equivalent)

- Status: **PARTIAL**.

- Evidence – configuration:
    - `pom.xml` properties:
        - `jacoco-maven-plugin.version>0.8.12</jacoco-maven-plugin.version>`.
    - There is no direct view of the `jacoco-maven-plugin` configuration in the shown POM section, but:
        - `sonar.coverage.jacoco.xmlReportPaths` property is set to `target/site/jacoco/jacoco.xml`.
    - `sonar-project.properties`:
        - `sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml`.
    - `target/jacoco.exec` exists in the workspace, indicating JaCoCo runtime data has been generated by previous test
      runs.

- Gaps:
    - The XML report file `target/site/jacoco/jacoco.xml` is not visible in the workspace listing provided.
    - The `pom.xml` snippet with the JaCoCo Maven plugin execution (e.g. in `report` phase) is not present in the
      excerpt; it likely exists later, but cannot be asserted without explicit file content.

- Commands & paths (based only on evidence):
    - Test execution (unit + integration):
        - Command (from `Makefile`):
            - `mvn clean test-compile` (`compile` target) – compiles tests but does not necessarily run them.
            - `mvn clean install` (`install` target) – will run tests via Maven’s default lifecycle.
        - No dedicated `test` target in `Makefile`, but standard Maven commands apply.
    - Coverage report path (declared, but not verified in repo content):
        - Expected XML report (as per Sonar config):
            - `target/site/jacoco/jacoco.xml`.

- Conclusion:
    - **PARTIAL** – strong evidence of JaCoCo configuration and usage (properties + `jacoco.exec`), but the XML report
      path and plugin execution are not fully verifiable from shown files.

### 4) How tests run in containerized environment (Testcontainers / docker compose test)

- Status: **PARTIAL**.

- Evidence – Testcontainers dependencies:
    - `pom.xml`:
        - `org.testcontainers:rabbitmq` (scope `test`).
        - `org.testcontainers:postgresql` (scope `test`).
        - `org.testcontainers:junit-jupiter` (scope `test`).
    - Indicates intent to run integration tests with ephemeral containers.

- Evidence – Docker-based test environment:
    - `docker-compose.yml` + `Makefile` show how to start infrastructure (Postgres, RabbitMQ, MinIO), but there is **no
      dedicated CI/test compose file**.

- Gaps:
    - No explicit Maven profile or `Makefile` target showing `mvn test` in conjunction with Docker or
      Testcontainers-only profile.
    - No `.github/workflows` steps describing Testcontainers usage during CI.

- Commands (deduced from repo content):
    - Generic test command:
        - `mvn clean test` or `mvn clean install` (not explicitly wired to Docker in `Makefile`).
    - There is no **documented** command like `docker-compose -f docker-compose.test.yml up` used before tests.

- Conclusion:
    - Evidence of Testcontainers dependencies is present, but execution strategy (how CI or devs actually run them in a
      containerized environment) is not documented – **PARTIAL**.

---

## E. C4 – CI chain definition aligned to Git workflow

### 1) CI configuration files

- Status (global C4): **MISSING**.

- Evidence:
    - Search for GitHub workflows `**/.github/workflows/*.yml` returned **no files**.
    - No alternative CI pipeline definition (e.g., `.gitlab-ci.yml`) exists in the root. There is a
      `Maven.gitlab_template.yml`, but it is a template and not an active pipeline file.
- Contrast with documentation:
    - `README.md` states:
        - “Continuous Integration is handled via **GitHub Actions**.”
        - On each PR targeting `dev`, the project is built, tests are executed, and SonarCloud analysis is performed.
    - However, **no workflow file** is present to support this claim.

- Conclusion:
    - In line with RS6840 rule 3: **If no workflow exists → C4 = MISSING.**
    - All sub-criteria below inherit **MISSING** due to lack of concrete CI configuration files.

### 2) Triggers

- Status: **MISSING**.
- Evidence:
    - **MISSING – No evidence found in repository** of any CI trigger configuration (no `on: push` or `on: pull_request`
      definitions).

### 3) Maven cache

- Status: **MISSING**.
- Evidence:
    - **MISSING – No evidence found in repository** of cache configuration for Maven dependencies in CI (no
      `actions/cache` usage or equivalent).

### 4) Test + coverage export

- Status: **MISSING**.
- Evidence:
    - While tests and JaCoCo are configured locally, there is **no CI step** defined to run tests and publish coverage (
      e.g., as artifacts or as Sonar input).
    - **MISSING – No evidence found in repository** of CI jobs executing `mvn test` or collecting
      `target/site/jacoco/jacoco.xml`.

### 5) Static analysis integration (SonarCloud if present)

- Status: **MISSING** (in CI context).
- Evidence:
    - Local Sonar configuration exists (`sonar-project.properties`, `pom.xml` properties).
    - However, there is **no workflow job** invoking `mvn sonar:sonar` or using a Sonar GitHub Action.
    - **MISSING – No evidence found in repository** for SonarCloud CI integration.

### 6) Docker build/push readiness per service image

- Status: **MISSING** (in CI context).
- Evidence:
    - `Dockerfile` exists and is referenced by `docker-compose.yml` for `backend-service` (`build: .`).
    - There is **no CI workflow** that builds and pushes Docker images to a registry (e.g., GHCR, Docker Hub).
    - No `Makefile` target for CI docker build/tag/push flow.
    - **MISSING – No evidence found in repository** of Docker build/push steps in CI.

### 7) Required secrets (SONAR_TOKEN, GHCR, etc.)

- Status: **MISSING** (in CI context).
- Evidence:
    - `sonar-project.properties` suggests that Sonar credentials are passed via CLI/CI, but **no workflow** declares
      required secrets or environment variables.
    - **MISSING – No evidence found in repository** of declared CI secrets like `SONAR_TOKEN`, `GHCR_TOKEN`, etc.

### ASCII CI diagram

Given the absence of any CI configuration in the repository, the following schematic represents the **intended** chain
as per `README.md`, but must be explicitly marked as non-implemented:

```text
(dev feature branch) -- PR --> (dev branch)
    |  (INTENDED, NOT IMPLEMENTED IN REPO)
    v
[GitHub Actions Workflow - MISSING]
    - Checkout code
    - Setup JDK 21
    - Cache Maven dependencies
    - mvn clean verify (tests + JaCoCo)
    - mvn sonar:sonar (SonarCloud)
    - (optional) Build Docker image (backend-service)
    - (optional) Push image to registry
    v
(dev branch updated) --> (main branch release)
```

**Important:** This diagram is conceptual and **not** backed by any workflow file; C4 remains **MISSING**.

---

## F. Observability evidence

### 1) /actuator/prometheus exposure evidence

- Status: **OK**.

- Evidence – Spring Actuator & Micrometer:
    - `pom.xml`:
        - Dependency: `spring-boot-starter-actuator`.
        - Dependency: `io.micrometer:micrometer-registry-prometheus-simpleclient`.
    - `src/main/resources/application.yml` – `management` block:
      ```yaml
      management:
        endpoints:
          web:
            exposure:
              include: prometheus,health,info, metrics
        endpoint:
          prometheus:
            access: unrestricted
        metrics:
          tags:
            application: ${spring.application.name}
      ```
    - `src/main/resources/application-docker.yml` – `management` block:
      ```yaml
      management:
        endpoints:
          web:
            exposure:
              include: ${MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE:prometheus,health,info}
        endpoint:
          health:
            show-details: always
        metrics:
          tags:
            application: ${spring.application.name}
      ```
    - This proves that `/actuator/prometheus` is enabled and exposed over HTTP.

### 2) Logging destination (stdout/file) and Promtail compatibility

- Status: **PARTIAL**.

- Evidence – logging configuration:
    - `application.yml`:
      ```yaml
      logging:
        level:
          root: INFO
          com.simplon_project.skillhub: DEBUG
          org.springframework.amqp: INFO
          com.rabbitmq.client: INFO
      ```
    - `application-docker.yml`:
      ```yaml
      logging:
        level:
          root: INFO
          com.skillshub: DEBUG
          org.springframework.amqp: INFO
          com.rabbitmq.client: INFO
      ```
    - No explicit `logging.file.name` or `logging.file.path` is set, so Spring Boot will log to **stdout** by default.

- Evidence – Promtail & Loki integration:
    - `docker-compose.yml`:
        - `loki` service configured with `loki-config.yml`.
        - `promtail` service with:
            - Volumes mounting Docker containers and Docker socket for log discovery.
            - Mount of `promtail-config.yml` and project `logs` directory.
    - Local `logs/` directory contains historical log files (`backend-service-*.log`, `skills-hub-*.log`), indicating
      that logs may also be written to files via external configuration or logback settings (not shown in repo
      snippets).
    - `Makefile`:
        - Targets `loki_up`, `loki_logs`, `loki_down` for observability stack.

- Gaps:
    - There is no `logback-spring.xml` or explicit file appender configuration in the provided snippets, so file-based
      logging is inferred only from existing log files, not configuration.
    - `promtail-config.yml` is present but its content is not shown; the exact scraping configuration is not auditable
      here.

- Conclusion:
    - Logging to stdout is clearly supported; compatibility with Promtail is **likely** (due to Docker and logs
      volumes), but without content of `promtail-config.yml`, full path matching cannot be proven.
    - Status **PARTIAL** according to strict methodology.

### 3) Recommended dashboards (CPU, RPS, p95, 5xx)

- Status: **MISSING** (in repo files).

- Evidence:
    - `README.md` – *Observability & Monitoring* mentions:
        - “Preconfigured Grafana dashboards.”
    - However, no Grafana dashboard JSON files or provisioning configs (e.g., `grafana/provisioning/dashboards/*.json`)
      are present in the workspace listing.
- Conclusion:
    - **MISSING – No evidence found in repository** of concrete Grafana dashboards for CPU, RPS, p95 latency, or 5xx
      rates.

---

## G. Blockers & minimal action plan

### Identified blockers (strict RS6840 methodology)

1) **C4 – CI chain is entirely missing from the repository.**
    - No `.github/workflows` or equivalent pipeline definition.
    - No automated tests, coverage, or static analysis in CI.
    - Risk: No enforceable quality gate on PRs or releases; non-compliant with RS6840 C4.

2) **Secrets hardcoded in configuration files (MinIO, RabbitMQ).**
    - Files:
        - `docker-compose.yml` – environment variables:
            - `MINIO_ROOT_USER: minioadmin`, `MINIO_ROOT_PASSWORD: minioadmin123`.
            - `RABBITMQ_DEFAULT_USER: admin`, `RABBITMQ_DEFAULT_PASS: admin`.
        - `application.yml` / `application-docker.yml` – default values:
            - `MINIO_ACCESS_KEY:minioadmin`, `MINIO_SECRET_KEY:minioadmin123`.
    - Risk: Violates RS6840 rule 6; encourages reuse of weak credentials in non-dev environments.

3) **Branch strategy diverges from required delivery branch `master`.**
    - Documentation (`README.md`) defines `dev` and `main`, no mention of `master`.
    - Risk: Governance and tooling expecting releases from `master` will not align with current practice.

4) **No documented secrets management strategy beyond `.env` ignore rules.**
    - No `*.env.example` templates or description of how to provision secrets for prod/staging.
    - Risk: Inconsistent secret handling and manual, error-prone setup.

5) **Resource limits not defined on containers.**
    - `docker-compose.yml` lacks CPU/memory limits for all services.
    - Risk: Potential resource contention on Hetzner host; fails RS6840 best practice for containerized deployments.

### Minimal ordered action plan for MVP deployability (Ubuntu Hetzner + Docker Compose "prod")

1) **Introduce a basic CI pipeline (GitHub Actions) for build, tests, and coverage.**
    - Add `.github/workflows/ci.yml` that:
        - Triggers on `push` and `pull_request` to the active development branch (`dev`) and delivery branch (`main` or
          `master`, see step 2).
        - Sets up JDK 21.
        - Caches Maven dependencies.
        - Runs `mvn clean verify` (or `mvn clean install`) to execute tests.
        - Generates JaCoCo report and (optionally) runs Sonar analysis with `mvn sonar:sonar` using `SONAR_TOKEN`
          secret.
    - Outcome: C4 moves from **MISSING** to at least **PARTIAL**.

2) **Align branch strategy with required delivery branch `master`.**
    - Choose one of the following and update documentation + CI accordingly:
        - Option A: Rename `main` to `master` and adjust `README.md` to document `dev` → `master` flow.
        - Option B: Add a `master` branch that mirrors `main` for delivery and update tooling to treat `master` as the
          release branch.
    - Update new CI workflow triggers to include `master`.
    - Outcome: C2 branch strategy moves from **PARTIAL** to **OK** for RS6840.

3) **Refactor secrets to use environment variables and templates, not hardcoded defaults.**
    - For `docker-compose.yml`:
        - Replace hardcoded `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD`, `RABBITMQ_DEFAULT_USER`, `RABBITMQ_DEFAULT_PASS`
          with references to environment variables (e.g., `${MINIO_ROOT_USER}`, `${MINIO_ROOT_PASSWORD}`, etc.) with no
          secrets in the committed file.
    - For `application.yml` / `application-docker.yml`:
        - Remove secret default values (e.g., `minioadmin`, `minioadmin123`) and keep only `${MINIO_ACCESS_KEY}` /
          `${MINIO_SECRET_KEY}` with **no** default.
    - Add a versioned `env.example` (or `envs/example.env`) that documents required variables without real secrets.
    - Outcome: C2 secrets strategy can move to **OK** and blocker #2 is removed.

4) **Define a minimal secrets management strategy in documentation.**
    - Extend `README.md` (or add `doc/SECURITY_SECRETS.md`) with:
        - List of required env variables for each environment (dev, staging, prod).
        - Indication that secrets are stored outside VCS (e.g., GitHub Secrets, Hetzner host env, or secrets manager).
        - Instructions to inject these env vars in Docker Compose `prod` deployment.
    - Outcome: Clarifies how to safely manage secrets for MVP.

5) **Add container resource limits for production deployment.**
    - Update `docker-compose.yml` or introduce `docker-compose.prod.yml` to define per-service
      `deploy.resources.limits` (CPU, memory) for:
        - `backend-service`.
        - `postgres-*` instances.
        - `rabbitmq`, `minio`, and observability stack as needed.
    - Outcome: Satisfies RS6840 best practice for predictable resource usage on the Hetzner host.

6) **Clarify and document the prod Docker Compose stack for Ubuntu Hetzner.**
    - Add a short section to `README.md` or a `doc/DEPLOYMENT_PROD.md` describing:
        - Prerequisites on the Ubuntu host (Docker, Docker Compose).
        - Network creation (`docker network create skillshub-net`).
        - Required env file location (e.g., `/opt/skillshub/envs/prod.env`).
        - Command to start the stack:
          ```bash
          docker-compose --env-file ./envs/prod.env up -d
          ```
    - Outcome: C1 environment documentation moves closer to **OK**.

7) **(Optional but recommended) Add at least one concrete Grafana dashboard JSON to the repo.**
    - Provide a basic dashboard with:
        - CPU usage.
        - Requests per second (RPS).
        - p95 latency.
        - 5xx error rate.
    - Provision it via Grafana provisioning config.
    - Outcome: Observability (F) can move from **PARTIAL/MISSING** elements to **OK** for dashboards.

