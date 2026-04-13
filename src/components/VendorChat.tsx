import { useState, useEffect, useRef, useMemo } from 'react'
import { Check, CircleCheck, Coins, CreditCard, MessageSquare, X } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { useChat } from '../contexts/ChatContext'
import type { Message } from '../types/chat'
import { unreadBadgeDigit, vendorNegotiationStep } from '../utils/chatUnread'
import {
  findPendingBuyerOffer,
  isSecondBuyerOfferPending,
  vendorMaySubmitFinalPrice,
} from '../utils/negotiationMessages'

export default function VendorChat() {
  const { user } = useAuth()
  const {
    activeConversationId,
    setActiveConversation,
    markAsRead,
    getMessages,
    acceptNegotiation,
    rejectNegotiation,
    sendSellerFinalPrice,
    getVendorConversations
  } = useChat()

  const [finalPriceInput, setFinalPriceInput] = useState('')
  const [finalBusy, setFinalBusy] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const vendorConversations = user ? getVendorConversations(user.id) : []
  const activeConv = vendorConversations.find((c) => c.id === activeConversationId)
  const messages = activeConversationId ? getMessages(activeConversationId) : []
  const showFinalPriceForm = useMemo(() => vendorMaySubmitFinalPrice(messages), [messages])
  const secondRoundPending = useMemo(() => isSecondBuyerOfferPending(messages), [messages])
  const activeStep = useMemo(() => vendorNegotiationStep(messages), [messages])
  const pendingOfferMsg = useMemo(
    () => (activeConv ? findPendingBuyerOffer(messages, activeConv.buyerId) : null),
    [messages, activeConv?.buyerId]
  )
  /** Tant qu’une offre acheteur est en attente : uniquement Accepter / Refuser (tour 1) ou prix final (tour 2). */
  const vendorChatLockedForOffer = pendingOfferMsg != null

  const vendorIdsKey = useMemo(() => vendorConversations.map((c) => c.id).join('|'), [vendorConversations])
  const firstVendorConvId = vendorConversations[0]?.id

  /** Toujours une conversation vendeur sélectionnée si la liste n’est pas vide (évite l’écran vide après /chat). */
  useEffect(() => {
    if (!user || !firstVendorConvId) return
    const valid =
      !!activeConversationId && vendorIdsKey.split('|').filter(Boolean).includes(activeConversationId)
    if (!valid) {
      setActiveConversation(firstVendorConvId)
      markAsRead(firstVendorConvId)
    }
  }, [user, firstVendorConvId, vendorIdsKey, activeConversationId, setActiveConversation, markAsRead])

  // Auto-scroll
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [activeConversationId, messages.length])

  const handleConversationClick = (convId: string) => {
    setActiveConversation(convId)
    markAsRead(convId)
  }

  const handleAcceptNegotiation = () => {
    if (!activeConv || !pendingOfferMsg?.metadata?.negotiationId) return
    void acceptNegotiation(activeConv.id, pendingOfferMsg.metadata.negotiationId)
  }

  const handleRejectNegotiation = () => {
    if (!activeConv || !pendingOfferMsg?.metadata?.negotiationId) return
    void rejectNegotiation(activeConv.id, pendingOfferMsg.metadata.negotiationId)
  }

  const handleSubmitFinalPrice = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!activeConversationId || !activeConv) return
    const v = parseInt(finalPriceInput.replace(/\s/g, ''), 10)
    if (!v || v < 1) return
    setFinalBusy(true)
    try {
      await sendSellerFinalPrice(activeConversationId, v)
      setFinalPriceInput('')
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Impossible d’enregistrer le dernier prix')
    } finally {
      setFinalBusy(false)
    }
  }


  /* const formatTime = (timestamp: string) => {
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
  } */

  if (!user) return null

  return (
    <div style={{
      display: 'flex',
      height: '600px',
      border: '1px solid var(--border)',
      borderRadius: 8,
      overflow: 'hidden',
      background: 'var(--page-bg)'
    }}>
      {/* Sidebar conversations */}
      <div style={{
        width: '300px',
        borderRight: '1px solid var(--border)',
        display: 'flex',
        flexDirection: 'column',
        background: 'var(--surface)'
      }}>
        <div style={{
          padding: '16px',
          borderBottom: '1px solid var(--border)',
          fontWeight: 600
        }}>
          <div>Conversations ({vendorConversations.length})</div>
          <div style={{ fontSize: '0.7em', color: 'var(--muted)', fontWeight: 400, marginTop: 6 }}>
            Mise à jour auto des messages (~5 s)
          </div>
        </div>

        <div style={{ flex: 1, overflowY: 'auto' }}>
          {vendorConversations.length === 0 ? (
            <div style={{
              padding: '40px 20px',
              textAlign: 'center',
              color: 'var(--muted)'
            }}>
              Aucune conversation
            </div>
          ) : (
            vendorConversations.map((conv) => {
              const rowMessages = getMessages(conv.id)
              const step = vendorNegotiationStep(rowMessages)
              const unreadDigit = unreadBadgeDigit(conv.unreadCount)
              return (
              <div
                key={conv.id}
                onClick={() => handleConversationClick(conv.id)}
                style={{
                  padding: '12px 16px',
                  borderBottom: '1px solid var(--border)',
                  cursor: 'pointer',
                  background: activeConversationId === conv.id ? 'var(--surface-elevated)' : 'transparent',
                  transition: 'background 0.2s'
                }}
              >
                <div style={{ display: 'flex', gap: 10, alignItems: 'start' }}>
                  <div
                    style={{
                      width: 22,
                      height: 22,
                      borderRadius: '50%',
                      background: 'var(--border)',
                      color: 'var(--text)',
                      fontSize: '0.75em',
                      fontWeight: 700,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      flexShrink: 0,
                      marginTop: 2,
                    }}
                    title="Étape de négociation (1 à 3)"
                  >
                    {step}
                  </div>
                  <img
                    src={conv.productImage}
                    alt={conv.productTitle}
                    style={{
                      width: 40,
                      height: 40,
                      objectFit: 'cover',
                      borderRadius: 6
                    }}
                  />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{
                      fontSize: '0.9em',
                      fontWeight: 600,
                      color: 'var(--text)',
                      marginBottom: 4,
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap'
                    }}>
                      {conv.buyerName}
                    </div>
                    <div style={{
                      fontSize: '0.8em',
                      color: 'var(--muted)',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap'
                    }}>
                      {conv.productTitle}
                    </div>
                    <div style={{
                      fontSize: '0.75em',
                      color: 'var(--muted)',
                      marginTop: 4,
                      display: 'flex',
                      justifyContent: 'space-between'
                    }}>
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5 }}>
                        {conv.status === 'negotiating' && (
                          <>
                            <Coins size={13} strokeWidth={1.7} aria-hidden /> Négociation
                          </>
                        )}
                        {conv.status === 'payment' && (
                          <>
                            <CreditCard size={13} strokeWidth={1.7} aria-hidden /> Paiement
                          </>
                        )}
                        {conv.status === 'active' && (
                          <>
                            <MessageSquare size={13} strokeWidth={1.7} aria-hidden /> Active
                          </>
                        )}
                        {conv.status === 'completed' && (
                          <>
                            <CircleCheck size={13} strokeWidth={1.7} aria-hidden /> Terminé
                          </>
                        )}
                      </span>
                      {unreadDigit != null && (
                        <span
                          title={
                            conv.unreadCount > 3
                              ? `3+ nouveaux messages`
                              : `${conv.unreadCount} nouveau(x) message(s)`
                          }
                          style={{
                          background: '#c2410c',
                          color: 'white',
                          borderRadius: '50%',
                          minWidth: 22,
                          height: 22,
                          padding: '0 4px',
                          fontWeight: 700,
                          fontSize: '0.8em',
                          display: 'inline-flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                        }}
                        >
                          {unreadDigit}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
              )
            })
          )}
        </div>
      </div>

      {/* Zone de chat */}
      <div style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column'
      }}>
        {activeConv ? (
          <>
            {/* Header */}
            <div style={{
              padding: '16px',
              borderBottom: '1px solid var(--border)',
              background: 'var(--surface)'
            }}>
              <div style={{ display: 'flex', gap: 10, alignItems: 'center', justifyContent: 'space-between' }}>
                <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                  <img
                    src={activeConv.productImage}
                    alt={activeConv.productTitle}
                    style={{
                      width: 40,
                      height: 40,
                      objectFit: 'cover',
                      borderRadius: 6
                    }}
                  />
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '0.95em' }}>
                      {activeConv.buyerName}
                    </div>
                    <div style={{ fontSize: '0.8em', color: 'var(--muted)' }}>
                      {activeConv.productTitle}
                    </div>
                    <div style={{ fontSize: '0.72em', color: 'var(--muted)', marginTop: 4 }}>
                      Étape {activeStep}/3 —{' '}
                      {activeStep === 1 && 'première phase (offre acheteur ou attente)'}
                      {activeStep === 2 && 'deuxième proposition ou saisie du dernier prix'}
                      {activeStep === 3 && 'prix accepté ou dernier prix envoyé (paiement côté acheteur)'}
                    </div>
                  </div>
                </div>

                {/* Actions négociation : offre acheteur en attente (1ʳᵉ ou 2ᵉ proposition) */}
                {pendingOfferMsg && (
                  <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                    <button
                      type="button"
                      onClick={handleAcceptNegotiation}
                      style={{
                        padding: '8px 16px',
                        borderRadius: 6,
                        border: 'none',
                        background: 'var(--accent)',
                        color: 'white',
                        fontWeight: 600,
                        cursor: 'pointer',
                        fontSize: '0.85em',
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 6,
                      }}
                    >
                      <Check size={15} strokeWidth={2} aria-hidden />
                      Accepter
                    </button>
                    {!secondRoundPending && (
                      <button
                        type="button"
                        onClick={handleRejectNegotiation}
                        style={{
                          padding: '8px 16px',
                          borderRadius: 6,
                          border: '1px solid var(--border)',
                          background: 'transparent',
                          color: 'var(--text)',
                          fontWeight: 600,
                          cursor: 'pointer',
                          fontSize: '0.85em',
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: 6,
                        }}
                      >
                        <X size={15} strokeWidth={2} aria-hidden />
                        Refuser
                      </button>
                    )}
                  </div>
                )}

              </div>

              {/* Infos négociation — offre en attente */}
              {pendingOfferMsg && pendingOfferMsg.metadata?.offeredPrice != null && (
                <div style={{
                  marginTop: 12,
                  padding: 12,
                  background: 'var(--surface-elevated)',
                  borderRadius: 6,
                  fontSize: '0.85em',
                  border: '1px solid rgba(251, 191, 36, 0.35)',
                }}>
                  <div style={{ fontWeight: 700, marginBottom: 8, color: '#fbbf24' }}>
                    Proposition en attente de votre réponse
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                    <span>Quantité : <strong>{pendingOfferMsg.metadata.quantity ?? 1}</strong></span>
                    <span>
                      Prix proposé :{' '}
                      <strong>{pendingOfferMsg.metadata.offeredPrice.toLocaleString('fr-FR')} FCFA</strong>
                    </span>
                  </div>
                  <div style={{ color: 'var(--accent)', fontWeight: 600 }}>
                    Total :{' '}
                    {(
                      pendingOfferMsg.metadata.offeredPrice * (pendingOfferMsg.metadata.quantity ?? 1)
                    ).toLocaleString('fr-FR')}{' '}
                    FCFA
                  </div>
                </div>
              )}
            </div>

            {vendorChatLockedForOffer && (
              <div
                style={{
                  padding: '10px 16px',
                  background: 'rgba(251, 191, 36, 0.1)',
                  borderBottom: '1px solid #92400e',
                  fontSize: '0.82em',
                  color: 'var(--text)',
                }}
              >
                <strong>Règle :</strong> pas de message libre.{' '}
                {secondRoundPending ? (
                  <>
                    Au <strong>tour 2</strong>, pas de refus sec : <strong>acceptez</strong> la proposition ou indiquez
                    un <strong>prix final</strong> ci-dessous.
                  </>
                ) : (
                  <>
                    Utilisez <strong>Accepter</strong> ou <strong>Refuser</strong> (tour 1 uniquement pour le refus sec).
                  </>
                )}
              </div>
            )}

            {showFinalPriceForm && activeConversationId && (
              <div
                style={{
                  padding: '12px 16px',
                  borderBottom: '1px solid var(--border)',
                  background: 'var(--surface-elevated)',
                }}
              >
                <p style={{ margin: '0 0 10px', fontSize: '0.85em', color: 'var(--text)' }}>
                  {secondRoundPending ? (
                    <>
                      La 2e proposition est en cours : vous pouvez l’<strong>accepter</strong> ou fixer votre{' '}
                      <strong>prix final</strong> (≤ prix affiché). L’acheteur devra valider ce montant avant paiement.
                    </>
                  ) : (
                    <>
                      Les deux propositions ont été refusées. Indiquez votre <strong>prix final</strong> (≤ prix
                      affiché). L’acheteur pourra le valider ou refuser.
                    </>
                  )}
                </p>
                <form onSubmit={handleSubmitFinalPrice} style={{ display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'center' }}>
                  <input
                    type="number"
                    min={1}
                    placeholder="Montant FCFA"
                    value={finalPriceInput}
                    onChange={(e) => setFinalPriceInput(e.target.value)}
                    style={{
                      width: 140,
                      padding: '8px 10px',
                      borderRadius: 8,
                      border: '1px solid var(--border)',
                      background: 'var(--input-bg)',
                      color: 'var(--text)',
                    }}
                  />
                  <button
                    type="submit"
                    disabled={finalBusy || !finalPriceInput.trim()}
                    style={{
                      padding: '8px 14px',
                      borderRadius: 8,
                      border: 'none',
                      background: '#2a9d8f',
                      color: '#fff',
                      fontWeight: 600,
                      cursor: finalBusy ? 'wait' : 'pointer',
                    }}
                  >
                    {finalBusy ? '…' : 'Valider le dernier prix'}
                  </button>
                </form>
              </div>
            )}

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
                <VendorMessageBubble key={msg.id} message={msg} />
              ))}
              <div ref={messagesEndRef} />
            </div>

            <div
              style={{
                padding: '14px 16px',
                borderTop: '1px solid var(--border)',
                background: '#0a0c0e',
                fontSize: '0.82em',
                color: 'var(--text-faint)',
              }}
            >
              Aucun message texte libre : actions de négociation uniquement (accepter, refus tour 1, prix final).
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
            <p>Sélectionnez une conversation</p>
          </div>
        )}
      </div>

    </div>
  )
}

