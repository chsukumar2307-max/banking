<!--
Sync Impact Report
Version change: v1.1.0 → v1.1.1
Modified principles: None (patch: date update only)
Added sections: None
Removed sections: None
Templates requiring updates: None found (⚠ pending if templates are later added)
Follow-up TODOs:
- TODO(RATIFICATION_DATE): original adoption date unknown
-->

# Banking Backend Application Constitution

## 1. Architecture Principles
- Follow a layered architecture: Controller, Service, Repository.
- Controllers expose REST APIs and delegate business logic to Services.
- Services encapsulate business logic and interact with Repositories.
- Repositories handle data access using JPA/Hibernate.
- Use DTOs for API request/response payloads; never expose entities directly.
- Use Mapper classes or libraries (e.g., MapStruct) for DTO/entity conversion.
- Design for statelessness in services to support horizontal scaling.

## 2. Coding Standards
- Use Lombok for boilerplate reduction (e.g., @Data, @Builder, @Slf4j).
- Prefer constructor injection for dependencies.
- Follow Java naming conventions: classes (PascalCase), methods/fields (camelCase), constants (UPPER_SNAKE_CASE).
- Use validation annotations (e.g., @NotNull, @Size, @Email) in DTOs and entities.
- All code must be peer-reviewed before merging.
- Maintain clear, up-to-date documentation for all public APIs and modules.

## 3. Security & Compliance
- OTPs must have a configurable expiration (default: 5 minutes).
- Secure API endpoints with authentication and authorization (Spring Security + JWT).
- Never expose internal or sensitive data in API responses.
- Sanitize all inputs and outputs.
- Store secrets (e.g., JWT secret, DB credentials) securely using environment variables or secret managers; never commit secrets to source control.
- Implement audit logging for all sensitive operations (e.g., login, transfers).
- Regularly review dependencies for vulnerabilities.
- Comply with relevant data protection regulations (e.g., GDPR, PCI DSS where applicable).

## 4. Logging Rules
- Use SLF4J for logging throughout the application.
- Log API requests and responses (excluding sensitive data).
- Log errors and exceptions with context.
- Ensure logs do not contain passwords, OTPs, or other sensitive information.

## 5. Error Handling
- Use a GlobalExceptionHandler for centralized error management.
- All API responses must follow a standard format (e.g., status, message, data).
- Return meaningful error messages without leaking internal details.

## 6. Database Rules
- Use JPA/Hibernate for ORM and data access.
- Use UUID or auto-increment IDs for primary keys.
- Store OTPs with creation and expiration timestamps.
- Use MySQL as the primary database.
- Apply Flyway for schema migrations.

## 7. Redis Rules
- Use Redis for temporary OTP caching.
- Configure TTL for OTP keys (default: 5 minutes).
- Ensure Redis is not used for persistent storage.

## 8. Docker Rules
- Application must run in Docker containers alongside MySQL and Redis.
- Provide docker-compose.yml for local development and testing.
- Ensure environment variables are configurable via Docker.
- Containers must be stateless; persistent data must be stored in volumes or external databases.

## 9. Testing & Documentation
- All business logic must be covered by automated unit and integration tests.
- Use test containers or mocks for database and Redis in CI pipelines.
- Maintain Swagger/OpenAPI documentation for all REST endpoints.
- All new features require corresponding updates to documentation and tests.

## Governance
- This constitution supersedes all other project practices and policies.
- Amendments require documentation, team approval, and a migration plan if breaking.
- Versioning follows semantic versioning: MAJOR for breaking/removal, MINOR for new/expanded principles, PATCH for clarifications.
- Compliance is reviewed before each release; non-compliance must be remediated before deployment.
- Ratification and amendment dates must be recorded in ISO format (YYYY-MM-DD).

**Version**: v1.1.1 | **Ratified**: TODO(RATIFICATION_DATE): original adoption date unknown | **Last Amended**: 2026-03-17


