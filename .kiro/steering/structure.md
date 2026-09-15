# Project Structure

## Layout

```
src/main/java/com/example/demo/
├── SpringBatchDemoApplication.java     # @SpringBootApplication + @EnableTask entry point
├── config/
│   └── BatchConfiguration.java         # declares the whole job (souJavaJob) + steps + reader/writer beans
├── models/                             # JPA entities (Register, Transaction, RegTypeOne/Two/Three, FileHeader, FileFooter)
├── repositories/                       # Spring Data JPA repositories (TransactionRepository)
└── steps/
    ├── chunklets/                      # ItemProcessor + ItemWriter for the chunk step
    ├── mappers/                        # LineMapper, FieldSetMappers, RecordSeparatorPolicy
    ├── tasklets/                       # Tasklet steps (FileDownloadTasklet — currently a stub)
    └── tokenizers/                     # FixedLengthTokenizer definitions per record type

src/main/resources/
├── application.properties              # datasource, OTLP export config
├── banner.txt
└── input/                              # sample fixed-length input files (exemplo-sou-java-10.txt is the default)

src/test/java/com/example/demo/         # JUnit 5 tests (SpringBatchDemoApplicationTests, TransactionRepositoryTest)
src/test/resources/                     # test application.properties (H2, OTLP disabled)

docker/                                 # docker-compose files + Dockerfile for the SCDF/OTel stack
grafana/                                # Grafana dashboard JSON
docs/                                   # design/spec docs (e.g. the Spring Batch 6 upgrade spec)
```

## Batch flow (config/BatchConfiguration.java)

Job `souJavaJob` = `fileDownloadingStep` (a Tasklet, currently a logging stub) → `transactionProcessingStep` (chunk size 2500: `FlatFileItemReader` → `TransactionItemProcessor` → `TransactionItemWriter` → JPA).

- **Record separation** — `DefaultRecordSeparationPolicy` concatenates the 4 physical lines into one logical record (`isEndOfRecord` triggers on the next `0100`/`0000`/`9999`; has an `isFirstRead` guard).
- **Line mapping** — `DefaultCompositeLineMapper` (extends `PatternMatchingCompositeLineMapper`) dispatches by prefix: `0100*` → `TransactionTokenizer` + `TransactionFieldSetMapper`; `*` → `DefaultTokenizer` + `DefaultFieldSetMapper`. Header/footer mappers exist but are commented out (TODO re: generics).
- **Reader** — `@StepScope` `FlatFileItemReader` reads job parameters `baseDir` (default `/input/`) and `fileName` (default `exemplo-sou-java-10.txt`) via SpEL, resolved to a `ClassPathResource`.

## Conventions / gotchas

- `@Configuration` is CGLIB-proxied (`proxyBeanMethods=true`), so intra-class calls like `reader(null, null)` return the real `@StepScope` proxy — these nulls are intentional, do NOT "fix" them.
- Entities: `Register` is a `@MappedSuperclass`; `Transaction` is the aggregate root; `RegTypeOne/Two/Three` use `@OneToOne` + `@MapsId` to share the transaction's primary key. Don't assign different ids to the reg types — the mapper copies the transaction id onto them on purpose.
- Code and comments are in **Portuguese** (Brazilian developer). Match this when editing existing code.
- Commit messages use conventional-commit prefixes (`fix:`, `chore:`, etc.).
