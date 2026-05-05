<?php

namespace App\Services;

class GeniusPaySignatureService
{
    public function isValid(string $rawBody, ?string $signatureHeader, ?string $legacyHeader = null, ?string $timestamp = null): bool
    {
        $secret = (string) env('GENIUSPAY_WEBHOOK_SECRET', '');
        if ($secret === '') {
            return true;
        }
        if (! $signatureHeader && ! $legacyHeader) {
            return false;
        }

        $rawComputed = hash_hmac('sha256', $rawBody, $secret);
        if ($legacyHeader && hash_equals($rawComputed, $legacyHeader)) {
            return true;
        }

        if ($signatureHeader && hash_equals($rawComputed, $signatureHeader)) {
            return true;
        }

        if ($signatureHeader && $timestamp) {
            $tsComputed = hash_hmac('sha256', $timestamp.'.'.$rawBody, $secret);
            return hash_equals($tsComputed, $signatureHeader);
        }

        return false;
    }
}
