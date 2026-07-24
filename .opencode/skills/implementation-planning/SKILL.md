---
name: implementation-planning
description: Breaks approved requirements into small coherent implementation tasks with exact responsibilities, dependencies, files, tests and verification steps before coding begins.
compatibility: opencode
metadata:
  workflow: planning
---

# Implementation Planning

Use for multi-step changes after requirements are understood.

The purpose of a plan is to make implementation predictable for an agent with limited context.

## Principles

Plans must be:
- concrete
- executable
- small enough to reason about
- ordered by dependency
- independently verifiable where practical
- free of speculative work

Use:
- DRY
- YAGNI
- test-first where behavior changes
- existing project conventions

## Inspect First

Before planning:
1. inspect relevant code
2. inspect existing tests
3. inspect FEATURE.md
4. inspect ARCHITECTURE.md
5. identify existing patterns
6. identify likely files affected

Do not create a theoretical plan disconnected from the repository.

## Define Interfaces Early

For tasks that interact, define contracts first.

Examples:
- DTO shapes
- method signatures
- URL/query parameter contracts
- database fields
- fragment names
- return types

Do not let independent agents invent incompatible interfaces.

## Task Size

A task should be the smallest coherent unit that:
- produces meaningful behavior
- has its own verification
- could reasonably be accepted or rejected independently

Avoid microscopic tasks.

Bad:
Task 1 create DTO.
Task 2 create mapper.
Task 3 create repository.
Task 4 create service.

Better:
Task 1 implement backend use case with persistence and focused tests.

## Task Template

Use:

```markdown
### Task N: Name

**Goal**
Concrete outcome.

**Agent**
backend-java-spring-boot | frontend-thymeleaf | database-engineer | ...

**Depends on**
Previous task or `none`.

**Files**
- Create: exact/path
- Modify: exact/path
- Test: exact/path

**Contract**
Inputs, outputs and interfaces consumed/produced.

**Steps**
- [ ] create failing behavioral test where practical
- [ ] run and verify expected failure
- [ ] implement minimum required behavior
- [ ] run focused tests
- [ ] refactor without changing behavior
- [ ] run verification

**Verification**
Exact command(s).

**Expected**
Observable successful behavior.
```

## Behavioral Changes

Prefer RED -> GREEN -> REFACTOR.

The important property is not ceremony.

The important property is proving that:
1. the test detects missing/broken behavior;
2. implementation makes it pass;
3. refactoring preserves it.

## Database Changes

Plan migrations before application code that depends on them.

Specify:
- columns/tables/constraints
- migration safety
- indexes
- compatibility impact
- rollback or forward-fix strategy

## Frontend Changes

Specify:
- URL
- controller contract
- model attributes
- template/fragments
- paging/filter/search parameters
- empty/error states

## External Integrations

Specify:
- request
- response
- authentication
- timeout
- failure mapping
- idempotency/retry behavior
- tests

## No Placeholder Tasks

Avoid:
- "add appropriate validation"
- "handle errors"
- "write some tests"
- "implement as needed"
- "etc."
- "TBD"

State exactly what behavior is expected.

## Self Review

Before execution check:
- every acceptance criterion maps to work
- no task duplicates another
- dependencies are ordered
- contracts are consistent
- high-risk decisions already went through architecture gate
- verification exists for each meaningful behavior
