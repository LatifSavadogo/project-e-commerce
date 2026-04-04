# Ecomarket — Backend Spring Boot

API REST pour une marketplace (Ecomarket) : comptes acheteur / vendeur / admin, annonces, messagerie avec offres de prix, paiements traçables, plaintes, administration et outils (export RGPD, chatbot par règles, réinitialisation de mot de passe par e-mail).

---

## Stack

| Élément | Technologie |
|--------|-------------|
| Runtime | Java 17 |
| Framework | Spring Boot 3.5.x |
| Persistance | Spring Data JPA, Hibernate |
| Base (prod / dev) | MySQL |
| Sécurité | Spring Security, sessions HTTP (~24 h), BCrypt |
| API | REST, multipart pour fichiers |
| Documentation | Springdoc OpenAPI 3 (Swagger UI) |
| E-mail | Spring Mail (SMTP) |

Les tests Maven utilisent **H2** en mémoire (`src/test/resources/application.properties`).

---

## Prérequis

- JDK 17+
- Maven 3.9+
- MySQL avec une base créée (ex. `projetecommerce`), alignée sur `spring.datasource.*` dans `application.properties`

---

## Lancement

```bash
mvn spring-boot:run
```

Application (par défaut) : `http://localhost:8080`  
Swagger UI : `http://localhost:8080/swagger-ui.html`  
OpenAPI JSON : `http://localhost:8080/v3/api-docs`

---

## Configuration importante (`application.properties` / variables d’environnement)

