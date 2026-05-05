<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class EnsureSessionVersion
{
    public function handle(Request $request, Closure $next)
    {
        $user = $request->user();
        if (! $user || ! $request->is('api/*')) {
            return $next($request);
        }

        if (isset($user->compteActif) && ! $user->compteActif) {
            auth()->logout();
            $request->session()->invalidate();

            return response()->json([
                'authenticated' => false,
                'accountDisabled' => true,
            ], 401);
        }

        $stored = $request->session()->get('ECOM_SESSION_VERSION');
        $userVersion = (int) ($user->session_version ?? 0);
        if ($stored === null) {
            $request->session()->put('ECOM_SESSION_VERSION', $userVersion);
            return $next($request);
        }

        $sessionVersion = (int) $stored;
        if ($sessionVersion !== $userVersion) {
            auth()->logout();
            $request->session()->invalidate();

            return response()->json([
                'authenticated' => false,
                'sessionRevoked' => true,
            ], 401);
        }

        return $next($request);
    }
}
