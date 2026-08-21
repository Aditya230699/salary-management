# Security Review

A deliberate audit pass over the running application, not a checklist exercise. Each item
below was reproduced against a live instance before being fixed, and re-verified after.

## Findings and fixes

| Severity | Finding | Why it mattered | Fix |
|---|---|---|---|
| Critical | `/h2-console/**` was `permitAll` | An unauthenticated SQL console over 10,000 salary records. Read *and* write. | Console disabled by default; only enabled under the `dev` profile, and the security chain only opens the path when `app.security.h2-console-exposed` is true. |
| Critical | JWT signing key hardcoded in a committed properties file | A published signing key lets anyone mint a valid token for any user, including `admin`. | Key now read from `JWT_SECRET`. Under the `prod` profile the app refuses to start if the development key or a key under 32 bytes is present. |
| High | Every `/api/**` route only required `authenticated()` | Any valid token had full read/write access to all salary data; roles existed but were never enforced. | Routes require `HR_MANAGER` or `ADMIN`; write operations additionally carry `@PreAuthorize` as defence in depth. `anyRequest()` is now `denyAll()` so a new endpoint is closed until it is deliberately opened. |
| High | Unbounded `size` parameter | `?size=100000` returned the entire employee table in one response: a trivial way to exfiltrate the dataset or exhaust memory. | Clamped to `app.pagination.max-page-size` (100), a property that already existed but was never read. Audit endpoint capped the same way. |
| Medium | `sortBy` passed straight into `Sort` | Unknown properties became HTTP 500s and let a caller probe the entity graph by observing which names error. | Sorting restricted to an explicit whitelist that also maps `department` to the `department.name` path. |
| Medium | Bad credentials returned HTTP 500 | The catch-all handler swallowed `AuthenticationException`, so a client could not distinguish a wrong password from an outage, and the login screen's 401 branch was dead code. | `AuthenticationException` maps to 401 with a message that does not reveal whether the username exists. |
| Medium | Catch-all handler discarded exceptions silently | Real faults left no trace to diagnose. | Stack traces are logged server-side while the response stays opaque, so internal details are never returned to a client. |
| Medium | Unauthenticated requests returned 403 | The frontend could not tell "log in again" from "not allowed", so an expired session looked like a permissions bug. | `HttpStatusEntryPoint(UNAUTHORIZED)` returns 401, and the frontend interceptor acts on it. |
| Medium | CORS registered only via `WebMvcConfigurer` | Pre-flight `OPTIONS` carries no `Authorization` header, so the security chain rejected it before it reached MVC. Browser calls from Angular would fail even with a valid token. | CORS published as a `CorsConfigurationSource` bean and wired into the security chain. Allowed headers narrowed to `Authorization` and `Content-Type`. |
| Medium | Expired token left the SPA in a broken state | Every request failed while the app still believed it was authenticated; the only recovery was clearing storage by hand. | Interceptor clears the session and redirects to login on 401, exempting the login request itself to avoid a redirect loop. |
| Low | `backend/data/` untracked but not ignored | One `git add -A` from committing the seeded salary database to a public repository. | Added to `.gitignore` along with `*.mv.db` and `*.trace.db`. |
| Low | Client-supplied `currency` trusted on create | A caller could store figures under a currency unrelated to the employee's country. | Currency is derived server-side from the country via `CurrencyResolver`. |

## Verified after fixing

Reproduced against a live instance with 10,000 seeded employees:

```
[PASS] H2 console is not exposed              -> 401
[PASS] Unauthenticated API returns 401        -> 401
[PASS] Bad password returns 401 (was 500)     -> 401
[PASS] Unknown user returns 401               -> 401
[PASS] Page size clamped to 100               -> size=99999 returned 100
[PASS] Unknown sort field rejected            -> 400
[PASS] Unknown status rejected                -> 400
[PASS] Unsupported country rejected           -> 400
[PASS] Duplicate email rejected               -> 409
```

## Controls in place

- Passwords hashed with BCrypt; plaintext never stored or logged.
- Stateless JWT (HS256), 24 hour expiry, no session store.
- All queries parameterised through JPQL named parameters; no string-concatenated SQL.
- Bean Validation on every request body, with field-level errors returned as a map.
- Audit trail records who changed what and when, and is now readable through the API.
- Error responses never echo SQL, class names, or file paths.

## Accepted risks, stated deliberately

**JWT held in `localStorage`.** Readable by injected script, so a successful XSS becomes
token theft. The alternative, an `HttpOnly` `Secure` `SameSite` cookie, is the stronger
choice and would require re-enabling CSRF protection. `localStorage` was kept because the
API is deliberately stateless and cookie-based auth changes the CSRF posture across every
endpoint. Angular's default output escaping is the compensating control, and no
`bypassSecurityTrust*` call or `innerHTML` binding exists in this codebase.

**CSRF disabled.** Correct while credentials travel in an `Authorization` header, because
there is no ambient credential for a browser to replay. This assumption is recorded in
`SecurityConfig` so that a future move to cookies re-opens the question rather than
silently inheriting an unsafe default.

**No rate limiting on login.** Brute force is unthrottled. Out of scope for an assessment
running on a single local instance; in production this belongs at the gateway or behind a
bucket-per-account limiter with lockout, not in application code.

**`ddl-auto=update`.** Convenient for a reviewer running the project cold, unsafe for
production where schema changes should be explicit, reviewed migrations. A real deployment
would use Flyway or Liquibase and set `ddl-auto=validate`.

**H2 embedded database.** Chosen so the project runs with no database install; the
deployed instance runs it in-memory because the container filesystem is ephemeral, while
a file-based mode remains available locally. Neither mode encrypts data at rest. Salary
data in production warrants a managed database with encryption at rest plus column-level
encryption or tokenisation for the pay figures themselves.
