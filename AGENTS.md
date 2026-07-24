# File Importer Agent Guide

## Start Here

- `PROJECT_SPECIFICATION.md` is the current functional source of truth. `FEATURE.md` and `opencode.json` are empty, and `README.md` is only a placeholder; do not infer requirements from them.
- The application is implemented under `src/`; `pom.xml`, Maven Wrapper, `compose.yaml`, Testcontainers tests, and CI are present.
- `.opencode/package.json`, its lockfile, and `node_modules` support OpenCode itself. They are not an application frontend toolchain.
- Repo-local specialist agents and skills live under `.opencode/`. Preserve them when bootstrapping the application.

## Required Shape

- Build one modular Spring Boot JAR with Java 25, Spring Boot 4.1, Spring MVC, Thymeleaf, Bootstrap 5 via CDN, minimal vanilla JavaScript, Spring Data JPA, Spring Batch, PostgreSQL, Liquibase, Maven Wrapper, JUnit 5, and PostgreSQL Testcontainers.
- Do not introduce Lombok, React/Angular/Vue, an npm application pipeline, WebFlux, H2, Flyway, or a separate frontend application.
- Keep web controllers/DTOs, business services, persistence, batch processing, and shared JSON/error concerns separate. Controllers contain no business logic; use constructor injection and never expose JPA entities from JSON endpoints.

## Domain And Data Traps

- IDs for `Parent`, `Child`, and `FileImport` are application-generated UUID v7. Dynamic properties are non-null `Map<String,Object>` values stored as PostgreSQL `jsonb`; blank form/CSV input means `{}`, and the JSON root must be an object.
- `externalKey` is import-only, optional for persisted records, and globally unique separately for Parents and Children. Manual CRUD neither requests nor generates it. Comparisons are exact and case-sensitive.
- A Child cannot move between Parents. Verify nested Child URLs against the Parent in the URL. Parent deletion is restricted when Children exist; do not use `CascadeType.ALL`.
- Use optimistic locking for Parent, Child, and FileImport. Forms must submit the version for edits and deletes; stale writes and deleting a Parent with Children return HTTP 409.
- Liquibase owns all application and Spring Batch metadata schema. Keep Hibernate at `ddl-auto: validate`; use PostgreSQL `uuid`, `jsonb`, and `timestamp with time zone`, with explicitly named constraints/indexes from the specification.

## CSV Import Traps

- `POST /imports` must securely persist the upload and enqueue Spring Batch work, then redirect immediately. Keep accepted files for audit; remove a copied file if post-copy validation fails.
- Store under a configurable root using a generated UUID filename, normalized containment checks, and symlink-escape protection. Never use the client filename as the physical path.
- Parse UTF-8/BOM CSV as a stream with a real CSV parser. Support quoted commas/newlines and LF/CRLF; report the physical line where the parser finishes the record. Never use `String.split` or load the whole file.
- Process Parents completely before Children regardless of file order, using two chunk-oriented steps (default chunk size 100). The first occurrence claims an external key even if that row later fails another validation; track Parent and Child duplicates separately.
- Valid rows commit despite invalid rows. Persist row errors and update import counters at chunk boundaries. `processedRows = successfulRows + failedRows + skippedRows` and must not exceed `totalRows`.
- Progress polling is sequential Fetch polling at about one second only for `QUEUED`/`RUNNING`; stop at terminal status and derive percentages only from persisted counters.

## Commands And Verification

- Local application startup: `docker compose up -d`, then `./mvnw spring-boot:run`.
- Full delivery gate: `./mvnw clean verify`. Integration tests require Docker because database-sensitive tests use PostgreSQL Testcontainers; never substitute H2.
- Focus a test with `./mvnw -Dtest=ClassName test` or `./mvnw -Dtest=ClassName#methodName test`.
- Before claiming completion, also prove Liquibase startup/schema validation and the documented Docker startup path; a passing compile alone is insufficient.
