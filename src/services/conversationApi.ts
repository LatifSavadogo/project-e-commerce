import { apiJson } from './apiClient'
import type { ChatMessageDtoJson, ConversationDtoJson } from '../types/backend'

export async function openConversation(idVendeur: number, idArticle?: number): Promise<ConversationDtoJson> {
  const body =
    idArticle != null ? { idVendeur, idArticle } : { idVendeur }
  return apiJson<ConversationDtoJson>('/api/v1/conversations', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function fetchMyConversations(): Promise<ConversationDtoJson[]> {
  return apiJson<ConversationDtoJson[]>('/api/v1/conversations/mine')
}

export async function fetchMessages(conversationId: number): Promise<ChatMessageDtoJson[]> {
  return apiJson<ChatMessageDtoJson[]>(`/api/v1/conversations/${conversationId}/messages`)
}

export async function postMessage(
  conversationId: number,
  contenu: string,
  prixPropose?: number,
  /** Obligatoire côté serveur si prixPropose est défini (1–100). */
  quantite?: number
): Promise<ChatMessageDtoJson> {
  return apiJson<ChatMessageDtoJson>(`/api/v1/conversations/${conversationId}/messages`, {
    method: 'POST',
    body: JSON.stringify({
      contenu,
      prixPropose: prixPropose ?? null,
      quantite: quantite ?? null,
    }),
  })
}

export type OfferStatut = 'ACCEPTED' | 'REFUSED'

export async function respondToOffer(
  conversationId: number,
  messageId: number,
  statut: OfferStatut
): Promise<ChatMessageDtoJson> {
  return apiJson<ChatMessageDtoJson>(
    `/api/v1/conversations/${conversationId}/messages/${messageId}/offer`,
    {
      method: 'PATCH',
      body: JSON.stringify({ statut }),
    }
  )
}

/** Après 2 refus des propositions acheteur : dernier prix acceptable (FCFA). */
export async function postSellerFinalOffer(
  conversationId: number,
  prix: number
): Promise<ChatMessageDtoJson> {
  return apiJson<ChatMessageDtoJson>(`/api/v1/conversations/${conversationId}/offre-finale-vendeur`, {
    method: 'POST',
    body: JSON.stringify({ prix: Math.round(prix) }),
  })
}

/** Acheteur : valider le prix final (paiement) ou refuser définitivement. */
export async function buyerRespondToFinalOffer(
  conversationId: number,
  messageId: number,
  accept: boolean
): Promise<ChatMessageDtoJson> {
  return apiJson<ChatMessageDtoJson>(
    `/api/v1/conversations/${conversationId}/messages/${messageId}/final-offer`,
    {
      method: 'PATCH',
      body: JSON.stringify({ accept }),
    }
  )
}
