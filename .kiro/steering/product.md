# Product

Spring Batch demo/template application that ingests a fixed-length flat file of bank-style transactions, splits each multi-line logical record into related JPA entities, and persists them to PostgreSQL.

It is packaged as a Spring Cloud Task app (`@EnableTask`) so it can run standalone or under Spring Cloud Data Flow (SCDF). Observability is exported via OpenTelemetry (OTLP) and surfaced through Prometheus/Grafana.

The repo is intended as a fork-friendly starting point: much of the batch logic is a working skeleton with intentional stubs and TODOs (e.g. the file-download step is a logging stub, and header/footer mapping is present but commented out). Do not assume every feature is fully wired up.

## Domain concept: multi-line records

One logical transaction spans four physical lines in the input file, distinguished by a 4-char prefix (regId):

- `0000` — file header (first line, skipped by the reader)
- `0100` — transaction line (aggregate root)
- `0101` / `0102` / `0103` — three sub-register lines
- `9999` — file footer

All four entities for a transaction share the same primary key (the transaction id). `Transaction` (regId `0100`) owns the associations and cascades saves to the three `RegType*` rows.
