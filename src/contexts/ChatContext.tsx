/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react'
import type { Conversation, Message, ChatState, LocationData, Negotiation } from '../types/chat'
import type { Product } from '../contexts/ProductContext'
import type { ChatMessageDtoJson, ConversationDtoJson } from '../types/backend'
import {
  buyerRespondToFinalOffer,
  fetchMessages,
  fetchMyConversations,
  openConversation,
  postMessage,
  postSellerFinalOffer,
  respondToOffer,
} from '../services/conversationApi'
import { articleMainPhotoUrl } from '../utils/articleUrls'
import { dateFromDto } from '../utils/dateFromDto'
import { countUnreadForViewer, getLastReadMessageId, setLastReadMessageId } from '../utils/chatUnread'
import { findPendingBuyerOffer } from '../utils/negotiationMessages'
import { useAuth } from './AuthContext'

type ChatContextType = {
  conversations: Conversation[]
  messages: Record<string, Message[]>
  activeConversationId: string | null
  unreadTotal: number
  chatLoading: boolean
  startConversation: (product: Product, buyerId: string, buyerName: string) => Promise<string>
  sendMessage: (conversationId: string, text: string, senderId: string, senderName: string) => Promise<void>
  /** Offre de prix (acheteur uniquement, conversation liée à un article). */
  sendBuyerPriceOffer: (
    conversationId: string,
    prixPropose: number,
    quantite: number,
    messageOptionnel?: string
  ) => Promise<void>
  sendVendorMessage: (conversationId: string, text: string) => Promise<void>
  acceptNegotiation: (conversationId: string, negotiationId: string) => Promise<void>
  rejectNegotiation: (conversationId: string, negotiationId: string, reason?: string) => Promise<void>
  /** Après 2 refus acheteur : dernier prix vendeur (FCFA). */
  sendSellerFinalPrice: (conversationId: string, prix: number) => Promise<void>
  /** Acheteur : valider ou refuser le prix final vendeur. */
  respondToSellerFinal: (conversationId: string, messageId: string, accept: boolean) => Promise<void>
  shareLocation: (conversationId: string, location: LocationData) => void
  setActiveConversation: (conversationId: string | null) => void
  markAsRead: (conversationId: string) => void
  getConversation: (conversationId: string) => Conversation | undefined
  getMessages: (conversationId: string) => Message[]
  getVendorConversations: (vendorId: string) => Conversation[]
  getBuyerConversations: (buyerId: string) => Conversation[]
  refreshConversations: () => Promise<void>
}

const ChatContext = createContext<ChatContextType | undefined>(undefined)

function mapConversationDto(d: ConversationDtoJson): Conversation {
  const id = String(d.idconversation)
  const productId = d.idArticle != null ? String(d.idArticle) : ''
  const productImage = d.idArticle != null ? articleMainPhotoUrl(d.idArticle) : ''
  const now = dateFromDto(d.datecreation)
  return {
    id,
    productId,
    productTitle: d.articleLibelle || 'Article',
    productImage,
    buyerId: d.idAcheteur != null ? String(d.idAcheteur) : '',
    buyerName: d.acheteurNom || 'Acheteur',
    sellerId: d.idVendeur != null ? String(d.idVendeur) : '',
    sellerName: d.vendeurNom || 'Vendeur',
    status: 'active',
    unreadCount: 0,
    createdAt: now,
    updatedAt: dateFromDto(d.dateupdate ?? d.datecreation),
  }
}