| Clé / variable | Description |
|----------------|-------------|
| `spring.datasource.*` | URL, utilisateur, mot de passe MySQL |
| `app.security.bootstrap-super-admin-password` / `BOOTSTRAP_SUPER_ADMIN_PASSWORD` | Mot de passe initial des comptes super-admin bootstrap (`latif@admin.com`, `pare@admin.com`) |
| `app.crypto.aes-secret` / `APP_CRYPTO_AES_SECRET` | Secret pour le chiffrement AES des références de paiement en base |
| `app.security.swagger-open` | `true` : Swagger sans session ; `false` : UI + spec réservées aux utilisateurs connectés (même origine) |
| **E-mail (mot de passe oublié)** | `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `PASSWORD_RESET_FRONTEND_URL`, `PASSWORD_RESET_TOKEN_MINUTES` |

---

## Sécurité et rôles

- **Rôles** (constantes `RoleNames`) : `ACHETEUR`, `VENDEUR`, `ADMIN`, `SUPER_ADMIN`.
- **Sessions** : cookie `JSESSIONID` après `POST /api/auth/login`.
- **Filtre HTTP** : tout ce qui est sous `/api/v1/admin/**` exige `ADMIN` ou `SUPER_ADMIN` (en plus des `@PreAuthorize` sur les contrôleurs).
- **Catalogue public** : seuls certains `GET` sous `/api/v1/articles` sont anonymes (liste, fiche, photos), pas un `/**` générique.
- **OPTIONS /*** : autorisé pour le préflight CORS.
- CORS configuré pour `http://localhost:5173` avec credentials.

---

## Fonctionnalités métier (récapitulatif)

### Authentification & comptes

- Inscription multipart (acheteur / vendeur, pièce d’identité, catégorie vendeur si besoin).
- Connexion / déconnexion / `GET /api/auth/me`.
- **Mot de passe oublié (e-mail réel)**  
  - `POST /api/auth/forgot-password` `{ "email" }`  
  - `POST /api/auth/reset-password` `{ "token", "newPassword" }`  
  - Jeton à usage unique, durée limitée, hash SHA-256 en base ; lien vers le front configurable (`PASSWORD_RESET_FRONTEND_URL`).
- **Réinitialisation par admin** : `PUT /api/v1/users/{id}/admin-password` (corps `newPassword`).

### Articles

- CRUD vendeur / staff, catégorie imposée pour le vendeur selon son inscription.
- Blocage / message d’avertissement (admin).
- **Règle** : un **ADMIN** ne peut pas **bloquer** une annonce dont le vendeur est **ADMIN** ou **SUPER_ADMIN** (un **SUPER_ADMIN** peut modérer).
- **Jusqu’à 6 photos** : champ `photos` (plusieurs fichiers) ou `photo` (une) en multipart ; entités `Article` + `ArticleImage` ; `GET .../photo/{filename}` pour la galerie.
- Vues incrémentées à la consultation ; articles bloqués exclus du catalogue public.

### Messagerie & offres

- Conversations acheteur / vendeur (souvent liées à un article).
- Messages avec **prix proposé** et statut d’offre (`PENDING` / `ACCEPTED` / `REFUSED`).
- Le vendeur (ou le staff) répond via `PATCH .../messages/{messageId}/offer`.
- Paiement avec **`prixUnitaireNegocie`** possible si une offre **acceptée** correspond (même article, acheteur, vendeur, montant).

### Paiements

- Enregistrement avec moyen de paiement, quantité, référence externe.
- **Unicité** de la référence (hash) ; **chiffrement AES-GCM** de la référence stockée.
- Reçu téléchargeable (texte).

### Plaintes & admin

- Dépôt et suivi de plaintes ; côté admin : liste, marquer comme lues.
- **Dashboard** : totaux utilisateurs, articles, plaintes non lues, paiements, **sessions HTTP actives** (compteur depuis le démarrage du serveur).

### Autres endpoints utiles

- **Chatbot (règles / regex, sans API externe)** : `POST /api/v1/chatbot/reply` — intentions type prix, quantité, urgence, sentiment ; suggestions de phrases.
- **Export RGPD** : `GET /api/v1/users/me/data-export` — JSON (profil sans mot de passe, annonces, transactions métadonnées sans référence en clair, plaintes, conversations et messages).

### Bootstrap

- Création des rôles et des super-admins si absents (`EcommerceBootstrap`).

---

## Structure des packages (aperçu)

- `config` — Sécurité, CORS, OpenAPI, écouteur de sessions, bootstrap.
- `controller` — REST par domaine (`Auth`, `Article`, `Admin*`, `Payment`, `Conversation`, etc.).
- `dto` — Objets d’entrée / sortie API.
- `exception` — Handlers JSON (401, 403, 404, validation, etc.).
- `model` — Entités JPA (`User`, `Article`, `ArticleImage`, `Conversation`, `ChatMessage`, `EcomTransaction`, `Complaint`, `PasswordResetToken`, …).
- `repository` — Spring Data JPA.
- `service` — Logique métier, mail, reset MDP, chatbot, export RGPD, paiement, etc.
- `security` — Constantes de rôles.

---

## Tests

```bash
mvn test
```

Utilise H2 + propriétés dédiées ; pas besoin de MySQL pour la suite de tests par défaut.

---

## Documentation OpenAPI

Une fois l’application démarrée, utiliser Swagger UI pour explorer et tester les endpoints.  
Pour les routes protégées : se connecter via `POST /api/auth/login` **sur le même hôte/port** que l’API pour que le cookie soit envoyé, ou utiliser le schéma **sessionCookie** (`JSESSIONID`) décrit dans la config OpenAPI.

---

## Fichiers et dépendances notables ajoutés ou étendus

- Dépendances : `spring-boot-starter-security`, `spring-boot-starter-mail`, `springdoc-openapi-starter-webmvc-ui`, `h2` (scope test).
- Entités / repos : offres sur messages, transactions, plaintes, `ArticleImage`, `PasswordResetToken`, etc.
- Services : `PasswordResetService`, `MailNotificationService`, `ChatbotService`, `GdprExportService`, extensions `ArticleService`, `PaymentService`, `MessagingService`, etc.
- `ActiveSessionCounter` + listener HTTP session pour les stats « connectés ».
- `GlobalExceptionHandler` (erreurs API homogènes).

---

## Licence / usage

Projet académique / interne — adapter mots de passe, secrets SMTP et AES, et désactiver ou protéger Swagger en production (`app.security.swagger-open=false`, `springdoc.*.enabled=false` si besoin).
