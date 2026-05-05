# Backend Laravel Migration Documentation

This document describes the Laravel backend implemented in `C:\project-e-commerce-back-laravel` for the Spring Boot -> Laravel migration effort.

## 1) Project scope and current status

The Laravel workspace is isolated from the original Spring Boot backend:

- Source Spring Boot (unchanged): `C:\project-e-commerce-back`
- New Laravel workspace: `C:\project-e-commerce-back-laravel`

Current implementation status:

- Route surface for frontend contract is broadly implemented (`/api/auth/*`, `/api/v1/*`).
- Core middleware and API error standardization are implemented.
- Legacy schema-oriented models are implemented for core entities.
- Webhook signature + idempotency baseline exists.
- Several complex business flows are currently baseline/skeleton and must be finalized for strict parity.

## 2) Implemented architecture

### 2.1 Bootstrapping and request pipeline

Main bootstrap file:

- `bootstrap/app.php`

Implemented features:

- API routing enabled with `routes/api.php`.
- Middleware aliases registered:
  - `session.version` -> `App\Http\Middleware\EnsureSessionVersion`
  - `seller.contract` -> `App\Http\Middleware\EnforceSellerContract`
  - `security.audit` -> `App\Http\Middleware\SecurityAuditLogging`
- Central JSON exception rendering for `api/*` paths with normalized payload:
  - `error`
  - `message`
  - `status`

### 2.2 Middleware equivalents from Spring filters

- `app/Http/Middleware/EnsureSessionVersion.php`
  - Validates session version consistency (`ECOM_SESSION_VERSION` vs user `session_version`).
  - Invalidates session and returns 401 JSON on mismatch.
- `app/Http/Middleware/EnforceSellerContract.php`
  - Blocks seller access when vendor contract is not accepted.
  - Returns 403 JSON.
- `app/Http/Middleware/SecurityAuditLogging.php`
  - Emits security audit logs for auth/admin route spaces.

### 2.3 Error response policy

`bootstrap/app.php` maps HTTP status to stable API error codes:

- 400 -> `BAD_REQUEST`
- 401 -> `UNAUTHORIZED`
- 403 -> `FORBIDDEN`
- 404 -> `NOT_FOUND`
- 409 -> `CONFLICT`
- 422 -> `VALIDATION_ERROR`
- default -> `INTERNAL_ERROR`

## 3) Data model mapping (legacy schema style)

Implemented Eloquent models:

- `app/Models/User.php`
- `app/Models/Role.php`
- `app/Models/Pays.php`
- `app/Models/Article.php`
- `app/Models/Cart.php`
- `app/Models/CartItem.php`
- `app/Models/EcomTransaction.php`
- `app/Models/Livraison.php`
- `app/Models/Wallet.php`
- `app/Models/Conversation.php`
- `app/Models/ChatMessage.php`

Mapping strategy:

- Explicit legacy table names (`User`, `Role`, `Article`, etc.).
- Explicit primary keys (`iduser`, `idrole`, etc.).
- Timestamps disabled where legacy schema does not follow Laravel defaults.

## 4) Controllers and responsibilities

### 4.1 Auth

File: `app/Http/Controllers/AuthController.php`

Implemented:

- `GET /api/auth/csrf`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`
- `POST /api/auth/register`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`

Notes:

- Session auth flow is implemented.
- Forgot/reset currently baseline (response scaffolding).

### 4.2 Catalog, roles, countries, article media/admin article ops

File: `app/Http/Controllers/CatalogController.php`

Implemented read and CRUD surface for:

- Roles (`/roles`)
- Pays (`/pays`)
- FamilleArticle (`/familleArticles`)
- TypeArticle (`/typeArticles`)
- Articles read and admin/article edits
- Photo serving endpoints

### 4.3 Cart

File: `app/Http/Controllers/CartController.php`

Implemented:

- `GET /api/v1/cart`
- `POST /api/v1/cart/items`
- `POST /api/v1/cart/items/from-negotiation`
- `PATCH /api/v1/cart/items/{idcartitem}`
- `DELETE /api/v1/cart/items/{idcartitem}`
- `DELETE /api/v1/cart`

### 4.4 Payments

File: `app/Http/Controllers/PaymentController.php`

Implemented:

- `POST /api/v1/payments`
- `POST /api/v1/payments/cart-checkout`
- `GET /api/v1/payments/mine`
- `GET /api/v1/payments/sales`
- `GET /api/v1/payments/sales/dashboard`
- `GET /api/v1/payments/{transactionId}/receipt`
- `GET /api/v1/payments/{transactionId}/livraison/qr`
- `GET /api/v1/payments/{transactionId}/suivi`
- `POST /api/v1/payments/{idtransaction}/rating`

Note:

- Complex transaction/escrow and delivery-state orchestration are not yet strict-equivalent.

### 4.5 Conversations and negotiation

File: `app/Http/Controllers/ConversationController.php`

Implemented:

- Conversation creation/list
- Message list/create
- Offer status update
- Final offer seller/buyer endpoints

### 4.6 Wallet

