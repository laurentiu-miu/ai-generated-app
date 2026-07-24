---
name: verification-code-review
description: Evidence-based completion and code-review gate that verifies requirements, build, tests, security, transactions, database behavior, maintainability and actual repository diff before success is claimed.
compatibility: opencode
metadata:
  workflow: verification
---

# Verification and Code Review

## Fundamental Rule

NO COMPLETION CLAIM WITHOUT FRESH EVIDENCE.

Never infer "tests pass", "build works", "bug fixed" or "feature complete" from code inspection alone.

## Verification Gate

Before stating success:
1. identify what proves the claim
2. execute it
3. read output
4. inspect exit status/failures
5. compare evidence to claim
6. report actual state

## Agent Reports

Do not trust "backend agent says done" as verification.

Inspect `git diff` and run relevant commands.

## Review Order

1. Requirements
2. Correctness
3. Architecture
4. Security
5. Transactions
6. Database
7. Errors
8. Performance
9. Tests
10. Maintainability

## Requirements

Check FEATURE.md for missing acceptance criteria, changed semantics and hidden assumptions.

## Correctness

Look for incorrect conditions, null handling, boundary errors, duplicate behavior and invalid state transitions.

## Architecture

Check implementation against ARCHITECTURE.md.

Look for accidental new architecture.

## Security

Check relevant security boundaries.

## Transactions

Check transaction location, rollback behavior, remote calls inside transaction, self-invocation and concurrency.

## Database

Check constraints, migration safety, indexes, N+1, locking and query scalability.

## Errors

Check swallowed exceptions, misleading status codes, leaked internals and missing contextual logging.

## Performance

Check obvious risks such as unbounded queries, N+1, HTTP call per row and enormous in-memory collections.

Do not speculate about micro-optimizations.

## Tests

Tests should prove behavior.

Ask:
- would this test fail if behavior broke?
- is important failure behavior covered?
- is PostgreSQL tested where PostgreSQL matters?
- are mocks hiding real behavior?

## Maintainability

Look for duplication, unnecessary generic abstractions, excessively large methods/classes, unclear names and mixed responsibilities.

## Finding Severity

BLOCKER: cannot safely deliver.

HIGH: material correctness/security/data risk.

MEDIUM: real defect or significant maintainability issue.

LOW: small improvement/polish.

Do not inflate severity.

## Review Discipline

Do not create findings to look useful.

A clean implementation may legitimately have no significant findings.

Do not demand stylistic rewrites without engineering value.

## Final Result

Use one:
- APPROVE
- APPROVE WITH MINOR FINDINGS
- CHANGES REQUIRED
- BLOCKED

BLOCKER or unresolved HIGH issues prohibit approval.

## Final Verification

For meaningful Java work normally run:

```bash
./mvnw verify
```

Read the output.

Evidence beats confidence.
