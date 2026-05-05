<?php

namespace App\Services;

use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;

class PaymentDomainService
{
    public function createPayment(array $payload, int $buyerId): array
    {
        return DB::transaction(function () use ($payload, $buyerId): array {
            if (strtoupper((string) $payload['moyenPaiement']) !== 'WALLET') {
                throw ValidationException::withMessages(['moyenPaiement' => 'Seul WALLET est autorise.']);
            }

            $reference = trim((string) $payload['referenceExterne']);
            if ($reference === '' || mb_strlen($reference) > 120) {
                throw ValidationException::withMessages(['referenceExterne' => 'Reference externe invalide.']);
            }

            $hash = hash('sha256', mb_strtoupper($reference));
            $exists = DB::table('EcomTransaction')->where('refExterneHash', $hash)->exists();
            if ($exists) {
                throw ValidationException::withMessages(['referenceExterne' => 'Reference deja utilisee.']);
            }

            $article = DB::table('Article')->where('idarticle', $payload['idArticle'])->first();
            if (! $article) {
                throw ValidationException::withMessages(['idArticle' => 'Article introuvable.']);
            }
            if ((int) ($article->idvendeur ?? 0) === $buyerId) {
                throw ValidationException::withMessages(['idArticle' => 'Vous ne pouvez pas acheter votre propre article.']);
            }

            $qty = (int) $payload['quantite'];
            $unit = (float) ($payload['prixUnitaireNegocie'] ?? $article->prixunitaire ?? 0);
            if ($qty < 1 || $qty > 100) {
                throw ValidationException::withMessages(['quantite' => 'Quantite invalide.']);
            }
            $total = round($unit * $qty, 2);

            $transactionId = DB::table('EcomTransaction')->insertGetId([
                'idarticle' => $payload['idArticle'],
                'idacheteur' => $buyerId,
                'idvendeur' => $article->idvendeur ?? null,
                'quantite' => $qty,
                'prixUnitaire' => $unit,
                'montantTotal' => $total,
                'moyenPaiement' => 'WALLET',
                'referenceExterne' => $reference,
                'refExterneHash' => $hash,
                'paiementConfirme' => false,
                'datecreation' => now(),
            ], 'idtransaction');

            $livraisonId = app(LivraisonDomainService::class)->createPendingForTransaction((int) $transactionId);

            return [
                'idtransaction' => (int) $transactionId,
                'idArticle' => (int) $payload['idArticle'],
                'quantite' => $qty,
                'prixUnitaire' => $unit,
                'montantTotal' => $total,
                'moyenPaiement' => 'WALLET',
                'idLivraison' => $livraisonId,
                'livraisonStatut' => 'EN_ATTENTE',
            ];
        });
    }
}
