<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class AdminController extends Controller
{
    public function dashboardStats()
    {
        return response()->json([
            'users' => DB::table('User')->count(),
            'articles' => DB::table('Article')->count(),
            'transactions' => DB::table('EcomTransaction')->count(),
        ]);
    }

    public function payments()
    {
        return response()->json(DB::table('EcomTransaction')->orderByDesc('idtransaction')->limit(200)->get());
    }

    public function livraisons()
    {
        return response()->json(DB::table('Livraison')->orderByDesc('idlivraison')->limit(200)->get());
    }

    public function livraisonSuivi(int $idlivraison)
    {
        return response()->json(DB::table('Livraison')->where('idlivraison', $idlivraison)->first());
    }

    public function roleUpgrades(Request $request)
    {
        $status = $request->query('status');
        $query = DB::table('RoleUpgradeRequest');
        if ($status) {
            $query->where('status', $status);
        }
        return response()->json($query->orderByDesc('id')->get());
    }

    public function roleUpgradeApprove(int $id)
    {
        DB::table('RoleUpgradeRequest')->where('id', $id)->update(['status' => 'APPROVED']);
        return response()->json(['id' => $id, 'status' => 'APPROVED']);
    }

    public function roleUpgradeReject(Request $request, int $id)
    {
        DB::table('RoleUpgradeRequest')->where('id', $id)->update([
            'status' => 'REJECTED',
            'adminMotif' => $request->input('adminMotif'),
        ]);
        return response()->json(['id' => $id, 'status' => 'REJECTED']);
    }

    public function roleUpgradeFileCnib(int $id)
    {
        return response()->json(['id' => $id, 'file' => 'cnib']);
    }

    public function roleUpgradeFilePhoto(int $id)
    {
        return response()->json(['id' => $id, 'file' => 'photo']);
    }

    public function complaints()
    {
        return response()->json(DB::table('Complaint')->orderByDesc('idplainte')->get());
    }

    public function complaintLu(int $id)
    {
        DB::table('Complaint')->where('idplainte', $id)->update(['lu' => true]);
        return response()->json(['id' => $id, 'lu' => true]);
    }
}
