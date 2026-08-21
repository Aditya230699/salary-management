# Salary Management System - Requirements Document

## Goal

Build a web-based salary management platform for ACME organization's HR team to replace manual Excel-based salary tracking for 10,000 employees across multiple countries. The system enables the HR Manager to manage employee salary data efficiently and answer compensation-related questions about the organization.

## User Persona

**HR Manager** - Responsible for managing compensation data, running salary reports, and ensuring pay equity across departments and countries.

## Scope & Features

### Core Features (In Scope)

1. **Employee Management**
   - View all employees with search, filter (department, country, status), and pagination
   - View employee details including current compensation
   - Edit employee attributes, including status, which is how an employee is deactivated

2. **Salary Management**
   - View and update salary (base pay, bonus, deductions)
   - Dated salary history: each change supersedes the previous record rather than overwriting it
   - Currency of record derived from the employee's country

3. **Answering pay questions**
   - Pay distribution per country: min, 25th percentile, median, 75th percentile, max, average, total payroll
   - Narrowing to one country unlocks department and designation breakdowns for like-for-like comparison
   - Headcount by department and country

4. **Audit trail**
   - Every employee and salary change records what changed, who changed it, and when
   - Readable per employee through the API and the UI

5. **Security**
   - Authentication with role-based access (HR Manager, Admin)
   - See `SECURITY.md` for the full review, including accepted risks

6. **Data Seeding**
   - Seed script generating 10,000 employees across 5 countries, reproducible via a fixed random seed

### A deliberate decision on currency

Salaries are held in local currency. An organisation-wide average across INR, USD, GBP,
EUR and AUD is arithmetically valid and completely meaningless, and an HR manager would
reasonably read it as real. So money is only ever reported within a single currency:
per-country by default, and per-department or per-designation once the user narrows to one
country. Cross-currency comparison would need agreed FX rates and an as-of date, which is
a product decision rather than an implementation detail, so it is excluded rather than
approximated.

### Deliberately Excluded (Out of Scope)

| Feature | Reasoning |
|---------|-----------|
| Payroll processing/disbursement | Complex domain requiring payment gateway integration; beyond scope of a management tool |
| Tax calculations | Country-specific rules add significant complexity without demonstrating core engineering skill |
| FX conversion / normalised global pay figures | Requires agreed rates and an as-of date; reporting per currency is the honest alternative |
| Employee self-service portal | Assessment focuses on the HR Manager persona only |
| Create-employee UI | The API supports it and the seeder exercises it, but new employees realistically arrive from an onboarding system rather than being typed into a salary tool. Editing and deactivation, which the persona does need, are in the UI |
| Approval workflows | Adds state-machine complexity; the HR Manager has direct edit rights |
| Email notifications | Not core to the salary management problem |
| Excel import/export | Logical for migration, but adds surface area without demonstrating architecture |
| Multi-tenancy | Single organisation; no tenant isolation needed |

## Technical Architecture

- **Backend**: Java 17 + Spring Boot 3.4 + Spring Security + JPA/Hibernate
- **Database**: H2 (embedded, in-memory by default, zero install) with a schema that ports to PostgreSQL
- **Frontend**: Angular 17 + Angular Material, standalone components with lazy routes
- **Auth**: JWT-based stateless authentication
- **API**: REST with pagination, whitelisted sorting, structured error responses

## Non-Functional Requirements

- 10,000 employee records with sub-second query response (dashboard measured at ~130ms)
- Paginated responses, default 20 and capped at 100 per page
- Server-side validation on every endpoint
- Parameterised queries only
- Clean separation of concerns (Controller -> Service -> Repository)

## Success Criteria

- HR Manager can log in, browse and filter employees, edit details, update salaries, and read pay distributions
- Salary history stays coherent: no record can end before it begins
- Unit tests cover the core business logic, including the percentile maths and the history rules
- Application starts and seeds 10,000 employees with no manual steps
- Commit history shows how the solution evolved

## Known gaps

- **No video demo yet.** Requested by the assessment and still outstanding.

Deployment is no longer a gap: the API runs on Render (containerised via
`backend/Dockerfile`) and the frontend on Netlify (`netlify.toml`). The free-tier
container is ephemeral, so the database is in-memory and re-seeds on boot; see the
Deployment section of `README.md`.
