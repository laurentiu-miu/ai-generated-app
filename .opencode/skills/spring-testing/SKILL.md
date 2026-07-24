---
name: spring-testing
description: Efficient Java and Spring Boot 4.1 testing with JUnit, AssertJ, focused Spring test slices, Testcontainers and pragmatic RED-GREEN-REFACTOR behavior.
compatibility: opencode
metadata:
  framework: spring-boot-4.1
  testing: junit
---

# Spring Testing

## Principle

Use the smallest test scope that provides confidence.

Preference:
plain unit test > Spring slice test > integration test > end-to-end test

Do not start Spring when a plain Java test is enough.

## Behavior Changes

Prefer RED -> GREEN -> REFACTOR.

### RED
Write a test demonstrating desired behavior.
Run it.
Confirm it fails for the expected reason.

### GREEN
Implement the smallest correct change.
Run the test.

### REFACTOR
Improve design while keeping tests green.

Do not add unrelated behavior during refactoring.

## Unit Tests

For pure business logic:
- JUnit
- AssertJ
- Mockito only for actual collaborators

Do not load Spring context.

## Mockito

Mock boundaries, not the class under test.

Avoid mocks when a real simple object is easier.

Prefer asserting final behavior.

## Controller Tests

Use focused MVC testing such as `@WebMvcTest` with current Spring testing APIs.

Verify:
- status
- validation
- view name or response
- model
- headers
- errors

Do not use `@SpringBootTest` just to test one controller.

## Repository Tests

Use `@DataJpaTest` with PostgreSQL Testcontainers when PostgreSQL semantics matter.

Verify:
- custom queries
- unique constraints
- mappings
- sorting
- pagination
- projections

## RestClient Tests

Use focused REST-client testing facilities such as `@RestClientTest` where appropriate.

Verify:
- method
- URL
- headers
- request serialization
- response mapping
- error mapping

## Full Integration Tests

Use `@SpringBootTest` only when interaction of multiple real application layers is the thing being tested.

Keep the number of full-context tests controlled.

## Testcontainers

Prefer Testcontainers for PostgreSQL and infrastructure behavior that cannot be accurately simulated.

## Test Naming

Names should explain behavior.

## Test Structure

Use clear Arrange / Act / Assert semantics without excessive comments.

Test one coherent behavior.

## Production Scenarios

Prioritize:
1. critical business path
2. boundary values
3. validation
4. failure behavior
5. concurrency where meaningful
6. integration boundaries

## Bug Fixes

A bug fix should normally include a regression test that fails without the fix.

## Avoid Brittle Tests

Do not assert private method calls, irrelevant SQL count, implementation details or exact logging text unless those are genuinely part of required behavior.

## Determinism

Do not use arbitrary `Thread.sleep(...)` for synchronization.

Wait for a meaningful condition where asynchronous behavior is tested.

Control clocks where time affects behavior.

Avoid dependence on execution order.

## Coverage

Coverage is a signal, not the objective.

Do not write meaningless assertions to increase percentage.

## Before Completion

Run focused tests first.

Then, for a meaningful feature:

```bash
./mvnw verify
```

Read actual failures and exit status.

Do not extrapolate from one passing test to the entire build.
