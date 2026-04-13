import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { RefreshCw, Radio } from 'lucide-react'
import { fetchLivreurDashboard, fetchLivraisonsDisponibles } from '../../services/livreurApi'
import type { LivreurDashboardDtoJson } from '../../types/backend'

const POLL_MS = 10_000

export default function LivreurOverview() {
  const [dash, setDash] = useState<LivreurDashboardDtoJson | null>(null)
  const [offres, setOffres] = useState<number>(0)
  const [err, setErr] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [lastSync, setLastSync] = useState<Date | null>(null)

  const load = useCallback(async () => {
    setErr(null)
    try {
      const [d, dispo] = await Promise.all([fetchLivreurDashboard(), fetchLivraisonsDisponibles()])
      setDash(d)
      setOffres(dispo.length)
      setLastSync(new Date())
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'Erreur réseau')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load()
    const t = window.setInterval(() => void load(), POLL_MS)
    return () => window.clearInterval(t)
  }, [load])

  return (
    <div className="container" style={{ paddingTop: 28, paddingBottom: 48, maxWidth: 960 }}>
      <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'flex-start', justifyContent: 'space-between', gap: 16 }}>
        <div>
          <h1 style={{ margin: '0 0 8px', fontSize: '1.65rem' }}>Tableau de bord</h1>
          <p className="meta" style={{ margin: 0, maxWidth: 520 }}>
            Dès qu’un acheteur valide un paiement, une livraison apparaît dans les offres à saisir. Actualisation automatique
            toutes les {POLL_MS / 1000} s.
          </p>
        </div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'center' }}>
          {lastSync && (
            <span className="meta" style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
              <Radio size={14} className="livreur-pulse-icon" aria-hidden />
              Sync {lastSync.toLocaleTimeString('fr-FR')}
            </span>
          )}
          <button type="button" className="livreur-btn-ghost" onClick={() => void load()} disabled={loading}>
            <RefreshCw size={16} aria-hidden />
            Actualiser
          </button>
        </div>
      </div>

      {err && (
        <p role="alert" style={{ color: 'var(--danger)', marginTop: 20 }}>
          {err}
        </p>
      )}

      <div className="livreur-kpi-grid" style={{ marginTop: 28 }}>
        <Link to="/livreur/offres" className="livreur-kpi livreur-kpi--link" style={{ textDecoration: 'none', color: 'inherit' }}>
          <div className="livreur-kpi-label">Offres visibles</div>
          <div className="livreur-kpi-value">{loading ? '…' : offres}</div>
          <div className="meta" style={{ marginTop: 8, fontSize: '0.82rem' }}>
            Commandes en attente d’un livreur →
          </div>
        </Link>
        <div className="livreur-kpi" style={{ borderLeftColor: '#f59e0b' }}>
          <div className="livreur-kpi-label">Mes courses en cours</div>
          <div className="livreur-kpi-value">{dash != null ? dash.livraisonsEnCours : '—'}</div>
        </div>
        <Link
          to="/livreur/historique"
          className="livreur-kpi livreur-kpi--link"
          style={{ textDecoration: 'none', color: 'inherit', borderLeftColor: '#64748b' }}
        >
          <div className="livreur-kpi-label">Livrées (total)</div>
          <div className="livreur-kpi-value">{dash != null ? dash.livraisonsLivrees : '—'}</div>
          <div className="meta" style={{ marginTop: 8, fontSize: '0.82rem' }}>
            Détail dans l’historique →
          </div>
        </Link>
        <div className="livreur-kpi" style={{ borderLeftColor: '#a78bfa' }}>
          <div className="livreur-kpi-label">Livrées moto</div>
          <div className="livreur-kpi-value">{dash != null ? dash.livraisonsLivreesMoto : '—'}</div>
        </div>
        <div className="livreur-kpi" style={{ borderLeftColor: '#38bdf8' }}>
          <div className="livreur-kpi-label">Livrées véhicule</div>
          <div className="livreur-kpi-value">{dash != null ? dash.livraisonsLivreesVehicule : '—'}</div>
        </div>
      </div>

      <div className="livreur-panel" style={{ marginTop: 28 }}>
        <h2 className="livreur-panel-title">Règles rapides</h2>
        <ul className="meta" style={{ margin: 0, paddingLeft: 20, lineHeight: 1.7 }}>
          <li>
            <strong>Accepter</strong> : la course vous est attribuée ; les autres livreurs ne peuvent plus la prendre.
          </li>
          <li>
            <strong>Ignorer</strong> : elle disparaît de votre liste seulement ; un collègue peut toujours l’accepter.
          </li>
          <li>
            Les <strong>montants</strong> ne vous sont pas affichés. Ouvrez l’itinéraire Maps depuis « Mes courses » une fois la
            course acceptée.
          </li>
          <li>
            La livraison se termine en <strong>scannant le QR</strong> présenté par le client (pas de bouton « livrée » sans scan).
          </li>
        </ul>
      </div>
    </div>
  )
}
