# Security Notes

## Admin role sign-up hardening

### Current behavior (analysis)

- Sign-up accepts `userType` from client request and persists it directly in auth storage: `auth-service/src/main/java/com/cimhub/authservice/service/RegistrationService.java`.
- `UserType` includes `ADMIN`, `DRIVER`, and `PASSENGER`: `common/src/main/java/com/cimhub/common/enums/UserType.java`.
- There is no explicit validation that restricts sign-up to only `DRIVER`/`PASSENGER`; only a driver license check exists for `DRIVER`.
- For `ADMIN` sign-up, sub-profile creation is skipped (only passenger/driver branches exist), but account creation and JWT role issuance still complete.
- API gateway and services trust JWT role for admin access (`X-User-Role` + admin guards), so a self-registered `ADMIN` could reach `/api/admin/**` endpoints.

### Risk

- Privilege escalation via public sign-up path: attacker can request `userType=ADMIN` and gain administrative access.

### Hardening decisions

- Public sign-up must reject `ADMIN` role with `400 Bad Request`.
- Allowed public roles are only `DRIVER` and `PASSENGER`.
- Admin account provisioning must happen only through a separate trusted path (seed/migration/internal tooling), never through `/api/auth/signup`.

### Implementation checklist

- Add role allowlist validation in `RegistrationService.registerUser(...)` to block `ADMIN`.
- Update request/docs text to explicitly state: `userType` is `DRIVER` or `PASSENGER` only.
- Add/keep unit tests for rejection of `ADMIN` sign-up.
- Audit existing auth users for `ADMIN` created via sign-up and remediate (disable, rotate tokens, incident review if needed).
