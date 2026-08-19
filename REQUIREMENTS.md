# Salary Management System - Requirements Document

## Goal

Build a web-based salary management platform for ACME organization's HR team to replace manual Excel-based salary tracking for 10,000 employees across multiple countries. The system enables the HR Manager to manage employee salary data efficiently and answer compensation-related questions about the organization.

## User Persona

**HR Manager** - Responsible for managing compensation data, running salary reports, and ensuring pay equity across departments and countries.

## Scope & Features

### Core Features (In Scope)

1. **Employee Management**
   - View all employees with search, filter, and pagination
   - View employee details (name, department, country, designation, join date)
   - Add/edit/deactivate employees

2. **Salary Management**
   - View and update employee salary (base pay, bonuses, deductions)
   - Track salary history with effective dates
   - Multi-currency support based on employee country

3. **Analytics & Reporting**
   - Average salary by department, country, designation
   - Salary distribution overview (min, max, median, percentiles)
   - Headcount by department and country
   - Total compensation cost per department/country

4. **Dashboard**
   - Overview metrics (total employees, avg salary, total payroll cost)
   - Quick filters by department, country, designation

5. **Security**
   - Authentication (login/logout)
   - Role-based access (HR Manager role)
   - Audit trail for salary changes

6. **Data Seeding**
   - Seed script generating 10,000 realistic employees across 5+ countries

### Deliberately Excluded (Out of Scope)

| Feature | Reasoning |
|---------|-----------|
| Payroll processing/disbursement | Complex domain requiring payment gateway integration; beyond scope of management tool |
| Tax calculations | Country-specific tax rules add significant complexity without demonstrating core engineering skills |
| Employee self-service portal | Assessment focuses on HR Manager persona only |
| Email notifications | Not core to the salary management problem |
| File upload (Excel import/export) | While logical for migration, adds scope without demonstrating architecture |
| Multi-tenancy | Single org requirement; no need for tenant isolation |
| Approval workflows | Adds state machine complexity; HR Manager has direct edit access |
| Real-time currency conversion | Static currency per country is sufficient for salary records |

## Technical Architecture

- **Backend**: Java 17 + Spring Boot 3 + Spring Security + JPA/Hibernate
- **Database**: H2 (embedded, easy to run) with schema suitable for PostgreSQL migration
- **Frontend**: Angular 17 + Angular Material
- **Auth**: JWT-based stateless authentication
- **API**: RESTful with proper HTTP semantics, pagination, error handling

## Non-Functional Requirements

- Support 10,000 employee records with sub-second query response
- Paginated API responses (default 20 per page)
- Input validation on all endpoints
- Parameterized queries (no SQL injection risk)
- Proper error handling with meaningful messages
- Clean separation of concerns (Controller → Service → Repository)

## Success Criteria

- HR Manager can log in, browse employees, update salaries, and view analytics
- All CRUD operations work correctly
- Unit tests cover core business logic
- Application starts and seeds data without manual steps
- Clean, incremental commit history showing development progression
