<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class EnforceSellerContract
{
    private const WHITELIST = [
        'api/auth/me',
        'api/auth/logout',
        'api/v1/me/vendor-contract/accept',
        'api/v1/me/role-upgrade',
        'api/v1/me/role-upgrade/*',
        'api/v1/users/me/delivery-location',
        'api/v1/me/delivery-location',
        'api/v1/cart',
        'api/v1/cart/*',
        'api/v1/payments',
        'api/v1/payments/*',
        'api/v1/wallet',
        'api/v1/wallet/*',
    ];

    public function handle(Request $request, Closure $next)
    {
        $user = $request->user();
        if (! $user) {
            return $next($request);
        }

        $isSeller = strtolower((string) ($user->librole ?? '')) === 'vendeur';
        $accepted = (bool) ($user->vendeurContratVenteAccepte ?? true);

        if ($isSeller && ! $accepted && ! $request->is(self::WHITELIST)) {
            return response()->json([
                'error' => 'VENDOR_CONTRACT_REQUIRED',
            ], 403);
        }

        return $next($request);
    }
}
