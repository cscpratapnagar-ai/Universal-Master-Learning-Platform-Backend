# Universal Master Learning Platform - Backend

Enterprise backend for the Universal Master Learning Platform.

## Architecture
Modular monolith with layered architecture:
Controller → Service → ServiceImpl → Repository → Entity

Feature modules own their DTOs, mappers, exceptions and events where needed.

## Local verification

Start PostgreSQL and the backend first, then run the smoke checks from the repository root in Windows PowerShell:

```powershell
.\scripts\smoke-test-local.ps1
```

To target another local backend URL:

```powershell
.\scripts\smoke-test-local.ps1 -BaseUrl "http://localhost:8081"
```

The script checks Actuator health (including PostgreSQL), public platform status, the OpenAPI login route, and the expected `401 Unauthorized` response from the protected profile endpoint when no access token is supplied. A non-zero exit code indicates a failed check.
