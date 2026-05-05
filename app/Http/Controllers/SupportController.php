<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class SupportController extends Controller
{
    public function complaintCreate(Request $request)
    {
        $payload = $request->validate([
            'titre' => ['required', 'string'],
            'description' => ['required', 'string'],
            'idArticle' => ['nullable', 'integer'],
        ]);
        $payload['idAuteur'] = $request->user()->iduser;
        $payload['lu'] = false;
        DB::table('Complaint')->insert($payload);
        return response()->json(['created' => true], 201);
    }

    public function complaintsMine(Request $request)
    {
        return response()->json(DB::table('Complaint')->where('idAuteur', $request->user()->iduser)->get());
    }

    public function chatbotReply(Request $request)
    {
        $request->validate(['message' => ['required', 'string']]);
        return response()->json(['reply' => 'Feature connected.']);
    }
}
