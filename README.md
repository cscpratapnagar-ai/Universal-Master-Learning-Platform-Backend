# Universal Master Learning Platform - Backend

Enterprise backend for the Universal Master Learning Platform.

## Prerequisites

- JDK 21
- Maven 3.9+
- Node.js 20+ and npm
- PostgreSQL 17 (local Docker setup uses host port 5433)
- Mailpit is optional for local email testing (SMTP port 1025, inbox at http://localhost:8025)

## Architecture

Modular monolith with layered architecture:

Controller → Service → ServiceImpl → Repository → Entity

Feature modules own their DTOs, mappers, exceptions and events where needed.

## One-command full verification (Windows PowerShell)

Keep the backend and UI repositories as sibling folders under the same workspace, then run this from the backend repository root:

```powershell
git pull origin main
.\scripts\verify-platform.ps1
```

The script automatically:
- Runs the backend Maven test suite.
- Checks live backend and PostgreSQL health; if healthy, runs the API smoke tests.
- Installs UI dependencies with `npm ci` only when the UI's `node_modules` folder is missing.
- Runs the Angular production build and ChromeHeadless unit tests.
- Prints a pass/fail/skip summary and returns a non-zero exit code when a check fails.

For headless UI unit tests, Chrome must be installed and available to the test runner. If the backend is not running, live API smoke tests are marked as skipped; start the backend and rerun for complete local verification.

Optional flags:

```powershell
.\scripts\verify-platform.ps1 -SkipBackendTests
.\scripts\verify-platform.ps1 -SkipUiTests
.\scripts\verify-platform.ps1 -SkipSmokeTest
```

## Individual backend checks

Run the backend test suite:

```powershell
mvn --batch-mode --no-transfer-progress test
```

Run smoke checks against the running backend:

```powershell
.\scripts\smoke-test-local.ps1
```

To target another local backend URL:

```powershell
.\scripts\smoke-test-local.ps1 -BaseUrl "http://localhost:8081"
```

The smoke test checks Actuator health (including PostgreSQL), public platform status, the OpenAPI login route, and the expected `401 Unauthorized` response from the protected profile endpoint when no access token is supplied.

GitHub Actions runs backend tests and UI build/unit tests automatically on pushes to `main`, pull requests targeting `main`, and can be started manually from the Actions tab.

## Local service URLs

- Backend API: http://localhost:8080
- Health: http://localhost:8080/actuator/health
- OpenAPI UI: http://localhost:8080/swagger-ui/index.html
- Mailpit inbox (when running): http://localhost:8025
