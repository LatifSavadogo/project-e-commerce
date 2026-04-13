import type { Message } from '../types/chat'

/** Proposition de prix acheteur encore en attente de réponse vendeur (identifiée par id auteur API). */
export function isPendingBuyerPriceMessage(msg: Message, buyerId: string): boolean {
  if (msg.type !== 'negotiation' || msg.offerStatus !== 'PENDING') return false
  if (!msg.metadata?.offeredPrice || !msg.metadata.negotiationId) return false
  if (msg.metadata.offreFinaleVendeur === true) return false
  const aid = msg.authorUserId != null ? String(msg.authorUserId) : ''
  if (buyerId) return aid === String(buyerId)
  return msg.sender === 'client'
}

/** Dernière offre acheteur en attente (la plus récente dans le fil). */
export function findPendingBuyerOffer(messages: Message[], buyerId: string): Message | null {
  for (let i = messages.length - 1; i >= 0; i--) {
    const m = messages[i]
    if (isPendingBuyerPriceMessage(m, buyerId)) return m
  }
  return null
}

/** Prix final vendeur en attente de validation acheteur (tour 3). */
export function findPendingSellerFinalOffer(messages: Message[]): Message | null {
  for (let i = messages.length - 1; i >= 0; i--) {
    const m = messages[i]
    if (
      m.type === 'negotiation' &&
      m.metadata?.offreFinaleVendeur === true &&
      m.offerStatus === 'PENDING_BUYER_FINAL'
    ) {
      return m
    }
  }
  return null
}

/** 2e contre-proposition acheteur en attente (tour 2 : pas de refus sec vendeur). */
export function isSecondBuyerOfferPending(messages: Message[]): boolean {
  const buyerOffers = messages.filter(
    (m) =>
      m.type === 'negotiation' &&
      m.sender === 'client' &&
      m.metadata?.offeredPrice != null &&
      m.metadata.offreFinaleVendeur !== true
  )
  return buyerOffers.length === 2 && buyerOffers[1]?.offerStatus === 'PENDING'
}

/**
 * Le vendeur peut saisir un prix final : après deux refus, ou bien pendant l’attente de la 2e proposition.
 */
export function vendorMaySubmitFinalPrice(messages: Message[]): boolean {
  if (messages.some((m) => m.metadata?.offreFinaleVendeur)) return false
  if (isSecondBuyerOfferPending(messages)) return true
  const buyerOffers = messages.filter(
    (m) =>
      m.type === 'negotiation' &&
      m.sender === 'client' &&
      m.metadata?.offeredPrice != null &&
      m.metadata.offreFinaleVendeur !== true
  )
  if (buyerOffers.length !== 2) return false
  if (!buyerOffers.every((m) => m.offerStatus === 'REFUSED')) return false
  const anyPending = messages.some(
    (m) => m.type === 'negotiation' && m.offerStatus === 'PENDING' && m.sender === 'client'
  )
  return !anyPending
}
