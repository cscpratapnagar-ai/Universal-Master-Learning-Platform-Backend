# Universal Master Learning Platform - Backend

Enterprise backend for the Universal Master Learning Platform.

## Prerequisites

- JDK 21
- Maven 3.9+
- PostgreSQL 17 (local Docker setup uses host port 5433)
- Mailpit is optional for local email testing (SMTP port 1025, inbox at http://localhost:8025)

## Architecture

Modular monolith with layered architecture:

Controller → Service → ServiceImpl → Repository → Entity

Feature modules own their DTOs, mappers, exceptions and events where needed.

## Local verification

Run the backend test suite from the repository root:

```powershell
mvn --batch-mode --no-transfer-progress test
```

Start PostgreSQL and the backend first, then run the smoke checks from the repository root in Windows PowerShell:

```powershell
.\scripts\smoke-test-local.ps1
```

To target another local backend URL:

```powershell
.\scripts\smoke-test-local.ps1 -BaseUrl "http://localhost:8081"
```

The smoke test checks Actuator health (including PostgreSQL), public platform status, the OpenAPI login route, and the expected `401 Unauthorized` response from the protected profile endpoint when no access token is supplied. A non-zero exit code indicates a failed check.

GitHub Actions runs the Maven test suite on pushes to `main` and pull requests targeting `main`.

## Local service URLs

- Backend API: http://localhost:8080
- Health: http://localhost:8080/actuator/health
- OpenAPI UI: http://localhost:8080/swagger-ui/index.html
- Mailpit inbox (when running): http://localhost:8025
