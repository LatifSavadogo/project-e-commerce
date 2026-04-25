import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { CreditCard, X } from 'lucide-react'
import { createPayment, type PaymentMethod } from '../services/paymentApi'
import { createPaydunyaOrderInvoice } from '../services/paydunyaApi'
import { ApiError } from '../services/apiClient'
import { useAuth } from '../contexts/AuthContext'
import { iconSm } from './ui/iconProps'
import DeliveryMapPicker from './DeliveryMapPicker'

const METHODS: { value: PaymentMethod; label: string }[] = [
  { value: 'PAYDUNYA', label: 'PayDunya (Orange, Moov, carte…)' },
  { value: 'ORANGE_MONEY', label: 'Orange Money' },
  { value: 'MOOV_MONEY', label: 'Moov Money' },
  { value: 'VIREMENT', label: 'Virement' },
  { value: 'ESPECES', label: 'Espèces' },
]

type Props = {
  articleId: number
  prixCatalogue: number
  /** Pré-remplit le champ « prix négocié » (ex. offre acceptée ou dernier prix vendeur). */
  defaultPrixNegocie?: number
  /** Pré-remplit la quantité (ex. quantité négociée acceptée). */
  defaultQuantite?: number
  onSuccess: (transactionId: number) => void
}

export default function PaymentPanel({
  articleId,
  prixCatalogue,
  defaultPrixNegocie,
  defaultQuantite,
  onSuccess,
}: Props) {
  const { user } = useAuth()
  const hasDomicileGps = user != null && user.latitude != null && user.longitude != null
  const domLat = user?.latitude != null ? Number(user.latitude) : 12.3714
  const domLng = user?.longitude != null ? Number(user.longitude) : -1.5197

  const [open, setOpen] = useState(false)
  const [deliveryMode, setDeliveryMode] = useState<'domicile' | 'autre'>('domicile')
  const [orderLat, setOrderLat] = useState<number | null>(null)
  const [orderLng, setOrderLng] = useState<number | null>(null)
  const [quantite, setQuantite] = useState('1')
  const [moyen, setMoyen] = useState<PaymentMethod>('PAYDUNYA')
  const [reference, setReference] = useState('')
  const [prixNego, setPrixNego] = useState(
    defaultPrixNegocie != null ? String(defaultPrixNegocie) : ''
  )
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleMapPick = useCallback((lat: number, lng: number) => {
    setOrderLat(lat)
    setOrderLng(lng)
  }, [])

  useEffect(() => {
    if (defaultPrixNegocie != null) {
      setPrixNego(String(defaultPrixNegocie))
    }
  }, [defaultPrixNegocie])

  useEffect(() => {
    if (defaultQuantite != null && defaultQuantite >= 1 && defaultQuantite <= 100) {
      setQuantite(String(defaultQuantite))
    }
  }, [defaultQuantite])

  useEffect(() => {
    if (deliveryMode === 'domicile') {
      setOrderLat(null)
      setOrderLng(null)
    }
  }, [deliveryMode])

  const handlePay = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    if (!hasDomicileGps) {
      setError('Enregistrez d’abord le GPS de votre domicile dans Mon compte.')
      return
    }
    if (deliveryMode === 'autre' && (orderLat == null || orderLng == null)) {
      setError('Cliquez sur la carte pour choisir le lieu de livraison de cette commande.')
      return
    }
    const q = parseInt(quantite, 10)
    if (!q || q < 1 || q > 100) {
      setError('Quantité entre 1 et 100')
      return
    }
    if (moyen !== 'PAYDUNYA' && !reference.trim()) {
      setError('Référence de transaction obligatoire (reçue après paiement mobile, etc.)')
      return
    }
    const negoRaw = prixNego.trim()
    let prixUnitaireNegocie: number | undefined
    if (negoRaw) {
      const n = parseInt(negoRaw, 10)
      if (!n || n < 1 || n > prixCatalogue) {
        setError(`Prix négocié invalide (1 à ${prixCatalogue} FCFA, après offre acceptée)`)
        return
      }
      prixUnitaireNegocie = n
    }
    setBusy(true)
    try {
      if (moyen === 'PAYDUNYA') {
        const inv = await createPaydunyaOrderInvoice({
          idArticle: articleId,
          quantite: q,
          prixUnitaireNegocie: prixUnitaireNegocie ?? null,
          livraisonLatitude: deliveryMode === 'autre' && orderLat != null ? orderLat : null,
          livraisonLongitude: deliveryMode === 'autre' && orderLng != null ? orderLng : null,
        })
        window.location.assign(inv.checkoutUrl)
        return
      }
      const res = await createPayment({
        idArticle: articleId,
        quantite: q,
        moyenPaiement: moyen,
        referenceExterne: reference.trim(),
        prixUnitaireNegocie: prixUnitaireNegocie ?? null,
        livraisonLatitude: deliveryMode === 'autre' && orderLat != null ? orderLat : null,
        livraisonLongitude: deliveryMode === 'autre' && orderLng != null ? orderLng : null,
      })
      onSuccess(res.idtransaction)
      setReference('')
      setPrixNego('')
      setDeliveryMode('domicile')
      setOrderLat(null)
      setOrderLng(null)
      setOpen(false)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : err instanceof Error ? err.message : 'Paiement refusé')
    } finally {
      setBusy(false)
    }
  }

  const canSubmit =
    hasDomicileGps && (deliveryMode === 'domicile' || (orderLat != null && orderLng != null))

  if (!open) {
    return (
      <button
        type="button"
        onClick={() => setOpen(true)}
        style={{
          background: 'transparent',
          border: '1px solid var(--accent)',
          color: 'var(--accent)',
          padding: '14px',
          fontWeight: 600,
          cursor: 'pointer',
          borderRadius: 12,
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 10,
        }}
      >
        <CreditCard {...iconSm} aria-hidden />
        Enregistrer un paiement
      </button>
    )
  }

  return (
    <div
      style={{
        border: '1px solid var(--border)',
        borderRadius: 12,
        padding: 16,
        background: 'var(--surface)',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
        <h3 style={{ margin: 0, fontSize: '1.1em' }}>Paiement (API)</h3>
        <button
          type="button"
          onClick={() => setOpen(false)}
          style={{
            background: 'transparent',
            border: 'none',
            color: 'var(--muted)',
            cursor: 'pointer',
            padding: 4,
            lineHeight: 0,
            borderRadius: 8,
          }}
          aria-label="Fermer"
        >
          <X {...iconSm} aria-hidden />
        </button>
      </div>
      <p style={{ fontSize: '0.85em', color: 'var(--muted)', marginTop: 0 }}>
        Prix catalogue : <strong>{prixCatalogue} FCFA</strong>.
        {moyen === 'PAYDUNYA'
          ? ' Vous serez redirigé vers PayDunya (Burkina Faso et UEMOA) pour payer en ligne.'
          : ' Renseignez la référence unique de votre transaction.'}
      </p>
      {!hasDomicileGps && (
        <p role="alert" style={{ fontSize: '0.88em', color: 'var(--danger)', marginBottom: 12 }}>
          GPS du domicile manquant.{' '}
          <Link to="/profile" style={{ fontWeight: 600 }}>
            Mon compte
          </Link>{' '}
          → « Domicile (GPS) ».
        </p>
      )}
      <form onSubmit={handlePay} className="form-stack form-stack--tight">
        <fieldset style={{ border: '1px solid var(--border)', borderRadius: 10, padding: 12, margin: 0 }}>
          <legend style={{ fontSize: '0.88rem', padding: '0 6px' }}>Livraison de cette commande</legend>
          <label style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8, cursor: 'pointer' }}>
            <input
              type="radio"
              name="liv-pay"
              checked={deliveryMode === 'domicile'}
              onChange={() => setDeliveryMode('domicile')}
            />
            À mon domicile (coordonnées du profil)
          </label>
          <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
            <input
              type="radio"
              name="liv-pay"
              checked={deliveryMode === 'autre'}
              onChange={() => setDeliveryMode('autre')}
            />
            Autre lieu (travail, service…) — choisir sur la carte
          </label>
          {deliveryMode === 'autre' && hasDomicileGps && (
            <div style={{ marginTop: 12 }}>
              <DeliveryMapPicker
                centerLat={domLat}
                centerLng={domLng}
                markerLat={orderLat}
                markerLng={orderLng}
                onPick={handleMapPick}
                height={220}
                footerHint="Zoomez puis cliquez sur le lieu de livraison pour cette commande (travail, service…). Seules les coordonnées sont envoyées au serveur."
              />
              {orderLat != null && orderLng != null && (
                <p className="form-hint" style={{ marginTop: 8 }}>
                  Point sélectionné : {orderLat.toFixed(6)}, {orderLng.toFixed(6)}{' '}
                  <a
                    href={`https://www.google.com/maps/search/?api=1&query=${orderLat}%2C${orderLng}`}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    Voir dans Google Maps
                  </a>
                </p>
              )}
            </div>
          )}
        </fieldset>
        <div className="form-field">
          <label className="form-label">Quantité</label>
          <input type="number" min={1} max={100} value={quantite} onChange={(e) => setQuantite(e.target.value)} required />
        </div>
        <div className="form-field">
          <label className="form-label">Moyen de paiement</label>
          <select value={moyen} onChange={(e) => setMoyen(e.target.value as PaymentMethod)}>
            {METHODS.map((m) => (
              <option key={m.value} value={m.value}>
                {m.label}
              </option>
            ))}
          </select>
        </div>
        {moyen !== 'PAYDUNYA' && (
          <div className="form-field">
            <label className="form-label">Référence externe</label>
            <input
              type="text"
              value={reference}
              onChange={(e) => setReference(e.target.value)}
              placeholder="Ex. code opérateur / référence virement"
              required
              maxLength={200}
            />
          </div>
        )}
        <div className="form-field">
          <label className="form-label">Prix unitaire négocié (optionnel)</label>
          <input
            type="number"
            min={1}
            max={prixCatalogue}
            value={prixNego}
            onChange={(e) => setPrixNego(e.target.value)}
            placeholder={`Laisser vide pour ${prixCatalogue} FCFA`}
          />
          <p className="form-hint">Uniquement si une offre a été acceptée à ce prix.</p>
        </div>
        {error && <p className="form-hint" style={{ color: 'var(--danger)' }}>{error}</p>}
        <button
          type="submit"
          disabled={busy || !canSubmit}
          style={{
            padding: '12px 18px',
            background: 'var(--accent)',
            border: 'none',
            fontWeight: 600,
            borderRadius: 12,
            color: '#fff',
            cursor: busy || !canSubmit ? 'not-allowed' : 'pointer',
            opacity: busy || !canSubmit ? 0.65 : 1,
          }}
        >
          {busy ? 'Traitement…' : 'Valider le paiement'}
        </button>
      </form>
    </div>
  )
}
