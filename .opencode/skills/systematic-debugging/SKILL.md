---
name: systematic-debugging
description: Root-cause-first debugging process for bugs, failing tests, build failures, integrations and performance issues using reproduction, evidence, hypothesis testing and regression verification.
compatibility: opencode
metadata:
  workflow: debugging
---

# Systematic Debugging

## Core Rule

DO NOT FIX BEFORE INVESTIGATING ROOT CAUSE.

A plausible guess is not evidence.

## Phase 1 - Establish the Failure

Record:
- expected behavior
- actual behavior
- exact reproduction
- frequency
- environment
- error message
- stack trace
- relevant logs

Read the complete error.

Do not stop at the first line.

## Phase 2 - Reproduce

Determine:
- can it be reproduced reliably?
- under what inputs?
- only locally?
- only CI?
- only production?
- after a particular change?

If not reproducible, gather evidence instead of making speculative changes.

## Phase 3 - Inspect Recent Change

Use where useful:
- git status
- git diff
- git log

Check:
- dependency changes
- configuration
- schema changes
- environment
- deployment
- related refactoring

## Phase 4 - Locate the Failing Boundary

Trace where correct state becomes incorrect.

For Spring:
Browser/client -> Controller -> Service -> Repository -> PostgreSQL

For outbound integration:
Service -> RestClient -> Network -> Remote service

## Data Flow Tracing

When a bad value appears deep in a stack:
1. where was bad value observed?
2. which caller supplied it?
3. where was it created?
4. what assumption allowed it?
5. continue backwards until source is found

Fix source, not only downstream symptom.

## Compare Working vs Broken

Find similar working code.

List actual differences.

Check annotations, transaction boundary, thread, configuration, bean lifecycle, query, serialization, timeout and environment.

## Hypothesis

Form exactly one concrete hypothesis.

Avoid broad "maybe X/Y/Z" guessing.

## Test the Hypothesis

Change one variable.

Prefer observation/instrumentation before implementation changes.

If disproved, create a new hypothesis.

Do not accumulate speculative fixes.

## Fix

Once root cause is supported:
1. create regression test/reproduction where practical
2. make the smallest correct fix
3. run focused verification
4. run broader relevant tests
5. reproduce original scenario again

## Three-Failure Rule

After approximately three genuinely different failed fixes, stop.

Reconsider architecture, assumptions, reproduction or misunderstood contract.

This is a strong candidate for escalation to `problem-solver` or `architect`.

## Async Issues

Inspect:
- executor pool size
- queue
- task lifetime
- exception handling
- scheduling semantics
- overlapping runs
- shared mutable state
- transaction context
- shutdown behavior

Do not use `Thread.sleep` as a synchronization fix.

## Database Issues

Inspect:
- actual SQL
- parameters
- transaction isolation
- locks
- uniqueness
- constraints
- query plan
- connection pool
- transaction duration

## HTTP Issues

Inspect:
- DNS
- connection establishment
- TLS
- timeout
- response timeout
- proxy
- server close
- request size
- retries
- remote logs

Do not add retries before understanding whether operation is safe to retry.

## Performance

Measure.

Identify CPU, memory, DB, network, lock contention, query volume and thread starvation.

Never optimize purely from intuition.

## Evidence Package for Escalation

Prepare:
- Problem
- Expected
- Actual
- Reproduction
- Evidence
- Relevant Code
- Hypotheses Tested
- Changes Tried
- Current Best Hypothesis

## Final Rule

Root cause first.

Focused fix second.

Regression proof third.
