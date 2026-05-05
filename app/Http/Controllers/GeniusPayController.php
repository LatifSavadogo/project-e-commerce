<?php

namespace App\Http\Controllers;

use App\Services\GeniusPaySignatureService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class GeniusPayController extends Controller
{
    public function orderInvoice()
    {
        return response()->json(['checkoutUrl' => null, 'geniuspayReference' => null], 201);
    }

    public function certificationInvoice()
    {
        return response()->json(['checkoutUrl' => null, 'geniuspayReference' => null], 201);
    }

    public function webhook(Request $request, GeniusPaySignatureService $signatureService)
    {
        $signature = $request->header('X-Webhook-Signature');
        $legacySignature = $request->header('X-GENIUSPAY-SIGNATURE');
        $timestamp = $request->header('X-Webhook-Timestamp');
        $raw = (string) $request->getContent();
        if (! $signatureService->isValid($raw, $signature, $legacySignature, $timestamp)) {
            return response()->json(['error' => 'FORBIDDEN', 'message' => 'Invalid webhook signature'], 403);
        }

        $eventId = (string) $request->input('id');
        if ($eventId !== '') {
            $exists = DB::table('WebhookEvent')->where('provider', 'GENIUSPAY')->where('provider_event_id', $eventId)->exists();
            if ($exists) {
                return response()->json(['received' => true, 'duplicate' => true]);
            }
            DB::table('WebhookEvent')->insert([
                'provider' => 'GENIUSPAY',
                'provider_event_id' => $eventId,
                'payload' => $raw,
                'created_at' => now(),
            ]);
        }

        return response()->json(['received' => true, 'event' => $request->input('event')]);
    }
}
