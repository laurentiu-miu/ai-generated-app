# File Importer

Spring Boot application for Parent/Child administration and asynchronous CSV imports.

## Requirements

- Java 25
- Docker with Compose

## Run Locally

```bash
docker compose up -d
./mvnw spring-boot:run
```

Open <http://localhost:8080>. PostgreSQL defaults to database/user/password `file_importer` on port `15432`.

Configuration can be overridden with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `APP_UPLOAD_DIRECTORY`, `APP_UPLOAD_MAX_SIZE`, `APP_BATCH_CHUNK_SIZE`, `APP_BATCH_CORE_POOL_SIZE`, `APP_BATCH_MAX_POOL_SIZE`, and `APP_BATCH_QUEUE_CAPACITY`.

## CSV Format

The exact header is:

```csv
recordType,parentExternalKey,parentDisplayName,childExternalKey,childDisplayName,properties
```

`P` rows define Parents and `C` rows define Children. UTF-8 and UTF-8 BOM, quoted commas, and quoted multiline fields are supported. See `samples/example.csv`.

Imports run asynchronously in two Spring Batch chunk steps: all Parents first, then Children. Accepted files remain under the configured upload directory for audit.

## Verify

Docker must be available because integration tests use PostgreSQL Testcontainers.

```bash
./mvnw clean verify
```

Run one test with `./mvnw -Dtest=ClassName test` or one method with `./mvnw -Dtest=ClassName#methodName test`.

Liquibase owns both application and Spring Batch schemas; Hibernate only validates them at startup.
