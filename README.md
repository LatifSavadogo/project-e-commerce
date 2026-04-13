# Ecomarket — Front React (Vite)

Application **React 19** + **TypeScript** + **Vite 7** pour le marketplace **Ecomarket**. Elle consomme l’API **Spring Boot** du dépôt backend (sessions HTTP avec cookies, CORS configuré côté serveur pour `http://localhost:5173`).

## Prérequis

- **Node.js** 20+ (recommandé)
- Backend Spring Boot démarré (par défaut `http://localhost:8080`)

## Installation

```bash
npm install
```

## Configuration

1. Copier l’exemple d’environnement :

   ```bash
   copy .env.example .env
   ```

   Sous Linux/macOS : `cp .env.example .env`

2. Ajuster **`VITE_API_BASE_URL`** si le backend n’est pas sur le port 8080 :

   ```env
   VITE_API_BASE_URL=http://localhost:8080
   ```

Les variables **`VITE_*`** sont injectées au build ; redémarrer `npm run dev` après modification.

## Scripts

| Commande        | Description                          |
|----------------|--------------------------------------|
| `npm run dev`  | Serveur de dev Vite (hot reload)     |
| `npm run build`| Compilation TypeScript + bundle prod |
| `npm run preview` | Prévisualiser le build localement |
| `npm run lint` | ESLint                               |

## Démarrage

1. Démarrer le backend Spring Boot.
2. Lancer le front :

   ```bash
   npm run dev
   ```

3. Ouvrir **http://localhost:5173**

## Fonctionnalités principales (alignées API)

- Catalogue, fiche article, filtres par type d’article
- Authentification (session), inscription vendeur / acheteur
- Mot de passe oublié / réinitialisation (`/forgot-password`, `/reset-password`)
- Profil : achats, ventes (vendeur), export RGPD, réclamations
- Messagerie acheteur / vendeur, **offres de prix** dans le chat
- Paiements (création, historique, reçu)
- Assistant FAQ (`/help/assistant`, chatbot API)
- Espace admin : stats dashboard, utilisateurs, articles, plaintes

## Structure utile

```
src/
  components/     # UI réutilisable (Header, Footer, cartes, paiement…)
  contexts/       # Auth, produits, plaintes, chat
  pages/          # Écrans routés
  services/       # Appels HTTP (apiClient + ressources)
  config/         # `api.ts` (base URL)
  types/          # Types TS / DTOs JSON
```

## Build de production

```bash
npm run build
```

Les fichiers statiques sont générés dans **`dist/`**. Les requêtes API utilisent toujours **`VITE_API_BASE_URL`** : en production, définir cette URL vers l’hôte réel du backend et servir le front derrière le même domaine ou configurer CORS côté Spring.

## Dépannage

- **401 / redirection login** : vérifier que les cookies de session sont envoyés (`credentials: 'include'` est déjà utilisé) et que l’origine front correspond à la config CORS du backend.
- **CORS** : le backend autorise typiquement `http://localhost:5173` ; ajouter votre origine si vous changez de port ou de domaine.
- **Variables d’env non prises en compte** : uniquement les clés préfixées `VITE_` sont exposées au client ; redémarrer le serveur Vite après `.env`.

## Licence

Projet privé / usage selon les règles du dépôt parent.
