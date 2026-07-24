---
name: spring-rest-api
description: Designs consistent Spring MVC REST APIs with resource-oriented URLs, semantic HTTP status codes, validation, ProblemDetail errors, pagination, compatibility and springdoc OpenAPI documentation.
compatibility: opencode
metadata:
  protocol: http-rest
---

# Spring REST APIs

## Resource Design

Prefer nouns.

Examples:
GET /customers
POST /customers
GET /customers/{id}
PATCH /customers/{id}
DELETE /customers/{id}

Avoid action-style CRUD URLs.

Use action endpoints only when the operation is not naturally CRUD.

## Status Codes

Use semantics correctly.

Common mapping:
- 200 OK
- 201 Created
- 204 No Content
- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 409 Conflict
- 422 Unprocessable Content
- 500 Internal Server Error

For creation prefer 201 Created with Location when appropriate.

Never return 200 containing an error object for an actual HTTP error.

## DTOs

REST boundaries use request/response DTOs.

Do not serialize JPA entities directly.

## Validation

Use Bean Validation for request structure.

Separate malformed/structurally invalid input from business conflicts.

## Error Responses

Use Spring `ProblemDetail` / `application/problem+json`.

Centralize mapping with `@ControllerAdvice`.

Never expose stack trace, SQL, internal class names, credentials or server paths.

## Pagination

Never expose an unbounded large collection.

Offset pagination is appropriate for administrative UI and small/medium collections.

Cursor/keyset pagination is preferred for large datasets and event streams.

Cursor ordering must be deterministic and include a unique tie-breaker.

## Sorting

Whitelist sortable properties.

Do not concatenate raw user-provided sort fields into SQL.

Always provide deterministic ordering.

## Filtering

Use query parameters.

Avoid creating dozens of near-identical endpoints.

## API Compatibility

Before changing existing contract ask whether consumers will break.

Prefer additive compatible changes.

Use explicit versioning when breaking changes genuinely require it.

## Idempotency

Understand operation semantics.

For retryable POST workflows consider an idempotency/business key when duplicate execution would be harmful.

## OpenAPI

Use a compatible `springdoc-openapi-starter-webmvc-*` dependency when REST API documentation is required.

Document meaningful endpoint purpose, parameters, errors, schemas and security.

## Security

Authorize operations at the correct boundary.

Never trust IDs or tenant values solely because the client submitted them.

Validate ownership/permissions server-side.

## Testing

Test at least:
- success
- validation
- not found
- conflict
- authorization where relevant
- serialized shape
- pagination/filtering
- error format
