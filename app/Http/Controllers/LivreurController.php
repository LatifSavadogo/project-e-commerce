<?php

namespace App\Http\Controllers;

use App\Services\LivraisonDomainService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class LivreurController extends Controller
{
    public function dashboard()
    {
        return response()->json([
            'livraisonsEnCours' => 0,
            'livraisonsLivrees' => 0,
            'livraisonsLivreesMoto' => 0,
            'livraisonsLivreesVehicule' => 0,
            'dernieresCourses' => [],
        ]);
    }

    public function disponibles()
    {
        return response()->json([]);
    }

    public function mesLivraisons()
    {
        return response()->json(['enCours' => [], 'terminees' => [], 'limiteChargee' => 0]);
    }

    public function prendre(Request $request, int $id)
    {
        $request->validate(['typeEngin' => ['required', 'string']]);
        $ok = DB::transaction(function () use ($request, $id): bool {
            $livraison = DB::table('Livraison')->where('idlivraison', $id)->lockForUpdate()->first();
            if (! $livraison || ($livraison->statut ?? null) !== 'EN_ATTENTE' || ! empty($livraison->idlivreur)) {
                return false;
            }
            DB::table('Livraison')->where('idlivraison', $id)->update([
                'idlivreur' => $request->user()->iduser,
                'statut' => 'EN_COURS',
                'typeEnginUtilise' => $request->input('typeEngin'),
                'datePriseEnCharge' => now(),
            ]);
            return true;
        });
        if (! $ok) {
            return response()->json(['error' => 'BUSINESS_RULE', 'message' => 'Livraison indisponible.'], 409);
        }
        return response()->json(['idlivraison' => $id, 'taken' => true]);
    }

    public function terminerParScan(Request $request, LivraisonDomainService $livraisonDomainService)
    {
        $payload = $request->validate([
            'idlivraison' => ['required', 'integer'],
            'clientQrPayload' => ['required', 'string'],
        ]);
        $ok = $livraisonDomainService->terminerParScan((int) $payload['idlivraison'], (string) $payload['clientQrPayload']);
        if (! $ok) {
            return response()->json(['error' => 'BUSINESS_RULE', 'message' => 'QR invalide ou livraison non eligible.'], 400);
        }
        return response()->json(['completed' => true]);
    }

    public function position(Request $request, int $idlivraison)
    {
        $request->validate(['latitude' => ['required', 'numeric'], 'longitude' => ['required', 'numeric']]);
        return response()->json(['idlivraison' => $idlivraison, 'updated' => true]);
    }

    public function profilEngin(Request $request)
    {
        $request->validate(['typeEngin' => ['required', 'string']]);
        return response()->json(['updated' => true]);
    }

    public function ignorer(int $id)
    {
        return response()->json(['idlivraison' => $id, 'ignored' => true]);
    }

    public function positionGlobal(Request $request)
    {
        $request->validate(['latitude' => ['required', 'numeric'], 'longitude' => ['required', 'numeric']]);
        return response()->json(['updated' => true]);
    }
}
