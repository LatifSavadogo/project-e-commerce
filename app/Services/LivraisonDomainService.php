<?php

namespace App\Services;

use Illuminate\Support\Facades\DB;

class LivraisonDomainService
{
    public const MAX_DISTANCE_LIVREUR_VENDEUR_KM = 5.0;
    public const MAX_DISTANCE_TOTALE_KM = 15.0;

    public function createPendingForTransaction(int $transactionId): int
    {
        $token = bin2hex(random_bytes(16));
        $pickupCode = (string) random_int(1000000000, 9999999999);

        $id = DB::table('Livraison')->insertGetId([
            'idtransaction' => $transactionId,
            'statut' => 'EN_ATTENTE',
            'clientDeliveryToken' => $token,
            'vendorPickupCode' => $pickupCode,
            'datecreation' => now(),
        ], 'idlivraison');

        return (int) $id;
    }

    public function terminerParScan(int $idLivraison, string $payload): bool
    {
        return DB::transaction(function () use ($idLivraison, $payload): bool {
            $livraison = DB::table('Livraison')->where('idlivraison', $idLivraison)->lockForUpdate()->first();
            if (! $livraison || $livraison->statut !== 'EN_COURS') {
                return false;
            }

            $parts = explode(';', $payload);
            if (count($parts) !== 3 || $parts[0] !== 'ECOM' || (int) $parts[1] !== $idLivraison) {
                return false;
            }

            if (! hash_equals((string) ($livraison->clientDeliveryToken ?? ''), (string) $parts[2])) {
                return false;
            }

            DB::table('Livraison')->where('idlivraison', $idLivraison)->update([
                'statut' => 'LIVREE',
                'dateLivraison' => now(),
            ]);
            return true;
        });
    }
}
