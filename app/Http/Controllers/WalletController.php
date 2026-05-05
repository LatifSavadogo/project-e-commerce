<?php

namespace App\Http\Controllers;

use App\Models\Wallet;
use App\Services\WalletDomainService;
use Illuminate\Http\Request;

class WalletController extends Controller
{
    public function me(Request $request, WalletDomainService $walletDomainService)
    {
        $wallet = $walletDomainService->getOrCreateForUser((int) $request->user()->iduser);
        return response()->json($wallet);
    }

    public function depositsCheckout(Request $request)
    {
        $request->validate(['amount' => ['required', 'numeric', 'min:0.01']]);
        return response()->json(['checkoutUrl' => null, 'status' => 'PENDING'], 201);
    }

    public function topupsCheckout(Request $request)
    {
        $request->validate(['amount' => ['required', 'numeric', 'min:200']]);
        return response()->json(['checkoutUrl' => null, 'status' => 'PENDING'], 201);
    }

    public function reconcile(Request $request)
    {
        $request->validate(['reference' => ['nullable', 'string']]);
        return response()->json(['reconciled' => true]);
    }

    public function syncHistory()
    {
        return response()->json(['synced' => true]);
    }

    public function requestWithdrawal(Request $request)
    {
        $request->validate([
            'montant' => ['required', 'numeric', 'min:100'],
            'mobileMoneyNumber' => ['nullable', 'string', 'max:32'],
        ]);
        return response()->json(['status' => 'REQUESTED'], 201);
    }
}
