<?php

use Illuminate\Foundation\Application;
use Illuminate\Foundation\Configuration\Exceptions;
use Illuminate\Foundation\Configuration\Middleware;
use Illuminate\Auth\AuthenticationException;
use Illuminate\Session\TokenMismatchException;
use Symfony\Component\HttpKernel\Exception\HttpExceptionInterface;

return Application::configure(basePath: dirname(__DIR__))
    ->withRouting(
        api: __DIR__.'/../routes/api.php',
        web: __DIR__.'/../routes/web.php',
        commands: __DIR__.'/../routes/console.php',
        health: '/up',
    )
    ->withMiddleware(function (Middleware $middleware): void {
        $middleware->validateCsrfTokens(except: [
            'api/v1/geniuspay/webhook',
            'api/auth/login',
            'api/auth/register',
            'api/auth/forgot-password',
            'api/auth/reset-password',
            'api/v1/me/vendor-contract/accept',
        ]);

        $middleware->alias([
            'session.version' => \App\Http\Middleware\EnsureSessionVersion::class,
            'seller.contract' => \App\Http\Middleware\EnforceSellerContract::class,
            'security.audit' => \App\Http\Middleware\SecurityAuditLogging::class,
            'role' => \App\Http\Middleware\EnsureRole::class,
        ]);
    })
    ->withExceptions(function (Exceptions $exceptions): void {
        $exceptions->render(function (\Throwable $e, \Illuminate\Http\Request $request) {
            if (! $request->is('api/*')) {
                return null;
            }

            if ($e instanceof TokenMismatchException) {
                return response()->json([
                    'error' => 'Jeton CSRF invalide ou manquant. Rechargez la page puis reessayez.',
                    'code' => 'FORBIDDEN_CSRF',
                    'path' => '/'.$request->path(),
                ], 403);
            }

            if ($e instanceof AuthenticationException) {
                return response()->json([
                    'error' => 'Authentification requise',
                    'code' => 'UNAUTHORIZED',
                ], 401);
            }

            $status = $e instanceof HttpExceptionInterface ? $e->getStatusCode() : 500;

            return response()->json([
                'error' => match ($status) {
                    400 => 'BAD_REQUEST',
                    401 => 'UNAUTHORIZED',
                    403 => 'FORBIDDEN',
                    404 => 'NOT_FOUND',
                    409 => 'CONFLICT',
                    422 => 'VALIDATION_ERROR',
                    default => 'INTERNAL_ERROR',
                },
                'message' => $e->getMessage() ?: 'Unexpected server error.',
                'status' => $status,
            ], $status);
        });
    })->create();
