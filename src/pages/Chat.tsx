import { useState, useEffect, useRef, useMemo } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Coins, CreditCard, MapPin, MessageSquare, X } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { useChat } from '../contexts/ChatContext'
import { useProducts } from '../contexts/ProductContext'
import PaymentPanel from '../components/PaymentPanel'
import { unreadBadgeDigit } from '../utils/chatUnread'
import { findPendingBuyerOffer, findPendingSellerFinalOffer } from '../utils/negotiationMessages'
import type { Message } from '../types/chat'

/** Prix auquel le paiement est autorisé (offre acceptée ou prix final validé). */
function getPayableAcceptedUnitPrice(messages: Message[]): number | undefined {
  for (let i = messages.length - 1; i >= 0; i--) {
    const m = messages[i]
    if (m.type !== 'negotiation' || m.metadata?.offeredPrice == null || m.metadata.offeredPrice <= 0) continue
    if (m.metadata.offreFinaleVendeur) {
      if (m.offerStatus === 'VALIDATED' || m.offerStatus === 'ACCEPTED') return m.metadata.offeredPrice
    } else if (m.offerStatus === 'ACCEPTED') {
      return m.metadata.offeredPrice
    }
  }
  return undefined
}

/** Quantité associée au prix payable (offre acceptée ou prix final validé). */
function getPayableAcceptedQuantity(messages: Message[]): number {
  for (let i = messages.length - 1; i >= 0; i--) {
    const m = messages[i]
    if (m.type !== 'negotiation' || m.metadata?.offeredPrice == null || m.metadata.offeredPrice <= 0) continue
    if (m.metadata.offreFinaleVendeur) {
      if (m.offerStatus === 'VALIDATED' || m.offerStatus === 'ACCEPTED') {
        return m.metadata.quantity != null && m.metadata.quantity > 0 ? m.metadata.quantity : 1
      }
    } else if (m.offerStatus === 'ACCEPTED') {
      return m.metadata.quantity != null && m.metadata.quantity > 0 ? m.metadata.quantity : 1
    }
  }
  return 1
}