function mapMessageDto(
  m: ChatMessageDtoJson,
  convId: string,
  buyerUserId: string,
  vendorUserId: string
): Message {
  const authorId = m.idAuteur != null ? String(m.idAuteur) : ''
  let sender: Message['sender'] = 'client'
  if (authorId && authorId === vendorUserId) sender = 'vendor'
  else if (authorId && authorId === buyerUserId) sender = 'client'

  const isOffer = m.prixPropose != null && m.prixPropose > 0
  const type: Message['type'] = isOffer ? 'negotiation' : 'text'

  let offerStatus: Message['offerStatus']
  if (isOffer) {
    const raw = (m.statutOffre || 'PENDING').toUpperCase()
    if (raw === 'ACCEPTED') offerStatus = 'ACCEPTED'
    else if (raw === 'REFUSED') offerStatus = 'REFUSED'
    else if (raw === 'PENDING_BUYER_FINAL') offerStatus = 'PENDING_BUYER_FINAL'
    else if (raw === 'VALIDATED') offerStatus = 'VALIDATED'
    else if (raw === 'EXPIRED') offerStatus = 'EXPIRED'
    else offerStatus = 'PENDING'
  }

  return {
    id: String(m.idmessage),
    conversationId: convId,
    authorUserId: authorId || undefined,
    sender,
    senderName: m.auteurNom || '',
    text: m.contenu,
    timestamp: dateFromDto(m.dateenvoi),
    type,
    offerStatus,
    metadata: isOffer
      ? {
          offeredPrice: m.prixPropose!,
          quantity: m.quantiteProposee != null && m.quantiteProposee > 0 ? m.quantiteProposee : 1,
          negotiationId: String(m.idmessage),
          offreFinaleVendeur: m.offreFinaleVendeur === true,
        }
      : undefined,
    isRead: true,
  }
}

function pendingNegotiationFromMessages(
  messages: Message[],
  productId: string,
  buyerId: string,
  _sellerId: string
): Negotiation | undefined {
  const msg = findPendingBuyerOffer(messages, buyerId)
  const price = msg?.metadata?.offeredPrice
  if (!msg?.metadata?.negotiationId || price == null) return undefined
  return {
    id: msg.metadata.negotiationId,
    conversationId: msg.conversationId,
    productId,
    buyerId: '',
    sellerId: '',
    originalPrice: price,
    offeredPrice: price,
    quantity: msg.metadata.quantity ?? 1,
    status: 'pending',
    createdAt: msg.timestamp,
  }
}

function hasPayableAcceptedPrice(messages: Message[]): boolean {
  return messages.some((m) => {
    if (m.type !== 'negotiation' || m.metadata?.offeredPrice == null || m.metadata.offeredPrice <= 0) {
      return false
    }
    if (m.metadata.offreFinaleVendeur) {
      return m.offerStatus === 'VALIDATED' || m.offerStatus === 'ACCEPTED'
    }
    return m.offerStatus === 'ACCEPTED'
  })
}

function enrichConversation(
  conv: Conversation,
  messages: Message[],
  viewerUserId: string | null
): Conversation {
  const pending = pendingNegotiationFromMessages(messages, conv.productId, conv.buyerId, conv.sellerId)
  const status: Conversation['status'] = pending
    ? 'negotiating'
    : hasPayableAcceptedPrice(messages)
      ? 'payment'
      : 'active'
  const lastRead =
    viewerUserId != null ? getLastReadMessageId(viewerUserId, conv.id) : null
  const unreadCount =
    viewerUserId != null
      ? countUnreadForViewer(messages, viewerUserId, conv.buyerId, conv.sellerId, lastRead)
      : 0
  return {
    ...conv,
    status,
    currentNegotiation: pending,
    unreadCount,
    lastMessage: messages.length ? messages[messages.length - 1].text : conv.lastMessage,
    lastMessageTime: messages.length ? messages[messages.length - 1].timestamp : conv.lastMessageTime,
  }
}

