# Architecture & Development Plan

This document contains the canonical development plan and rules for this repository.
Follow the "Complete Development Plan (STRICT ORDER)" described by the product owner.
Important constraints:
- Java 17, Spring Boot 3+, Maven
- Package base: com.yourorg.auth
- Follow step-by-step progression. Do not skip steps.
- Uniform API response standard must be used across controllers.
- Outbox pattern will be implemented for publishing domain events.

See top-level README for quick start.
