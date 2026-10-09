# QueueLess — Full-stack virtual queue management MVP

QueueLess lets a business create a queue, customers join remotely and receive a token, and staff call the next customer or mark a visit completed, skipped, or cancelled.

## Included features
- Create and list queues (clinic, salon, service centre, etc.)
- Join a queue with a customer name and optional phone number
- Automatic token numbers assigned under a database row lock
- Live-ish dashboard refresh (polls every 5 seconds)
- Staff actions: call next, complete, skip, cancel
- Waiting count and estimated wait time (configurable average minutes per customer)
- Request validation and readable API errors
- React + Vite frontend, Spring Boot REST backend, PostgreSQL
- Docker Compose for PostgreSQL

> This is a portfolio-ready MVP, not a production deployment. Before real-world use, add authentication/authorization for staff, rate limiting, audit logging, privacy controls, monitoring, and deployment secrets management. The current staff actions are intentionally open for local development.

## Requirements
- JDK 21
- Maven 3.9+
- Node.js 20+
- PostgreSQL 15+ OR Docker Desktop

## 1. Start PostgreSQL
From the project root:
```bash
docker compose up -d postgres
```
This creates database `queueless_db`, user `queueless`, password `queueless_dev_password` on host port 5433 (container port 5432), avoiding a conflict with a locally installed PostgreSQL server.

If you prefer your already-installed PostgreSQL instead, set `DB_URL=jdbc:postgresql://localhost:5433/queueless_db`, `DB_USERNAME` to your database user, and `DB_PASSWORD` to your existing password (or edit `application.properties`).

## 2. Start backend
```bash
cd backend
mvn spring-boot:run
```
Backend: http://localhost:8081
Health check: http://localhost:8081/api/v1/health
API documentation is described in `docs/API.md`.

Default DB settings are in `backend/src/main/resources/application.properties`. Environment variables override them:
- `DB_URL` (default `jdbc:postgresql://localhost:5433/queueless_db`)
- `DB_USERNAME` (default `queueless`)
- `DB_PASSWORD` (default `queueless_dev_password`)

If using your own local postgres user/password, edit these values or export environment variables.

## 3. Start frontend
Open another terminal:
```bash
cd frontend
npm install
npm run dev
```
Open the URL printed by Vite (normally http://localhost:5173).

## Suggested demo
1. Create a queue named `General Consultation`, business `City Clinic`, capacity `30`.
2. Select it and join as a customer (e.g. `Asha Patil`).
3. Join a second customer in another tab or from the same screen.
4. In the staff panel, press **Call next**, then **Complete** or **Skip**.
5. Watch the queue counts and estimated wait update.

## Notes
- Queue token numbering starts at 1 and increases for each queue.
- A queue's capacity limits the number of customers currently waiting or called; completed, skipped, and cancelled entries do not count toward capacity.
- Estimated waiting time = number of people waiting ahead × configured average service time (default 5 minutes). It is an estimate, not a promise.
- Hibernate schema update is enabled for local development. Use managed migrations (Flyway/Liquibase) for production.


## Security (JWT + roles)

This version includes Spring Security, BCrypt password hashing, stateless JWT authentication, and role-based API authorization. Public customer actions are queue reads and joining a queue. Creating queues, calling next, and changing entry status require STAFF or ADMIN. `/api/v1/admin/**` requires ADMIN.

### First admin account

Set these environment variables before starting the backend (do not commit real credentials):

PowerShell example:

```powershell
$env:APP_ADMIN_USERNAME = "admin"
$env:APP_ADMIN_PASSWORD = "use-a-unique-password-at-least-12-chars"
$env:JWT_SECRET = [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
mvn spring-boot:run
```

The bootstrap process creates the admin account only if that username does not already exist. Save the JWT_SECRET securely; changing it invalidates existing tokens. Log in in the frontend with that account. Create staff accounts only through a trusted/admin-only workflow; public staff registration is intentionally not exposed.

### API authentication

`POST /api/v1/auth/login` with `{ "username": "admin", "password": "..." }` returns an access token. Send it as `Authorization: Bearer <token>` for protected operations. `GET /api/v1/auth/me` returns the current identity. Access token lifetime defaults to 3600 seconds (`JWT_TTL_SECONDS`).

### Deployment safety

The included default JWT key and PostgreSQL credentials are for local development only. For deployment, set unique strong secrets and database credentials via environment variables, use HTTPS, configure the exact frontend origin, use database migrations instead of `ddl-auto=update`, and review logs/backups/rate limits before exposing publicly. This archive is a deployment-ready starting point, but must be built and smoke-tested in your target environment before production use.
