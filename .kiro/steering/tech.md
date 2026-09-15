# Tech Stack

## Core

- **Java 17**
- **Spring Boot 4.0.8**
- **Spring Batch 6.0.5** — batch processing
- **Spring Cloud 2025.1.3** / **Spring Cloud Task 2.5.0.RELEASE** — runnable under SCDF via `@EnableTask`
- **Spring Data JPA** (Hibernate) over **PostgreSQL** (runtime); **H2** for tests
- **Maven** (via the `./mvnw` wrapper) for build/dependency management

## Libraries

- **Lombok** — getters/setters/builders/logging (annotation processor configured in `pom.xml`)
- **OpenTelemetry / Micrometer OTLP** — metrics and traces exported over OTLP to `localhost:4318` (metrics) / `localhost:4317` (traces). Replaces the legacy Prometheus RSocket proxy.
- **logstash-logback-encoder** — JSON logging
- **Spring Boot Actuator** — management endpoints
- **JUnit 5** + **AssertJ** + **spring-batch-test** — testing

## Spring Batch 6 import notes

Batch 6 split core classes into two module namespaces — keep this in mind when adding imports:

- `org.springframework.batch.core.*` — Job/Step/JobRepository (now under `core.job` / `core.step` / etc.)
- `org.springframework.batch.infrastructure.item.*` — readers/writers/tokenizers/mappers (formerly `org.springframework.batch.item.*`)

`PatternMatchingCompositeLineMapper` lost its no-arg constructor; build the tokenizer/field-set-mapper maps and pass them to `super(...)`. `StepBuilder` now requires `JobRepository` and `PlatformTransactionManager`, so step bean methods take these as parameters and `souJavaJob` passes them through.

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

- Repository tests use `@DataJpaTest` with H2; `src/test/resources/application.properties` overrides the datasource and disables OTLP metrics/tracing export.
- Running the full app locally requires Postgres (`jdbc:postgresql://localhost:5432/dataflow`, user `root` / `rootpw`, hardcoded in `application.properties`).
- The full SCDF/Prometheus/OTel stack is launched via the `docker-compose` invocation documented in `README.md`.
