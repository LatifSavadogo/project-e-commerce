import type { Message } from '../types/chat'

const PREFIX = 'ecomarket_chat_lastRead_'

function key(userId: string, conversationId: string) {
  return `${PREFIX}${userId}_${conversationId}`
}

export function getLastReadMessageId(userId: string, conversationId: string): string | null {
  try {
    return localStorage.getItem(key(userId, conversationId))
  } catch {
    return null
  }
}

export function setLastReadMessageId(userId: string, conversationId: string, messageId: string) {
  try {
    localStorage.setItem(key(userId, conversationId), messageId)
  } catch {
    /* ignore */
  }
}

/** Messages de l’interlocuteur après la dernière lecture (ids messages croissants côté API). */
export function countUnreadForViewer(
  messages: Message[],
  viewerUserId: string,
  buyerId: string,
  sellerId: string,
  lastReadMessageId: string | null
): number {
  const isSellerView = String(viewerUserId) === String(sellerId)
  const isBuyerView = String(viewerUserId) === String(buyerId)
  if (!isSellerView && !isBuyerView) return 0

  const otherSender: Message['sender'] = isSellerView ? 'client' : 'vendor'
  let threshold = 0
  if (lastReadMessageId != null) {
    const n = parseInt(lastReadMessageId, 10)
    if (!Number.isNaN(n)) threshold = n
  }

  return messages.filter((m) => {
    if (m.sender !== otherSender) return false
    const id = parseInt(m.id, 10)
    if (Number.isNaN(id)) return false
    return id > threshold
  }).length
}

/** Affichage : 1, 2 ou 3 (plafonné). */
export function unreadBadgeDigit(count: number): number | null {
  if (count <= 0) return null
  return Math.min(count, 3)
}

/** Progression négociation vue vendeur (indicateur d’étape). */
export function vendorNegotiationStep(messages: Message[]): 1 | 2 | 3 {
  const buyerOffers = messages.filter(
    (m) => m.type === 'negotiation' && m.sender === 'client' && m.metadata?.offeredPrice != null
  )
  const hasFinal = messages.some((m) => m.metadata?.offreFinaleVendeur)
  const hasAcceptedPay = messages.some(
    (m) =>
      m.type === 'negotiation' &&
      m.offerStatus === 'ACCEPTED' &&
      m.metadata?.offeredPrice != null
  )
  const finalAwaitingBuyer = messages.some(
    (m) => m.metadata?.offreFinaleVendeur && m.offerStatus === 'PENDING_BUYER_FINAL'
  )
  if (hasFinal || hasAcceptedPay || finalAwaitingBuyer) return 3
  if (buyerOffers.length >= 2) return 2
  return 1
}
