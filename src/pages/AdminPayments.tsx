import { useAuth, isStaffRole } from '../contexts/AuthContext'
import { useNavigate, Link } from 'react-router-dom'
import { useEffect, useState, useCallback } from 'react'
import { fetchAdminPayments } from '../services/adminPaymentApi'
import type { PaymentResultDto } from '../services/paymentApi'
import { dateFromDto } from '../utils/dateFromDto'
import AdminLivraisonSuiviModal from '../components/AdminLivraisonSuiviModal'

const MOYEN: Record<string, string> = {
  ORANGE_MONEY: 'Orange Money',
  MOOV_MONEY: 'Moov Money',
  VIREMENT: 'Virement',
  ESPECES: 'Espèces',
}

export default function AdminPayments() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [rows, setRows] = useState<PaymentResultDto[]>([])
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState<string | null>(null)
  const [suiviLivraisonId, setSuiviLivraisonId] = useState<number | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setErr(null)
    try {
      setRows(await fetchAdminPayments())
    } catch {
      setErr('Impossible de charger les paiements.')
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    if (!user || !isStaffRole(user)) {
      navigate('/')
      return
    }
    void load()
  }, [user, navigate, load])

  if (!user || !isStaffRole(user)) return null

  return (
    <div className="container" style={{ paddingTop: 20, paddingBottom: 40 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
        <h1 style={{ margin: 0 }}>Paiements</h1>
        <button type="button" onClick={() => void load()} disabled={loading}>
          {loading ? '…' : 'Rafraîchir'}
        </button>
      </div>
      <p className="meta">Historique complet des transactions enregistrées (tous utilisateurs).</p>
      {err && <p style={{ color: '#f87171' }}>{err}</p>}
      {loading && <p className="meta">Chargement…</p>}
      {!loading && rows.length === 0 && (
        <p style={{ color: 'var(--muted)', padding: '32px 0' }}>Aucun paiement enregistré.</p>
      )}
      {!loading && rows.length > 0 && (
        <div style={{ overflowX: 'auto', marginTop: 16 }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border)', textAlign: 'left' }}>
                <th style={{ padding: '10px 8px' }}>ID</th>
                <th style={{ padding: '10px 8px' }}>Article</th>
                <th style={{ padding: '10px 8px' }}>Qté</th>
                <th style={{ padding: '10px 8px' }}>Total</th>
                <th style={{ padding: '10px 8px' }}>Moyen</th>
                <th style={{ padding: '10px 8px' }}>Date</th>
                <th style={{ padding: '10px 8px' }}>Livraison</th>
                <th style={{ padding: '10px 8px' }}>Fiche</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((p) => (
                <tr key={p.idtransaction} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                  <td style={{ padding: '10px 8px', fontFamily: 'monospace' }}>{p.idtransaction}</td>
                  <td style={{ padding: '10px 8px' }}>
                    {p.articleLibelle || `#${p.idArticle}`}
                  </td>
                  <td style={{ padding: '10px 8px' }}>{p.quantite}</td>
                  <td style={{ padding: '10px 8px' }}>
                    {p.montantTotal} FCFA <span className="meta">(frais {p.frais})</span>
                  </td>
                  <td style={{ padding: '10px 8px' }}>{MOYEN[p.moyenPaiement] || p.moyenPaiement}</td>
                  <td style={{ padding: '10px 8px', whiteSpace: 'nowrap' }}>
                    {p.datecreation
                      ? new Date(dateFromDto(p.datecreation)).toLocaleString('fr-FR')
                      : '—'}
                  </td>
                  <td style={{ padding: '10px 8px', fontSize: '0.85rem' }}>
                    {p.idLivraison != null ? (
                      <button
                        type="button"
                        className="link-button"
                        onClick={() => setSuiviLivraisonId(p.idLivraison!)}
                        style={{ color: '#5eead4' }}
                      >
                        #{p.idLivraison}
                        {p.livraisonStatut ? ` (${p.livraisonStatut})` : ''}
                      </button>
                    ) : (
                      '—'
                    )}
                  </td>
                  <td style={{ padding: '10px 8px' }}>
                    <Link to={`/product/${p.idArticle}`} style={{ color: '#5eead4' }}>
                      Voir annonce
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <AdminLivraisonSuiviModal idlivraison={suiviLivraisonId} onClose={() => setSuiviLivraisonId(null)} />
    </div>
  )
}
