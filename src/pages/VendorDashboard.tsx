import { useState, useEffect, useCallback, lazy, Suspense } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import type { LucideIcon } from 'lucide-react'
import { ArrowLeft, BarChart3, CreditCard, MessageSquare, Package, Plus } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { iconLg, iconSm } from '../components/ui/iconProps'
import VendorChat from '../components/VendorChat'
import {
  fetchMySales,
  fetchVendorSalesDashboard,
  downloadPaymentReceipt,
  type PaymentResultDto,
  type VendorSalesDashboard,
} from '../services/paymentApi'
import { dateFromDto } from '../utils/dateFromDto'

const VendorSalesCharts = lazy(() => import('../components/vendor/VendorSalesCharts'))

const MOYEN_LABEL: Record<string, string> = {
  PAYDUNYA: 'PayDunya',
  ORANGE_MONEY: 'Orange Money',
  MOOV_MONEY: 'Moov Money',
  VIREMENT: 'Virement',
  ESPECES: 'Espèces',
}

type TabId = 'dashboard' | 'messages' | 'products' | 'sales'

function KpiCard({
  label,
  value,
  hint,
  accent,
}: {
  label: string
  value: string
  hint?: string
  accent?: string
}) {
  return (
    <div
      style={{
        background: 'var(--input-bg)',
        border: '1px solid var(--border)',
        borderRadius: 10,
        padding: '16px 18px',
        borderLeft: accent ? `4px solid ${accent}` : undefined,
      }}
    >
      <div style={{ fontSize: '0.78em', color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
        {label}
      </div>
      <div style={{ fontSize: '1.45em', fontWeight: 700, color: 'var(--text)', marginTop: 6 }}>{value}</div>
      {hint && (
        <div style={{ fontSize: '0.8em', color: 'var(--text-faint)', marginTop: 8 }}>{hint}</div>
      )}
    </div>
  )
}

export default function VendorDashboard() {
  const { user, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState<TabId>('dashboard')
  const [sales, setSales] = useState<PaymentResultDto[]>([])
  const [salesErr, setSalesErr] = useState<string | null>(null)
  const [salesLoading, setSalesLoading] = useState(false)
  const [dash, setDash] = useState<VendorSalesDashboard | null>(null)
  const [dashErr, setDashErr] = useState<string | null>(null)
  const [dashLoading, setDashLoading] = useState(false)

  const loadSales = useCallback(async () => {
    setSalesLoading(true)
    setSalesErr(null)
    try {
      const list = await fetchMySales()
      setSales(list)
    } catch {
      setSalesErr('Impossible de charger vos ventes.')
    } finally {
      setSalesLoading(false)
    }
  }, [])

  const loadDashboard = useCallback(async () => {
    setDashLoading(true)
    setDashErr(null)
    try {
      const d = await fetchVendorSalesDashboard()
      setDash(d)
    } catch {
      setDashErr('Impossible de charger les statistiques.')
    } finally {
      setDashLoading(false)
    }
  }, [])

  useEffect(() => {
    if (activeTab === 'sales') {
      void loadSales()
    }
    if (activeTab === 'dashboard') {
      void loadDashboard()
    }
  }, [activeTab, loadSales, loadDashboard])

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/auth?mode=login')
      return
    }

    if (user?.accountType !== 'seller') {
      navigate('/')
    }
  }, [isAuthenticated, user, navigate])

  if (!user || user.accountType !== 'seller') return null

  const handleReceipt = async (id: number) => {
    try {
      const blob = await downloadPaymentReceipt(id)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `recu-ecomarket-${id}.pdf`
      a.click()
      URL.revokeObjectURL(url)
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Téléchargement impossible')
    }
  }

  const tabBtn = (id: TabId, label: string, Icon: LucideIcon) => (
    <button
      type="button"
      onClick={() => setActiveTab(id)}
      style={{
        background: 'transparent',
        border: 'none',
        borderBottom: activeTab === id ? '2px solid var(--accent)' : '2px solid transparent',
        padding: '12px 20px',
        cursor: 'pointer',
        color: activeTab === id ? 'var(--accent)' : 'var(--muted)',
        fontWeight: activeTab === id ? 600 : 400,
        transition: 'all 0.2s',
        display: 'inline-flex',
        alignItems: 'center',
        gap: 8,
      }}
    >
      <Icon {...iconSm} aria-hidden />
      {label}
    </button>
  )

  return (
    <div className="container" style={{ paddingTop: 20, paddingBottom: 40 }}>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 24,
          flexWrap: 'wrap',
          gap: 12,
        }}
      >
        <h1 style={{ margin: 0 }}>Tableau de bord vendeur</h1>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, alignItems: 'center' }}>
          <Link
            to="/vendor/certification"
            style={{
              padding: '8px 16px',
              borderRadius: 10,
              border: '1px solid var(--accent)',
              color: 'var(--accent)',
              fontWeight: 600,
              textDecoration: 'none',
            }}
          >
            Vendeur certifié
          </Link>
          <button
            type="button"
            onClick={() => navigate('/')}
            style={{
              background: 'transparent',
              border: '1px solid var(--border)',
              padding: '8px 16px',
              borderRadius: 10,
              display: 'inline-flex',
              alignItems: 'center',
              gap: 8,
              color: 'var(--text)',
              cursor: 'pointer',
            }}
          >
            <ArrowLeft {...iconSm} aria-hidden />
            Retour
          </button>
        </div>
      </div>

      <div
        style={{
          display: 'flex',
          gap: 12,
          marginBottom: 24,
          borderBottom: '1px solid var(--border)',
          flexWrap: 'wrap',
        }}
      >
        {tabBtn('dashboard', 'Performance', BarChart3)}
        {tabBtn('messages', 'Messages clients', MessageSquare)}
        {tabBtn('products', 'Mes produits', Package)}
        {tabBtn('sales', 'Historique ventes', CreditCard)}
      </div>

      {activeTab === 'dashboard' && (
        <div>
          <div
            style={{
              background: 'var(--surface)',
              border: '1px solid var(--border)',
              borderRadius: 8,
              padding: 20,
              marginBottom: 24,
            }}
          >
            <h2 style={{ margin: '0 0 8px', fontSize: '1.15em' }}>Indicateurs clés (KPI)</h2>
            <p style={{ margin: 0, color: 'var(--muted)', fontSize: '0.9em' }}>
              Données calculées à partir de vos paiements enregistrés. Les graphiques couvrent les{' '}
              <strong>90 derniers jours</strong> (jours sans vente à 0).
            </p>
          </div>

          {dashLoading && <p className="meta">Chargement des statistiques…</p>}
          {dashErr && <p style={{ color: '#f87171' }}>{dashErr}</p>}

          {!dashLoading && dash && (
            <>
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                  gap: 14,
                  marginBottom: 28,
                }}
              >
                <KpiCard
                  label="Chiffre d’affaires total"
                  value={`${dash.revenueTotal.toLocaleString('fr-FR')} FCFA`}
                  accent="#2a9d8f"
                />
                <KpiCard
                  label="Commandes (tout temps)"
                  value={String(dash.transactionCount)}
                  hint={`${dash.totalQuantitySold.toLocaleString('fr-FR')} unités vendues`}
                />
                <KpiCard
                  label="Panier moyen"
                  value={`${dash.averageOrderValue.toLocaleString('fr-FR')} FCFA`}
                />
                <KpiCard
                  label="CA — 7 derniers jours"
                  value={`${dash.revenueLast7Days.toLocaleString('fr-FR')} FCFA`}
                  hint={`${dash.ordersLast7Days} commande(s)`}
                  accent="#f4a261"
                />
                <KpiCard
                  label="CA — 30 derniers jours"
                  value={`${dash.revenueLast30Days.toLocaleString('fr-FR')} FCFA`}
                  hint={`${dash.ordersLast30Days} commande(s)`}
                  accent="#6366f1"
                />
              </div>

              <Suspense
                fallback={
                  <p className="meta" style={{ padding: 32 }}>
                    Chargement des graphiques…
                  </p>
                }
              >
                <VendorSalesCharts data={dash} />
              </Suspense>
            </>
          )}
        </div>
      )}

      {activeTab === 'messages' && (
        <div>
          <div
            style={{
              background: 'var(--surface)',
              border: '1px solid var(--border)',
              borderRadius: 8,
              padding: 20,
              marginBottom: 20,
            }}
          >
            <h3 style={{ margin: '0 0 8px 0' }}>Gérez vos conversations</h3>
            <p style={{ margin: 0, color: 'var(--muted)', fontSize: '0.9em' }}>
              Négociation structurée : jusqu’à deux contre-propositions, puis prix final si besoin. Mise à jour
              automatique des messages.
            </p>
          </div>
          <VendorChat />
        </div>
      )}

      {activeTab === 'products' && (
        <div
          style={{
            background: 'var(--surface)',
            border: '1px solid var(--border)',
            borderRadius: 8,
            padding: 40,
            textAlign: 'center',
          }}
        >
          <div style={{ marginBottom: 16, color: 'var(--accent)', display: 'flex', justifyContent: 'center' }}>
            <Package {...iconLg} size={48} strokeWidth={1.4} aria-hidden />
          </div>
          <h3 style={{ marginBottom: 12 }}>Gestion des produits</h3>
          <p style={{ color: 'var(--muted)', marginBottom: 20 }}>
            Publiez et mettez à jour vos annonces.
          </p>
          <button
            type="button"
            onClick={() => navigate('/vendor/add-product')}
            style={{
              background: 'var(--accent)',
              border: 'none',
              padding: '12px 24px',
              fontWeight: 600,
              cursor: 'pointer',
              borderRadius: 12,
              color: '#fff',
              display: 'inline-flex',
              alignItems: 'center',
              gap: 8,
            }}
          >
            <Plus {...iconSm} aria-hidden />
            Ajouter un produit
          </button>
        </div>
      )}

      {activeTab === 'sales' && (
        <div
          style={{
            background: 'var(--surface)',
            border: '1px solid var(--border)',
            borderRadius: 8,
            padding: 24,
          }}
        >
          <h3 style={{ margin: '0 0 12px 0' }}>Historique des ventes</h3>
          <p style={{ margin: '0 0 16px 0', color: 'var(--muted)', fontSize: '0.9em' }}>
            Détail de chaque paiement enregistré pour vos articles.
          </p>
          {salesLoading && <p className="meta">Chargement…</p>}
          {salesErr && <p style={{ color: '#f87171' }}>{salesErr}</p>}
          {!salesLoading && !salesErr && sales.length === 0 && (
            <p className="meta">Aucune vente enregistrée pour le moment.</p>
          )}
          {!salesLoading && sales.length > 0 && (
            <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
              {sales.map((p) => (
                <li
                  key={p.idtransaction}
                  style={{
                    padding: 12,
                    marginBottom: 8,
                    border: '1px solid var(--border)',
                    borderRadius: 8,
                    display: 'flex',
                    justifyContent: 'space-between',
                    flexWrap: 'wrap',
                    gap: 8,
                  }}
                >
                  <div>
                    <strong>{p.articleLibelle || `Article #${p.idArticle}`}</strong>
                    <div className="meta" style={{ fontSize: '0.85em' }}>
                      {p.quantite} × {p.prixUnitaire} FCFA = {p.montantTotal} FCFA (frais {p.frais}) —{' '}
                      {MOYEN_LABEL[p.moyenPaiement] || p.moyenPaiement}
                    </div>
                    {p.vendorPickupCode && (
                      <div
                        style={{
                          marginTop: 8,
                          padding: '8px 10px',
                          background: 'var(--input-bg)',
                          borderRadius: 8,
                          fontSize: '0.82em',
                          border: '1px solid var(--border)',
                        }}
                      >
                        <div style={{ color: 'var(--muted)', marginBottom: 4 }}>Code retrait livreur (à communiquer)</div>
                        <div style={{ fontWeight: 700, letterSpacing: '0.12em', fontFamily: 'monospace' }}>
                          {p.vendorPickupCode}
                        </div>
                        {p.vendorPackedReferenceBase64 && (
                          <button
                            type="button"
                            className="link-button"
                            style={{ marginTop: 6, fontSize: '0.78em' }}
                            onClick={() =>
                              void navigator.clipboard.writeText(p.vendorPackedReferenceBase64!).then(
                                () => alert('Référence encodée copiée.'),
                                () => alert('Copie impossible')
                              )
                            }
                          >
                            Copier la référence encodée (détails commande)
                          </button>
                        )}
                      </div>
                    )}
                    <div className="meta" style={{ fontSize: '0.8em', marginTop: 8 }}>
                      {p.datecreation
                        ? new Date(dateFromDto(p.datecreation)).toLocaleString('fr-FR')
                        : ''}
                    </div>
                  </div>
                  <button type="button" onClick={() => void handleReceipt(p.idtransaction)}>
                    Reçu PDF
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  )
}
