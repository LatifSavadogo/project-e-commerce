# Livraisons, suivi acheteur et reçu PDF

Document de référence pour les fonctionnalités ajoutées autour des **livraisons**, du **QR client**, du **suivi de commande** et du **reçu PDF**.

---

## Rôle livreur

Le compte démo livreur est créé au bootstrap (`livreur@demo.ecom`, mot de passe via `BOOTSTRAP_LIVREUR_PASSWORD`). Les routes sous `/api/v1/livreur/**` exigent le rôle **LIVREUR**.

---

## API livreur (`/api/v1/livreur`)

| Méthode | Chemin | Rôle |
|--------|--------|------|
| GET | `/dashboard` | Statistiques (en cours, livrées, etc.) |
| GET | `/livraisons/disponibles` | Courses disponibles |
| GET | `/livraisons/mes-livraisons` | **En cours + historique** (livrées / annulées), DTO `LivreurMesLivraisonsDTO` |
| POST | `/livraisons/{id}/ignorer` | Ignorer une offre |
| POST | `/livraisons/{id}/prendre` | Prise en charge (type d’engin) |
| PATCH | `/livraisons/{id}/position` | Partage position GPS (suivi acheteur) |
| POST | `/livraisons/{id}/terminer` | Finalisation : **scan du QR client** (`LivraisonTerminerRequestDTO` avec contenu scanné) |

La finalisation vérifie le payload `ECOM;<idlivraison>;<jeton>` et le compare au champ `client_delivery_token` en base.

---

## API acheteur / paiement (extrait)

| Méthode | Chemin | Description |
|--------|--------|-------------|
| GET | `/api/v1/payments/{id}/livraison/qr` | Pack QR client : `qrPayload`, image PNG en Base64, même logique que le reçu PDF (côté acheteur). |
| GET | `/api/v1/payments/{id}/suivi` | Suivi étapes, liens Maps selon règles métier (ex. plus de Maps après livraison terminée côté acheteur). |
| GET | `/api/v1/payments/{id}/receipt` | **Reçu PDF** (`Content-Type: application/pdf`). |

### Qui peut télécharger le reçu ?

`PaymentService.assertCanViewReceipt` : **acheteur**, **vendeur**, **staff** (admin / super-admin), ou **livreur assigné** à la transaction.

---

## Reçu PDF (implémentation)

- **Bibliothèque** : OpenPDF (`com.github.librepdf:openpdf`), voir `pom.xml`.
- **Classe principale** : `PaymentReceiptPdfWriter` — mise en page A4, en-tête, tableau de commande, section codes QR.
- **Encodage** : police Helvetica **CP1252** pour le français ; montants formatés sans espaces insécables (`formatFcfa`) pour éviter un PDF corrompu.
- **Vendeur sur le reçu** : nom / prénom uniquement (**pas** d’e-mail vendeur).

### QR sur le reçu

1. **QR réception (gauche)** — réservé à l’**acheteur** sur le PDF (les autres profils voient un message à la place).  
   - Génération : `LivraisonService.buildClientQrPackForBuyer` puis `QrPngService.encodeQrAsPngBytes(payload)` — **même chaîne et même résolution (280 px)** que l’API `/livraison/qr`.  
   - Non affiché si livraison **LIVREE** / **ANNULEE** (aligné avec l’app).
2. **QR commande (droite)** — `BuyerOrderReceiptQrCodec` : JSON minimal (ids, article, quantité, statut livraison) en Base64 URL ; **pas** le jeton secret ; ne remplace pas le QR livreur.

`PaymentService.buildReceiptPdf` est transactionnel **read-write** pour permettre la création du jeton client si besoin (comme l’endpoint QR).

---

## Services et utilitaires utiles

| Élément | Rôle |
|---------|------|
| `LivraisonService` | Création livraison, QR client, suivi, maps, scan livreur, partition « mes livraisons ». |
| `QrPngService` | Génération PNG (ZXing), taille par défaut 280 px ; surcharge pour autres tailles si besoin. |
| `BuyerOrderReceiptQrCodec` | Charge utile du QR « référence d’achat » sur le reçu. |
| `DeliveryTokens` | Jeton hex sécurisé pour le QR client. |
| `LivraisonRepository` | Requêtes dédiées (ex. historique livreur avec limite raisonnable). |

---

## Tests

- `mvn test` — dont `LivraisonApresPaiementIntegrationTest` (paiement → QR client → prise en charge → scan → LIVREE).

---

## Front (hors dépôt)

Pour le téléchargement du PDF, le client HTTP doit envoyer **`Accept: application/pdf`** sur `GET .../receipt` et gérer le corps binaire + en-têtes `Content-Type` / `Content-Length`.
