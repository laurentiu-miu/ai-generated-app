---
name: feature-analysis
description: Converts product requests into precise functional requirements, business rules, scenarios, acceptance criteria, edge cases and explicit assumptions for FEATURE.md.
compatibility: opencode
metadata:
  artifact: FEATURE.md
---

# Feature Analysis

Focus on WHAT must happen.

Do not prematurely design HOW Java will implement it.

## Understand

Identify:
- user/business goal
- actors
- trigger
- inputs
- outputs
- state changes
- business rules
- permissions
- failure behavior
- success criteria

## FEATURE.md Structure

Use as applicable:

```markdown
# Feature Name

## Goal
## Actors
## Functional Requirements
## Business Rules
## User Flows
## Validation
## Acceptance Criteria
## Edge Cases
## Non-Functional Requirements
## Out of Scope
## Assumptions
## Open Questions
```

## Requirement IDs

For substantial features use stable IDs:
- FR-001
- BR-001
- AC-001

## Good Requirements

Good:
> FR-003: The user can filter target groups by name using a case-insensitive search.

Bad:
> Add a search box using repository method `findByNameContainingIgnoreCase`.

The second prescribes implementation instead of behavior.

## Acceptance Criteria

Prefer concrete observable outcomes.

Given / When / Then is useful when behavior has state.

## Validation

Separate structural validation from business validation.

Structural examples:
- required
- maximum length
- format

Business examples:
- unique within tenant
- status transition allowed
- referenced entity must exist

## Edge Cases

Always consider where relevant:
- empty input
- blank input
- min/max values
- duplicate operations
- record not found
- already deleted
- empty result
- multiple pages
- invalid page/sort
- concurrency
- retry
- partial failure
- permissions

## Non-Functional Requirements

Capture only when relevant:
- performance
- security
- auditability
- observability
- availability
- scalability
- data retention

Do not invent arbitrary performance targets.

## Assumptions

Use assumptions for small ambiguities that do not materially alter business intent.

Do not silently assume something that changes:
- data ownership
- permissions
- billing
- security
- deletion behavior
- irreversible operations

## Out of Scope

Explicitly state obvious adjacent work that is not requested when scope confusion is likely.

## Avoid Technical Design

Do not define:
- class names
- annotations
- JPA mappings
- package structure
- database indexes
- controller implementation

Those belong to architecture/planning.
