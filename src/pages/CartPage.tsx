import { useState, useCallback, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Trash2, ShoppingCart, CreditCard } from 'lucide-react'
import { useAuth, isStaffRole } from '../contexts/AuthContext'
import { useCart } from '../contexts/CartContext'
import { cartCheckout, type PaymentMethod } from '../services/paymentApi'
import { ApiError } from '../services/apiClient'
import { articleMainPhotoUrl } from '../utils/articleUrls'
import { iconSm } from '../components/ui/iconProps'
import DeliveryMapPicker from '../components/DeliveryMapPicker'

const METHODS: { value: PaymentMethod; label: string }[] = [
  { value: 'ORANGE_MONEY', label: 'Orange Money' },
  { value: 'MOOV_MONEY', label: 'Moov Money' },
  { value: 'VIREMENT', label: 'Virement' },
  { value: 'ESPECES', label: 'Espèces' },
]

function unitPrice(item: { prixUnitaireNegocie?: number | null; prixUnitaireCatalogue: number }) {
  return item.prixUnitaireNegocie != null ? item.prixUnitaireNegocie : item.prixUnitaireCatalogue
}

function lineTotal(item: {
  quantity: number
  prixUnitaireNegocie?: number | null
  prixUnitaireCatalogue: number
}) {
  return unitPrice(item) * item.quantity
}

