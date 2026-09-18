# Tech Stack

## Core

- **Java 25** (`java.version` in `pom.xml`; the code targets Java 17 language features but builds/runs on newer JDKs — expect JDK "restricted method / Unsafe" warnings from Maven/Jansi/Guava that are harmless; silence with `MAVEN_OPTS="--enable-native-access=ALL-UNNAMED"`)
- **Spring Boot 4.1.1**
- **Spring Batch 6.0.5** — batch processing
- **Spring Cloud 2025.1.3** / **Spring Cloud Task 2.5.0.RELEASE** — runnable under SCDF via `@EnableTask`
- **Spring Data JPA** (Hibernate) over **PostgreSQL 18** (runtime); **H2** for tests
- **Maven** (via the `./mvnw` wrapper) for build/dependency management

## Libraries

- **Lombok 1.18.48** — getters/setters/builders/logging (annotation processor configured in `pom.xml`)
- **OpenTelemetry / Micrometer OTLP** — metrics, traces, and logs over OTLP. Uses the official **`spring-boot-starter-opentelemetry`** (Boot 4) which wires the OTel SDK autoconfig + `micrometer-tracing-bridge-otel` + `micrometer-registry-otlp` and registers the `Tracer` in the `ObservationRegistry`. Replaces the legacy Prometheus RSocket proxy and the old manually-assembled OTel deps.
  - Metrics endpoint: `management.otlp.metrics.export.url` (default `localhost:4318/v1/metrics`).
  - Traces endpoint (Boot 4 key): `management.opentelemetry.tracing.export.otlp.endpoint` (`localhost:4318/v1/traces`). The Boot 3 key `management.otlp.tracing.export.url` is no longer recognized.
  - Logs endpoint: `management.opentelemetry.logging.export.otlp.endpoint` (`localhost:4318/v1/logs`).
  - `management.tracing.sampling.probability=1.0` — the job runs once and exits, so the default 10% would leave most runs untraced.
- **logstash-logback-encoder 9.0** — JSON console logging. The `CONSOLE` (JSON) appender feeds an `ASYNC` (`LoggingEventAsyncDisruptorAppender`) appender; do NOT nest one appender inside another (logback 1.3+ forbids it — use `<appender-ref>`). `traceId`/`spanId` from the MDC are emitted as `trace_id`/`span_id` fields for Grafana/Tempo correlation.
- **OpenTelemetry Logback appender** (`opentelemetry-logback-appender-1.0:2.10.0-alpha` + `opentelemetry-api-incubator:1.62.0-alpha`, both pinned to match OTel SDK 1.62.0) — ships logs over OTLP. Declared as the `OTEL` appender on the root logger (attached directly, NOT behind `ASYNC`, so trace context in the ThreadLocal is read on the logging thread). `OpenTelemetryAppenderInitializer` (`observability/`) calls `OpenTelemetryAppender.install(openTelemetry)` at startup because Logback initializes before the Spring context.
- **Spring Boot Actuator** — management endpoints
- **JUnit 5** + **AssertJ** + **spring-batch-test** + **jqwik 1.10.1** (property-based testing) — testing

## Spring Batch 6 import notes

Batch 6 split core classes into two module namespaces — keep this in mind when adding imports:

- `org.springframework.batch.core.*` — Job/Step/JobRepository (now under `core.job` / `core.step` / etc.)
- `org.springframework.batch.infrastructure.item.*` — readers/writers/tokenizers/mappers (formerly `org.springframework.batch.item.*`)

`PatternMatchingCompositeLineMapper` lost its no-arg constructor; build the tokenizer/field-set-mapper maps and pass them to `super(...)`. `StepBuilder` now requires `JobRepository` and `PlatformTransactionManager`, so step bean methods take these as parameters and `souJavaJob` passes them through.

## Batch observability (spans → trace IDs in logs)

`BatchConfiguration` injects an `ObservationRegistry` and passes it to the `JobBuilder` and every `StepBuilder` via `.observationRegistry(...)`. This makes Spring Batch 6 create observations (spans) for job/step execution; the OTel bridge then populates the MDC with `traceId`/`spanId`, which the JSON encoder emits as `trace_id`/`span_id`. Without this wiring a plain batch job creates no spans, so log lines carry no trace context.

## Two TransactionManager beans

`spring-cloud-starter-task` registers `springCloudTaskTransactionManager` alongside the JPA `transactionManager`. Any `@Transactional` must use an explicit qualifier: `@Transactional("transactionManager")` from `org.springframework.transaction.annotation.Transactional` (NOT `jakarta.transaction.Transactional`, which has no qualifier attribute). Omitting the qualifier throws `NoUniqueBeanDefinitionException` at step-execution time.

## Common commands

```bash
./mvnw test                                                        # run all tests (in-memory H2, no external services)
./mvnw test -Dtest=TransactionRepositoryTest                       # single test class
./mvnw test -Dtest=TransactionRepositoryTest#testSaveTransaction   # single test method
./mvnw spring-boot:run                                             # run the app (needs PostgreSQL on localhost:5432/dataflow, user root/rootpw)
./mvnw clean package                                               # build the jar
```

- Repository tests use `@DataJpaTest` with H2; `src/test/resources/application.properties` overrides the datasource and disables OTLP metrics/tracing/logging export (`management.otlp.metrics.export.enabled`, `management.tracing.export.otlp.enabled`, `management.logging.export.otlp.enabled` — the Boot 4.1 keys; the old `management.otlp.tracing.export.enabled` is a silent no-op).
- Running the full app locally requires Postgres (`jdbc:postgresql://localhost:5432/dataflow`, user `root` / `rootpw`, hardcoded in `application.properties`).
- **Run the batch job standalone (no SCDF):** start just Postgres with `docker compose -f docker/docker-compose-postgres-only.yml up -d` (a minimal, standalone `postgres:18-alpine` — distinct from `docker-compose-postgres.yml`, which is an *override* layered on `docker-compose.yml` and errors if run alone), then `./mvnw spring-boot:run`. The job runs on startup and the process exits.
- OTLP export (metrics/traces/logs) fails quietly when no collector is listening — the app still runs and console JSON logs still carry trace IDs. To actually view traces/logs, point the endpoints at the LGTM stack in `docker/docker-compose-otel-lgtm.yml`.
- The full SCDF/Prometheus/OTel stack is launched via the `docker-compose` invocation documented in `README.md`.
