import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { ExternalLink, History, MapPin, Package, RefreshCw, Route } from 'lucide-react'
import { fetchMesLivraisonsLivreur } from '../../services/livreurApi'
import type { LivraisonLivreurDtoJson } from '../../types/backend'
import { dateFromDto } from '../../utils/dateFromDto'

const POLL_MS = 10_000

function fmtWhen(iso?: string | null): string {
  if (!iso) return '—'
  try {
    return new Date(dateFromDto(iso)).toLocaleString('fr-FR', {
      dateStyle: 'short',
      timeStyle: 'short',
    })
  } catch {
    return iso
  }
}

function statutLabel(s: string): string {
  switch (s) {
    case 'EN_COURS':
      return 'En cours'
    case 'LIVREE':
      return 'Livrée'
    case 'ANNULEE':
      return 'Annulée'
    default:
      return s
  }
}

function CourseDetailCard({ c, variant }: { c: LivraisonLivreurDtoJson; variant: 'encours' | 'terminee' }) {
  return (
    <li
      className={`livreur-run-card${variant === 'encours' ? ' livreur-run-card--active' : ''}`}
      style={{ flexDirection: 'column', alignItems: 'stretch', gap: 12 }}
    >
      <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', gap: 12 }}>
        <div style={{ flex: '1 1 220px', minWidth: 0 }}>
          <strong style={{ fontSize: '1.02rem' }}>{c.articleLibelle || `Livraison #${c.idlivraison}`}</strong>
          <div className="livreur-run-meta" style={{ marginTop: 6 }}>
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
              <Package size={14} aria-hidden />
              Commande #{c.idtransaction ?? '—'} · Livraison #{c.idlivraison}
            </span>
            <br />
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4, marginTop: 4 }}>
              <MapPin size={14} aria-hidden />
              {c.acheteurVille || '—'} · {c.acheteurEmail || '—'}
            </span>
            <br />
            <span style={{ marginTop: 4, display: 'inline-block' }}>
              Vendeur : <strong>{c.vendeurEmail || '—'}</strong>
            </span>
            <br />
            Engin : <strong>{c.typeEnginUtilise || '—'}</strong>
            {variant === 'terminee' && (
              <>
                <br />
                Statut : <strong>{statutLabel(c.statut)}</strong>
              </>
            )}
          </div>
        </div>
        <div style={{ fontSize: '0.82rem', color: 'var(--muted)', minWidth: 200 }}>
          <div>Créée : {fmtWhen(c.datecreation)}</div>
          <div>Prise en charge : {fmtWhen(c.datePriseEnCharge)}</div>
          {variant === 'terminee' && <div>Clôturée : {fmtWhen(c.dateLivraison)}</div>}
        </div>
      </div>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, alignItems: 'center' }}>
        {c.lieuDepotCarteUrl && (
          <a
            href={c.lieuDepotCarteUrl}
            target="_blank"
            rel="noreferrer"
            style={{ display: 'inline-flex', alignItems: 'center', gap: 6, color: 'var(--link)', fontSize: '0.88rem' }}
          >
            <ExternalLink size={14} aria-hidden />
            Lieu de dépôt client (Maps)
          </a>
        )}
        {variant === 'encours' && c.navigation?.itineraireComplet && (
          <a
            href={c.navigation.itineraireComplet}
            target="_blank"
            rel="noreferrer"
            style={{ display: 'inline-flex', alignItems: 'center', gap: 6, color: 'var(--link)', fontSize: '0.88rem' }}
          >
            <ExternalLink size={14} aria-hidden />
            Itinéraire complet
          </a>
        )}
        {variant === 'encours' && (
          <Link to="/livreur/courses" style={{ fontSize: '0.88rem', fontWeight: 600 }}>
            → Scanner le QR &amp; terminer (Mes courses)
          </Link>
        )}
      </div>
    </li>
  )
}

export default function LivreurHistoriquePage() {
  const location = useLocation()
  const [data, setData] = useState<{ enCours: LivraisonLivreurDtoJson[]; terminees: LivraisonLivreurDtoJson[]; limite: number } | null>(null)
  const [err, setErr] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    setErr(null)
    try {
      const r = await fetchMesLivraisonsLivreur()
      setData({ enCours: r.enCours ?? [], terminees: r.terminees ?? [], limite: r.limiteChargee ?? 0 })
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'Erreur')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load()
    const t = window.setInterval(() => void load(), POLL_MS)
    const onVis = () => {
      if (document.visibilityState === 'visible') void load()
    }
    document.addEventListener('visibilitychange', onVis)
    return () => {
      window.clearInterval(t)
      document.removeEventListener('visibilitychange', onVis)
    }
  }, [load, location.pathname])

  return (
    <div className="container" style={{ paddingTop: 28, paddingBottom: 48, maxWidth: 960 }}>
      <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
        <div>
          <h1 style={{ margin: '0 0 6px', fontSize: '1.55rem', display: 'flex', alignItems: 'center', gap: 10 }}>
            <History size={26} strokeWidth={2} aria-hidden />
            Historique des livraisons
          </h1>
          <p className="meta" style={{ margin: 0, maxWidth: 560 }}>
            En cours, livrées et annulées. Mise à jour automatique et à chaque retour sur cette page.
          </p>
        </div>
        <button type="button" className="livreur-btn-ghost" onClick={() => void load()} disabled={loading}>
          <RefreshCw size={16} aria-hidden />
          Actualiser
        </button>
      </div>

      {err && (
        <p role="alert" style={{ color: 'var(--danger)', marginTop: 20 }}>
          {err}
        </p>
      )}

      {data && data.limite > 0 && (data.enCours.length + data.terminees.length >= data.limite) && (
        <p className="meta" style={{ marginTop: 16, marginBottom: 0 }}>
          Affichage limité aux {data.limite} livraisons les plus récentes. Contactez l’administration pour un export
          complet si besoin.
        </p>
      )}

      <section style={{ marginTop: 28 }}>
        <h2 className="livreur-panel-title" style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: '1.15rem' }}>
          <Route size={22} aria-hidden />
          En cours ({data?.enCours.length ?? (loading ? '…' : 0)})
        </h2>
        {!loading && (data?.enCours.length ?? 0) === 0 ? (
          <div className="livreur-empty" style={{ marginTop: 12 }}>
            Aucune course active. Les offres à saisir sont sous « Offres à saisir ».
          </div>
        ) : (
          <ul className="livreur-run-list" style={{ marginTop: 16 }}>
            {(data?.enCours ?? []).map((c) => (
              <CourseDetailCard key={c.idlivraison} c={c} variant="encours" />
            ))}
          </ul>
        )}
      </section>

      <section style={{ marginTop: 36 }}>
        <h2 className="livreur-panel-title" style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: '1.15rem' }}>
          <History size={22} aria-hidden />
          Terminées — livrées &amp; annulées ({data?.terminees.length ?? (loading ? '…' : 0)})
        </h2>
        {!loading && (data?.terminees.length ?? 0) === 0 ? (
          <div className="livreur-empty" style={{ marginTop: 12 }}>
            Aucune livraison terminée enregistrée pour votre compte.
          </div>
        ) : (
          <ul className="livreur-run-list" style={{ marginTop: 16 }}>
            {(data?.terminees ?? []).map((c) => (
              <CourseDetailCard key={c.idlivraison} c={c} variant="terminee" />
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
