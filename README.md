# Salary Management System

A web-based employee salary management platform for HR managers to manage compensation data for 10,000+ employees across multiple countries.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2, Spring Security, JPA/Hibernate |
| Database | H2 (embedded, file-based) |
| Frontend | Angular 17, Angular Material |
| Auth | JWT (stateless, BCrypt hashed passwords) |
| Testing | JUnit 5, Mockito, Jasmine/Karma |

## Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Maven 3.8+

### Backend

```bash
cd backend
mvn spring-boot:run
```

The backend starts on `http://localhost:8080` and automatically seeds 10,000 employees on first run.

**Default Login:**
- Username: `hr_manager`
- Password: `password123`

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

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/login | Authenticate and get JWT token |
| GET | /api/employees | List employees (paginated, filtered) |
| GET | /api/employees/{id} | Get employee details |
| POST | /api/employees | Create new employee |
| PUT | /api/employees/{id} | Update employee |
| GET | /api/employees/{id}/salary/current | Get current salary |
| GET | /api/employees/{id}/salary/history | Get salary history |
| PUT | /api/employees/{id}/salary | Update salary |
| GET | /api/dashboard | Get analytics dashboard |
| GET | /api/departments | List all departments |

## Running Tests

### Backend
```bash
cd backend
mvn test
```

### Frontend
```bash
cd frontend
npm test
```
