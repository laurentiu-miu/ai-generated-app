---
name: architecture-design
description: Guides high-impact architecture decisions for Java 25 and Spring Boot 4.1 applications covering modular boundaries, transactions, integrations, consistency, data ownership, security and scalability.
compatibility: opencode
metadata:
  artifact: ARCHITECTURE.md
  stack: spring-boot
---

# Architecture Design

Use for decisions that are expensive to reverse.

Do not create architecture ceremony for trivial changes.

## Default Architecture

Unless requirements justify otherwise:

Browser -> Spring MVC + Thymeleaf -> Application / Domain -> Spring Data JPA -> PostgreSQL

Deploy as one Spring Boot application.

Default style: MODULAR MONOLITH.

## Technology Baseline

- Java 25
- Spring Boot 4.1
- Spring MVC
- Thymeleaf
- Bootstrap 5.3.8
- Spring Data JPA / Hibernate
- PostgreSQL
- Liquibase
- Maven
- Actuator
- RestClient
- springdoc-openapi where REST APIs exist

## Package by Feature

Prefer feature-local packages over global `controller/`, `service/`, `repository/`, `entity/` folders.

Feature boundaries improve locality and reduce cross-application coupling.

## Module Design

For each module determine:
- responsibility
- owned data
- public operations
- dependencies
- events/interactions
- invariants

Keep module APIs smaller than module internals.

## SOLID

Use pragmatically.

Do not create one interface for every class mechanically.

## Transactions

For each use case answer:
1. what must change atomically?
2. where does transaction start?
3. where does it finish?
4. what happens when something fails?
5. are external calls inside the transaction?
6. what happens under concurrent execution?

Prefer short database transactions.

Avoid slow HTTP calls inside transactions unless architecture explicitly demands it.

## Consistency

Prefer the simplest model meeting business requirements.

Use strong transactional consistency when data belongs to one database transaction.

Do not introduce eventual consistency without a real reason.

## Concurrency

Consider:
- database uniqueness
- optimistic locking
- pessimistic locking
- atomic SQL
- idempotency
- duplicate requests

Never use "check then insert" as the only uniqueness protection under concurrency.

## Integrations

For every external dependency decide:
- protocol
- authentication
- timeout
- failure semantics
- retry safety
- idempotency
- monitoring
- data ownership

Do not add automatic retries to non-idempotent operations without design.

## Security

Architecture must establish:
- authentication boundary
- authorization policy
- sensitive data boundaries
- secret management
- audit requirements

## Scaling

Optimize actual bottlenecks.

Typical order:
1. query/index improvement
2. pagination
3. connection pool tuning based on evidence
4. caching where justified
5. horizontal application scaling
6. architectural distribution only when necessary

## Reject by Default

Require explicit justification for:
- microservices
- Kafka/message broker
- CQRS
- event sourcing
- distributed transactions
- multiple databases for one domain
- internal framework creation

## Architecture Decision Format

For durable decisions:

```markdown
## ADR-NNN: Decision

### Context
### Decision
### Alternatives
### Trade-offs
### Consequences
### Risks
```

Prefer the least complex architecture satisfying the requirements.
