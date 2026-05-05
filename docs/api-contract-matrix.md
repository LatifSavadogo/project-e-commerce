# API Contract Matrix (Spring Boot -> Laravel)

This matrix mirrors the frontend-consumed API contract and is the implementation checklist for the Laravel migration.

## Auth

- `GET /api/auth/csrf`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `POST /api/auth/register`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`

## Core Catalog

- `GET /api/v1/pays`
- `GET /api/v1/typeArticles`
- `GET /api/v1/familleArticles`
- `GET /api/v1/articles`
- `GET /api/v1/articles/{id}`
- `GET /api/v1/articles/{id}/photo`
- `GET /api/v1/articles/{id}/photo/{filename}`

## Cart

- `GET /api/v1/cart`
- `POST /api/v1/cart/items`
- `PATCH /api/v1/cart/items/{idcartitem}`
- `DELETE /api/v1/cart/items/{idcartitem}`
- `DELETE /api/v1/cart`
- `POST /api/v1/cart/items/from-negotiation`

## Payments

- `POST /api/v1/payments`
- `POST /api/v1/payments/cart-checkout`
- `GET /api/v1/payments/mine`
- `GET /api/v1/payments/sales`
- `GET /api/v1/payments/sales/dashboard`
- `GET /api/v1/payments/{transactionId}/receipt`
- `GET /api/v1/payments/{transactionId}/livraison/qr`
- `GET /api/v1/payments/{transactionId}/suivi`
- `POST /api/v1/payments/{idtransaction}/rating`

## Conversation / Negotiation

- `POST /api/v1/conversations`
- `GET /api/v1/conversations/mine`
- `GET /api/v1/conversations/{conversationId}/messages`
- `POST /api/v1/conversations/{conversationId}/messages`
- `PATCH /api/v1/conversations/{conversationId}/messages/{messageId}/offer`
- `POST /api/v1/conversations/{conversationId}/offre-finale-vendeur`
- `PATCH /api/v1/conversations/{conversationId}/messages/{messageId}/final-offer`

## Wallet / GeniusPay

- `GET /api/v1/wallet/me`
- `POST /api/v1/wallet/deposits/checkout`
- `POST /api/v1/wallet/topups/checkout`
- `POST /api/v1/wallet/topups/reconcile`
- `POST /api/v1/wallet/topups/sync-history`
- `POST /api/v1/wallet/withdrawals/request`
- `POST /api/v1/geniuspay/invoices/order`
- `POST /api/v1/geniuspay/invoices/certification`
- `POST /api/v1/geniuspay/webhook`

## User / Role Upgrade / Vendor

- `POST /api/v1/users/me/delivery-location`
- `POST /api/v1/me/delivery-location`
- `GET /api/v1/me/role-upgrade`
- `POST /api/v1/me/role-upgrade`
- `GET /api/v1/me/vendor-certification`
- `POST /api/v1/me/vendor-certification/checkout`
- `POST /api/v1/me/vendor-contract/accept`
- `GET /api/v1/users/me/data-export`
- `GET /api/v1/vendors/{idVendeur}/rating`

## Livreur

- `GET /api/v1/livreur/dashboard`
- `GET /api/v1/livreur/livraisons/disponibles`
- `GET /api/v1/livreur/livraisons/mes-livraisons`
- `POST /api/v1/livreur/livraisons/{id}/prendre`
- `POST /api/v1/livreur/livraisons/terminer-par-scan`
- `POST /api/v1/livreur/livraisons/{idlivraison}/position`
- `PATCH /api/v1/livreur/profil/engin`
- `POST /api/v1/livreur/livraisons/{id}/ignorer`
- `POST /api/v1/livreur/position`

## Admin

- `GET /api/v1/admin/dashboard/stats`
- `GET /api/v1/admin/payments`
- `GET /api/v1/admin/livraisons`
- `GET /api/v1/admin/livraisons/{idlivraison}/suivi`
- `GET /api/v1/users`
- `PATCH /api/v1/users/{iduser}/admin-profile`
- `PATCH /api/v1/users/{iduser}/activation`
- `DELETE /api/v1/users/{iduser}`
- `GET /api/v1/users/{iduser}/cnib`
- `PUT /api/v1/users/{iduser}/admin-password`
- `GET /api/v1/admin/role-upgrades`
- `POST /api/v1/admin/role-upgrades/{id}/approve`
- `POST /api/v1/admin/role-upgrades/{id}/reject`
- `GET /api/v1/admin/role-upgrades/{id}/fichiers/cnib`
- `GET /api/v1/admin/role-upgrades/{id}/fichiers/photo`
- `GET /api/v1/admin/complaints`
- `PATCH /api/v1/admin/complaints/{id}/lu`

## Support

- `POST /api/v1/complaints`
- `GET /api/v1/complaints/mine`
- `POST /api/v1/chatbot/reply`
