<?php

namespace App\Services;

use App\Models\Wallet;
use Illuminate\Support\Facades\DB;

class WalletDomainService
{
    public const SELLER_ESCROW_PERCENT = 85;
    public const LIVREUR_ESCROW_PERCENT = 10;
    public const PLATFORM_ESCROW_PERCENT = 5;

    public function getOrCreateForUser(int $userId): Wallet
    {
        return Wallet::firstOrCreate(['iduser' => $userId], [
            'wallet_code' => "USER_{$userId}",
            'solde' => 0,
            'solde_en_attente' => 0,
        ]);
    }

    public function settleOrderEscrow(int $transactionId, int $sellerId, ?int $livreurId, float $amount): void
    {
        DB::transaction(function () use ($transactionId, $sellerId, $livreurId, $amount): void {
            $sellerWallet = Wallet::where('iduser', $sellerId)->lockForUpdate()->first();
            if (! $sellerWallet) {
                $sellerWallet = $this->getOrCreateForUser($sellerId);
            }

            $sellerPart = round($amount * self::SELLER_ESCROW_PERCENT / 100, 2);
            $livreurPart = round($amount * self::LIVREUR_ESCROW_PERCENT / 100, 2);
            $platformPart = round($amount * self::PLATFORM_ESCROW_PERCENT / 100, 2);

            $sellerWallet->solde_en_attente = ((float) $sellerWallet->solde_en_attente) + $sellerPart;
            $sellerWallet->save();

            if ($livreurId) {
                $livreurWallet = Wallet::where('iduser', $livreurId)->lockForUpdate()->first();
                if (! $livreurWallet) {
                    $livreurWallet = $this->getOrCreateForUser($livreurId);
                }
                $livreurWallet->solde_en_attente = ((float) $livreurWallet->solde_en_attente) + $livreurPart;
                $livreurWallet->save();
            } else {
                $platformPart += $livreurPart;
            }

            $platformWallet = Wallet::where('wallet_code', 'PLATFORM_MAIN')->lockForUpdate()->first();
            if (! $platformWallet) {
                $platformWallet = Wallet::create(['wallet_code' => 'PLATFORM_MAIN', 'solde' => 0, 'solde_en_attente' => 0]);
            }
            $platformWallet->solde_en_attente = ((float) $platformWallet->solde_en_attente) + $platformPart;
            $platformWallet->save();

            DB::table('EcomTransaction')->where('idtransaction', $transactionId)->update([
                'paiementConfirme' => true,
            ]);
        });
    }
}
