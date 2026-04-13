import { useEffect, useState } from 'react'
import { X } from 'lucide-react'
import { fetchAdminLivraisonSuivi } from '../services/adminLivraisonApi'
import type { CommandeSuiviDtoJson } from '../types/backend'
import { iconSm } from './ui/iconProps'

type Props = {
  idlivraison: number | null
  onClose: () => void
}

export default function AdminLivraisonSuiviModal({ idlivraison, onClose }: Props) {
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState<string | null>(null)
  const [suivi, setSuivi] = useState<CommandeSuiviDtoJson | null>(null)

  useEffect(() => {
    if (idlivraison == null) return
    let cancelled = false
    setLoading(true)
    setErr(null)
    void fetchAdminLivraisonSuivi(idlivraison)
      .then((d) => {
        if (!cancelled) setSuivi(d)
      })
      .catch((e) => {
        if (!cancelled) setErr(e instanceof Error ? e.message : 'Erreur')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [idlivraison])

  if (idlivraison == null) return null

  return (
    <div
      role="dialog"
      aria-modal="true"
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
        className="card"
        style={{
          maxWidth: 520,
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
        <h2 style={{ margin: '0 32px 12px 0' }}>Suivi livraison #{idlivraison}</h2>
        {loading && <p className="meta">Chargement…</p>}
        {err && <p style={{ color: '#f87171' }}>{err}</p>}
        {suivi && !loading && (
          <div style={{ fontSize: '0.9rem' }}>
            <p>
              Transaction #{suivi.idtransaction} — <strong>{suivi.statutLivraison}</strong>
            </p>
            {suivi.vendorPickupCode && (
              <p>
                Code retrait vendeur : <strong style={{ letterSpacing: '0.08em' }}>{suivi.vendorPickupCode}</strong>
              </p>
            )}
            {suivi.vendorPackedReferenceBase64 && (
              <details style={{ marginTop: 8 }}>
                <summary>Référence encodée (vendeur)</summary>
                <pre
                  style={{
                    fontSize: '0.75rem',
                    wordBreak: 'break-all',
                    marginTop: 8,
                    padding: 8,
                    background: 'var(--input-bg)',
                    borderRadius: 8,
                  }}
                >
                  {suivi.vendorPackedReferenceBase64}
                </pre>
              </details>
            )}
            <p className="meta">
              {suivi.articleLibelle} × {suivi.quantite} — {suivi.montantTotal != null ? `${suivi.montantTotal} FCFA` : ''}
            </p>
            <ol style={{ paddingLeft: 18, lineHeight: 1.6 }}>
              {suivi.etapes.map((e) => (
                <li key={e.code}>
                  {e.libelle}
                  {e.date ? ` — ${new Date(e.date).toLocaleString('fr-FR')}` : ''}
                </li>
              ))}
            </ol>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6, marginTop: 10 }}>
              {suivi.lienRetraitChezVendeur && (
                <a href={suivi.lienRetraitChezVendeur} target="_blank" rel="noreferrer">
                  Maps — vendeur
                </a>
              )}
              {suivi.lienLivraisonChezClient && (
                <a href={suivi.lienLivraisonChezClient} target="_blank" rel="noreferrer">
                  Maps — client
                </a>
              )}
              {suivi.lienTrajetVendeurVersClient && (
                <a href={suivi.lienTrajetVendeurVersClient} target="_blank" rel="noreferrer">
                  Trajet vendeur → client
                </a>
              )}
              {suivi.lienLivreurVersClient && (
                <a href={suivi.lienLivreurVersClient} target="_blank" rel="noreferrer">
                  Itinéraire livreur → client (Maps)
                </a>
              )}
              {suivi.livreurPositionMiseAJourAt && (
                <p className="meta" style={{ margin: 0 }}>
                  Dernière position livreur : {new Date(suivi.livreurPositionMiseAJourAt).toLocaleString('fr-FR')}
                </p>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