export default function CartPage() {
  const { user, isAuthenticated } = useAuth()
  const hasDomicileGps = user != null && user.latitude != null && user.longitude != null
  const domLat = user?.latitude != null ? Number(user.latitude) : 12.3714
  const domLng = user?.longitude != null ? Number(user.longitude) : -1.5197
  const navigate = useNavigate()
  const { cart, cartLoading, setLineQuantity, removeLine, emptyCart, refreshCart } = useCart()
  const [deliveryMode, setDeliveryMode] = useState<'domicile' | 'autre'>('domicile')
  const [orderLat, setOrderLat] = useState<number | null>(null)
  const [orderLng, setOrderLng] = useState<number | null>(null)
  const [moyen, setMoyen] = useState<PaymentMethod>('ORANGE_MONEY')
  const [reference, setReference] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)

  const handleMapPick = useCallback((lat: number, lng: number) => {
    setOrderLat(lat)
    setOrderLng(lng)
  }, [])

  useEffect(() => {
    if (deliveryMode === 'domicile') {
      setOrderLat(null)
      setOrderLng(null)
    }
  }, [deliveryMode])

  if (!isAuthenticated) {
    return (
      <div className="container cart-page" style={{ paddingTop: 32 }}>
        <h1>Panier</h1>
        <p className="meta">Connectez-vous pour voir votre panier.</p>
        <Link to="/auth?mode=login" className="button-primary" style={{ display: 'inline-block', padding: '12px 20px' }}>
          Se connecter
        </Link>
      </div>
    )
  }

  if (user && isStaffRole(user)) {
    return (
      <div className="container cart-page" style={{ paddingTop: 32 }}>
        <h1>Panier</h1>
        <p className="meta">Le panier client n’est pas disponible pour les comptes administrateur.</p>
      </div>
    )
  }

  const items = cart?.items ?? []
  const ids = items.map((i) => i.idcartitem)

  const handleCheckout = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setSuccess(null)
    if (items.length === 0) {
      setError('Votre panier est vide.')
      return
    }
    if (!reference.trim()) {
      setError('Indiquez la référence de votre paiement (reçue après Orange Money, virement, etc.).')
      return
    }
    if (!hasDomicileGps) {
      setError('Enregistrez le GPS de votre domicile dans Mon compte avant de payer.')
      return
    }
    if (deliveryMode === 'autre' && (orderLat == null || orderLng == null)) {
      setError('Choisissez sur la carte le lieu de livraison pour toutes les lignes du panier.')
      return
    }
    setBusy(true)
    try {
      const results = await cartCheckout({
        moyenPaiement: moyen,
        referenceExterne: reference.trim(),
        cartItemIds: ids,
        livraisonLatitude: deliveryMode === 'autre' && orderLat != null ? orderLat : null,
        livraisonLongitude: deliveryMode === 'autre' && orderLng != null ? orderLng : null,
      })
      setSuccess(
        `Paiement enregistré pour ${results.length} ligne(s). Numéros : ${results.map((r) => r.idtransaction).join(', ')}. Dans Mon compte → Mes achats : reçu PDF, suivi et QR livreur (jusqu’à la livraison).`
      )
      setReference('')
      setDeliveryMode('domicile')
      setOrderLat(null)
      setOrderLng(null)
      await refreshCart()
    } catch (err) {
      const msg =
        err instanceof ApiError ? err.message : err instanceof Error ? err.message : 'Paiement refusé'
      setError(msg)
    } finally {
      setBusy(false)
    }
  }

  const canPay =
    hasDomicileGps && (deliveryMode === 'domicile' || (orderLat != null && orderLng != null))

  return (
    <div className="container cart-page" style={{ paddingTop: 24, paddingBottom: 48 }}>
      <h1 style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <ShoppingCart {...iconSm} aria-hidden style={{ width: 28, height: 28 }} />
        Mon panier
      </h1>
      <p className="meta" style={{ marginTop: 8 }}>
        Articles ajoutés depuis le catalogue ou une offre acceptée dans la messagerie.
      </p>

      {cartLoading && <p className="meta">Chargement…</p>}

      {!cartLoading && items.length === 0 && (
        <div className="cart-empty card" style={{ padding: 32, marginTop: 24, textAlign: 'center' }}>
          <p className="meta" style={{ marginBottom: 16 }}>
            Votre panier est vide.
          </p>
          <Link to="/listings" className="button-primary" style={{ display: 'inline-block', padding: '12px 20px' }}>
            Parcourir les annonces
          </Link>
        </div>
      )}

      {items.length > 0 && (
        <div className="cart-layout">
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {items.map((item) => (
              <div
                key={item.idcartitem}
                className="cart-line card"
                style={{
                  padding: 16,
                  display: 'grid',
                  gridTemplateColumns: 'minmax(72px, 88px) 1fr auto',
                  gap: 16,
                  alignItems: 'start',
                }}
              >
                <Link to={`/product/${item.idArticle}`}>
                  <img
                    src={articleMainPhotoUrl(item.idArticle)}
                    alt=""
                    style={{ width: 88, height: 88, objectFit: 'cover', borderRadius: 10, display: 'block' }}
                  />
                </Link>
                <div style={{ minWidth: 0 }}>
                  <Link
                    to={`/product/${item.idArticle}`}
                    style={{ color: 'var(--text)', fontWeight: 600, textDecoration: 'none' }}
                  >
                    {item.libelleArticle}
                  </Link>
                  {item.negociationVerrouillee && (
                    <p className="meta" style={{ margin: '6px 0 0', fontSize: '0.8rem' }}>
                      Prix et quantité figés (accord négociation).
                    </p>
                  )}
                  <p style={{ margin: '8px 0 0', fontSize: '0.9rem', color: 'var(--muted)' }}>
                    {unitPrice(item).toLocaleString('fr-FR')} FCFA × {item.quantity} ={' '}
                    <strong style={{ color: 'var(--accent)' }}>{lineTotal(item).toLocaleString('fr-FR')} FCFA</strong>
                  </p>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8, alignItems: 'flex-end' }}>
                  {!item.negociationVerrouillee ? (
                    <label style={{ fontSize: '0.75rem', color: 'var(--muted)' }}>
                      Qté
                      <input
                        type="number"
                        min={1}
                        max={100}
                        value={item.quantity}
                        onChange={(e) => {
                          const q = parseInt(e.target.value, 10)
                          if (!q || q < 1 || q > 100) return
                          void setLineQuantity(item.idcartitem, q).catch((err) =>
                            alert(err instanceof Error ? err.message : 'Erreur')
                          )
                        }}
                        style={{
                          marginLeft: 8,
                          width: 64,
                          padding: '6px 8px',
                          borderRadius: 8,
                          border: '1px solid var(--border)',
                          background: 'var(--input-bg)',
                          color: 'var(--text)',
                        }}
                      />
                    </label>
                  ) : (
                    <span className="meta">Qté {item.quantity}</span>
                  )}
                  <button
                    type="button"
                    className="link-button"
                    onClick={() =>
                      void removeLine(item.idcartitem).catch((err) =>
                        alert(err instanceof Error ? err.message : 'Erreur')
                      )
                    }
                    style={{ display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: '0.85rem' }}
                  >
                    <Trash2 size={16} strokeWidth={2} aria-hidden />
                    Retirer
                  </button>
                </div>
              </div>
            ))}
            <button
              type="button"
              className="link-button"
              onClick={() => {
                if (!window.confirm('Vider tout le panier ?')) return
                void emptyCart().catch((err) => alert(err instanceof Error ? err.message : 'Erreur'))
              }}
              style={{ alignSelf: 'flex-start' }}
            >
              Vider le panier
            </button>
          </div>

          <div
            className="cart-summary card"
            style={{
              padding: 20,
              position: 'sticky',
              top: 72,
              border: '1px solid var(--border-subtle)',
            }}
          >
            <h2 style={{ marginTop: 0, fontSize: '1.1rem' }}>Récapitulatif</h2>
            <p style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--accent)' }}>
              Total estimé : {(cart?.montantTotalEstime ?? 0).toLocaleString('fr-FR')} FCFA
            </p>
            {!hasDomicileGps && (
              <p role="status" style={{ fontSize: '0.88rem', color: 'var(--danger, #e05252)', marginTop: 12, marginBottom: 0 }}>
                GPS du domicile requis :{' '}
                <Link to="/profile" style={{ fontWeight: 600 }}>
                  Mon compte → Domicile (GPS)
                </Link>
                .
              </p>
            )}
            <form onSubmit={(e) => void handleCheckout(e)} className="form-stack form-stack--tight" style={{ marginTop: 16 }}>
              <fieldset style={{ border: '1px solid var(--border)', borderRadius: 10, padding: 12, margin: 0 }}>
                <legend style={{ fontSize: '0.85rem', padding: '0 6px' }}>Livraison (toutes les lignes)</legend>
                <label style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8, cursor: 'pointer' }}>
                  <input
                    type="radio"
                    name="liv-cart"
                    checked={deliveryMode === 'domicile'}
                    onChange={() => setDeliveryMode('domicile')}
                  />
                  Domicile (profil)
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
                  <input
                    type="radio"
                    name="liv-cart"
                    checked={deliveryMode === 'autre'}
                    onChange={() => setDeliveryMode('autre')}
                  />
                  Autre lieu — carte
                </label>
                {deliveryMode === 'autre' && hasDomicileGps && (
                  <div style={{ marginTop: 12 }}>
                    <DeliveryMapPicker
                      centerLat={domLat}
                      centerLng={domLng}
                      markerLat={orderLat}
                      markerLng={orderLng}
                      onPick={handleMapPick}
                      height={200}
                      footerHint="Zoomez puis cliquez sur le lieu de livraison pour tout le panier (travail, service…). Seules les coordonnées sont envoyées."
                    />
                    {orderLat != null && orderLng != null && (
                      <p className="form-hint" style={{ marginTop: 6 }}>
                        {orderLat.toFixed(6)}, {orderLng.toFixed(6)}{' '}
                        <a
                          href={`https://www.google.com/maps/search/?api=1&query=${orderLat}%2C${orderLng}`}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          Google Maps
                        </a>
                      </p>
                    )}
                  </div>
                )}
              </fieldset>
              <div className="form-field">
                <label className="form-label" htmlFor="cart-moyen">
                  Moyen de paiement
                </label>
                <select id="cart-moyen" value={moyen} onChange={(e) => setMoyen(e.target.value as PaymentMethod)}>
                  {METHODS.map((m) => (
                    <option key={m.value} value={m.value}>
                      {m.label}
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-field">
                <label className="form-label" htmlFor="cart-ref">
                  Référence de transaction
                </label>
                <input
                  id="cart-ref"
                  value={reference}
                  onChange={(e) => setReference(e.target.value)}
                  placeholder="Ex. référence Orange Money"
                  autoComplete="off"
                />
              </div>
              {error && (
                <p role="alert" style={{ color: 'var(--danger, #e05252)', margin: 0, fontSize: '0.9rem' }}>
                  {error}
                </p>
              )}
              {success && (
                <p style={{ color: 'var(--accent)', margin: 0, fontSize: '0.9rem' }}>{success}</p>
              )}
              <button
                type="submit"
                className="button-primary"
                disabled={busy || !canPay}
                style={{ width: '100%', marginTop: 8, opacity: busy || !canPay ? 0.65 : 1 }}
              >
                <CreditCard {...iconSm} aria-hidden style={{ marginRight: 8, verticalAlign: 'middle' }} />
                {busy ? 'Traitement…' : 'Payer tout le panier'}
              </button>
            </form>
            <p className="form-hint" style={{ marginTop: 12, marginBottom: 0 }}>
              Une transaction enregistrée par ligne ; la référence est complétée côté serveur pour chaque article.
            </p>
            <button type="button" className="link-button" style={{ marginTop: 16, width: '100%' }} onClick={() => navigate('/profile')}>
              Voir mes achats / reçus
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
