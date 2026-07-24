---
name: spring-boot-4-java-25
description: Production-focused implementation standards for Java 25 and Spring Boot 4.1 including structure, dependency injection, configuration, transactions, HTTP clients, observability and maintainability.
compatibility: opencode
metadata:
  language: java-25
  framework: spring-boot-4.1
---

# Java 25 + Spring Boot 4.1

## Required Baseline

Use:
- Java 25
- Spring Boot 4.1
- Maven
- constructor injection
- package-by-feature
- Bean Validation
- Actuator
- externalized configuration

Prefer current Spring Boot 4 APIs instead of legacy/deprecated APIs.

## Dependency Injection

Use constructor injection.

Do not use field injection.

## Java 25

Prefer language features that improve readability.

Use records for immutable data carriers.

Do not use records for mutable JPA entities.

Prefer immutable values.

## Package by Feature

Prefer feature-local code over global technical-layer folders.

## Component Responsibilities

Controller: HTTP/UI concerns only.

Service: business use case and transaction boundary.

Repository: persistence access.

Entity: persistent state and domain invariants appropriate to entity.

Do not create a service merely to forward a repository call when it adds no value.

## DTO Boundaries

Never expose JPA entities directly through public REST APIs.

Use dedicated request DTOs, response DTOs and projections.

## Validation

Use Bean Validation for request structure.

Put business rules in business logic.

Important durable rules should also exist as database constraints.

## Configuration

Use `@ConfigurationProperties` for related settings.

Do not scatter many `@Value` fields across components.

Never hardcode passwords, API tokens, secrets or production URLs.

## Transactions

Put transactions around business use cases.

Keep transactions short.

Avoid remote HTTP calls inside database transactions unless architecture explicitly requires it.

Understand proxy behavior.

Do not assume `@Transactional` works on self-invocation.

Do not catch an exception and accidentally prevent expected rollback.

## RestClient

For normal synchronous outbound HTTP prefer Spring `RestClient`.

Centralize:
- base URL
- authentication
- timeouts
- error mapping
- observability

Avoid introducing a reactive stack solely for HTTP calls in an otherwise blocking Spring MVC application.

## Error Handling

Use domain/application exceptions for meaningful failures.

Map errors at application boundaries.

Do not swallow exceptions and continue as if operation succeeded.

Preserve useful cause information.

## Logging

Use SLF4J parameterized logging.

Never log secrets.

Avoid logging full sensitive payloads.

## Actuator

Applications should expose appropriate operational endpoints.

At minimum consider:
- health
- readiness
- liveness where deployment platform uses them
- metrics

Do not expose sensitive actuator endpoints publicly without security consideration.

## Time

Use `Instant` for timestamps representing points in time unless business semantics require a local date/time.

Store timestamps consistently, normally UTC.

## Collections

Return empty collections rather than `null`.

Avoid unnecessary mutation.

## Optional

Use `Optional` primarily for return values representing absence.

Avoid Optional entity fields, Optional method parameters and unsafe `Optional.get()`.

## Performance

Prevent:
- N+1 queries
- loading huge tables
- unbounded list endpoints
- excessive mapping
- remote calls in loops

Measure before introducing complex optimization.

## Maintainability

Prefer boring, readable Spring code.

Avoid:
- generic base-service frameworks
- generic repositories over Spring Data
- reflection-based internal frameworks
- unnecessary factories
- one-interface-per-class ceremony

Simple code is a feature.