File: `app/Http/Controllers/WalletController.php`

Implemented:

- Wallet read (`/wallet/me`)
- Deposit/topup checkout scaffolding
- Reconcile/sync-history scaffolding
- Withdrawal request scaffolding

### 4.7 GeniusPay integration

File: `app/Http/Controllers/GeniusPayController.php`

Implemented:

- Invoice endpoints (`/geniuspay/invoices/order`, `/geniuspay/invoices/certification`)
- Webhook endpoint (`/geniuspay/webhook`) with signature verification + idempotency baseline.

Supporting service:

- `app/Services/GeniusPaySignatureService.php`

### 4.8 User, profile, role-upgrade, vendor actions

File: `app/Http/Controllers/UserController.php`

Implemented:

- Delivery location (both route variants)
- Role-upgrade read/create
- Vendor certification read/checkout scaffold
- Vendor contract acceptance
- User data export scaffold
- Vendor rating read scaffold
- Admin user management endpoints (`/users`, activation, delete, cnib, admin-password)

### 4.9 Livreur

File: `app/Http/Controllers/LivreurController.php`

Implemented route surface for:

- Dashboard
- Available/assigned deliveries
- Take delivery
- Complete by QR
- Position updates
- Profile vehicle update
- Ignore delivery

### 4.10 Admin and support

Files:

- `app/Http/Controllers/AdminController.php`
- `app/Http/Controllers/SupportController.php`

Implemented:

- Dashboard stats
- Payments/livraisons list + follow
- Role-upgrade admin workflow
- Complaints admin workflow
- Complaint creation/list-mine
- Chatbot reply scaffold

## 5) Route map

Main route file:

- `routes/api.php`

Organization:

- `Route::middleware('web')` wrapping API groups for session/CSRF behavior.
- `/api/auth/*` group for authentication endpoints.
- `/api/v1/*` group for business endpoints.
- Auth-required routes protected with `auth` + `session.version`.

Reference matrix:

- `docs/api-contract-matrix.md`

## 6) Security and compliance implementation

Implemented:

- Session-based auth baseline.
- Session version invalidation middleware.
- Seller contract enforcement middleware.
- Security audit logging middleware.
- API JSON error normalization.
- CORS config with credentials support:
  - `config/cors.php`

Environment variables prepared:

- `.env.example` includes DB, session, CORS, and GeniusPay-related placeholders.

## 7) Database and migrations

Migration added:

- `database/migrations/2026_05_04_151600_create_webhook_event_table.php`

Purpose:

- Persist external webhook event IDs for idempotency.

Legacy schema strategy:

- Existing Spring Boot schema is assumed and mapped via explicit model metadata.
- No destructive schema rewrites were introduced.

## 8) Testing

Implemented test file:

- `tests/Feature/ApiContractSmokeTest.php`

Coverage:

- CSRF endpoint availability.
- Auth requirement check on protected route.

Important constraint:

- Runtime test execution was not possible in this environment because `php` and `composer` were not installed on the host at execution time.

## 9) Gap analysis for strict 1:1 parity

The following areas are present but still require strict business-equivalence completion:

- Full DTO-level response parity with all frontend expected fields for every endpoint.
- Wallet escrow split rules (vendor/livreur/platform percentages and states).
- Delivery lifecycle locking/concurrency (`lockForUpdate`) equivalent to Spring transactional behavior.
- Full negotiation lifecycle business rules (status transitions, constraints, conversion to cart).
- Password reset token hashing/storage/email pipeline strict equivalence.
- File upload/inspection hardening equivalent to Spring file checks.
- Admin/business authorization granularity parity (role/policy matrix).
- GeniusPay API outbound integration flow finalization (currently endpoint scaffolding + webhook validation baseline).
- Reporting and metrics parity (admin dashboards and sales aggregates).

## 10) Runbook (when PHP/Composer are available)

From `C:\project-e-commerce-back-laravel`:

1. `composer install`
2. `copy .env.example .env`
3. `php artisan key:generate`
4. Configure DB credentials in `.env`
5. `php artisan migrate`
6. `php artisan test`
7. `php artisan serve`

## 11) File inventory created/modified in migration workspace

Core docs/config:

- `docs/api-contract-matrix.md`
- `docs/BACKEND_LARAVEL_COMPLETE_DOCUMENTATION.md`
- `config/cors.php`
- `.env.example`
- `README.md`

Core app:

- `bootstrap/app.php`
- `routes/api.php`
- `app/Http/Controllers/*.php` (migration controllers listed above)
- `app/Http/Middleware/*.php`
- `app/Models/*.php` (legacy mapped core models)
- `app/Services/GeniusPaySignatureService.php`
- `app/Support/ApiResponse.php`
- `database/migrations/2026_05_04_151600_create_webhook_event_table.php`
- `tests/Feature/ApiContractSmokeTest.php`

---

If you want strict final parity, the next phase should be a hardening sprint that replaces remaining scaffolds with exact Spring business logic and validates each endpoint contract with executable integration tests against the frontend payload expectations.
