# Architecture & Design Decisions

## System Architecture

```
┌─────────────────┐       ┌──────────────────────┐       ┌──────────────┐
│   Angular 17    │──────▶│  Spring Boot 3 API   │──────▶│   H2 / SQL   │
│   (Frontend)    │ HTTP  │  (Backend)           │  JPA  │  (Database)  │
│   Port: 4200    │◀──────│  Port: 8080          │◀──────│              │
└─────────────────┘       └──────────────────────┘       └──────────────┘
        │                          │
        │ JWT Token                │ BCrypt
        ▼                          ▼
┌─────────────────┐       ┌──────────────────────┐
│  localStorage   │       │   Security Layer     │
│  (auth state)   │       │   (JWT + Roles)      │
└─────────────────┘       └──────────────────────┘
```

## Backend Architecture (Layered)

```
Controller Layer (REST API)
    ├── AuthController      → POST /api/auth/login
    ├── EmployeeController  → CRUD /api/employees
    ├── SalaryController    → /api/employees/{id}/salary
    ├── DashboardController → GET /api/dashboard
    └── DepartmentController→ GET /api/departments

Service Layer (Business Logic)
    ├── AuthService         → JWT token generation
    ├── EmployeeService     → CRUD + validation + audit
    ├── SalaryService       → Salary history management
    ├── DashboardService    → Aggregated analytics
    └── AuditService        → Change tracking

Repository Layer (Data Access)
    ├── EmployeeRepository  → Custom JPQL queries with filters
    ├── SalaryRepository    → Analytics aggregation queries
    ├── DepartmentRepository
    ├── AppUserRepository
    └── AuditLogRepository
```

## Key Design Decisions

### 1. Salary History Pattern (Effective Dating)
**Decision:** Each salary change creates a new record with `effectiveDate`. The superseded record gets `endDate` set to the day before.

**Reasoning:** This provides a complete audit trail, supports "as of" queries, and avoids destructive updates. The HR domain requires knowing historical compensation for compliance and reporting.

**Invariant that has to be enforced:** the new effective date must fall strictly after the record it supersedes. Without that check, back-dating a change closes the previous row at `effectiveDate - 1`, which can land *before* that row's own start, leaving a record that ends before it begins. The history then cannot be read back in order and "current salary" becomes ambiguous. This is enforced in `SalaryService` and mirrored in the date picker so the user does not have to discover it via a server error.

### 1a. Money is only aggregated within a currency
**Decision:** No organisation-wide pay figures. Statistics are grouped per country, and department/designation breakdowns only appear once the caller narrows to a single country.

**Reasoning:** Salaries are stored in local currency. A mean over INR, USD, GBP, EUR and AUD is arithmetically computable and semantically meaningless, and the risk is not that it errors, it is that an HR manager reads it as real and makes a decision on it. Reporting per currency is honest; normalising would require agreed FX rates and an as-of date, which is a product decision the assessment does not settle.

For the same reason, a standard employee edit cannot change country. A transfer needs an
approved FX rate, a compensation amount, and an effective date; relabelling an existing
INR amount as USD would create silently incorrect salary data. That belongs in a dedicated
compensation-transfer workflow rather than a generic profile edit.

**Consequence:** the dashboard reports median and quartiles, not just an average. A mean hides skew from a handful of executive salaries, and "half the team earns below X" is the question a pay-equity conversation actually starts from.

### 2. Stateless JWT Authentication
**Decision:** JWT tokens stored client-side, no server sessions.

**Reasoning:** Simpler to scale horizontally, no session store needed, clean separation between frontend/backend. Trade-off: can't instantly revoke tokens (acceptable for this use case with 24h expiry).

### 3. H2 Database (Embedded)
**Decision:** Use H2, in-memory by default, instead of PostgreSQL/MySQL.

