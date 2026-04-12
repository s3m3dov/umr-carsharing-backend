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

## Internal Service-to-Service Security

### Current Strategy: Perimeter Defense

The system uses a "hard shell, soft center" security model for internal APIs (`/api/internal/**`).

- **External Protection:** The `api-gateway` uses an `InternalPathBlockingFilter` to strip and block any incoming requests from the public internet that match the `/api/internal/**` pattern.
- **Internal Access:** Within the cluster network, services like `user-service` and `auth-service` can communicate with each other's internal endpoints without providing a JWT token.

### Identified Risks

1.  **Lateral Movement:** If an attacker compromises one service within the cluster (e.g., via a dependency vulnerability), they can call any `/api/internal/**` endpoint in any other service without authentication.
2.  **Cluster Access:** Any entity with internal network access to the Kubernetes namespace (e.g., via `kubectl exec` or a misconfigured `port-forward`) can bypass the gateway and trigger administrative actions like user deletion.

### Future Hardening Recommendations

To move toward a "Zero Trust" architecture, the following should be implemented:

- **Shared Secrets:** Implement a simple header-based shared secret (e.g., `X-Internal-Secret`) that must be present and valid for all internal API calls.
- **Service Mesh (mTLS):** Use a service mesh like Istio to enforce that only specific services (e.g., `user-service`) are allowed to initiate connections to `auth-service`'s internal ports.
- **Kubernetes NetworkPolicies:** Restrict traffic at the network level so that only authorized pods can reach the internal API ports of other services.
