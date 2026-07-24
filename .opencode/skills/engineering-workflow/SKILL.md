---
name: engineering-workflow
description: Efficient end-to-end software delivery workflow for deciding when to analyze, design, implement, test, debug, review and document changes while minimizing premium-model usage.
compatibility: opencode
metadata:
  stack: java-spring
  priority: core
---

# Engineering Workflow

Use this skill for non-trivial feature development, refactoring or bug fixing.

## Primary Principle

LOCAL EXECUTION, PREMIUM JUDGMENT.

Most work should be performed by local agents.

Premium agents are quality gates, not normal implementation workers.

## Sources of Truth

When present, read in this order:

1. user request
2. FEATURE.md
3. ARCHITECTURE.md
4. AGENTS.md
5. existing implementation
6. existing tests

Never silently contradict a higher-priority source.

## Classify the Work

### Trivial

Examples:
- rename
- text change
- minor CSS/Bootstrap adjustment
- simple validation
- obvious compilation fix
- simple DTO field

Workflow:
implement -> focused test -> verify

Do not invoke premium agents automatically.

### Normal

Examples:
- CRUD feature
- REST endpoint
- Thymeleaf list/form
- repository query
- Liquibase migration
- normal integration

Workflow:
requirements -> plan -> local implementation -> local QA -> verification

Premium review only if risk warrants it.

### High Risk

Examples:
- security
- concurrency
- transactions
- important data migration
- new architectural boundary
- major external integration
- breaking API change
- complex performance work

Workflow:
requirements -> architecture gate -> plan -> local implementation -> local QA -> premium review -> documentation

## Feature Workflow

For meaningful new behavior:

1. Understand requested outcome.
2. Inspect existing repository patterns.
3. Read FEATURE.md.
4. Use business analysis only when requirements need formalization.
5. Determine architectural impact.
6. Use premium architect only when architecture risk is meaningful.
7. Create an implementation plan.
8. Decompose by coherent responsibility.
9. Delegate to the relevant local specialist.
10. Test close to implementation.
11. Run independent QA.
12. Fix defects locally.
13. Escalate difficult unresolved defects only after systematic debugging.
14. Use premium code review for meaningful/high-risk changes.
15. Update documentation if externally visible behavior or setup changed.
16. Verify before claiming completion.

## Avoid Over-Orchestration

Do not create a task for every file.

A task should deliver an independently testable piece of behavior.

Keep tightly coupled changes together.

Bad:
- agent A creates entity
- agent B creates repository
- agent C creates service
- agent D creates controller

when all four belong to one small feature.

Better:
- backend agent implements the complete backend slice
- frontend agent implements UI
- database agent handles complex persistence/migration when specialist knowledge is useful

## Parallelization

Parallelize only independent tasks.

Good:
- frontend page built against an already agreed interface
- Docker update independent of business implementation
- documentation after behavior stabilizes

Bad:
- two agents modifying the same service
- database schema and entity model being independently invented
- parallel work with undefined contracts

## Premium Budget Rules

Use `architect` only when a decision is expensive to reverse or difficult to reason about locally.

Use `problem-solver` only after:
- failure reproduced
- evidence collected
- local debugging attempted
- hypotheses tested

Use `code-reviewer` once near completion rather than after every small task.

## Complexity Rules

Prefer:
- simple code
- modular monolith
- package-by-feature
- explicit contracts
- synchronous flows unless async is justified
- database constraints
- established Spring mechanisms

Avoid:
- speculative abstractions
- generic frameworks
- unnecessary microservices
- event-driven architecture without need
- CQRS without need
- unnecessary caching
- unnecessary interfaces

## Stop Conditions

Stop implementation and reassess when:
- requirement contradicts architecture
- migration risks data loss
- security semantics are unclear
- concurrency correctness is uncertain
- three distinct attempted fixes failed
- implementation requires major architecture not in plan

## Completion

Never declare success from agent statements alone.

Completion requires fresh evidence appropriate to the change.

Typical final verification:

```bash
./mvnw verify
```

plus targeted checks where relevant.

Evidence before claims.
