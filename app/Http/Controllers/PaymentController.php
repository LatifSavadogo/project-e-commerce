<?php

namespace App\Http\Controllers;

use App\Models\EcomTransaction;
use App\Services\PaymentDomainService;
use Illuminate\Http\Request;

class PaymentController extends Controller
{
    public function create(Request $request, PaymentDomainService $paymentDomainService)
    {
        $payload = $request->validate([
            'idArticle' => ['required', 'integer'],
            'quantite' => ['required', 'integer', 'min:1', 'max:100'],
            'moyenPaiement' => ['required', 'string'],
            'referenceExterne' => ['required', 'string'],
            'prixUnitaireNegocie' => ['nullable', 'numeric'],
            'livraisonLatitude' => ['nullable', 'numeric', 'between:-90,90'],
            'livraisonLongitude' => ['nullable', 'numeric', 'between:-180,180'],
        ]);

        $result = $paymentDomainService->createPayment($payload, (int) $request->user()->iduser);
        return response()->json($result, 201);
    }

    public function cartCheckout(Request $request)
    {
        $request->validate([
            'moyenPaiement' => ['required', 'string'],
            'referenceExterne' => ['required', 'string'],
            'cartItemIds' => ['required', 'array'],
            'livraisonLatitude' => ['nullable', 'numeric', 'between:-90,90'],
            'livraisonLongitude' => ['nullable', 'numeric', 'between:-180,180'],
        ]);
        return response()->json(['status' => 'ok', 'mode' => 'cart-checkout'], 201);
    }

    public function mine(Request $request)
    {
        return response()->json(EcomTransaction::where('idacheteur', $request->user()->iduser)->get());
    }

    public function sales(Request $request)
    {
        return response()->json(EcomTransaction::where('idvendeur', $request->user()->iduser)->get());
    }

    public function salesDashboard(Request $request)
    {
        $sales = EcomTransaction::where('idvendeur', $request->user()->iduser)->get();
        return response()->json([
            'total' => $sales->count(),
            'amount' => $sales->sum('montantTotal'),
        ]);
    }

    public function receipt(int $transactionId)
    {
        return response("Receipt for transaction {$transactionId}", 200, ['Content-Type' => 'application/pdf']);
    }

    public function qr(int $transactionId)
    {
        return response()->json([
            'idtransaction' => $transactionId,
            'idlivraison' => 0,
            'qrPayload' => base64_encode((string) $transactionId),
            'qrImagePngBase64' => '',
        ]);
    }

    public function suivi(int $transactionId)
    {
        return response()->json([
            'idtransaction' => $transactionId,
            'livreurAssigne' => false,
            'navigationDisponible' => false,
            'etapes' => [],
        ]);
    }

    public function rating(Request $request, int $idtransaction)
    {
        $request->validate(['note' => ['required', 'numeric', 'min:1', 'max:5']]);
        return response()->json(['idtransaction' => $idtransaction, 'saved' => true], 201);
    }
}
