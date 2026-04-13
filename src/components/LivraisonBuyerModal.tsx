import { useEffect, useState } from 'react'
import QRCode from 'react-qr-code'
import { X } from 'lucide-react'
import { ApiError } from '../services/apiClient'
import { fetchClientLivraisonQr, fetchCommandeSuivi } from '../services/paymentApi'
import type { ClientLivraisonQrDtoJson, CommandeSuiviDtoJson } from '../types/backend'
import { iconSm } from './ui/iconProps'

type Props = {
  mode: 'qr' | 'suivi' | null
  transactionId: number | null
  onClose: () => void
}

export default function LivraisonBuyerModal({ mode, transactionId, onClose }: Props) {
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState<string | null>(null)
  const [qr, setQr] = useState<ClientLivraisonQrDtoJson | null>(null)
  const [suivi, setSuivi] = useState<CommandeSuiviDtoJson | null>(null)

  useEffect(() => {
    if (!mode || transactionId == null) return
    let cancelled = false
    setLoading(true)
    setErr(null)
    setQr(null)
    setSuivi(null)
    const run = async () => {
      try {
        if (mode === 'qr') {
          const data = await fetchClientLivraisonQr(transactionId)
          if (!cancelled) setQr(data)
        } else {
          const data = await fetchCommandeSuivi(transactionId)
          if (!cancelled) setSuivi(data)
        }
      } catch (e) {
        if (!cancelled) {
          if (e instanceof ApiError) {
            if (e.status === 403 || e.status === 401) {
              setErr('Connexion requise ou commande non autorisée.')
            } else if (e.status >= 500) {
              setErr(
                e.message && e.message !== 'Internal Server Error'
                  ? e.message
                  : 'Erreur serveur lors du chargement du QR. Redémarrez l’API si besoin, ou contactez le support.'
              )
            } else {
              setErr(e.message || 'Impossible de charger les données.')
            }
          } else {
            setErr(e instanceof Error ? e.message : 'Erreur')
          }
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    void run()
    return () => {
      cancelled = true
    }
  }, [mode, transactionId])

  if (!mode || transactionId == null) return null

  return (
    <div
      className="livraison-modal-backdrop"
      role="dialog"
      aria-modal="true"
      aria-labelledby="livraison-modal-title"
      onClick={(e) => e.target === e.currentTarget && onClose()}
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
          maxWidth: 480,
          width: '100%',
          maxHeight: '90vh',
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
          onClick={onClose}
          aria-label="Fermer"
          style={{
            position: 'absolute',
            top: 12,
            right: 12,
            background: 'transparent',
            border: 'none',
            cursor: 'pointer',
            color: 'var(--muted)',
            padding: 8,
          }}
        >
          <X {...iconSm} />
        </button>
        <h2 id="livraison-modal-title" style={{ margin: '0 32px 16px 0', fontSize: '1.15rem' }}>
          {mode === 'qr' ? 'QR de livraison' : 'Suivi de commande'}
        </h2>
        {loading && <p className="meta">Chargement…</p>}
        {err && (
          <p role="alert" style={{ color: 'var(--danger)' }}>
            {err}
          </p>
        )}
        {mode === 'qr' && qr && !loading && (
          <div>
            <p className="meta" style={{ marginBottom: 12 }}>
              {qr.message || 'Montrez ce code au livreur à la réception.'}
            </p>
            <p style={{ fontSize: '0.9rem', marginBottom: 8 }}>
              <strong>{qr.articleLibelle}</strong> × {qr.quantite}
            </p>
            <div
              style={{
                display: 'flex',
                justifyContent: 'center',
                marginBottom: 12,
                padding: 14,
                background: '#ffffff',
                borderRadius: 12,
                border: '1px solid var(--border)',
              }}
            >
              <QRCode
                value={qr.qrPayload}
                size={220}
                level="M"
                title="QR livraison"
                style={{ height: 'auto', maxWidth: '100%', width: '100%' }}
                viewBox="0 0 220 220"
              />
            </div>
            <p style={{ fontSize: '0.78rem', color: 'var(--muted)', marginBottom: 4 }}>Texte encodé dans le QR</p>
            <pre
              style={{
                marginTop: 0,
                marginBottom: 12,
                padding: 10,
                background: 'var(--input-bg)',
                borderRadius: 8,
                overflow: 'auto',
                wordBreak: 'break-all',
                fontSize: '0.8rem',
                lineHeight: 1.45,
              }}
            >
              {qr.qrPayload}
            </pre>
            <details style={{ fontSize: '0.82rem', color: 'var(--muted)' }}>
              <summary style={{ cursor: 'pointer' }}>Aide affichage</summary>
              <p style={{ marginTop: 8, marginBottom: 0 }}>
                Augmentez la luminosité de l’écran pour faciliter le scan par la caméra du livreur. En cas de souci,
                copiez le texte ci-dessus pour le coller côté livreur.
              </p>
            </details>
          </div>
        )}
        {mode === 'suivi' && suivi && !loading && (
          <div style={{ fontSize: '0.92rem' }}>
            <p>
              <strong>Statut livraison :</strong> {suivi.statutLivraison || '—'}
            </p>
            {suivi.livreurAssigne && (
              <p className="meta">
                Livreur : {suivi.livreurPrenom} {suivi.livreurNom}
              </p>
            )}
            <ol style={{ paddingLeft: 18, margin: '12px 0', lineHeight: 1.6 }}>
              {suivi.etapes.map((e) => (
                <li key={e.code}>
                  <strong>{e.libelle}</strong>
                  {e.date ? ` — ${new Date(e.date).toLocaleString('fr-FR')}` : ''}
                </li>
              ))}
            </ol>
            {(suivi.statutLivraison === 'LIVREE' || suivi.statutLivraison === 'ANNULEE') && !suivi.navigationDisponible && (
              <p className="meta" style={{ marginTop: 12, marginBottom: 0 }}>
                Le suivi sur Google Maps n’est plus proposé : la commande est clôturée côté livraison.
              </p>
            )}
            {suivi.navigationDisponible && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: 12 }}>
                {suivi.lienRetraitChezVendeur && (
                  <a href={suivi.lienRetraitChezVendeur} target="_blank" rel="noopener noreferrer">
                    Lieu de retrait (vendeur) — Google Maps
                  </a>
                )}
                {suivi.lienLivraisonChezClient && (
                  <a href={suivi.lienLivraisonChezClient} target="_blank" rel="noopener noreferrer">
                    Adresse de livraison — Google Maps
                  </a>
                )}
                {suivi.lienTrajetVendeurVersClient && (
                  <a href={suivi.lienTrajetVendeurVersClient} target="_blank" rel="noopener noreferrer">
                    Trajet vendeur → client (itinéraire Maps)
                  </a>
                )}
                {suivi.lienLivreurVersClient && (
                  <a href={suivi.lienLivreurVersClient} target="_blank" rel="noopener noreferrer">
                    Suivre le livreur vers vous (Google Maps)
                  </a>
                )}
                {suivi.livreurPositionMiseAJourAt && (
                  <p className="meta" style={{ margin: 0 }}>
                    Position livreur mise à jour : {new Date(suivi.livreurPositionMiseAJourAt).toLocaleString('fr-FR')}
                  </p>
                )}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