**Reasoning:** Zero-config setup for reviewers (no database installation needed) and a
free-tier cloud container with an ephemeral filesystem, where a file database would be
wiped on every redeploy anyway; the seeder rebuilds the full 10,000-employee dataset in
about two seconds on boot. `SPRING_DATASOURCE_URL` can point at a file-based H2 URL to
keep data between local runs. Schema is JPA/Hibernate-managed, so migration to PostgreSQL
is a config change. Trade-off: no production-grade features, but acceptable for assessment.

### 4. Pagination for 10K Employees
**Decision:** Server-side pagination with Spring Data `Pageable`, default 20 per page.

**Reasoning:** Loading 10K records to the frontend would be terrible UX and performance. Server-side pagination with database LIMIT/OFFSET keeps response times consistent regardless of dataset size.

### 5. Standalone Angular Components
**Decision:** Use Angular 17 standalone components with lazy-loaded routes.

**Reasoning:** No NgModules needed, smaller bundle sizes via code splitting, simpler mental model. Each component declares its own dependencies explicitly.

### 6. Batch Seeding with Fixed Random Seed
**Decision:** Seed 10K employees in batches of 500 with `Random(42)`.

**Reasoning:** Batch inserts avoid memory pressure. Fixed seed ensures reproducible data across runs (same employees every time), making demos and debugging consistent.

### 7. Single source of truth for country and currency
**Decision:** `CurrencyResolver` owns the country list and the country-to-currency mapping.

**Reasoning:** That mapping was originally duplicated in the seeder, the dashboard aggregation, and a hardcoded array in the Angular filter. Adding a country meant three coordinated edits, and any drift between them produced wrong reports rather than an error. The frontend now fetches the list, so it cannot disagree with the backend.

## Performance Considerations

- **Database indexes** on frequently filtered columns (email, department_id, country, status)
- **Paginated queries** prevent full table scans for list views, with page size capped at 100
- **Lazy loading** of JPA relationships
- **Batch inserts** for seeding (500 per batch, 10,000 employees in ~2 seconds)
- **Angular lazy routes** for a smaller initial bundle
- **Dashboard aggregation in one query.** The first implementation ran three aggregate
  queries per department plus three per country, roughly 45 round trips to render one page.
  It now pulls the in-force salary rows once and groups them in memory: 3 queries,
  measured at ~130ms for 10,000 employees. At the stated organisation size the projection
  is a small payload; an order of magnitude more data would push the grouping back into
  SQL behind a materialised summary table.
- **Derived view models computed once per load.** Binding Angular tables to method calls
  rebuilt the arrays on every change detection pass and re-rendered continuously.

## Security Measures

Full review, including verified fixes and accepted risks, is in `SECURITY.md`. Summary:

- BCrypt password hashing
- JWT with HMAC-SHA256, signing key supplied via `JWT_SECRET`, startup refuses the
  development key under the `prod` profile
- All `/api/**` routes require the `HR_MANAGER` or `ADMIN` role; `anyRequest()` is
  `denyAll()` so a new endpoint is closed until deliberately opened
- H2 console disabled outside the `dev` profile
- CORS published as a `CorsConfigurationSource` bean so the security chain applies it and
  pre-flight `OPTIONS` is not rejected before reaching MVC
- Bean Validation on every request body; sort fields whitelisted; page size clamped
- Parameterised JPQL only
- Error responses never leak SQL, class names, or paths; stack traces are logged instead
- Audit trail on every employee and salary change, readable through the API

## Trade-offs Considered

| Decision | Pros | Cons | Why Chosen |
|----------|------|------|------------|
| H2 vs PostgreSQL | Zero setup, portable | Not production-grade | Reviewer convenience; trivial to swap |
| JWT vs Session | Stateless, scalable | Can't revoke instantly | Simple architecture for assessment scope |
| Angular Material vs Custom | Fast development, accessible | Opinionated styling | Assessment focuses on logic, not custom CSS |
| Single repo vs Monorepo tool | Simple structure | No shared tooling | Appropriate scale for this project |
| Lombok vs Manual code | Less boilerplate | Implicit code generation | Industry standard, reduces noise in entities |
