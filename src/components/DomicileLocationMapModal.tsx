import { useCallback, useEffect, useId, useState } from 'react'
import { X, LocateFixed } from 'lucide-react'
import DeliveryMapPicker from './DeliveryMapPicker'
import { patchMyDeliveryLocation } from '../services/meProfileApi'
import { ApiError } from '../services/apiClient'
import { iconSm } from './ui/iconProps'

const DEFAULT_LAT = 12.3714
const DEFAULT_LNG = -1.5197

type Props = {
  open: boolean
  onClose: () => void
  savedLat: number | null | undefined
  savedLng: number | null | undefined
  onSaved: () => void | Promise<void>
}

/**
 * Modale plein écran : carte OSM, clic pour placer le domicile, enregistrement API puis fermeture.
 */
export default function DomicileLocationMapModal({ open, onClose, savedLat, savedLng, onSaved }: Props) {
  const titleId = useId()
  const [mapKey, setMapKey] = useState(0)
  const [stableCenter, setStableCenter] = useState({ lat: DEFAULT_LAT, lng: DEFAULT_LNG })
  const [draftLat, setDraftLat] = useState<number | null>(null)
  const [draftLng, setDraftLng] = useState<number | null>(null)
  const [busy, setBusy] = useState(false)
  const [err, setErr] = useState<string | null>(null)

  useEffect(() => {
    if (!open) return
    setErr(null)
    const hasSaved =
      savedLat != null && savedLng != null && !Number.isNaN(Number(savedLat)) && !Number.isNaN(Number(savedLng))
    const lat0 = hasSaved ? Number(savedLat) : DEFAULT_LAT
    const lng0 = hasSaved ? Number(savedLng) : DEFAULT_LNG
    setStableCenter({ lat: lat0, lng: lng0 })
    setMapKey((k) => k + 1)
    if (hasSaved) {
      setDraftLat(Number(savedLat))
      setDraftLng(Number(savedLng))
    } else {
      setDraftLat(null)
      setDraftLng(null)
    }
  }, [open, savedLat, savedLng])

  const handlePick = useCallback((lat: number, lng: number) => {
    setDraftLat(lat)
    setDraftLng(lng)
    setErr(null)
  }, [])

  const mapHeight = typeof window !== 'undefined' ? Math.min(Math.round(window.innerHeight * 0.42), 420) : 320

  const useMyPosition = () => {
    if (!navigator.geolocation) {
      setErr('Géolocalisation indisponible sur cet appareil.')
      return
    }
    setErr(null)
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const lat = pos.coords.latitude
        const lng = pos.coords.longitude
        setStableCenter({ lat, lng })
        setDraftLat(lat)
        setDraftLng(lng)
        setMapKey((k) => k + 1)
      },
      () =>
        setErr(
          'Impossible de lire la position. Autorisez la géolocalisation dans le navigateur, ou cliquez sur la carte.'
        ),
      { enableHighAccuracy: true, timeout: 20000 }
    )
  }

  const save = async () => {
    if (draftLat == null || draftLng == null) {
      setErr('Placez un point sur la carte (clic), puis validez.')
      return
    }
    setBusy(true)
    setErr(null)
    try {
      await patchMyDeliveryLocation(draftLat, draftLng)
      await onSaved()
      onClose()
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : e instanceof Error ? e.message : 'Erreur')
    } finally {
      setBusy(false)
    }
  }

  if (!open) return null

  return (
    <div
      className="livraison-modal-backdrop"
      role="dialog"
      aria-modal="true"
      aria-labelledby={titleId}
      onClick={(e) => e.target === e.currentTarget && !busy && onClose()}
      style={{
        position: 'fixed',
        inset: 0,
        background: 'rgba(0,0,0,0.55)',
        zIndex: 200,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 16,
      }}
    >
      <div
        className="livraison-modal-panel card"
        style={{
          maxWidth: 560,
          width: '100%',
          maxHeight: '92vh',
          overflow: 'auto',
          padding: 20,
          position: 'relative',
          background: 'var(--surface-elevated)',
          border: '1px solid var(--border)',
          borderRadius: 14,
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <button
          type="button"
          onClick={() => !busy && onClose()}
          aria-label="Fermer"
          disabled={busy}
          style={{
            position: 'absolute',
            top: 12,
            right: 12,
            background: 'transparent',
            border: 'none',
            cursor: busy ? 'not-allowed' : 'pointer',
            color: 'var(--muted)',
            padding: 8,
          }}
        >
          <X {...iconSm} />
        </button>
        <h2 id={titleId} style={{ margin: '0 36px 12px 0', fontSize: '1.12rem' }}>
          Choisir le domicile sur la carte
        </h2>
        <p className="meta" style={{ marginTop: 0, marginBottom: 12 }}>
          Zoomez, déplacez la vue, puis <strong>cliquez</strong> sur votre point de remise habituel. Validez pour
          enregistrer ; la fenêtre se ferme après succès.
        </p>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, marginBottom: 12, alignItems: 'center' }}>
          <button
            type="button"
            className="livreur-btn-secondary"
            disabled={busy}
            style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}
            onClick={() => useMyPosition()}
          >
            <LocateFixed size={18} aria-hidden />
            Centrer sur ma position
          </button>
          {draftLat != null && draftLng != null && (
            <span className="form-hint" style={{ margin: 0 }}>
              {draftLat.toFixed(6)}, {draftLng.toFixed(6)}
            </span>
          )}
        </div>
        <DeliveryMapPicker
          key={mapKey}
          centerLat={stableCenter.lat}
          centerLng={stableCenter.lng}
          markerLat={draftLat}
          markerLng={draftLng}
          onPick={handlePick}
          height={mapHeight}
          footerHint="Un clic pose le marqueur. Coordonnées WGS84 (identiques à Google Maps). Ce point est votre domicile de référence pour payer ; un autre lieu reste possible au moment du paiement."
        />
        {err && (
          <p role="alert" style={{ color: 'var(--danger)', marginTop: 12, marginBottom: 0 }}>
            {err}
          </p>
        )}
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, marginTop: 16, justifyContent: 'flex-end' }}>
          <button type="button" className="link-button" disabled={busy} onClick={() => !busy && onClose()}>
            Annuler
          </button>
          <button type="button" className="button-primary" disabled={busy || draftLat == null || draftLng == null} onClick={() => void save()}>
            {busy ? 'Enregistrement…' : 'Enregistrer et fermer'}
          </button>
        </div>
      </div>
    </div>
  )
}