export default function Chat() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { user } = useAuth()
  const { products } = useProducts()
  const {
    conversations,
    activeConversationId,
    sendMessage,
    sendBuyerPriceOffer,
    respondToSellerFinal,
    setActiveConversation,
    getMessages,
    markAsRead,
    startConversation,
  } = useChat()

  const [messageText, setMessageText] = useState('')
  const [offerPriceInput, setOfferPriceInput] = useState('')
  const [offerQtyInput, setOfferQtyInput] = useState('1')
  const [offerBusy, setOfferBusy] = useState(false)
  const [payOkId, setPayOkId] = useState<number | null>(null)
  const [finalDecisionBusy, setFinalDecisionBusy] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const productId = searchParams.get('productId')
    if (!productId || !user) return
    const product = products.find((p) => p.id === productId)
    if (!product?.sellerId) return
    let cancelled = false
    ;(async () => {
      try {
        const convId = await startConversation(product, user.id, `${user.prenoms} ${user.nom}`)
        if (!cancelled) setActiveConversation(convId)
      } catch (e) {
        console.error(e)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [searchParams, user, products, startConversation, setActiveConversation])

  const messageScrollKey =
    activeConversationId != null ? getMessages(activeConversationId).length : 0

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [activeConversationId, messageScrollKey])

  // Rediriger si pas connecté
  useEffect(() => {
    if (!user) {
      navigate('/auth')
    }
  }, [user, navigate])

  if (!user) return null

  const activeConv = conversations.find(c => c.id === activeConversationId)
  const messages = activeConversationId ? getMessages(activeConversationId) : []

  const handleSendMessage = (e?: React.FormEvent, customMessage?: string) => {
    if (e) e.preventDefault()
    if (activeConv?.productId) {
      return
    }
    const message = customMessage || messageText
    if (!message.trim() || !activeConversationId) return

    void sendMessage(activeConversationId, message, user.id, `${user.prenoms} ${user.nom}`)
    setMessageText('')
  }

  const handleConversationClick = (convId: string) => {
    setActiveConversation(convId)
    markAsRead(convId)
  }

  const isBuyerInConv = activeConv && user.id === activeConv.buyerId
  const convHasArticle = !!activeConv?.productId
  const buyerPendingOffer =
    !!activeConv && findPendingBuyerOffer(messages, activeConv.buyerId) != null
  const pendingSellerFinal = useMemo(() => findPendingSellerFinalOffer(messages), [messages])
  const acceptedUnitPrice = useMemo(() => getPayableAcceptedUnitPrice(messages), [messages])
  const acceptedQuantity = useMemo(() => getPayableAcceptedQuantity(messages), [messages])
  const productForConv = useMemo(
    () => (activeConv?.productId ? products.find((p) => p.id === activeConv.productId) : undefined),
    [products, activeConv?.productId]
  )
  const canPayNegotiated =
    !!isBuyerInConv &&
    convHasArticle &&
    acceptedUnitPrice != null &&
    productForConv &&
    !productForConv.isBlocked

  const handleSubmitOffer = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!activeConversationId || !isBuyerInConv || !convHasArticle) return
    const v = parseInt(offerPriceInput.replace(/\s/g, ''), 10)
    if (!v || v < 1) return
    const q = parseInt(offerQtyInput.replace(/\s/g, ''), 10)
    if (!q || q < 1 || q > 100) {
      alert('Indiquez une quantité entre 1 et 100.')
      return
    }
    setOfferBusy(true)
    try {
      await sendBuyerPriceOffer(activeConversationId, v, q)
      setOfferPriceInput('')
      setOfferQtyInput('1')
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Offre impossible')
    } finally {
      setOfferBusy(false)
    }
  }

  const formatTime = (timestamp: string) => {
    const date = new Date(timestamp)
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)

    if (minutes < 1) return 'À l\'instant'
    if (minutes < 60) return `${minutes}min`
    if (hours < 24) return `${hours}h`
    if (days < 7) return `${days}j`
    return date.toLocaleDateString()
  }

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      background: 'var(--page-bg)',
      display: 'flex',
      zIndex: 100
    }}>
      {/* Sidebar conversations */}
      <div style={{
        width: '350px',
        borderRight: '1px solid var(--border)',
        display: 'flex',
        flexDirection: 'column',
        background: 'var(--surface)'
      }}>
        {/* Header */}
        <div style={{
          padding: '16px 20px',
          borderBottom: '1px solid var(--border)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <div>
            <h2 style={{ margin: 0, fontSize: '1.2em' }}>Messages</h2>
            <div style={{ fontSize: '0.7em', color: 'var(--muted)', marginTop: 4 }}>
              Actualisation automatique (~5 s)
            </div>
          </div>
          <button
            type="button"
            onClick={() => navigate('/')}
            style={{
              background: 'transparent',
              border: '1px solid var(--border)',
              padding: '6px 12px',
              fontSize: '0.9em',
              cursor: 'pointer',
              borderRadius: 10,
              display: 'inline-flex',
              alignItems: 'center',
              gap: 6,
              color: 'var(--text)',
            }}
          >
            <X size={16} strokeWidth={1.75} aria-hidden />
            Fermer
          </button>
        </div>

        {/* Liste conversations */}
        <div style={{ 
          flex: 1, 
          overflowY: 'auto',
          background: 'var(--page-bg)'
        }}>
          {conversations.length === 0 ? (
            <div style={{ 
              padding: '40px 20px', 
              textAlign: 'center',
              color: 'var(--muted)'
            }}>
              Aucune conversation
            </div>
          ) : (
            conversations.map((conv) => {
              const unreadDigit = unreadBadgeDigit(conv.unreadCount)
              return (
              <div
                key={conv.id}
                onClick={() => handleConversationClick(conv.id)}
                style={{
                  padding: '16px 20px',
                  borderBottom: '1px solid var(--border)',
                  cursor: 'pointer',
                  background: activeConversationId === conv.id ? 'var(--surface-elevated)' : 'transparent',
                  transition: 'background 0.2s'
                }}
              >
                <div style={{ display: 'flex', gap: 12, alignItems: 'start' }}>
                  <img
                    src={conv.productImage}
                    alt={conv.productTitle}
                    style={{
                      width: 50,
                      height: 50,
                      objectFit: 'cover',
                      borderRadius: 8
                    }}
                  />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ 
                      display: 'flex', 
                      justifyContent: 'space-between',
                      marginBottom: 4
                    }}>
                      <span style={{ 
                        fontWeight: 600,
                        fontSize: '0.95em',
                        color: 'var(--text)',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap'
                      }}>
                        {conv.productTitle}
                      </span>
                      {conv.lastMessageTime && (
                        <span style={{ 
                          fontSize: '0.75em',
                          color: 'var(--muted)',
                          flexShrink: 0,
                          marginLeft: 8
                        }}>
                          {formatTime(conv.lastMessageTime)}
                        </span>
                      )}
                    </div>
                    <div style={{ 
                      fontSize: '0.85em',
                      color: 'var(--muted)',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                      display: 'flex',
                      alignItems: 'center',
                      gap: 8
                    }}>
                      <span style={{ flex: 1, minWidth: 0 }}>
                        {conv.lastMessage || 'Nouvelle conversation'}
                      </span>
                      {unreadDigit != null && (
                        <span
                          title={
                            conv.unreadCount > 3
                              ? '3+ nouveaux messages du vendeur'
                              : `${conv.unreadCount} nouveau(x) message(s)`
                          }
                          style={{
                          background: '#c2410c',
                          color: 'white',
                          borderRadius: '50%',
                          minWidth: 22,
                          height: 22,
                          padding: '0 4px',
                          display: 'inline-flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontSize: '0.75em',
                          fontWeight: 700,
                          flexShrink: 0
                        }}
                        >
                          {unreadDigit}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            )})
          )}
        </div>
      </div>

      {/* Zone de chat */}
      <div style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        background: 'var(--page-bg)'
      }}>
        {activeConv ? (
          <>
            {/* Header conversation */}
            <div style={{
              padding: '16px 20px',
              borderBottom: '1px solid var(--border)',
              background: 'var(--surface)'
            }}>
              <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                <img
                  src={activeConv.productImage}
                  alt={activeConv.productTitle}
                  style={{
                    width: 45,
                    height: 45,
                    objectFit: 'cover',
                    borderRadius: 8
                  }}
                />
                <div>
                  <h3 style={{ margin: 0, fontSize: '1.1em' }}>
                    {activeConv.productTitle}
                  </h3>
                  <p style={{ 
                    margin: 0, 
                    fontSize: '0.85em',
                    color: 'var(--muted)',
                    marginTop: 2
                  }}>
                    via Ecomarket Assistant
                  </p>
                </div>
              </div>
            </div>

            {/* Messages */}
            <div style={{
              flex: 1,
              overflowY: 'auto',
              padding: '20px',
              display: 'flex',
              flexDirection: 'column',
              gap: 12
            }}>
              {messages.map((msg) => (
                <MessageBubble
                  key={msg.id}
                  message={msg}
                  currentUserId={user.id}
                  buyerId={activeConv.buyerId}
                  sellerId={activeConv.sellerId}
                />
              ))}
              <div ref={messagesEndRef} />
            </div>

            {/* Input message */}
            <div style={{
              borderTop: '1px solid var(--border)',
              background: 'var(--surface)'
            }}>
              {isBuyerInConv && convHasArticle && !buyerPendingOffer && !pendingSellerFinal && (
                <form
                  onSubmit={handleSubmitOffer}
                  style={{
                    padding: '12px 20px',
                    borderBottom: '1px solid var(--border)',
                    display: 'flex',
                    flexWrap: 'wrap',
                    gap: 8,
                    alignItems: 'center',
                  }}
                >
                  <span
                    style={{
                      fontSize: '0.85em',
                      color: 'var(--muted)',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: 6,
                    }}
                  >
                    <Coins size={15} strokeWidth={1.7} aria-hidden />
                    Contre-proposition (FCFA) — max 2, ≤ prix affiché (réduction min. éventuelle côté serveur)
                  </span>
                  <label style={{ fontSize: '0.85em', color: 'var(--muted)', display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                    Qté
                    <input
                      type="number"
                      min={1}
                      max={100}
                      title="Quantité souhaitée pour cette proposition"
                      value={offerQtyInput}
                      onChange={(e) => setOfferQtyInput(e.target.value)}
                      style={{
                        width: 64,
                        padding: '8px 10px',
                        borderRadius: 8,
                        border: '1px solid var(--border)',
                        background: 'var(--input-bg)',
                        color: 'var(--text)',
                      }}
                    />
                  </label>
                  <input
                    type="number"
                    min={1}
                    placeholder="Montant FCFA"
                    value={offerPriceInput}
                    onChange={(e) => setOfferPriceInput(e.target.value)}
                    style={{
                      width: 120,
                      padding: '8px 10px',
                      borderRadius: 8,
                      border: '1px solid var(--border)',
                      background: 'var(--input-bg)',
                      color: 'var(--text)',
                    }}
                  />
                  <button
                    type="submit"
                    disabled={offerBusy || !offerPriceInput.trim()}
                    style={{
                      padding: '8px 14px',
                      borderRadius: 8,
                      border: 'none',
                      background: 'var(--accent)',
                      color: '#fff',
                      cursor: offerBusy ? 'wait' : 'pointer',
                    }}
                  >
                    {offerBusy ? '…' : 'Envoyer l’offre'}
                  </button>
                </form>
              )}
              {isBuyerInConv && buyerPendingOffer && (
                <p style={{ padding: '10px 20px', margin: 0, fontSize: '0.85em', color: '#f59e0b' }}>
                  Une offre est en attente de réponse du vendeur.
                </p>
              )}
              {isBuyerInConv && pendingSellerFinal && (
                <div
                  style={{
                    padding: '14px 20px',
                    borderBottom: '1px solid var(--border)',
                    background: 'rgba(251, 191, 36, 0.08)',
                  }}
                >
                  <p style={{ margin: '0 0 12px', fontSize: '0.9em', color: 'var(--text)' }}>
                    Le vendeur propose un{' '}
                    <strong>prix final</strong> :{' '}
                    {pendingSellerFinal.metadata!.offeredPrice!.toLocaleString('fr-FR')} FCFA
                    {(pendingSellerFinal.metadata?.quantity ?? 1) > 1 && (
                      <>
                        {' '}
                        pour <strong>{pendingSellerFinal.metadata!.quantity} unités</strong>
                      </>
                    )}
                    . Validez pour passer au paiement, ou refusez pour terminer la négociation.
                  </p>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                    <button
                      type="button"
                      disabled={finalDecisionBusy}
                      onClick={() => {
                        if (!activeConversationId || !pendingSellerFinal.metadata?.negotiationId) return
                        setFinalDecisionBusy(true)
                        void respondToSellerFinal(
                          activeConversationId,
                          pendingSellerFinal.metadata.negotiationId,
                          true
                        ).catch((err) =>
                          alert(err instanceof Error ? err.message : 'Action impossible')
                        ).finally(() => setFinalDecisionBusy(false))
                      }}
                      style={{
                        padding: '8px 16px',
                        borderRadius: 8,
                        border: 'none',
                        background: '#2a9d8f',
                        color: '#fff',
                        fontWeight: 600,
                        cursor: finalDecisionBusy ? 'wait' : 'pointer',
                      }}
                    >
                      Valider le prix final → paiement
                    </button>
                    <button
                      type="button"
                      disabled={finalDecisionBusy}
                      onClick={() => {
                        if (!activeConversationId || !pendingSellerFinal.metadata?.negotiationId) return
                        setFinalDecisionBusy(true)
                        void respondToSellerFinal(
                          activeConversationId,
                          pendingSellerFinal.metadata.negotiationId,
                          false
                        ).catch((err) =>
                          alert(err instanceof Error ? err.message : 'Action impossible')
                        ).finally(() => setFinalDecisionBusy(false))
                      }}
                      style={{
                        padding: '8px 16px',
                        borderRadius: 8,
                        border: '1px solid #f87171',
                        background: 'transparent',
                        color: '#f87171',
                        fontWeight: 600,
                        cursor: finalDecisionBusy ? 'wait' : 'pointer',
                      }}
                    >
                      Refuser (fin)
                    </button>
                  </div>
                </div>
              )}

              {canPayNegotiated && (
                <div
                  style={{
                    padding: '14px 20px',
                    borderBottom: '1px solid var(--border)',
                    background: 'rgba(42, 157, 143, 0.08)',
                  }}
                >
                  <p style={{ margin: '0 0 10px', fontSize: '0.9em', color: 'var(--text)' }}>
                    Prix convenu :{' '}
                    <strong>{acceptedUnitPrice!.toLocaleString('fr-FR')} FCFA</strong>
                    {acceptedQuantity > 1 && (
                      <>
                        {' '}
                        pour <strong>{acceptedQuantity} unités</strong>
                      </>
                    )}
                    {' '}
                    — vous pouvez enregistrer votre paiement ci-dessous.
                  </p>
                  <PaymentPanel
                    articleId={Number(activeConv!.productId)}
                    prixCatalogue={Math.round(productForConv!.price)}
                    defaultPrixNegocie={acceptedUnitPrice}
                    defaultQuantite={acceptedQuantity}
                    onSuccess={(id) => {
                      setPayOkId(id)
                      setTimeout(() => setPayOkId(null), 10000)
                    }}
                  />
                  {payOkId != null && (
                    <p style={{ color: '#4ade80', margin: '10px 0 0', fontSize: '0.85em' }}>
                      Paiement enregistré (transaction #{payOkId}). Retrouvez le reçu dans votre profil.
                    </p>
                  )}
                </div>
              )}

              {activeConv?.productId && (
                <p
                  style={{
                    padding: '10px 20px',
                    margin: 0,
                    fontSize: '0.82em',
                    color: 'var(--muted)',
                    borderBottom: '1px solid var(--border)',
                  }}
                >
                  Pas de message texte libre sur une annonce : uniquement contre-propositions chiffrées et validation du
                  prix final.
                </p>
              )}

              {!activeConv?.productId && (
                <form
                  onSubmit={handleSendMessage}
                  style={{
                    padding: '16px 20px',
                    display: 'flex',
                    gap: 12
                  }}
                >
                  <input
                    type="text"
                    value={messageText}
                    onChange={(e) => setMessageText(e.target.value)}
                    placeholder="Message…"
                    style={{
                      flex: 1,
                      padding: '12px 16px',
                      borderRadius: 24,
                      border: '1px solid var(--border)',
                      background: 'var(--page-bg)',
                      color: 'var(--text)',
                      fontSize: '0.95em'
                    }}
                  />
                  <button
                    type="submit"
                    disabled={!messageText.trim()}
                    style={{
                      padding: '12px 24px',
                      borderRadius: 24,
                      border: 'none',
                      background: messageText.trim() ? '#2a9d8f' : 'var(--surface-elevated)',
                      color: messageText.trim() ? 'white' : 'var(--muted)',
                      fontWeight: 600,
                      cursor: messageText.trim() ? 'pointer' : 'not-allowed',
                      transition: 'all 0.2s'
                    }}
                  >
                    Envoyer
                  </button>
                </form>
              )}
            </div>
          </>
        ) : (
          <div style={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--muted)',
            flexDirection: 'column',
            gap: 12
          }}>
            <MessageSquare size={48} strokeWidth={1.35} aria-hidden style={{ opacity: 0.45 }} />
            <p style={{ fontSize: '1.1em' }}>Sélectionnez une conversation</p>
          </div>
        )}
      </div>
    </div>
  )
}

