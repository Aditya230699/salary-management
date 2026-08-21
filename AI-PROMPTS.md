# AI Tools & Prompts Used

## Development Environment

- **AI Tool:** Kiro (AI-powered IDE built on VS Code)
- **Approach:** Collaborative development with AI assistance for code generation, architecture decisions, and testing

## How AI Was Used

### 1. Requirements Analysis
- Fed the assessment PDF content to AI for structured extraction
- Used AI to help frame the requirements document with clear scope/exclusions
- AI helped identify security considerations for salary data (PII, audit trails)

### 2. Architecture & Design
- Discussed trade-offs (H2 vs PostgreSQL, JWT vs Sessions) with AI
- AI suggested the effective-dating pattern for salary history
- Used AI to plan the layered architecture (Controller → Service → Repository)

### 3. Code Generation
- AI generated entity classes with proper JPA annotations and indexes
- Repository layer with custom JPQL queries for filtering and analytics
- Service layer with business logic, validation, and audit logging
- Security configuration (JWT filter chain, CORS, BCrypt)
- Data seeder with realistic multi-country employee generation

### 4. Frontend Development
- AI scaffolded Angular 17 standalone components
- Generated Angular Material UI components with proper accessibility
- Created HTTP services with interceptors for JWT auth
- Built reactive forms with validation

### 5. Testing
- AI wrote unit tests with clear arrange-act-assert patterns
- Used Mockito for service isolation in backend tests
- HttpClientTestingModule for frontend service tests
- Tests cover edge cases (null handling, duplicates, auth failures)

## AI Workflow Principles

1. **Think first, generate second** — Discussed architecture before writing code
2. **Incremental commits** — Each logical unit committed separately for traceability
3. **Review generated code** — Verified all AI output for correctness and consistency
4. **AI as pair programmer** — Used for acceleration while maintaining engineering judgment
5. **Security by default** — Parameterized queries, input validation, proper auth from the start

## Prompts Summary

| Phase | Prompt Intent | Outcome |
|-------|--------------|---------|
| Planning | "Analyze assessment requirements, identify security considerations" | Requirements doc with scope/exclusions |
| Design | "Design database schema for salary management with history tracking" | Entity model with effective-dating |
| Backend | "Implement Spring Boot REST API with JWT auth, pagination, filtering" | Full backend with 5 controllers, 5 services |
| Seeding | "Generate 10K realistic employees across 5 countries with proper salary ranges" | DataSeeder with batch processing |
| Frontend | "Build Angular 17 app with Material UI for employee/salary management" | 6 components with full CRUD flow |
| Testing | "Write unit tests for core business logic with edge cases" | 47 backend tests + 29 frontend specs |

## What I Verified Manually

- Database schema relationships make logical sense
- Salary effective-dating closes old records correctly
- Pagination works for 10K records without performance issues
- JWT auth flow works end-to-end (login → token → protected routes)
- All CRUD operations maintain data integrity
- Audit logs capture who changed what and when
