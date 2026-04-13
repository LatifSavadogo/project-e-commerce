export type MessageSender = 'client' | 'vendor' | 'bot'

export type MessageType = 
  | 'text'           // Message texte normal
  | 'negotiation'    // Proposition de prix/quantité
  | 'system'         // Message système du bot
  | 'location'       // Partage de localisation
  | 'payment'        // Informations de paiement
  | 'payment_flow'   // Flux de paiement interactif

export type ConversationStatus = 
  | 'active'         // Conversation en cours
  | 'negotiating'    // En phase de négociation
  | 'payment'        // En attente de paiement
  | 'completed'      // Transaction terminée
  | 'cancelled'      // Annulée

export type NegotiationStatus = 
  | 'pending'        // En attente de réponse vendeur
  | 'accepted'       // Acceptée par vendeur
  | 'rejected'       // Refusée par vendeur
  | 'counter'        // Contre-proposition vendeur

export type PickupMode = 
  | 'pickup'         // Retrait sur place
  | 'delivery'       // Livraison (à implémenter)
  | null

export interface Message {
  id: string
  conversationId: string
  /** id utilisateur auteur (API), fiable pour distinguer acheteur / vendeur. */
  authorUserId?: string
  sender: MessageSender
  senderName: string
  text: string
  timestamp: string
  type: MessageType
  /** Présent si le backend a créé une offre (statutOffre). */
  offerStatus?:
    | 'PENDING'
    | 'ACCEPTED'
    | 'REFUSED'
    | 'PENDING_BUYER_FINAL'
    | 'VALIDATED'
    | 'EXPIRED'
  metadata?: {
    offeredPrice?: number
    quantity?: number
    negotiationId?: string
    /** Message backend : dernier prix vendeur (ACCEPTED, utilisable au paiement). */
    offreFinaleVendeur?: boolean
    location?: LocationData
    paymentInfo?: PaymentInfo
  }
  isRead: boolean
  isTransferred?: boolean  // Si le message a été transféré au vendeur
}

export interface LocationData {
  latitude: number
  longitude: number
  address: string
  description?: string
}

export interface PaymentInfo {
  reference: string
  amount: number
  phoneNumbers: string[]
  methods: string[]  // ['Orange Money', 'Moov Money', etc.]
}

export interface Negotiation {
  id: string
  conversationId: string
  productId: string
  buyerId: string
  sellerId: string
  originalPrice: number
  offeredPrice: number
  quantity: number
  status: NegotiationStatus
  vendorResponse?: string
  counterPrice?: number
  createdAt: string
  respondedAt?: string
}

export interface Order {
  id: string
  conversationId: string
  productId: string
  productTitle: string
  buyerId: string
  buyerName: string
  sellerId: string
  sellerName: string
  quantity: number
  unitPrice: number
  totalPrice: number
  pickupMode: PickupMode
  pickupLocation?: LocationData
  paymentReference: string
  paymentPhones: string[]
  status: 'pending_payment' | 'paid' | 'ready_pickup' | 'completed' | 'cancelled'
  createdAt: string
  paidAt?: string
  completedAt?: string
}

export interface Conversation {
  id: string
  productId: string
  productTitle: string
  productImage: string
  buyerId: string
  buyerName: string
  sellerId: string
  sellerName: string
  status: ConversationStatus
  lastMessage?: string
  lastMessageTime?: string
  unreadCount: number
  currentNegotiation?: Negotiation
  order?: Order
  createdAt: string
  updatedAt: string
}

export interface ChatState {
  conversations: Conversation[]
  messages: Record<string, Message[]>  // conversationId -> messages
  activeConversationId: string | null
  unreadTotal: number
}
