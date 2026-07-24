---
name: project-documentation
description: Creates and maintains concise README and developer documentation based strictly on actual repository behavior, configuration, build commands, runtime setup and operational interfaces.
compatibility: opencode
metadata:
  artifact: README.md
---

# Project Documentation

Documentation must describe the application that actually exists.

Inspect repository before writing.

## Goals

A developer unfamiliar with the project should quickly understand:
1. what it does
2. how it is structured
3. what is required locally
4. how to run it
5. how to test it
6. how to configure it
7. how to troubleshoot it

## Do Not Invent

Never invent:
- environment variables
- ports
- endpoints
- commands
- dependencies
- features
- architecture
- configuration

Derive them from repository.

## README Suggested Structure

Use only relevant sections:
- Project
- Purpose
- Architecture
- Technology Stack
- Requirements
- Getting Started
- Database
- Configuration
- Build
- Testing
- Running Locally
- Docker
- API Documentation
- Operational Endpoints
- Troubleshooting
- Development Notes

## Purpose

Explain business purpose in a few sentences.

Do not turn README into marketing copy.

## Architecture

Explain major structure only if actually true.

Link to `ARCHITECTURE.md` for deeper decisions.

## Stack

Include exact major versions where repository defines them.

Do not guess dependency versions.

## Requirements

List actual prerequisites only.

## Commands

Prefer copy-pasteable commands.

Prefer Maven Wrapper over assuming global Maven.

## Configuration

Tables work well for configuration.

Never publish real secrets.

Use placeholders.

## Database

Describe PostgreSQL requirement, local startup method, Liquibase behavior and useful setup/reset commands if repository provides them.

Do not suggest destructive commands without clear warning.

## Running

Provide the shortest reliable startup path.

## Testing

Describe actual test commands and any integration-test prerequisites.

Do not claim tests exist that do not exist.

## Docker

Document actual Docker/Compose files.

Include build, run, relevant ports and configuration.

## API

When springdoc exists, document actual API documentation path.

Do not assume a Swagger URL without confirming project configuration.

## Actuator

Document health/readiness endpoints only if enabled and accessible as described.

## Troubleshooting

Include recurring real issues.

Avoid speculative giant troubleshooting lists.

## Updating Existing README

Prefer targeted edits.

Preserve useful project-specific content.

Remove stale documentation when implementation changes.

## Documentation Quality

Prefer concise, factual, executable and structured documentation.

Avoid duplicated source code, huge prose and generated filler.

## Verification

Before finishing:
1. compare commands to project
2. compare file paths
3. compare versions
4. compare environment variables
5. compare URLs/endpoints
6. inspect documentation diff

Documentation is wrong if it cannot be followed.
