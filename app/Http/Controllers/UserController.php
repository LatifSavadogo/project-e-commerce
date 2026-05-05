<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;

class UserController extends Controller
{
    public function setDeliveryLocation(Request $request)
    {
        $payload = $request->validate([
            'latitude' => ['required', 'numeric'],
            'longitude' => ['required', 'numeric'],
        ]);
        $request->user()->update($payload);
        return response()->json(['saved' => true]);
    }

    public function meRoleUpgrade()
    {
        return response()->json(DB::table('RoleUpgradeRequest')->where('iduser', auth()->id())->latest('id')->first());
    }

    public function createRoleUpgrade(Request $request)
    {
        $payload = $request->validate([
            'roleDemande' => ['required', 'string'],
            'vendeurInternational' => ['nullable', 'boolean'],
        ]);
        $payload['iduser'] = $request->user()->iduser;
        $payload['status'] = 'PENDING';
        DB::table('RoleUpgradeRequest')->insert($payload);
        return response()->json(['status' => 'PENDING'], 201);
    }

    public function vendorCertification()
    {
        return response()->json(['active' => false, 'until' => null]);
    }

    public function vendorCertificationCheckout()
    {
        return response()->json(['checkoutUrl' => null], 201);
    }

    public function acceptVendorContract(Request $request)
    {
        $request->user()->update(['vendeurContratVenteAccepte' => true]);
        return response()->json(['accepted' => true]);
    }

    public function dataExport()
    {
        return response()->json(['export' => null]);
    }

    public function vendorRating(int $idVendeur)
    {
        return response()->json(['idVendeur' => $idVendeur, 'noteMoyenne' => null, 'nombreAvis' => 0]);
    }

    public function index()
    {
        return response()->json(User::query()->paginate(25));
    }

    public function adminPatch(Request $request, int $iduser)
    {
        $user = User::findOrFail($iduser);
        $user->update($request->only(['idrole', 'idpays', 'idtypeVendeur', 'vendeurInternational']));
        return response()->json($user);
    }

    public function activation(Request $request, int $iduser)
    {
        $user = User::findOrFail($iduser);
        $user->compteActif = (bool) $request->input('compteActif', true);
        $user->save();
        return response()->json($user);
    }

    public function delete(int $iduser)
    {
        User::where('iduser', $iduser)->delete();
        return response()->json(['deleted' => true]);
    }

    public function cnib(int $iduser)
    {
        $user = User::findOrFail($iduser);
        return response()->json(['iduser' => $iduser, 'cnib' => $user->cnib]);
    }

    public function adminPassword(Request $request, int $iduser)
    {
        $request->validate(['password' => ['required', 'string', 'min:8']]);
        $user = User::findOrFail($iduser);
        $user->password = Hash::make($request->string('password'));
        $user->save();
        return response()->json(['updated' => true]);
    }
}
