<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Services\PasswordResetService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;

class AuthController extends Controller
{
    public function csrf(Request $request)
    {
        return response()->json(['token' => csrf_token()]);
    }

    public function login(Request $request)
    {
        $credentials = $request->validate([
            'email' => ['required', 'email'],
            'password' => ['required', 'string'],
        ]);

        $user = User::where('email', $credentials['email'])->first();
        if (! $user || ! Hash::check($credentials['password'], $user->password)) {
            return response()->json(['error' => 'Email ou mot de passe incorrect'], 401);
        }
        if (isset($user->compteActif) && ! $user->compteActif) {
            return response()->json(['error' => 'Compte desactive. Contactez un administrateur.'], 401);
        }

        Auth::login($user);
        $request->session()->regenerate();
        $request->session()->put('ECOM_SESSION_VERSION', (int) ($user->session_version ?? 0));

        return response()->json(['authenticated' => true, 'user' => $user]);
    }

    public function me(Request $request)
    {
        $user = $request->user();
        if (! $user || (isset($user->compteActif) && ! $user->compteActif)) {
            return response()->json(['authenticated' => false]);
        }

        return response()->json(['authenticated' => true, 'user' => $user]);
    }

    public function logout(Request $request)
    {
        Auth::guard('web')->logout();
        $request->session()->invalidate();
        $request->session()->regenerateToken();

        return response()->json(['success' => true, 'message' => 'Deconnexion reussie']);
    }

    public function register(Request $request)
    {
        $validated = $request->validate([
            'nom' => ['required', 'string'],
            'prenom' => ['required', 'string'],
            'email' => ['required', 'email', 'unique:User,email'],
            'password' => ['required', 'string', 'min:8'],
            'idpays' => ['nullable', 'integer'],
            'ville' => ['nullable', 'string'],
        ]);

        $acheteur = \App\Models\Role::whereRaw('UPPER(librole)=?', ['ACHETEUR'])->first();
        $validated['idrole'] = $acheteur?->idrole ?? $validated['idrole'] ?? null;
        $user = User::create($validated);
        return response()->json($user, 201);
    }

    public function forgotPassword(Request $request, PasswordResetService $passwordResetService)
    {
        $request->validate(['email' => ['required', 'email']]);
        $passwordResetService->createTokenForEmail((string) $request->input('email'));
        return response()->json(['message' => 'Si cette adresse est inscrite, un e-mail de reinitialisation a ete envoye.']);
    }

    public function resetPassword(Request $request, PasswordResetService $passwordResetService)
    {
        $payload = $request->validate([
            'token' => ['required', 'string'],
            'newPassword' => ['required', 'string', 'min:6'],
        ]);
        if (! $passwordResetService->reset($payload['token'], $payload['newPassword'])) {
            return response()->json(['error' => 'Lien invalide ou expire. Demandez un nouvel e-mail.'], 400);
        }
        return response()->json(['message' => 'Mot de passe mis a jour. Vous pouvez vous connecter.']);
    }
}
