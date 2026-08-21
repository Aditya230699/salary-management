# Salary Management System

A web-based employee salary management platform for HR managers to manage compensation data for 10,000+ employees across multiple countries.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.4, Spring Security, JPA/Hibernate |
| Database | H2 (embedded, in-memory by default) |
| Frontend | Angular 17, Angular Material |
| Auth | JWT (stateless, BCrypt hashed passwords) |
| Testing | JUnit 5, Mockito, Jasmine/Karma |

## Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Maven 3.8+

### Backend

Maven does not need to be installed; the wrapper is committed.

```bash
cd backend
./mvnw spring-boot:run          # mvnw.cmd on Windows
```

The backend starts on `http://localhost:8080` and seeds 10,000 employees on startup
(~2 seconds). The default database is in-memory, so data resets on every restart; point
`SPRING_DATASOURCE_URL` at a file-based H2 URL (for example
`jdbc:h2:file:./data/salarydb`) to keep data between runs. To also expose the H2 console
for local inspection:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The console is off outside the `dev` profile on purpose: it is an unauthenticated
read/write window into salary data. See `SECURITY.md`.

**Default Login:**
- Username: `hr_manager`
- Password: `password123`

These are development seed accounts. In any shared environment set `JWT_SECRET`; under the
`prod` profile the application refuses to start with the development signing key.

### Frontend

```bash
cd frontend
npm install
ng serve
```

Open `http://localhost:4200` in your browser.

## Features

- **Dashboard** — Total employees, avg salary, payroll by department/country
- **Employee Management** — Search, filter, sort, paginate 10K employees
- **Salary Management** — Update salaries with history tracking
- **Multi-country** — India, USA, UK, Germany, Australia with local currencies
- **Security** — JWT auth, role-based access, audit trail
- **Analytics** — Salary distribution by department, country, designation

## Project Structure

```
salary-management/
├── REQUIREMENTS.md          # Requirements document
├── ARCHITECTURE.md          # Design decisions & trade-offs
├── SECURITY.md             # Security review, fixes, and accepted risks
├── AI-PROMPTS.md           # AI tools usage documentation
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/salarymanagement/
│       │   ├── entity/       # JPA entities
│       │   ├── repository/   # Data access layer
│       │   ├── service/      # Business logic
│       │   ├── controller/   # REST API endpoints
│       │   ├── dto/          # Request/Response objects
│       │   ├── security/     # JWT auth
│       │   ├── config/       # Spring configs
│       │   ├── exception/    # Error handling
│       │   └── seed/         # 10K employee data seeder
│       └── test/             # Unit tests
└── frontend/
    └── src/app/
        ├── components/       # Angular UI components
        ├── services/         # HTTP services
        ├── guards/           # Route protection
        ├── interceptors/     # JWT token injection
        └── models/           # TypeScript interfaces
```

## Deployment

The application is deployed:

| What | Where |
|------|-------|
| Frontend | https://jocular-druid-283316.netlify.app |
| Backend API | https://salary-management-api-bcg5.onrender.com |
| Source | https://github.com/Aditya230699/salary-management |

Log in with the demo credentials below. Note that the API runs on Render's free tier:
after ~15 minutes of inactivity the container sleeps, and the **first request can take a
few minutes** while the JVM boots and re-seeds 10,000 employees. If that happens, open
`GET /api/health` (no token needed) once to wake the container, wait for `{"status":"UP"}`
and reload.

The backend is containerised via `backend/Dockerfile`; the free-tier container is
ephemeral, which is why the default database is in-memory and re-seeds on boot. The
frontend deploys from `netlify.toml` (build settings, SPA redirects, security headers).
The frontend reads the API URL from `frontend/src/environments/environment.prod.ts`; the
API always allows the localhost dev origin and the Netlify origin (hard-coded in
`CorsConfig`), and `CORS_ALLOWED_ORIGINS` can add further origins.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/health | Unauthenticated liveness probe (also wakes the sleeping free-tier container) |
| POST | /api/auth/login | Authenticate and get JWT token |
| GET | /api/employees | List employees. Supports `search`, `department`, `country`, `status`, `page`, `size` (max 100), `sortBy` (whitelisted), `sortDir` |
| GET | /api/employees/{id} | Get employee details |
| POST | /api/employees | Create new employee |
| PUT | /api/employees/{id} | Update employee attributes. Country transfers require an explicit compensation-transfer workflow |
| GET | /api/employees/{id}/salary/current | Get current salary |
| GET | /api/employees/{id}/salary/history | Get dated salary history |
| PUT | /api/employees/{id}/salary | Update salary. Effective date must fall after the record it supersedes |
| GET | /api/employees/{id}/audit | Read the change history for one employee |
| GET | /api/dashboard | Pay distribution per country. Add `?country=` to unlock department and designation breakdowns |
| GET | /api/dashboard/countries | Countries the organisation operates in |
| GET | /api/departments | List all departments |

All `/api/**` routes except `login` and `health` require a bearer token and the `HR_MANAGER` or `ADMIN` role.

### Reading the dashboard

Money is only ever reported within a single currency. There is no organisation-wide
average salary, because averaging INR, USD, GBP, EUR and AUD together produces a number
that looks authoritative and means nothing. Pick a country to compare departments and
designations like for like. The reasoning is in `REQUIREMENTS.md`.

## Running Tests

### Backend
```bash
cd backend
./mvnw test          # 47 tests
```

### Frontend
```bash
cd frontend
npm test             # 29 specs, ChromeHeadless
```

Backend coverage focuses on the rules that are expensive to get wrong: percentile maths
(including even/odd medians and quartiles over a known uniform distribution), the salary
history ordering rule, currency derivation, page-size clamping, and sort whitelisting.
Frontend coverage focuses on the interceptor's 401 behaviour and timezone-sensitive date
formatting.
