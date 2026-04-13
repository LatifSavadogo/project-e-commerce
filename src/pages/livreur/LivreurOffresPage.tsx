import { useCallback, useEffect, useState } from 'react'
import { Package, MapPin, RefreshCw, Radio } from 'lucide-react'
import { ApiError } from '../../services/apiClient'
import { fetchLivraisonsDisponibles, ignorerLivraison, prendreLivraison } from '../../services/livreurApi'
import type { LivraisonLivreurDtoJson } from '../../types/backend'
import { iconSm } from '../../components/ui/iconProps'

const POLL_MS = 8_000

export default function LivreurOffresPage() {
  const [rows, setRows] = useState<LivraisonLivreurDtoJson[]>([])
  const [err, setErr] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [lastSync, setLastSync] = useState<Date | null>(null)

  const load = useCallback(async () => {
    setErr(null)
    try {
      const list = await fetchLivraisonsDisponibles()
      setRows(list)
      setLastSync(new Date())
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'Chargement impossible')
    }
  }, [])

  useEffect(() => {
    void load()
    const t = window.setInterval(() => void load(), POLL_MS)
    return () => window.clearInterval(t)
  }, [load])

  const onPrendre = async (id: number, engin: 'MOTO' | 'VEHICULE') => {
    setBusyId(id)
    try {
      await prendreLivraison(id, engin)
      await load()
    } catch (e) {
      const msg =
        e instanceof ApiError
          ? e.message
          : e instanceof Error
            ? e.message
            : 'Action impossible'
      alert(msg)
    } finally {
      setBusyId(null)
    }
  }

  const onIgnorer = async (id: number) => {
    setBusyId(id)
    try {
      await ignorerLivraison(id)
      await load()
    } catch (e) {
      const msg =
        e instanceof ApiError
          ? e.message
          : e instanceof Error
            ? e.message
            : 'Action impossible'
      alert(msg)
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div className="container" style={{ paddingTop: 28, paddingBottom: 48, maxWidth: 960 }}>
      <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
        <div>
          <h1 style={{ margin: '0 0 6px', fontSize: '1.55rem' }}>Offres à saisir</h1>
          <p className="meta" style={{ margin: 0 }}>
            Liste mise à jour automatiquement · la première acceptation bloque la course pour les autres livreurs
          </p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          {lastSync && (
            <span className="meta" style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
              <Radio size={14} className="livreur-pulse-icon" aria-hidden />
              {lastSync.toLocaleTimeString('fr-FR')}
            </span>
          )}
          <button type="button" className="livreur-btn-ghost" onClick={() => void load()}>
            <RefreshCw size={16} aria-hidden />
            Sync
          </button>
        </div>
      </div>

      {err && (
        <p role="alert" style={{ color: 'var(--danger)', marginTop: 20 }}>
          {err}
        </p>
      )}

      {rows.length === 0 && !err ? (
        <div className="livreur-empty" style={{ marginTop: 28 }}>
          Aucune offre pour l’instant. Les commandes payées apparaissent ici pour être livrées.
        </div>
      ) : (
        <ul className="livreur-run-list" style={{ marginTop: 24 }}>
          {rows.map((c) => (
            <li key={c.idlivraison} className="livreur-run-card livreur-run-card--offer">
              <div>
                <strong style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}>
                  <Package {...iconSm} aria-hidden />
                  {c.articleLibelle || `Course #${c.idlivraison}`}
                </strong>
                <div className="livreur-run-meta">
                  <MapPin size={14} style={{ verticalAlign: '-2px', marginRight: 4 }} aria-hidden />
                  {c.acheteurVille || '—'} · {c.acheteurEmail}
                  <br />
                  Vendeur : {c.vendeurEmail}
                  <br />
                  Quantité : <strong>{c.quantite ?? '—'}</strong>
                </div>
              </div>
              <div className="livreur-run-actions" style={{ flexDirection: 'column', alignItems: 'stretch' }}>
                <button
                  type="button"
                  className="button-primary"
                  disabled={busyId === c.idlivraison}
                  onClick={() => void onPrendre(c.idlivraison, 'MOTO')}
                >
                  Accepter · Moto
                </button>
                <button
                  type="button"
                  className="livreur-btn-secondary"
                  disabled={busyId === c.idlivraison}
                  onClick={() => void onPrendre(c.idlivraison, 'VEHICULE')}
                >
                  Accepter · Véhicule
                </button>
                <button
                  type="button"
                  className="link-button"
                  style={{ fontSize: '0.88rem', marginTop: 4 }}
                  disabled={busyId === c.idlivraison}
                  onClick={() => void onIgnorer(c.idlivraison)}
                >
                  Ignorer (masquer pour moi)
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
