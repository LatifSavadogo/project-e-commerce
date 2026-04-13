import { useAuth, isStaffRole } from '../contexts/AuthContext'
import { useComplaints } from '../contexts/ComplaintContext'
import { useNavigate, Link } from 'react-router-dom'
import { useEffect, useState, useCallback, useMemo, lazy, Suspense } from 'react'
import { Activity, CreditCard, Mail, Package, Users } from 'lucide-react'
import { fetchAdminDashboardStats, type AdminDashboardStats } from '../services/adminDashboardApi'
import { fetchAdminPayments } from '../services/adminPaymentApi'
import type { PaymentResultDto } from '../services/paymentApi'

const AdminDashboardCharts = lazy(() => import('../components/admin/AdminDashboardCharts'))

const PAY_LABELS: Record<string, string> = {
  ORANGE_MONEY: 'Orange Money',
  MOOV_MONEY: 'Moov Money',
  VIREMENT: 'Virement',
  ESPECES: 'Espèces',
}

export default function AdminDashboard() {
  const { user, getAllUsers, refreshAdminUsers } = useAuth()
  const { complaints, markAsRead, refreshComplaints } = useComplaints()
  const navigate = useNavigate()
  const [isAuthorized, setIsAuthorized] = useState(false)
  const [stats, setStats] = useState<AdminDashboardStats | null>(null)
  const [paymentsList, setPaymentsList] = useState<PaymentResultDto[]>([])
  const [statsErr, setStatsErr] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [lastRefresh, setLastRefresh] = useState<Date | null>(null)

  const loadStats = useCallback(async () => {
    setLoading(true)
    try {
      const [s, pay] = await Promise.all([
        fetchAdminDashboardStats(),
        fetchAdminPayments().catch(() => [] as PaymentResultDto[]),
      ])
      setStats(s)
      setPaymentsList(pay)
      setStatsErr(null)
      setLastRefresh(new Date())
      await refreshComplaints()
      await refreshAdminUsers()
    } catch {
      setStatsErr('Impossible de charger les statistiques serveur.')
    } finally {
      setLoading(false)
    }
  }, [refreshComplaints, refreshAdminUsers])

  useEffect(() => {
    if (user && isStaffRole(user)) {
      setIsAuthorized(true)
      void loadStats()
    } else if (user) {
      alert('Accès refusé: Cette page est réservée aux administrateurs')
      navigate('/')
    }
  }, [user, navigate, loadStats])

  useEffect(() => {
    if (isAuthorized) {
      const interval = setInterval(() => {
        void loadStats()
      }, 10000)
      return () => clearInterval(interval)
    }
  }, [isAuthorized, loadStats])

  const complaintsPreview = useMemo(() => complaints.slice(0, 5), [complaints])

  const overviewData = useMemo(() => {
    if (!stats) return []
    return [
      { name: 'Sessions', value: stats.sessionsActives },
      { name: 'Utilisateurs', value: stats.usersTotal },
      { name: 'Articles', value: stats.articlesTotal },
      { name: 'Paiements', value: stats.paymentsTotal },
      { name: 'Plaintes non lues', value: stats.complaintsUnread },
    ]
  }, [stats])

  const rolesData = useMemo(() => {
    const users = getAllUsers()
    const m = new Map<string, number>()
    for (const u of users) {
      const key = (u.librole || '—').toUpperCase()
      m.set(key, (m.get(key) || 0) + 1)
    }
    return Array.from(m.entries())
      .map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value)
  }, [getAllUsers])

  const complaintsChartData = useMemo(() => {
    const unread = complaints.filter((c) => !c.read).length
    const read = complaints.length - unread
    return [
      { name: 'Non lues', value: unread },
      { name: 'Lues', value: read },
    ]
  }, [complaints])

  const paymentsChartData = useMemo(() => {
    const m = new Map<string, number>()
    for (const p of paymentsList) {
      const label = PAY_LABELS[p.moyenPaiement] || p.moyenPaiement
      m.set(label, (m.get(label) || 0) + 1)
    }
    return Array.from(m.entries())
      .map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value)
  }, [paymentsList])

  if (!user || !isAuthorized) {
    return (
      <div className="container" style={{ paddingTop: 40, textAlign: 'center' }}>
        <p>Vérification des autorisations...</p>
      </div>
    )
  }

  const refreshLabel = lastRefresh
    ? lastRefresh.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    : '—'

  return (
    <div className="container admin-dashboard-page" style={{ paddingTop: 20, paddingBottom: 48 }}>
      <header className="admin-dash-header">
        <div>
          <h1 style={{ margin: 0, fontSize: '1.65rem', fontWeight: 700 }}>Tableau de bord</h1>
          <p style={{ color: 'var(--muted)', marginTop: 8, marginBottom: 0 }}>
            Vue opérationnelle Ecomarket — bonjour {user.prenoms} {user.nom}
          </p>
        </div>
        <div className="admin-dash-meta">
          <span className="admin-dash-live">
            <span className="admin-dash-live-dot" aria-hidden />
            Données rafraîchies toutes les 10 s
          </span>
          <span className="meta">Dernière synchro : {refreshLabel}</span>
          <button type="button" className="button-primary" disabled={loading} onClick={() => void loadStats()}>
            {loading ? 'Mise à jour…' : 'Actualiser'}
          </button>
        </div>
      </header>

      {statsErr && <p style={{ color: '#f87171', marginBottom: 16 }}>{statsErr}</p>}

      <section className="admin-kpi-grid" aria-label="Indicateurs clés">
        <button
          type="button"
          className="admin-kpi-card"
          onClick={() => navigate('/admin/users')}
        >
          <div className="admin-kpi-icon" aria-hidden>
            <Activity size={26} strokeWidth={1.55} />
          </div>
          <div className="admin-kpi-label">Sessions actives</div>
          <div className="admin-kpi-value">{stats?.sessionsActives ?? '—'}</div>
          <div className="admin-kpi-hint">Connexions suivies côté serveur</div>
        </button>

        <button type="button" className="admin-kpi-card" onClick={() => navigate('/admin/users')}>
          <div className="admin-kpi-icon" aria-hidden>
            <Users size={26} strokeWidth={1.55} />
          </div>
          <div className="admin-kpi-label">Utilisateurs</div>
          <div className="admin-kpi-value">{stats?.usersTotal ?? '—'}</div>
          <div className="admin-kpi-hint">Comptes enregistrés</div>
        </button>

        <button type="button" className="admin-kpi-card" onClick={() => navigate('/admin/articles')}>
          <div className="admin-kpi-icon" aria-hidden>
            <Package size={26} strokeWidth={1.55} />
          </div>
          <div className="admin-kpi-label">Articles</div>
          <div className="admin-kpi-value">{stats?.articlesTotal ?? '—'}</div>
          <div className="admin-kpi-hint">Toutes annonces (y compris bloquées)</div>
        </button>

        <button type="button" className="admin-kpi-card" onClick={() => navigate('/admin/payments')}>
          <div className="admin-kpi-icon" aria-hidden>
            <CreditCard size={26} strokeWidth={1.55} />
          </div>
          <div className="admin-kpi-label">Paiements</div>
          <div className="admin-kpi-value">{stats?.paymentsTotal ?? '—'}</div>
          <div className="admin-kpi-hint">Transactions enregistrées</div>
        </button>

        <button type="button" className="admin-kpi-card" onClick={() => navigate('/admin/complaints')}>
          <div className="admin-kpi-icon" aria-hidden>
            <Mail size={26} strokeWidth={1.55} />
          </div>
          <div className="admin-kpi-label">Plaintes non lues</div>
          <div
            className={`admin-kpi-value ${(stats?.complaintsUnread ?? 0) > 0 ? 'admin-kpi-value--alert' : ''}`}
          >
            {stats?.complaintsUnread ?? '—'}
          </div>
          <div className="admin-kpi-hint">À traiter en priorité</div>
        </button>
      </section>

      <Suspense
        fallback={
          <div className="admin-chart-panel" style={{ marginBottom: 28, minHeight: 120 }}>
            <p className="meta" style={{ margin: 0 }}>
              Chargement des graphiques…
            </p>
          </div>
        }
      >
        <AdminDashboardCharts
          overview={overviewData}
          roles={rolesData}
          complaints={complaintsChartData}
          payments={paymentsChartData}
        />
      </Suspense>

      <section className="admin-activity-card">
        <div className="admin-activity-head">
          <h2 style={{ margin: 0, fontSize: '1.1rem' }}>Activité récente — plaintes</h2>
          <Link to="/admin/complaints" className="link-button" style={{ padding: '8px 14px' }}>
            Gestion complète
          </Link>
        </div>

        {complaints.length === 0 ? (
          <p style={{ color: 'var(--muted)', textAlign: 'center', padding: '32px 0' }}>Aucune plainte</p>
        ) : (
          <div style={{ display: 'grid', gap: 12 }}>
            {complaintsPreview.map((complaint) => (
              <div
                key={complaint.id}
                style={{
                  background: complaint.read ? 'var(--input-bg)' : '#151d28',
                  border: `1px solid ${complaint.read ? 'var(--border)' : 'rgba(42, 157, 143, 0.45)'}`,
                  borderRadius: 10,
                  padding: 14,
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap' }}>
                  <div>
                    <div style={{ fontWeight: 600 }}>
                      {complaint.userName}
                      {!complaint.read && (
                        <span
                          style={{
                            marginLeft: 8,
                            background: '#da3633',
                            color: 'white',
                            padding: '2px 8px',
                            borderRadius: 999,
                            fontSize: '0.7rem',
                            fontWeight: 700,
                          }}
                        >
                          NOUVEAU
                        </span>
                      )}
                    </div>
                    <div style={{ fontSize: '0.85em', color: 'var(--muted)' }}>{complaint.userEmail}</div>
                  </div>
                  <div style={{ fontSize: '0.8em', color: 'var(--muted)' }}>
                    {new Date(complaint.createdAt).toLocaleString('fr-FR', {
                      day: 'numeric',
                      month: 'short',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </div>
                </div>
                {complaint.titre && <p style={{ fontWeight: 600, marginBottom: 6 }}>{complaint.titre}</p>}
                <p style={{ margin: 0, color: 'var(--text-secondary)', fontSize: '0.9em', lineHeight: 1.5 }}>{complaint.message}</p>
                {!complaint.read && (
                  <button
                    type="button"
                    onClick={() => void markAsRead(complaint.id)}
                    style={{
                      marginTop: 10,
                      background: 'transparent',
                      border: '1px solid var(--admin-accent)',
                      color: 'var(--admin-accent)',
                      padding: '6px 12px',
                      fontSize: '0.85em',
                    }}
                  >
                    Marquer comme lu
                  </button>
                )}
              </div>
            ))}
            {complaints.length > 5 && (
              <p className="meta" style={{ textAlign: 'center', marginBottom: 0 }}>
                + {complaints.length - 5} autre(s) — <Link to="/admin/complaints">voir tout</Link>
              </p>
            )}
          </div>
        )}
      </section>
    </div>
  )
}