export function ChatProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const [state, setState] = useState<ChatState>({
    conversations: [],
    messages: {},
    activeConversationId: null,
    unreadTotal: 0,
  })
  const [chatLoading, setChatLoading] = useState(false)

  const loadAllConversations = useCallback(
    async (showLoading: boolean) => {
      if (!user) {
        setState({
          conversations: [],
          messages: {},
          activeConversationId: null,
          unreadTotal: 0,
        })
        return
      }
      if (showLoading) setChatLoading(true)
      try {
        const list = await fetchMyConversations()
        const base = list.map(mapConversationDto)
        const messages: Record<string, Message[]> = {}
        const enriched: Conversation[] = []
        const uid = user.id
        for (const c of base) {
          try {
            const dtos = await fetchMessages(Number(c.id))
            const buyerId = c.buyerId
            const vendorId = c.sellerId
            const msgs = dtos.map((d) => mapMessageDto(d, c.id, buyerId, vendorId))
            messages[c.id] = msgs
            enriched.push(enrichConversation(c, msgs, uid))
          } catch {
            messages[c.id] = []
            enriched.push(enrichConversation(c, [], uid))
          }
        }
        setState((prev) => ({
          ...prev,
          conversations: enriched,
          messages: { ...prev.messages, ...messages },
          unreadTotal: enriched.reduce((sum, x) => sum + x.unreadCount, 0),
        }))
      } catch (e) {
        console.error(e)
      } finally {
        if (showLoading) setChatLoading(false)
      }
    },
    [user]
  )

  const refreshConversations = useCallback(() => loadAllConversations(true), [loadAllConversations])

  const refreshConversationsSilent = useCallback(
    () => loadAllConversations(false),
    [loadAllConversations]
  )

  useEffect(() => {
    void loadAllConversations(true)
  }, [loadAllConversations])

  /** Mise à jour des messages en arrière-plan (sans recharger la page). */
  useEffect(() => {
    if (!user) return
    const tick = () => {
      if (document.visibilityState === 'hidden') return
      void loadAllConversations(false)
    }
    const id = window.setInterval(tick, 5000)
    return () => window.clearInterval(id)
  }, [user, loadAllConversations])

  useEffect(() => {
    if (!user) return
    const onVis = () => {
      if (document.visibilityState === 'visible') void loadAllConversations(false)
    }
    document.addEventListener('visibilitychange', onVis)
    return () => document.removeEventListener('visibilitychange', onVis)
  }, [user, loadAllConversations])

  const startConversation = useCallback(
    async (product: Product, buyerId: string, buyerName: string): Promise<string> => {
      if (!product.sellerId) throw new Error('Vendeur inconnu')
      const dto = await openConversation(Number(product.sellerId), Number(product.id))
      const conv = mapConversationDto(dto)
      const full: Conversation = {
        ...conv,
        buyerId,
        buyerName,
        productId: product.id,
        productTitle: product.title,
        productImage: product.image || articleMainPhotoUrl(Number(product.id)),
      }
      setState((prev) => {
        const exists = prev.conversations.some((c) => c.id === full.id)
        if (exists) return { ...prev, activeConversationId: full.id }
        return {
          ...prev,
          conversations: [full, ...prev.conversations],
          activeConversationId: full.id,
          messages: { ...prev.messages, [full.id]: prev.messages[full.id] || [] },
        }
      })
      await refreshConversations()
      return full.id
    },
    [refreshConversations]
  )

  const sendMessage = useCallback(
    async (conversationId: string, text: string, _senderId: string, _senderName: string) => {
      const viewerId = user?.id ?? null
      const dto = await postMessage(Number(conversationId), text)
      setState((prev) => {
        const conv = prev.conversations.find((c) => c.id === conversationId)
        const buyerId = conv?.buyerId || ''
        const vendorId = conv?.sellerId || ''
        const msg = mapMessageDto(dto, conversationId, buyerId, vendorId)
        const list = [...(prev.messages[conversationId] || []), msg]
        const newConv = prev.conversations.map((c) =>
          c.id === conversationId ? enrichConversation(c, list, viewerId) : c
        )
        return {
          ...prev,
          messages: { ...prev.messages, [conversationId]: list },
          conversations: newConv,
        }
      })
      await refreshConversationsSilent()
    },
    [refreshConversationsSilent, user]
  )

  const sendBuyerPriceOffer = useCallback(
    async (conversationId: string, prixPropose: number, quantite: number, messageOptionnel?: string) => {
      const viewerId = user?.id ?? null
      const dto = await postMessage(
        Number(conversationId),
        messageOptionnel?.trim() || '',
        Math.round(prixPropose),
        Math.round(quantite)
      )
      setState((prev) => {
        const conv = prev.conversations.find((c) => c.id === conversationId)
        const buyerId = conv?.buyerId || ''
        const vendorId = conv?.sellerId || ''
        const msg = mapMessageDto(dto, conversationId, buyerId, vendorId)
        const list = [...(prev.messages[conversationId] || []), msg]
        const newConv = prev.conversations.map((c) =>
          c.id === conversationId ? enrichConversation(c, list, viewerId) : c
        )
        return {
          ...prev,
          messages: { ...prev.messages, [conversationId]: list },
          conversations: newConv,
        }
      })
      await refreshConversationsSilent()
    },
    [refreshConversationsSilent, user]
  )

  const sendVendorMessage = useCallback(
    async (conversationId: string, text: string) => {
      await sendMessage(conversationId, text, '', '')
    },
    [sendMessage]
  )

  const acceptNegotiation = useCallback(
    async (conversationId: string, negotiationId: string) => {
      await respondToOffer(Number(conversationId), Number(negotiationId), 'ACCEPTED')
      await refreshConversationsSilent()
    },
    [refreshConversationsSilent]
  )

  const rejectNegotiation = useCallback(
    async (conversationId: string, negotiationId: string, _reason?: string) => {
      await respondToOffer(Number(conversationId), Number(negotiationId), 'REFUSED')
      await refreshConversationsSilent()
    },
    [refreshConversationsSilent]
  )

  const sendSellerFinalPrice = useCallback(
    async (conversationId: string, prix: number) => {
      await postSellerFinalOffer(Number(conversationId), prix)
      await refreshConversationsSilent()
    },
    [refreshConversationsSilent]
  )

  const respondToSellerFinal = useCallback(
    async (conversationId: string, messageId: string, accept: boolean) => {
      await buyerRespondToFinalOffer(Number(conversationId), Number(messageId), accept)
      await refreshConversationsSilent()
    },
    [refreshConversationsSilent]
  )

  const shareLocation = (_conversationId: string, _location: LocationData) => {}

  const setActiveConversation = (conversationId: string | null) => {
    setState((prev) => ({ ...prev, activeConversationId: conversationId }))
  }

  const markAsRead = useCallback(
    (conversationId: string) => {
      if (!user) return
      setState((prev) => {
        const msgs = prev.messages[conversationId] || []
        let maxId = 0
        for (const m of msgs) {
          const n = parseInt(m.id, 10)
          if (!Number.isNaN(n)) maxId = Math.max(maxId, n)
        }
        if (maxId > 0) setLastReadMessageId(user.id, conversationId, String(maxId))
        const conversations = prev.conversations.map((c) => {
          if (c.id !== conversationId) return c
          const unread = countUnreadForViewer(
            msgs,
            user.id,
            c.buyerId,
            c.sellerId,
            String(maxId)
          )
          return { ...c, unreadCount: unread }
        })
        return {
          ...prev,
          conversations,
          messages: {
            ...prev.messages,
            [conversationId]: msgs.map((m) => ({ ...m, isRead: true })),
          },
        }
      })
    },
    [user]
  )

  const getConversation = (conversationId: string) => state.conversations.find((c) => c.id === conversationId)

  const getMessages = (conversationId: string) => state.messages[conversationId] || []

  const getVendorConversations = (vendorId: string) =>
    state.conversations.filter((c) => String(c.sellerId) === String(vendorId))

  const getBuyerConversations = (buyerId: string) =>
    state.conversations.filter((c) => String(c.buyerId) === String(buyerId))

  return (
    <ChatContext.Provider
      value={{
        conversations: state.conversations,
        messages: state.messages,
        activeConversationId: state.activeConversationId,
        unreadTotal: state.conversations.reduce((sum, c) => sum + c.unreadCount, 0),
        chatLoading,
        startConversation,
        sendMessage,
        sendBuyerPriceOffer,
        sendVendorMessage,
        acceptNegotiation,
        rejectNegotiation,
        sendSellerFinalPrice,
        respondToSellerFinal,
        shareLocation,
        setActiveConversation,
        markAsRead,
        getConversation,
        getMessages,
        getVendorConversations,
        getBuyerConversations,
        refreshConversations,
      }}
    >
      {children}
    </ChatContext.Provider>
  )
}

export function useChat() {
  const context = useContext(ChatContext)
  if (!context) {
    throw new Error('useChat must be used within ChatProvider')
  }
  return context
}
