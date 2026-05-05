<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class EnsureRole
{
    public function handle(Request $request, Closure $next, string ...$roles)
    {
        $user = $request->user();
        $role = strtoupper((string) ($user->librole ?? ''));
        if (! $user || ! in_array($role, array_map('strtoupper', $roles), true)) {
            return response()->json([
                'error' => 'Acces refuse pour ce role ou cette ressource',
                'code' => 'FORBIDDEN',
                'path' => '/'.$request->path(),
            ], 403);
        }

        return $next($request);
    }
}