// Composant bulle de message
function MessageBubble({
  message,
  currentUserId,
  buyerId,
  sellerId,
}: {
  message: Message
  currentUserId: string
  buyerId: string
  sellerId: string
}) {
  const isBot = message.sender === 'bot'
  const isOwnMessage =
    (message.sender === 'client' && currentUserId === buyerId) ||
    (message.sender === 'vendor' && currentUserId === sellerId)

  const formatMessageTime = (timestamp: string) => {
    return new Date(timestamp).toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  return (
    <div style={{
      display: 'flex',
      justifyContent: isOwnMessage ? 'flex-end' : 'flex-start',
      marginBottom: 8
    }}>
      <div style={{
        maxWidth: '70%',
        padding: '12px 16px',
        borderRadius: 16,
        background: isBot ? 'var(--surface-elevated)' : 
                    isOwnMessage ? 'var(--accent)' : 
                    'var(--surface)',
        border: isBot ? '1px solid var(--border)' : 'none',
        color: isOwnMessage ? 'white' : 'var(--text)'
      }}>
        {!isOwnMessage && (
          <div style={{
            fontSize: '0.75em',
            color: isBot ? 'var(--accent)' : 'var(--muted)',
            marginBottom: 6,
            fontWeight: 600
          }}>
            {message.senderName}
          </div>
        )}

        {message.type === 'negotiation' && message.metadata?.offeredPrice != null && (
          <div
            style={{
              marginBottom: 10,
              padding: '10px 12px',
              borderRadius: 10,
              background: isOwnMessage ? 'rgba(0,0,0,0.2)' : 'rgba(42, 157, 143, 0.15)',
              border: `1px solid ${isOwnMessage ? 'rgba(255,255,255,0.25)' : 'rgba(42, 157, 143, 0.45)'}`,
              fontSize: '0.9em',
            }}
          >
            <div style={{ fontWeight: 700, marginBottom: 4, display: 'flex', alignItems: 'center', gap: 6 }}>
              <Coins size={15} strokeWidth={1.75} aria-hidden />
              {message.metadata.offreFinaleVendeur ? 'Dernier prix vendeur' : 'Proposition de prix'}
            </div>
            <div>
              <strong>{message.metadata.offeredPrice.toLocaleString('fr-FR')} FCFA</strong>
              <span style={{ opacity: 0.9 }}>
                {' '}
                / unité · Qté :{' '}
                <strong>{message.metadata.quantity != null && message.metadata.quantity > 0 ? message.metadata.quantity : 1}</strong>
              </span>
            </div>
            {message.offerStatus && (
              <div
                style={{
                  marginTop: 6,
                  fontSize: '0.8em',
                  fontWeight: 600,
                  color:
                    message.offerStatus === 'ACCEPTED'
                      ? '#4ade80'
                      : message.offerStatus === 'REFUSED'
                        ? '#f87171'
                        : '#fbbf24',
                }}
              >
                {message.offerStatus === 'PENDING' && 'En attente de réponse du vendeur'}
                {message.offerStatus === 'ACCEPTED' && 'Offre acceptée'}
                {message.offerStatus === 'REFUSED' && 'Offre refusée'}
                {message.offerStatus === 'PENDING_BUYER_FINAL' && 'En attente de votre validation'}
                {message.offerStatus === 'VALIDATED' && 'Prix final validé — paiement possible'}
                {message.offerStatus === 'EXPIRED' && 'Prix final refusé — négociation close'}
              </div>
            )}
          </div>
        )}

        {!(
          message.type === 'negotiation' &&
          message.metadata?.offeredPrice != null &&
          (message.text.trim().startsWith('Proposition de prix') ||
            message.text.trim().startsWith('Dernier prix du vendeur'))
        ) && (
          <div style={{ 
            whiteSpace: 'pre-wrap',
            lineHeight: 1.5,
            fontSize: '0.95em'
          }}>
            {message.text}
          </div>
        )}

        {message.metadata?.location && (
          <div style={{
            marginTop: 12,
            padding: 12,
            background: 'rgba(0,0,0,0.2)',
            borderRadius: 8
          }}>
            <div style={{ fontSize: '0.85em', marginBottom: 8, display: 'flex', alignItems: 'center', gap: 6 }}>
              <MapPin size={15} strokeWidth={1.7} aria-hidden />
              <strong>Adresse :</strong>
            </div>
            <div style={{ fontSize: '0.85em', color: 'var(--text)' }}>
              {message.metadata.location.address}
            </div>
            {message.metadata.location.description && (
              <div style={{ fontSize: '0.85em', color: 'var(--muted)', marginTop: 4 }}>
                {message.metadata.location.description}
              </div>
            )}
          </div>
        )}

        {message.metadata?.paymentInfo && (
          <div style={{
            marginTop: 12,
            padding: 12,
            background: 'rgba(42, 157, 143, 0.1)',
            borderRadius: 8,
            border: '1px solid rgba(42, 157, 143, 0.3)'
          }}>
            <div style={{ fontSize: '0.9em', fontWeight: 600, marginBottom: 8, display: 'flex', alignItems: 'center', gap: 6 }}>
              <CreditCard size={16} strokeWidth={1.7} aria-hidden />
              Informations de paiement
            </div>
            <div style={{ fontSize: '0.85em', color: 'var(--text)' }}>
              Référence : <strong>{message.metadata.paymentInfo.reference}</strong>
            </div>
          </div>
        )}

        <div style={{
          fontSize: '0.7em',
          color: isOwnMessage ? 'rgba(255,255,255,0.7)' : 'var(--muted)',
          marginTop: 6,
          textAlign: 'right'
        }}>
          {formatMessageTime(message.timestamp)}
        </div>
      </div>
    </div>
  )
}
