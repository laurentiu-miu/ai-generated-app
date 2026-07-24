---
name: devops-maven-docker-github
description: Build and delivery standards for Java 25 Spring Boot applications using Maven Wrapper, Docker and GitHub Actions with reproducibility, caching, security and operational health.
compatibility: opencode
metadata:
  build: maven
  container: docker
  ci: github-actions
---

# Maven + Docker + GitHub Actions

## Objectives

Optimize for:
- reproducibility
- fast feedback
- secure runtime
- deterministic CI
- low operational complexity

## Maven Wrapper

Prefer `./mvnw` over requiring developers/CI to install a matching Maven globally.

Typical verification:

```bash
./mvnw verify
```

## Maven

Keep `pom.xml` understandable.

Do not add plugins without purpose.

Keep versions controlled through Spring Boot dependency management, properties where appropriate and explicit plugin versions where needed.

Avoid unnecessary dependency duplication.

## CI Lifecycle

Normal pipeline:
checkout -> setup Java 25 -> cache Maven dependencies -> ./mvnw verify -> package/container -> security checks

Do not skip tests in the final CI build.

## GitHub Actions

Use least permissions required.

Pin or intentionally version important actions.

Use dependency caching.

Do not put secrets in workflow files.

Use repository/environment secrets.

Avoid giant workflow scripts containing application logic.

## Docker

Prefer multi-stage build.

Use an actual maintained Java 25 base image compatible with project requirements.

Do not blindly copy old Java 21 examples.

## Runtime Image

Prefer:
- minimal packages
- non-root user
- explicit work directory
- exec-form ENTRYPOINT
- predictable JVM behavior

Do not install shell/network tooling in runtime image without need.

## Non-Root

Application should run as non-root unless a concrete requirement prevents it.

## Signals

Use exec-form process startup so SIGTERM reaches the JVM correctly.

Spring Boot should have opportunity for graceful shutdown.

## Configuration

Inject environment-specific values at runtime.

Do not bake environment configuration into the image.

Never bake passwords, client secrets, tokens or private keys.

## Health

Use Actuator endpoints for deployment probes where applicable.

Distinguish liveness from readiness.

## PostgreSQL Local Development

Docker Compose may provide PostgreSQL for developers.

Tests should prefer Testcontainers rather than relying on a manually running compose stack.

## Image Reproducibility

Do not use `latest` for production-relevant base images.

Use controlled tags/digests according to organization policy.

## Docker Cache

Order layers so stable dependencies can be reused when practical.

Do not sacrifice maintainability for tiny theoretical cache improvements.

## Security Scanning

Integrate supported tools for dependency and container vulnerabilities.

Treat findings by severity/context.

Do not automatically upgrade major dependencies merely to silence a scanner without compatibility review.

## CI Failure

Never disable failing tests, ignore compiler failures or hide scanner exit codes to obtain a green pipeline.

Fix root cause.

## Deployment

The same artifact/image should ideally move through environments with configuration supplied externally.

Avoid rebuilding different code for each environment.

## Verify

Before completion, where applicable:
- `./mvnw verify`
- `docker build .`

Validate workflow syntax/configuration.

Evidence before claims.