// Composant bulle message vendeur
function VendorMessageBubble({ message }: { message: Message }) {
  const isBot = message.sender === 'bot'
  const isVendor = message.sender === 'vendor'

  const formatMessageTime = (timestamp: string) => {
    return new Date(timestamp).toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  return (
    <div style={{
      display: 'flex',
      justifyContent: isVendor ? 'flex-end' : 'flex-start',
      marginBottom: 8
    }}>
      <div style={{
        maxWidth: '70%',
        padding: '12px 16px',
        borderRadius: 16,
        background: isBot ? 'var(--surface-elevated)' :
                    isVendor ? 'var(--accent)' :
                    'var(--surface)',
        border: isBot ? '1px solid var(--border)' : 'none',
        color: isVendor ? 'white' : 'var(--text)'
      }}>
        {!isVendor && (
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
          <div style={{
            marginBottom: 8,
            padding: 10,
            background: isVendor ? 'rgba(0,0,0,0.2)' : 'rgba(42, 157, 143, 0.2)',
            borderRadius: 8,
            fontSize: '0.85em',
            border: `1px solid ${isVendor ? 'rgba(255,255,255,0.25)' : 'rgba(42, 157, 143, 0.35)'}`,
          }}>
            <div style={{ fontWeight: 700, marginBottom: 4, display: 'flex', alignItems: 'center', gap: 6 }}>
              <Coins size={15} strokeWidth={1.75} aria-hidden />
              {message.metadata.offreFinaleVendeur ? 'Dernier prix vendeur' : 'Proposition de prix'}
            </div>
            <div>
              <strong>{message.metadata.offeredPrice.toLocaleString('fr-FR')} FCFA</strong>
              {message.metadata.quantity != null && message.metadata.quantity > 1 && (
                <span> × {message.metadata.quantity}</span>
              )}
            </div>
            {message.offerStatus && (
              <div style={{
                marginTop: 6,
                fontSize: '0.8em',
                fontWeight: 600,
                color: message.offerStatus === 'ACCEPTED' ? '#4ade80'
                  : message.offerStatus === 'REFUSED' ? '#f87171' : '#fbbf24',
              }}>
                {message.offerStatus === 'PENDING' && 'En attente de votre réponse'}
                {message.offerStatus === 'ACCEPTED' && 'Acceptée'}
                {message.offerStatus === 'REFUSED' && 'Refusée'}
                {message.offerStatus === 'PENDING_BUYER_FINAL' && 'En attente validation acheteur'}
                {message.offerStatus === 'VALIDATED' && 'Validée par l’acheteur (paiement)'}
                {message.offerStatus === 'EXPIRED' && 'Refusée par l’acheteur'}
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
            fontSize: '0.9em'
          }}>
            {message.text}
          </div>
        )}

        <div style={{
          fontSize: '0.7em',
          color: isVendor ? 'rgba(255,255,255,0.7)' : 'var(--muted)',
          marginTop: 6,
          textAlign: 'right'
        }}>
          {formatMessageTime(message.timestamp)}
        </div>
      </div>
    </div>
  )
}
