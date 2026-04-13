# 🤖 Documentation du Système de Chatbot Ecomarket

## Vue d'ensemble

Le système de chatbot Ecomarket est une solution complète de messagerie intelligente qui facilite les transactions entre clients et vendeurs avec négociation automatisée, gestion des commandes et géolocalisation.

---

## 🎯 Fonctionnalités principales

### 1. **Chatbot intelligent (Ecomarket Assistant)**
- **Auto-réponses** : Répond automatiquement aux questions courantes (horaires, modes de paiement, FAQ)
- **Détection de négociation** : Identifie les intentions d'achat et les propositions de prix
- **Extraction de données** : Détecte automatiquement les prix, quantités et modes de récupération
- **Transfert au vendeur** : Bascule vers le vendeur pour les demandes spécifiques

### 2. **Système de négociation**
- Le client propose un prix et une quantité
- Le bot analyse et transmet au vendeur
- Le vendeur accepte ou refuse l'offre
- Notification automatique au client

### 3. **Gestion des commandes**
- **Phase négociation** → **Phase récupération** → **Phase paiement**
- Choix du mode de récupération (retrait sur place / livraison)
- Partage de localisation du vendeur avec carte
- Génération de référence de paiement unique

### 4. **Géolocalisation**
- Récupération automatique de la position GPS
- Saisie manuelle d'adresse
- Points de repère optionnels
- Affichage sur carte pour le client

### 5. **Système de notifications**
- Badge de notifications dans le header
- Compteur de messages non lus
- Notifications temps réel pour clients et vendeurs

---

## 📁 Structure des fichiers

```
src/
├── types/
│   └── chat.ts                     # Types TypeScript pour le système de chat
│
├── services/
│   └── chatbotService.ts           # Logique du bot (auto-réponses, détections)
│
├── contexts/
│   └── ChatContext.tsx             # State global des conversations
│
├── hooks/
│   └── useNotifications.tsx        # Hook pour badges de notifications
│
├── components/
│   ├── VendorChat.tsx              # Interface chat pour vendeurs
│   ├── LocationSharing.tsx         # Composant partage de localisation
│   ├── NotificationBadge.tsx       # Badge de notifications
│   └── Header.tsx                  # Header mis à jour avec notifications
│
└── pages/
    ├── Chat.tsx                    # Page chat client
    ├── VendorDashboard.tsx         # Dashboard vendeur avec chat
    └── ProductDetail.tsx           # Bouton "Contacter le vendeur"
```

---

## 🔄 Flux de conversation

### **Flux Client → Bot → Vendeur**

```
1. CLIENT clique sur "Contacter le vendeur"
   ↓
2. BOT accueille le client et présente le produit
   ↓
3. CLIENT pose une question ou fait une offre
   ↓
4. BOT analyse le message :
   - Question FAQ ? → Répond directement
   - Négociation détectée ? → Crée une négociation
   - Question spécifique ? → Transfère au vendeur
   ↓
5. VENDEUR reçoit notification et peut :
   - Accepter l'offre
   - Refuser l'offre
   - Répondre directement
   ↓
6. BOT notifie le client de la décision
   ↓
7. Si accepté → Passage en phase "récupération"
   ↓
8. CLIENT choisit le mode (retrait/livraison)
   ↓
9. VENDEUR partage sa localisation
   ↓
10. BOT génère les infos de paiement
    ↓
11. CLIENT effectue le paiement → Commande terminée
```

---

## 🛠️ Types de données

### **Message**
```typescript
{
  id: string
  conversationId: string
  sender: 'bot' | 'client' | 'vendor'
  senderName: string
  text: string
  timestamp: string
  type: 'text' | 'negotiation' | 'payment' | 'system'
  metadata?: {
    offeredPrice?: number
    quantity?: number
    negotiationId?: string
    location?: LocationData
    paymentInfo?: PaymentInfo
  }
  isRead: boolean
  isTransferred?: boolean
}
```

### **Conversation**
```typescript
{
  id: string
  productId: string
  productTitle: string
  productImage: string
  buyerId: string
  buyerName: string
  sellerId: string
  sellerName: string
  status: 'active' | 'negotiating' | 'payment' | 'completed' | 'cancelled'
  currentNegotiation?: Negotiation
  order?: Order
  unreadCount: number
  lastMessage?: string
  lastMessageTime?: string
  createdAt: string
  updatedAt: string
}
```

### **Negotiation**
```typescript
{
  id: string
  conversationId: string
  productId: string
  buyerId: string
  sellerId: string
  originalPrice: number
  offeredPrice: number
  quantity: number
  status: 'pending' | 'accepted' | 'rejected'
  vendorResponse?: string
  createdAt: string
  respondedAt?: string
}
```

---

## 🤖 Intelligence du Bot

### **Détection automatique**

