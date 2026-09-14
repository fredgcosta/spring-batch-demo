# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **Migration completed (2026-09):** this repo was upgraded from Spring Boot 2.2.6/Batch 4.x to **Spring Boot 4.0.8 / Spring Batch 6.0.5 / Spring Cloud 2025.1.3 / Java 17**, with metrics moved from Prometheus RSocket to OpenTelemetry (OTLP). The spec with the full plan, decisions and verification gates is in [`docs/spring-batch-6.0.5-upgrade-spec.md`](docs/spring-batch-6.0.5-upgrade-spec.md). The docker/SCDF/OTel Collector stack (Fase 3 of the spec) was updated but treated as an unverified spike — image tags were confirmed to exist on Docker Hub and compose files were validated with `docker compose config`, but the full stack was not spun up end-to-end.

## Project Overview

Spring Boot 4.0.8 (Java 17) batch application that reads a fixed-length flat file of bank-style transactions, splits each multi-line record into related JPA entities, and persists them to PostgreSQL. It is also packaged as a Spring Cloud Task app (`@EnableTask`) to run under Spring Cloud Data Flow (SCDF). The app itself is a fork-friendly demo/template — much of the batch logic is stubbed with TODOs.

## Commands

```bash
./mvnw test                                        # run all tests (uses in-memory H2, no external services)
./mvnw test -Dtest=TransactionRepositoryTest       # run a single test class
./mvnw test -Dtest=TransactionRepositoryTest#testSaveTransaction  # run a single test method
./mvnw spring-boot:run                             # run the app (needs PostgreSQL on localhost:5432/dataflow, user root/rootpw)
./mvnw clean package                               # build jar
```

- Tests are JUnit 5. Repository tests use `@DataJpaTest` with H2 (`src/test/resources/application.properties` overrides the datasource and disables OTLP metrics/tracing export).
- Running the full app locally requires Postgres; the main `application.properties` hardcodes `jdbc:postgresql://localhost:5432/dataflow` (user `root` / `rootpw`).
- Spring Batch 6 split core classes into two modules: `org.springframework.batch.core.*` (Job/Step/JobRepository, now under `core.job`/`core.step`/etc.) and the new `org.springframework.batch.infrastructure.item.*` (readers/writers/tokenizers/mappers, formerly `org.springframework.batch.item.*`). Keep this in mind when adding new imports.

## Batch Architecture (the non-obvious part)

The whole job is declared in `BatchConfiguration`: job `souJavaJob` = `fileDownloadingStep` (a `Tasklet` that is currently a logging stub) → `transactionProcessingStep` (chunk of 2500: FlatFileItemReader → TransactionItemProcessor → TransactionItemWriter → JPA).

### Multi-line record format
This is the core concept: **one logical transaction record spans 4 physical lines** in the input file:

- Line starting `0000` — file header (first line of file; skipped by the reader via `linesToSkip(1)`)
- Line starting `0100` — transaction line (id + 3 fields)
- Lines starting `0101`, `0102`, `0103` — three sub-register lines (3 fields each)
- Line starting `9999` — file footer

`DefaultRecordSeparationPolicy` implements `RecordSeparatorPolicy` to concatenate the physical lines into a single logical record: `isEndOfRecord` returns true when the next `0100`/`0000`/`9999` line arrives, and `postProcess` shifts the buffered lines. It has an `isFirstRead` guard so the first `0100` line doesn't terminate a (nonexistent) previous record.

`DefaultCompositeLineMapper` (extends `PatternMatchingCompositeLineMapper<Transaction>`) dispatches on the record prefix:
- `0100*` → `TransactionTokenizer` (a `FixedLengthTokenizer` with 17 named column ranges over the ~118-char joined record) + `TransactionFieldSetMapper`
- `*` (everything else, e.g. the `9999` footer) → `DefaultTokenizer` (only the 4-char regId) + `DefaultFieldSetMapper`

Since Batch 6 removed `PatternMatchingCompositeLineMapper`'s no-arg constructor, the tokenizer/field-set-mapper maps are built and passed to `super(...)` instead of being set via setters.

FileHeader/FileFooter tokenizers and mappers exist but are **commented out** in `DefaultCompositeLineMapper` with a TODO about generics — do not assume header/footer lines are fully supported.

### Entity model
`Register` is a `@MappedSuperclass` with `id` (the `@Id`) and `regId` (the 4-char line type code). `Transaction` (regId `0100`) is the aggregate root; `RegTypeOne/Two/Three` (regIds `0101`/`0102`/`0103`) are `@OneToOne` back-references with `@MapsId`, so **all four entities share the same primary key** (the transaction id). `Transaction` owns the associations with `cascade = ALL`, so `TransactionItemWriter` only saves `Transaction` items and the three `RegType*` rows are cascaded. Don't set different ids on the reg types — the mapper copies the transaction id onto them on purpose.

### Reader parameters
The `FlatFileItemReader` is `@StepScope` and reads job parameters `baseDir` (default `/input/`) and `fileName` (default `exemplo-sou-java-10.txt`) via SpEL `#{jobParameters[...] ?: ...}`, resolved to a `ClassPathResource`. The `reader(null, null)` calls inside `chunkletStep()` look like a bug but are intentional — `@Configuration` classes are CGLIB-proxied (`proxyBeanMethods=true`, unchanged in Boot 4), so the intra-class call is intercepted and returns the real `@StepScope` proxy instead of ever executing the method body with nulls. This is a `@Configuration` behavior, unrelated to the Batch version — don't "fix" these nulls. `chunkletStep`/`taskletStep` do now take `JobRepository`/`PlatformTransactionManager` parameters (required by the Batch 6 `StepBuilder` API), so `souJavaJob` must pass them through explicitly when calling those bean methods.

### Two `TransactionManager` beans
`spring-cloud-starter-task` registers its own `springCloudTaskTransactionManager` bean alongside the JPA `transactionManager`, so any `@Transactional` needs an explicit qualifier (`@Transactional("transactionManager")`, using `org.springframework.transaction.annotation.Transactional`, not `jakarta.transaction.Transactional` which has no bean-qualifier attribute) — see `TransactionItemWriter`. Omitting it throws `NoUniqueBeanDefinitionException` at step-execution time (compiles fine, only fails when the step runs).

## Other Notes

- Metrics/traces are exported via OTLP (`management.otlp.metrics.export.*` / `management.otlp.tracing.export.*` in `application.properties`), sent to `localhost:4318`/`localhost:4317` by default. Tests disable both. The old Prometheus RSocket proxy setup was removed.
- `docker/docker-compose*.yml` spin up the full SCDF environment (Data Flow server, Skipper, Kafka, Postgres, pgAdmin, Prometheus, OTel Collector) — see README for the exact `docker-compose` invocation with env vars. The `dataflow-server` service builds a local image from `docker/Dockerfile` (base image `springcloud/spring-cloud-dataflow-server:2.11.5-jdk17`). `docker/docker-compose-prometheus.yml` runs an OpenTelemetry Collector (`docker/otel-collector-config.yaml`) that receives OTLP and exposes a Prometheus-scrapable endpoint at `:8889`, replacing the old `prometheus-rsocket-proxy`.
- Grafana dashboard JSON files live in `grafana/` for the SCDF/Prometheus setup.
- Sample input files are in `src/main/resources/input/` (`exemplo-sou-java-10.txt` is the default; a 1M-record 7z archive is also present).
- Code and comments are in Portuguese (developer is Brazilian); commit messages use conventional-commit prefixes (fix:, chore:).
