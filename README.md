# QueueLess — Smart Queue Management System

A full-stack web application for managing queues digitally. QueueLess allows users to view available queues and join them, while authorized staff manage queue operations through role-based access control.

**Live Demo:** https://queueless-1-90mf.onrender.com/  
**GitHub Repository:** https://github.com/arti500/QueueLess

## Features

- **Queue Management:** Create and manage queues with authorized staff accounts.
- **Digital Queue Joining:** Users can join available queues online.
- **Queue Monitoring:** View queues and their entries.
- **Queue Operations:** Authorized staff can call the next entry and update entry statuses.
- **Authentication:** JWT-based authentication.
- **Role-Based Authorization:** Separate permissions for staff and administrators.
- **Persistent Storage:** PostgreSQL database.
- **Full-Stack Deployment:** React frontend and Spring Boot backend served through a single public URL.

## Tech Stack

| Layer | Technologies |
|---|---|
| Frontend | React.js, Vite, JavaScript |
| Backend | Java 21, Spring Boot 3.5.5 |
| Security | Spring Security, JWT, BCrypt |
| Database | PostgreSQL, Spring Data JPA / Hibernate |
| Build Tools | Maven, npm |
| Deployment | Render, Neon |

## Architecture

```text
User / Staff / Admin
        |
        v
 React Frontend (Vite)
        |
        v
 Spring Boot REST API
        |
        v
 Spring Security + JWT
        |
        v
 Spring Data JPA / Hibernate
        |
        v
 PostgreSQL Database
```

The React frontend communicates with the Spring Boot REST API. Spring Security protects restricted operations, and PostgreSQL stores application data.

## API Overview

Base URL: `/api/v1`

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/health` | Check backend health |
| POST | `/auth/login` | Authenticate a user |
| GET | `/queues` | List queues |
| GET | `/queues/{queueId}` | Get queue details |
| GET | `/queues/{queueId}/entries` | View queue entries |
| POST | `/queues/{queueId}/join` | Join a queue |
| POST | `/queues` | Create a queue — staff/admin |
| POST | `/queues/{queueId}/call-next` | Call next entry — staff/admin |
| PATCH | `/queues/{queueId}/entries/{entryId}/status` | Update entry status — staff/admin |

Administrative endpoints are protected by role-based authorization.

## Run Locally

### Prerequisites

- Java 21 or compatible JDK
- Maven
- Node.js and npm
- PostgreSQL

### 1. Clone the repository

```bash
git clone https://github.com/arti500/QueueLess.git
cd QueueLess
```

### 2. Configure the database

Create a PostgreSQL database named `queueless_db`.

Configure these environment variables for the backend:

```text
DB_URL=jdbc:postgresql://localhost:5432/queueless_db
DB_USERNAME=postgres
DB_PASSWORD=your_database_password
JWT_SECRET=your_generated_secret
JWT_TTL_SECONDS=3600
SERVER_PORT=8081
```

Use a securely generated JWT secret and your own database credentials.

### 3. Start the backend

From the `backend` directory, run:

```bash
mvn spring-boot:run
```

Backend health endpoint:

`http://localhost:8081/api/v1/health`

### 4. Start the frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

Open the local URL printed by Vite, normally:

`http://localhost:5173`

Configure any required administrator bootstrap credentials according to the application's environment configuration.

## Deployment

- **Application hosting:** Render
- **Database hosting:** Neon PostgreSQL
- **Public application:** https://queueless-1-90mf.onrender.com/

Production credentials and secrets should be configured through environment variables, never committed to the repository.

## Security

- JWT-based authentication
- Role-based access control for staff and administrators
- BCrypt password hashing
- Restricted queue-management operations
- Environment-based database credentials and JWT secrets

## Future Enhancements

- Real-time queue updates
- Email or SMS notifications
- Estimated waiting-time display
- Queue analytics dashboard
- Automated integration tests

---

**Built with Java, Spring Boot, React, and PostgreSQL.**
