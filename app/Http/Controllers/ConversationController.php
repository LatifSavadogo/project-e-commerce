<?php

namespace App\Http\Controllers;

use App\Models\ChatMessage;
use App\Models\Conversation;
use Illuminate\Http\Request;

class ConversationController extends Controller
{
    public function create(Request $request)
    {
        $payload = $request->validate([
            'idVendeur' => ['required', 'integer'],
            'idArticle' => ['required', 'integer'],
        ]);
        return response()->json(Conversation::create($payload + ['idAcheteur' => $request->user()->iduser]), 201);
    }

    public function mine(Request $request)
    {
        return response()->json(
            Conversation::where('idAcheteur', $request->user()->iduser)
                ->orWhere('idVendeur', $request->user()->iduser)
                ->get()
        );
    }

    public function messages(int $conversationId)
    {
        return response()->json(ChatMessage::where('idconversation', $conversationId)->get());
    }

    public function addMessage(Request $request, int $conversationId)
    {
        $payload = $request->validate([
            'contenu' => ['required', 'string'],
            'prixPropose' => ['nullable', 'numeric'],
            'quantite' => ['nullable', 'numeric'],
        ]);

        return response()->json(ChatMessage::create($payload + [
            'idconversation' => $conversationId,
            'idAuteur' => $request->user()->iduser,
        ]), 201);
    }

    public function offer(Request $request, int $conversationId, int $messageId)
    {
        $request->validate(['statut' => ['required', 'string']]);
        return response()->json(['conversationId' => $conversationId, 'messageId' => $messageId, 'updated' => true]);
    }

    public function finalOfferSeller(Request $request, int $conversationId)
    {
        $request->validate(['prix' => ['required', 'numeric']]);
        return response()->json(['conversationId' => $conversationId, 'offreFinaleVendeur' => true]);
    }

    public function finalOfferBuyer(Request $request, int $conversationId, int $messageId)
    {
        $request->validate(['accept' => ['required', 'boolean']]);
        return response()->json(['conversationId' => $conversationId, 'messageId' => $messageId, 'validated' => true]);
    }
}