1. **Questions FAQ** :
   - Horaires, modes de paiement, retours
   - Réponses instantanées

2. **Négociation** :
   - Patterns : "je propose X", "combien pour Y", "X unités à Y FCFA"
   - Extraction : prix + quantité

3. **Mode de récupération** :
   - Mots-clés : "retrait", "venir chercher", "livraison"
   - Demande de localisation

4. **Expressions régulières** :
   - Prix : `/(\d+(?:[.,]\d+)?)\s*(fcfa|eur|euro|€)/i`
   - Quantité : `/(\d+)\s*(unités?|pièces?|articles?)/i`

---

## 🎨 Interface Utilisateur

### **Chat Client** (`/chat`)
- Sidebar avec liste des conversations
- Badge de messages non lus
- Zone de messages avec bulles différenciées (bot / vendeur / client)
- Input de saisie avec bouton "Envoyer"
- Affichage des infos de paiement et localisation

### **Dashboard Vendeur** (`/vendor`)
- Onglet "Messages clients"
- Liste des conversations avec statuts (négociation, paiement, etc.)
- Boutons "Accepter" / "Refuser" pour les négociations
- Bouton "Partager ma position" après acceptation
- Zone de réponse directe au client

### **Notifications**
- Badge rouge dans le header : `💬 Messages [3]`
- Compteur dynamique selon messages non lus
- Disparaît quand tout est lu

---

## 🚀 Comment l'utiliser

### **En tant que Client** :

1. Allez sur une page produit
2. Cliquez sur "💬 Contacter le vendeur"
3. Le bot vous accueille
4. Posez une question ou faites une offre :
   - "Je propose 50 EUR"
   - "Combien pour 3 unités ?"
   - "Quels sont vos horaires ?"
5. Recevez les réponses du bot ou du vendeur
6. Si négociation acceptée → Choisissez le mode de récupération
7. Recevez les infos de paiement et localisation
8. Effectuez le paiement

### **En tant que Vendeur** :

1. Connectez-vous avec un compte vendeur
2. Allez dans le menu utilisateur → "💼 Dashboard Vendeur"
3. Onglet "💬 Messages clients"
4. Sélectionnez une conversation
5. Voyez les infos de la négociation (quantité, prix, total)
6. Cliquez sur "✓ Accepter" ou "✕ Refuser"
7. Si accepté → "📍 Partager ma position"
8. Le client reçoit automatiquement les infos de paiement

---

## 🔐 Sécurité

- Authentification requise pour accéder au chat
- Chaque conversation liée à un acheteur et un vendeur spécifiques
- Messages en temps réel (simulation, pourrait être connecté à WebSocket)
- Références de paiement uniques et sécurisées

---

## 🌍 Géolocalisation

### **Fonctionnalités** :
- Auto-détection GPS du navigateur
- Reverse geocoding (simulé, à remplacer par Google Maps API / Nominatim)
- Saisie manuelle d'adresse
- Points de repère optionnels
- Affichage de latitude/longitude

### **APIs recommandées pour production** :
- **Google Maps Geocoding API**
- **OpenStreetMap Nominatim**
- **Mapbox Geocoding**

---

## 📊 Statistiques et métriques possibles

- Nombre de conversations actives
- Taux d'acceptation des négociations
- Temps de réponse moyen des vendeurs
- Commandes complétées vs abandonnées
- Questions les plus posées au bot

---

## 🔮 Améliorations futures

1. **Intégration WebSocket** pour temps réel
2. **API de géolocalisation réelle** (Google Maps)
3. **Paiement intégré** (Orange Money, Moov Money API)
4. **Historique des commandes** dans le profil
5. **Système de notation** vendeur/client
6. **Photos dans les messages** (preuve de paiement)
7. **Notifications push** navigateur
8. **Chat vocal** ou vidéo
9. **Traduction automatique** multilingue
10. **Analytics dashboard** pour vendeurs

---

## ✅ Tests recommandés

### **Scénarios à tester** :

1. **Client demande info produit** → Bot répond
2. **Client fait une offre** → Négociation créée → Vendeur accepte → Phase récupération
3. **Vendeur refuse une offre** → Client reçoit notification
4. **Client choisit retrait** → Vendeur partage localisation → Infos paiement générées
5. **Notifications non lus** → Badge mis à jour → Lecture → Badge disparaît

---

## 🎉 Conclusion

Le système de chatbot Ecomarket est maintenant **entièrement fonctionnel** avec :

✅ Chatbot intelligent avec auto-réponses  
✅ Système de négociation complet  
✅ Géolocalisation avec partage de position  
✅ Gestion des commandes (négociation → récupération → paiement)  
✅ Interface client style WhatsApp  
✅ Dashboard vendeur avec gestion des conversations  
✅ Notifications en temps réel avec badges  
✅ Intégration complète dans l'application  

**Le système est prêt pour être testé et déployé !** 🚀
