<?php

namespace App\Http\Controllers;

use App\Models\Cart;
use App\Models\CartItem;
use Illuminate\Http\Request;

class CartController extends Controller
{
    public function show(Request $request)
    {
        $user = $request->user();
        $cart = Cart::firstOrCreate(['iduser' => $user->iduser]);
        $items = CartItem::where('idcart', $cart->idcart)->get();
        return response()->json([
            'idcart' => $cart->idcart,
            'items' => $items,
            'montantTotalEstime' => $items->sum(fn ($item) => ($item->prixUnitaireNegocie ?? $item->prixUnitaireCatalogue ?? 0) * ($item->quantity ?? 0)),
        ]);
    }

    public function addItem(Request $request)
    {
        $payload = $request->validate([
            'idArticle' => ['required', 'integer'],
            'quantity' => ['required', 'integer', 'min:1'],
        ]);
        $cart = Cart::firstOrCreate(['iduser' => $request->user()->iduser]);
        $item = CartItem::create(array_merge($payload, ['idcart' => $cart->idcart]));
        return response()->json($item, 201);
    }

    public function addFromNegotiation(Request $request)
    {
        $payload = $request->validate([
            'conversationId' => ['required', 'integer'],
            'messageId' => ['required', 'integer'],
        ]);
        return response()->json(['added' => true, 'source' => $payload], 201);
    }

    public function patchItem(Request $request, int $idcartitem)
    {
        $item = CartItem::findOrFail($idcartitem);
        $item->update($request->validate(['quantity' => ['required', 'integer', 'min:1']]));
        return response()->json($item);
    }

    public function deleteItem(int $idcartitem)
    {
        CartItem::where('idcartitem', $idcartitem)->delete();
        return response()->json(['deleted' => true]);
    }

    public function clear(Request $request)
    {
        $cart = Cart::where('iduser', $request->user()->iduser)->first();
        if ($cart) {
            CartItem::where('idcart', $cart->idcart)->delete();
        }
        return response()->json(['cleared' => true]);
    }
}
