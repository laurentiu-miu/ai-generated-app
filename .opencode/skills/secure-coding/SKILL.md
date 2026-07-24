---
name: secure-coding
description: Security-focused Java and Spring coding rules covering authorization, validation, secrets, SQL injection, XSS, CSRF, logging, file uploads, outbound HTTP and safe defaults.
compatibility: opencode
metadata:
  stack: java-spring
  domain: security
---

# Secure Coding

Security should be proportional to the application risk.

Do not introduce complex security architecture without requirements.

Do not ignore obvious vulnerabilities.

## Trust Boundaries

Treat all external input as untrusted:
- HTTP body
- query parameters
- path variables
- headers
- uploaded files
- external API responses
- imported files

Validate at boundaries.

## Authentication vs Authorization

Authentication answers: Who are you?

Authorization answers: Are you allowed to perform this operation?

Do not assume authenticated means authorized.

Check authorization server-side.

## Object Access

Never authorize solely because the user knows an ID.

For tenant/ownership-bound resources verify resource ownership before performing the operation.

## Bean Validation

Use Bean Validation for structural input constraints.

Business authorization/rules require separate checks.

## SQL Injection

Prefer Spring Data parameter binding, JPQL parameters and prepared statements.

Never concatenate untrusted values into SQL.

Whitelist dynamic sort fields and identifiers.

## XSS

Thymeleaf escaping should remain enabled.

Avoid `th:utext` for untrusted values.

Avoid embedding untrusted values into inline JavaScript.

## CSRF

For browser-based state-changing actions using session/cookie authentication, maintain Spring Security CSRF protections unless architecture has a sound reason not to.

Do not casually disable CSRF globally to fix a form error.

## Secrets

Never commit passwords, access tokens, private keys or API secrets.

Use external configuration/secret management.

Do not log secrets.

## Logging

Avoid logging credentials, tokens, full authorization headers and sensitive payloads unless explicitly permitted.

## Error Messages

Client-visible errors must not reveal SQL, stack traces, internal paths, class names, credentials or secret configuration.

## HTTP Clients

For RestClient:
- use HTTPS when required
- configure timeouts
- verify authentication
- validate expected response handling
- do not blindly trust remote content

Do not globally disable TLS certificate verification.

## SSRF

When the application accepts URLs from users, do not blindly let the server request arbitrary destinations.

Restrict scheme, host, port and private/internal networks according to requirements.

## File Uploads

When accepting files:
- limit size
- validate expected content/type
- do not trust original filename
- generate server-controlled storage names
- prevent path traversal
- keep uploads outside executable application paths
- consider malware scanning when risk requires it

Never concatenate user filename directly into filesystem path.

## Path Traversal

Normalize and restrict paths.

Ensure resolved path remains inside allowed root.

## Deserialization

Do not deserialize arbitrary polymorphic Java objects from untrusted input.

Use explicit DTOs.

## Mass Assignment

Do not bind external input directly into persistence entities.

Use request DTOs that contain only allowed fields.

## CORS

Do not indiscriminately enable every origin.

Configure only required origins/methods/headers.

## Security Headers

Use Spring Security defaults where appropriate.

Do not remove protective headers without understanding consequence.

## Dependencies

Avoid unnecessary dependencies.

Review security-sensitive upgrades carefully.

Do not use abandoned libraries where maintained platform functionality exists.

## Randomness

For security-sensitive random values use cryptographically secure mechanisms.

Do not use `Random` for tokens.

## Passwords

Never store plaintext passwords.

Use the password encoder configured by Spring Security and current organizational policy.

## Database

Use least-privileged database credentials.

## Review Checklist

For security-sensitive changes check:
- authentication
- authorization
- tenant/ownership
- validation
- SQL injection
- XSS
- CSRF
- secrets
- logging
- error leakage
- file/path safety
- outbound HTTP
