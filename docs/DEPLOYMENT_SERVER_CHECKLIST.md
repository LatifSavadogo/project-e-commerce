# Deployment Server Checklist (Laravel Migration)

Use this checklist to deploy `C:\project-e-commerce-back-laravel` safely on server.

## 1. Runtime prerequisites

- PHP 8.3+
- Composer 2+
- MySQL/PostgreSQL driver enabled in PHP
- OpenSSL extension enabled
- Web server (Nginx/Apache) + HTTPS
- Process manager for queue/scheduler if later enabled

## 2. Environment variables

Create `.env` from `.env.example` and set at least:

- `APP_ENV=production`
- `APP_DEBUG=false`
- `APP_KEY` (generated once)
- DB settings (`DB_CONNECTION`, `DB_HOST`, `DB_PORT`, `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD`)
- Session/cookie:
  - `SESSION_DRIVER=database` (or redis)
  - `SESSION_SECURE_COOKIE=true`
  - `SESSION_SAME_SITE=lax` (or `none` if cross-site + secure)
  - `SESSION_DOMAIN` set to your API domain
- CORS:
  - `CORS_ALLOWED_ORIGINS=https://your-frontend-domain`
- GeniusPay:
  - `GENIUSPAY_API_KEY`
  - `GENIUSPAY_API_SECRET`
  - `GENIUSPAY_WEBHOOK_SECRET`
- Crypto:
  - `APP_CRYPTO_AES_SECRET`

## 3. Install and boot

Run in project root:

1. `composer install --no-dev --optimize-autoloader`
2. `php artisan key:generate --force`
3. `php artisan config:cache`
4. `php artisan route:cache`
5. `php artisan view:cache`
6. `php artisan migrate --force`

## 4. Permissions

Ensure write permissions for:

- `storage/`
- `bootstrap/cache/`

## 5. Security checks before go-live

- CSRF cookie is present from `GET /api/auth/csrf`.
- Login requires valid credentials and creates session cookie.
- Logout rejects missing CSRF token.
- `/api/v1/admin/*` blocked for non-admin.
- `/api/v1/livreur/*` blocked for non-livreur/admin.
- GeniusPay webhook signature accepted only with valid signature (or secret intentionally empty).

## 6. Smoke tests after deploy

- `GET /api/auth/csrf` -> 200 + token
- `POST /api/auth/login` -> 200
- `GET /api/auth/me` -> returns authenticated object
- `GET /api/v1/articles` -> 200
- `GET /api/v1/cart` unauthenticated -> 401
- `POST /api/v1/geniuspay/webhook` invalid signature -> 403

## 7. Rollback safety

- Keep Spring backend as fallback service until Laravel passes end-to-end validation.
- Run DB backups before first migration run.
- Release behind feature flag or route switch at reverse proxy level.
