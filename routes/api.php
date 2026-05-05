<?php

use App\Http\Controllers\AdminController;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\CartController;
use App\Http\Controllers\CatalogController;
use App\Http\Controllers\ConversationController;
use App\Http\Controllers\GeniusPayController;
use App\Http\Controllers\LivreurController;
use App\Http\Controllers\PaymentController;
use App\Http\Controllers\SupportController;
use App\Http\Controllers\UserController;
use App\Http\Controllers\WalletController;
use Illuminate\Support\Facades\Route;

Route::middleware('web')->group(function () {
Route::prefix('auth')->middleware('security.audit')->group(function () {
    Route::get('/csrf', [AuthController::class, 'csrf']);
    Route::post('/login', [AuthController::class, 'login']);
    Route::post('/register', [AuthController::class, 'register']);
    Route::post('/forgot-password', [AuthController::class, 'forgotPassword']);
    Route::post('/reset-password', [AuthController::class, 'resetPassword']);
    Route::get('/me', [AuthController::class, 'me']);
    Route::middleware('auth')->group(function () {
        Route::post('/logout', [AuthController::class, 'logout']);
    });
});

Route::prefix('v1')->group(function () {
    Route::get('/roles', [CatalogController::class, 'roles']);
    Route::post('/roles', [CatalogController::class, 'createRole']);
    Route::put('/roles/{idrole}', [CatalogController::class, 'updateRole']);
    Route::delete('/roles/{idrole}', [CatalogController::class, 'deleteRole']);
    Route::get('/pays', [CatalogController::class, 'pays']);
    Route::post('/pays', [CatalogController::class, 'createPays']);
    Route::put('/pays/{idpays}', [CatalogController::class, 'updatePays']);
    Route::delete('/pays/{idpays}', [CatalogController::class, 'deletePays']);
    Route::get('/typeArticles', [CatalogController::class, 'typeArticles']);
    Route::post('/typeArticles', [CatalogController::class, 'createType']);
    Route::put('/typeArticles/{idtype}', [CatalogController::class, 'updateType']);
    Route::delete('/typeArticles/{idtype}', [CatalogController::class, 'deleteType']);
    Route::get('/familleArticles', [CatalogController::class, 'familleArticles']);
    Route::post('/familleArticles', [CatalogController::class, 'createFamille']);
    Route::put('/familleArticles/{idfamille}', [CatalogController::class, 'updateFamille']);
    Route::delete('/familleArticles/{idfamille}', [CatalogController::class, 'deleteFamille']);
    Route::get('/articles', [CatalogController::class, 'articles']);
    Route::get('/articles/{id}', [CatalogController::class, 'articleById']);
    Route::get('/articles/{id}/photo', [CatalogController::class, 'articlePhoto']);
    Route::get('/articles/{id}/photo/{filename}', [CatalogController::class, 'articlePhotoFile']);

    Route::middleware(['auth', 'session.version', 'seller.contract'])->group(function () {
        Route::get('/cart', [CartController::class, 'show']);
        Route::post('/articles', [CatalogController::class, 'createArticle']);
        Route::put('/articles/{id}/json', [CatalogController::class, 'updateArticleJson']);
        Route::delete('/articles/{id}', [CatalogController::class, 'deleteArticle']);
        Route::get('/admin/articles', [CatalogController::class, 'adminArticles']);
        Route::patch('/admin/articles/{id}', [CatalogController::class, 'adminPatchArticle']);

        Route::post('/cart/items', [CartController::class, 'addItem']);
        Route::post('/cart/items/from-negotiation', [CartController::class, 'addFromNegotiation']);
        Route::patch('/cart/items/{idcartitem}', [CartController::class, 'patchItem']);
        Route::delete('/cart/items/{idcartitem}', [CartController::class, 'deleteItem']);
        Route::delete('/cart', [CartController::class, 'clear']);

        Route::post('/payments', [PaymentController::class, 'create']);
        Route::post('/payments/cart-checkout', [PaymentController::class, 'cartCheckout']);
        Route::get('/payments/mine', [PaymentController::class, 'mine']);
        Route::get('/payments/sales', [PaymentController::class, 'sales']);
        Route::get('/payments/sales/dashboard', [PaymentController::class, 'salesDashboard']);
        Route::get('/payments/{transactionId}/receipt', [PaymentController::class, 'receipt']);
        Route::get('/payments/{transactionId}/livraison/qr', [PaymentController::class, 'qr']);
        Route::get('/payments/{transactionId}/suivi', [PaymentController::class, 'suivi']);
        Route::post('/payments/{idtransaction}/rating', [PaymentController::class, 'rating']);

        Route::post('/conversations', [ConversationController::class, 'create']);
        Route::get('/conversations/mine', [ConversationController::class, 'mine']);
        Route::get('/conversations/{conversationId}/messages', [ConversationController::class, 'messages']);
        Route::post('/conversations/{conversationId}/messages', [ConversationController::class, 'addMessage']);
        Route::patch('/conversations/{conversationId}/messages/{messageId}/offer', [ConversationController::class, 'offer']);
        Route::post('/conversations/{conversationId}/offre-finale-vendeur', [ConversationController::class, 'finalOfferSeller']);
        Route::patch('/conversations/{conversationId}/messages/{messageId}/final-offer', [ConversationController::class, 'finalOfferBuyer']);

        Route::get('/wallet/me', [WalletController::class, 'me']);
        Route::post('/wallet/deposits/checkout', [WalletController::class, 'depositsCheckout']);
        Route::post('/wallet/topups/checkout', [WalletController::class, 'topupsCheckout']);
        Route::post('/wallet/topups/reconcile', [WalletController::class, 'reconcile']);
        Route::post('/wallet/topups/sync-history', [WalletController::class, 'syncHistory']);
        Route::post('/wallet/withdrawals/request', [WalletController::class, 'requestWithdrawal']);

        Route::post('/users/me/delivery-location', [UserController::class, 'setDeliveryLocation']);
        Route::post('/me/delivery-location', [UserController::class, 'setDeliveryLocation']);
        Route::get('/me/role-upgrade', [UserController::class, 'meRoleUpgrade']);
        Route::post('/me/role-upgrade', [UserController::class, 'createRoleUpgrade']);
        Route::get('/me/vendor-certification', [UserController::class, 'vendorCertification']);
        Route::post('/me/vendor-certification/checkout', [UserController::class, 'vendorCertificationCheckout']);
        Route::post('/me/vendor-contract/accept', [UserController::class, 'acceptVendorContract']);
        Route::get('/users/me/data-export', [UserController::class, 'dataExport']);
        Route::get('/vendors/{idVendeur}/rating', [UserController::class, 'vendorRating']);

        Route::middleware('role:LIVREUR,ADMIN,SUPER_ADMIN')->group(function () {
            Route::get('/livreur/dashboard', [LivreurController::class, 'dashboard']);
            Route::get('/livreur/livraisons/disponibles', [LivreurController::class, 'disponibles']);
            Route::get('/livreur/livraisons/mes-livraisons', [LivreurController::class, 'mesLivraisons']);
            Route::post('/livreur/livraisons/{id}/prendre', [LivreurController::class, 'prendre']);
            Route::post('/livreur/livraisons/terminer-par-scan', [LivreurController::class, 'terminerParScan']);
            Route::post('/livreur/livraisons/{idlivraison}/position', [LivreurController::class, 'position']);
            Route::patch('/livreur/profil/engin', [LivreurController::class, 'profilEngin']);
            Route::post('/livreur/livraisons/{id}/ignorer', [LivreurController::class, 'ignorer']);
            Route::post('/livreur/position', [LivreurController::class, 'positionGlobal']);
        });

        Route::middleware('role:ADMIN,SUPER_ADMIN')->group(function () {
            Route::get('/users', [UserController::class, 'index']);
            Route::patch('/users/{iduser}/admin-profile', [UserController::class, 'adminPatch']);
            Route::patch('/users/{iduser}/activation', [UserController::class, 'activation']);
            Route::delete('/users/{iduser}', [UserController::class, 'delete']);
            Route::get('/users/{iduser}/cnib', [UserController::class, 'cnib']);
            Route::put('/users/{iduser}/admin-password', [UserController::class, 'adminPassword']);

            Route::get('/admin/dashboard/stats', [AdminController::class, 'dashboardStats'])->middleware('security.audit');
            Route::get('/admin/payments', [AdminController::class, 'payments']);
            Route::get('/admin/livraisons', [AdminController::class, 'livraisons']);
            Route::get('/admin/livraisons/{idlivraison}/suivi', [AdminController::class, 'livraisonSuivi']);
            Route::get('/admin/role-upgrades', [AdminController::class, 'roleUpgrades']);
            Route::post('/admin/role-upgrades/{id}/approve', [AdminController::class, 'roleUpgradeApprove']);
            Route::post('/admin/role-upgrades/{id}/reject', [AdminController::class, 'roleUpgradeReject']);
            Route::get('/admin/role-upgrades/{id}/fichiers/cnib', [AdminController::class, 'roleUpgradeFileCnib']);
            Route::get('/admin/role-upgrades/{id}/fichiers/photo', [AdminController::class, 'roleUpgradeFilePhoto']);
            Route::get('/admin/complaints', [AdminController::class, 'complaints']);
            Route::patch('/admin/complaints/{id}/lu', [AdminController::class, 'complaintLu']);
        });

        Route::post('/complaints', [SupportController::class, 'complaintCreate']);
        Route::get('/complaints/mine', [SupportController::class, 'complaintsMine']);
        Route::post('/chatbot/reply', [SupportController::class, 'chatbotReply']);
    });

    Route::post('/geniuspay/invoices/order', [GeniusPayController::class, 'orderInvoice']);
    Route::post('/geniuspay/invoices/certification', [GeniusPayController::class, 'certificationInvoice']);
    Route::post('/geniuspay/webhook', [GeniusPayController::class, 'webhook']);
});
});
