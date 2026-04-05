# Manual REST Integration Scenarios

Separated manual test scenarios for JetBrains HTTP Client or VS Code REST Client.

## Location

- `tests/rest-client/user/00-setup.http`
- `tests/rest-client/user/01-auth.http`
- `tests/rest-client/user/02-user-vehicle.http`
- `tests/rest-client/user/03-trip-booking.http`
- `tests/rest-client/user/04-review-cancel-negative.http`
- `tests/rest-client/admin/00-admin-setup.http`
- `tests/rest-client/admin/01-admin-auth.http`
- `tests/rest-client/admin/02-admin-user-management.http`
- `tests/rest-client/admin/03-admin-trip-booking-management.http`
- `tests/rest-client/admin/04-admin-review-reports.http`

## Run order

1. Run `user/00-setup.http`
2. Run `user/01-auth.http`
3. Run `user/02-user-vehicle.http`
4. Run `user/03-trip-booking.http`
5. Run `user/04-review-cancel-negative.http`

## Admin run order (completely separate)

1. Run `admin/00-admin-setup.http`
2. Run `admin/01-admin-auth.http`
3. Run `admin/02-admin-user-management.http`
4. Run `admin/03-admin-trip-booking-management.http`
5. Run `admin/04-admin-review-reports.http`

## Notes

- Start local ingress first (`./gitops.sh local ingress`) and keep it running.
- Base URL is `http://localhost:8888`.
- Run ID is set once in `user/00-setup.http` and reused by user-scope files.
- Admin scenarios use their own globals with `admin_` prefix from `admin/00-admin-setup.http`.
- The files auto-store tokens/trip IDs/ride IDs in REST Client global variables where possible.
- If signup returns `409`, continue with login steps in the same file.
