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
**Decision:** Each salary change creates a new record with `effectiveDate`. Previous record gets `endDate` set.

**Reasoning:** This provides complete audit trail, supports "as of" queries, and avoids destructive updates. The HR domain requires knowing historical compensation for compliance and reporting.

### 2. Stateless JWT Authentication
**Decision:** JWT tokens stored client-side, no server sessions.

**Reasoning:** Simpler to scale horizontally, no session store needed, clean separation between frontend/backend. Trade-off: can't instantly revoke tokens (acceptable for this use case with 24h expiry).

### 3. H2 Database (Embedded)
**Decision:** Use H2 with file-based persistence instead of PostgreSQL/MySQL.

**Reasoning:** Zero-config setup for reviewers (no database installation needed). Schema is JPA/Hibernate-managed, so migration to PostgreSQL is a config change. Trade-off: no production-grade features, but acceptable for assessment.

### 4. Pagination for 10K Employees
**Decision:** Server-side pagination with Spring Data `Pageable`, default 20 per page.

**Reasoning:** Loading 10K records to the frontend would be terrible UX and performance. Server-side pagination with database LIMIT/OFFSET keeps response times consistent regardless of dataset size.

### 5. Standalone Angular Components
**Decision:** Use Angular 17 standalone components with lazy-loaded routes.

**Reasoning:** No NgModules needed, smaller bundle sizes via code splitting, simpler mental model. Each component declares its own dependencies explicitly.

### 6. Batch Seeding with Fixed Random Seed
**Decision:** Seed 10K employees in batches of 500 with `Random(42)`.

**Reasoning:** Batch inserts avoid memory pressure. Fixed seed ensures reproducible data across runs (same employees every time), making demos and debugging consistent.

## Performance Considerations

- **Database indexes** on frequently filtered columns (email, department_id, country, status)
- **Paginated queries** prevent full table scans for list views
- **Lazy loading** of JPA relationships (no N+1 on list views)
- **Batch inserts** for seeding (500 per batch)
- **Angular lazy routes** for smaller initial bundle

## Security Measures

- BCrypt password hashing (cost factor 10)
- JWT with HMAC-SHA256 signing
- CORS whitelist (only Angular dev server)
- Input validation on all endpoints (Jakarta Validation)
- Parameterized JPQL queries (no SQL injection)
- Role-based endpoint access
- Audit logging for all salary changes

## Trade-offs Considered

| Decision | Pros | Cons | Why Chosen |
|----------|------|------|------------|
| H2 vs PostgreSQL | Zero setup, portable | Not production-grade | Reviewer convenience; trivial to swap |
| JWT vs Session | Stateless, scalable | Can't revoke instantly | Simple architecture for assessment scope |
| Angular Material vs Custom | Fast development, accessible | Opinionated styling | Assessment focuses on logic, not custom CSS |
| Single repo vs Monorepo tool | Simple structure | No shared tooling | Appropriate scale for this project |
| Lombok vs Manual code | Less boilerplate | Implicit code generation | Industry standard, reduces noise in entities |
