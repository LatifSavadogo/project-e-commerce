import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Package, RefreshCw } from 'lucide-react'
import { useAuth, isStaffRole } from '../contexts/AuthContext'
import { fetchAdminLivraisons } from '../services/adminLivraisonApi'
import type { AdminLivraisonListDtoJson } from '../types/backend'
import AdminLivraisonSuiviModal from '../components/AdminLivraisonSuiviModal'
import { iconSm } from '../components/ui/iconProps'

const STATUT_LABEL: Record<string, string> = {
  EN_ATTENTE: 'En attente livreur',
  EN_COURS: 'En cours',
  LIVREE: 'Livrée',
  ANNULEE: 'Annulée',
}

export default function AdminLivraisonsPage() {
  const { user } = useAuth()
  const [rows, setRows] = useState<AdminLivraisonListDtoJson[]>([])
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState<string | null>(null)
  const [suiviId, setSuiviId] = useState<number | null>(null)

  const load = useCallback(async () => {
    setErr(null)
    setLoading(true)
    try {
      const list = await fetchAdminLivraisons()
      setRows(list)
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'Impossible de charger les livraisons.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    if (user && isStaffRole(user)) void load()
  }, [user, load])

  if (!user || !isStaffRole(user)) {
    return (
      <div className="admin-page container">
        <p>Accès réservé au personnel.</p>
        <Link to="/auth">Connexion</Link>
      </div>
    )
  }

  return (
    <div className="admin-page container" style={{ paddingTop: 24, paddingBottom: 48 }}>
      <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <Package {...iconSm} aria-hidden />
          <h1 style={{ margin: 0, fontSize: '1.35rem' }}>Livraisons</h1>
        </div>
        <button type="button" className="button-secondary" onClick={() => void load()} disabled={loading}>
          <RefreshCw size={16} style={{ marginRight: 6, verticalAlign: '-2px' }} aria-hidden />
          Actualiser
        </button>
      </div>
      <p className="meta" style={{ marginTop: 8 }}>
        Suivez chaque commande du paiement à la livraison : ouvrez le détail pour la frise, les codes vendeur et les liens
        Maps.
      </p>

      {err && (
        <p role="alert" style={{ color: 'var(--danger)', marginTop: 16 }}>
          {err}
        </p>
      )}

      {loading && <p className="meta" style={{ marginTop: 20 }}>Chargement…</p>}

      {!loading && rows.length === 0 && <p className="meta" style={{ marginTop: 20 }}>Aucune livraison en base.</p>}

      {!loading && rows.length > 0 && (
        <div style={{ marginTop: 20, overflowX: 'auto' }}>
          <table className="admin-table" style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.88rem' }}>
            <thead>
              <tr style={{ textAlign: 'left', borderBottom: '1px solid var(--border)' }}>
                <th style={{ padding: '10px 8px' }}>#</th>
                <th style={{ padding: '10px 8px' }}>Statut</th>
                <th style={{ padding: '10px 8px' }}>Article</th>
                <th style={{ padding: '10px 8px' }}>Acheteur</th>
                <th style={{ padding: '10px 8px' }}>Livreur</th>
                <th style={{ padding: '10px 8px' }}>Création</th>
                <th style={{ padding: '10px 8px' }} />
              </tr>
            </thead>
            <tbody>
              {rows.map((r) => (
                <tr key={r.idlivraison} style={{ borderBottom: '1px solid var(--border)' }}>
                  <td style={{ padding: '10px 8px' }}>{r.idlivraison}</td>
                  <td style={{ padding: '10px 8px' }}>{STATUT_LABEL[r.statut] || r.statut}</td>
                  <td style={{ padding: '10px 8px' }}>
                    {r.articleLibelle || '—'}
                    {r.quantite != null ? ` × ${r.quantite}` : ''}
                  </td>
                  <td style={{ padding: '10px 8px' }}>
                    {r.acheteurEmail || '—'}
                    {r.acheteurVille ? (
                      <>
                        <br />
                        <span className="meta">{r.acheteurVille}</span>
                      </>
                    ) : null}
                  </td>
                  <td style={{ padding: '10px 8px' }}>{r.livreurNomComplet || r.livreurEmail || '—'}</td>
                  <td style={{ padding: '10px 8px', whiteSpace: 'nowrap' }}>
                    {r.datecreation ? new Date(r.datecreation).toLocaleString('fr-FR') : '—'}
                  </td>
                  <td style={{ padding: '10px 8px' }}>
                    <button type="button" className="button-primary" style={{ padding: '6px 12px', fontSize: '0.82rem' }} onClick={() => setSuiviId(r.idlivraison)}>
                      Suivi
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <AdminLivraisonSuiviModal idlivraison={suiviId} onClose={() => setSuiviId(null)} />
    </div>
  )
}
