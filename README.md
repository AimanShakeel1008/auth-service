# auth-service

Production-grade Authentication microservice (Spring Boot + Java 17).

## Purpose
Auth service provides:
- Signup (email/username + password)
- Login + JWTs (RS256 planned)
- Refresh tokens (opaque, rotated)
- Email verification, password reset
- Account lockout & rate limiting
- Transactional Outbox → Kafka `UserCreated` event
- JWKS endpoint and other infra endpoints

## Tech stack
- Java 17, Spring Boot 3+
- Maven
- PostgreSQL, Redis
- Kafka (preferred)
- Flyway, MapStruct, Lombok
- Testcontainers for integration tests
- Observability: Micrometer + Prometheus, OpenTelemetry + Jaeger
- MailHog for local email testing

## Branching
- `main` (protected — deployable)
- `develop` (integration)
- feature branches: `feature/<ticket-id>-short-desc`
- hotfix: `hotfix/<ticket-id>-short-desc`

## Step workflow
This repo follows the step-by-step plan in `docs/ARCHITECTURE.md` and the project owner’s step sequence.

## Getting started (dev)
1. Install Java 17, Maven, Docker.
2. Run `mvn -B -DskipTests=false test` (CI will run tests).
3. Next step: implement Step 2 (minimal Spring Boot skeleton).

---
