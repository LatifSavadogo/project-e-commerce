<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;

class SecurityAuditLogging
{
    public function handle(Request $request, Closure $next)
    {
        $response = $next($request);

        if ($request->is('api/auth/*') || $request->is('api/v1/admin/*')) {
            Log::info('security_audit', [
                'path' => $request->path(),
                'method' => $request->method(),
                'user_id' => optional($request->user())->iduser,
                'status' => method_exists($response, 'status') ? $response->status() : null,
                'ip' => $request->ip(),
            ]);
        }

        return $response;
    }
}
