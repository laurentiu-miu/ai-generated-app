---
name: postgres-jpa-liquibase
description: PostgreSQL, Spring Data JPA and Liquibase standards for relational modeling, mappings, constraints, indexes, query performance, concurrency and safe production migrations.
compatibility: opencode
metadata:
  database: postgresql
  persistence: jpa
  migration: liquibase
---

# PostgreSQL + JPA + Liquibase

## Start With Domain Invariants

Design in this order:
1. business entities
2. relationships
3. invariants
4. relational schema
5. constraints
6. indexes
7. JPA mapping

Do not start from Java annotations and let Hibernate invent the database design.

## Normalization

Prefer normalized relational modeling.

Denormalize only for a measured/understood reason.

Avoid EAV unless requirements genuinely require arbitrary attributes and relational alternatives are inadequate.

## Naming

Prefer consistent `snake_case`.

Follow existing project conventions when they differ.

## Data Types

Use types that represent the domain.

Use `uuid`, `boolean`, `date`, `timestamptz` and numeric/decimal for exact financial values as appropriate.

Do not use floating-point for money.

## Time

Prefer PostgreSQL `TIMESTAMPTZ` for points in time.

Use Java `Instant` when representing a UTC point in time.

## Constraints

Use the database to preserve durable integrity.

Consider:
- NOT NULL
- UNIQUE
- FOREIGN KEY
- CHECK

Application validation alone is not enough under concurrent writers or alternate data access paths.

## JPA Relationships

Default to LAZY unless a deliberate reason exists otherwise.

Avoid unnecessary bidirectional mappings.

Be careful with cascade operations.

Do not cascade deletes across large or loosely owned aggregates casually.

## N+1

Detect and prevent N+1 queries.

Possible solutions:
- fetch join
- `@EntityGraph`
- projection
- dedicated query

Do not solve everything with EAGER fetching.

## Read Models

For list/search/reporting queries, consider DTO/interface projections rather than loading large entity graphs.

Select what is needed.

## Repository Queries

Keep queries explicit enough to understand behavior.

For complicated business/reporting queries, JPQL or native SQL can be preferable to unreadable derived method names.

## Pagination

For UI/admin pages `Pageable` is often sufficient.

For very large traversal/history/event datasets prefer keyset/cursor pagination.

Stable ordering is mandatory.

## Indexing

Create indexes because queries need them.

Analyze:
- WHERE
- JOIN
- ORDER BY
- UNIQUE constraints

For composite indexes consider leading columns and access patterns.

Every extra index increases storage, insert cost, update cost and maintenance.

## Query Performance

Before optimizing, use `EXPLAIN (ANALYZE, BUFFERS)` where appropriate in a safe environment.

Look for:
- sequential scans on large filtered tables
- bad cardinality estimates
- expensive sorts
- repeated queries
- N+1
- unnecessary columns
- missing indexes

## Concurrency

Never implement uniqueness only as check-then-insert.

Use a unique constraint and handle the resulting conflict appropriately.

For mutable concurrent records consider optimistic locking when business semantics fit.

Use pessimistic locks only when justified.

## Transactions

Database operations that must succeed atomically belong in one transaction.

Keep transaction duration minimal.

Know isolation implications.

Do not assume transaction isolation prevents every business race.

## Liquibase

Never change an already-applied changeset.

Create a new changeset.

A changeset should represent a coherent migration.

Use stable unique IDs.

Follow existing repository conventions.

## Migration Safety

Before production migration consider:
- table size
- locks
- backfill volume
- index creation cost
- nullability
- deployment ordering
- old application compatibility
- rollback/forward-fix

For risky changes use expand/contract patterns where appropriate.

## Data Backfills

Large backfills should not automatically run as one huge startup migration.

Consider a separate controlled process where appropriate.

## Tests

Persistence tests should validate actual database behavior when it matters.

Use PostgreSQL Testcontainers for:
- custom SQL
- constraints
- PostgreSQL-specific features
- locking/concurrency semantics
- migrations

H2 is not a substitute when PostgreSQL behavior is relevant.
