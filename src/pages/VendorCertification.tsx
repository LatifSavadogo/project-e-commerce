import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import {
  fetchVendorCertificationStatus,
  createPaydunyaCertificationInvoice,
  type VendorCertificationPlan,
} from '../services/paydunyaApi'
import { ApiError } from '../services/apiClient'
import { Store } from 'lucide-react'
import { iconSm } from '../components/ui/iconProps'

export default function VendorCertification() {
  const { user, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState<string | null>(null)
  const [busy, setBusy] = useState<VendorCertificationPlan | null>(null)
  const [active, setActive] = useState(false)
  const [until, setUntil] = useState<string | null>(null)
  const [monthly, setMonthly] = useState(3000)
  const [yearly, setYearly] = useState(20_000)

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/auth?mode=login')
      return
    }
    if (user?.accountType !== 'seller') {
      navigate('/profile')
      return
    }
    let c = false
    ;(async () => {
      try {
        const s = await fetchVendorCertificationStatus()
        if (c) return
        setActive(s.active)
        setUntil(s.certifieJusqua ?? null)
        setMonthly(s.monthlyPriceFcfa)
        setYearly(s.yearlyPriceFcfa)
      } catch (e) {
        if (!c) setErr(e instanceof ApiError ? e.message : 'Chargement impossible.')
      } finally {
        if (!c) setLoading(false)
      }
    })()
    return () => {
      c = true
    }
  }, [isAuthenticated, navigate, user?.accountType])

  const pay = async (plan: VendorCertificationPlan) => {
    setErr(null)
    setBusy(plan)
    try {
      const inv = await createPaydunyaCertificationInvoice(plan)
      window.location.assign(inv.checkoutUrl)
    } catch (e) {
      setErr(e instanceof ApiError ? e.message : e instanceof Error ? e.message : 'Erreur')
    } finally {
      setBusy(null)
    }
  }

  if (!user || user.accountType !== 'seller') return null

  return (
    <div className="container" style={{ paddingTop: 24, paddingBottom: 48, maxWidth: 640 }}>
      <p style={{ marginBottom: 8 }}>
        <Link to="/vendor" className="meta">
          ← Dashboard vendeur
        </Link>
      </p>
      <h1 style={{ display: 'flex', alignItems: 'center', gap: 10, marginTop: 0 }}>
        <Store {...iconSm} aria-hidden />
        Vendeur certifié
      </h1>
      <p style={{ color: 'var(--muted)', lineHeight: 1.5 }}>
        Badge visible sur vos annonces : les acheteurs identifient plus facilement un vendeur engagé. Paiement sécurisé
        via <strong>PayDunya</strong> (adapté au Burkina Faso et à la zone UEMOA). Créez un compte marchand sur{' '}
        <a href="https://paydunya.com" target="_blank" rel="noopener noreferrer">
          paydunya.com
        </a>{' '}
        puis renseignez les clés API dans les variables d’environnement du serveur.
      </p>

      {loading && <p>Chargement…</p>}
      {err && (
        <p role="alert" style={{ color: 'var(--danger, #e05252)' }}>
          {err}
        </p>
      )}

      {!loading && (
        <div
          style={{
            marginTop: 24,
            padding: 20,
            borderRadius: 12,
            border: '1px solid var(--border)',
            background: 'var(--surface)',
          }}
        >
          <p style={{ marginTop: 0 }}>
            Statut :{' '}
            <strong>{active ? 'Certification active' : 'Non certifié ou expiré'}</strong>
            {until && (
              <>
                {' '}
                — fin prévue :{' '}
                {new Date(until).toLocaleString('fr-FR', { dateStyle: 'long', timeStyle: 'short' })}
              </>
            )}
          </p>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, marginTop: 16 }}>
            <button
              type="button"
              className="button-primary"
              disabled={busy !== null}
              onClick={() => void pay('MONTHLY')}
            >
              {busy === 'MONTHLY' ? 'Redirection…' : `Forfait mensuel — ${monthly.toLocaleString('fr-FR')} FCFA`}
            </button>
            <button
              type="button"
              className="button-primary"
              disabled={busy !== null}
              onClick={() => void pay('YEARLY')}
              style={{ background: 'var(--surface)', color: 'var(--accent)', border: '1px solid var(--accent)' }}
            >
              {busy === 'YEARLY' ? 'Redirection…' : `Forfait annuel — ${yearly.toLocaleString('fr-FR')} FCFA`}
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
