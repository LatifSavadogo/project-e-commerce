import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { UserPlus } from 'lucide-react'
import { useAuth, isStaffRole } from '../contexts/AuthContext'
import {
  approveRoleUpgrade,
  rejectRoleUpgrade,
  fetchAdminRoleUpgrades,
  adminRoleUpgradeCnibUrl,
  adminRoleUpgradePhotoUrl,
} from '../services/adminRoleUpgradeApi'
import type { RoleUpgradeRequestDtoJson } from '../types/backend'

function tagClass(status: string) {
  if (status === 'PENDING') return 'admin-role-tag admin-role-tag--pending'
  if (status === 'APPROVED') return 'admin-role-tag admin-role-tag--ok'
  if (status === 'REJECTED') return 'admin-role-tag admin-role-tag--no'
  return 'admin-role-tag'
}

export default function AdminRoleUpgrades() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [rows, setRows] = useState<RoleUpgradeRequestDtoJson[]>([])
  const [filter, setFilter] = useState<string>('PENDING')
  const [err, setErr] = useState<string | null>(null)
  const [busy, setBusy] = useState<number | null>(null)
  const [rejectId, setRejectId] = useState<number | null>(null)
  const [rejectMotif, setRejectMotif] = useState('')

  const load = useCallback(async () => {
    setErr(null)
    try {
      const list = await fetchAdminRoleUpgrades(filter || undefined)
      setRows(list)
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'Erreur chargement')
      setRows([])
    }
  }, [filter])

  useEffect(() => {
    if (!user || !isStaffRole(user)) {
      navigate('/')
      return
    }
    void load()
  }, [user, navigate, load])

  const doApprove = async (id: number) => {
    setBusy(id)
    try {
      await approveRoleUpgrade(id)
      await load()
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Refus API')
    } finally {
      setBusy(null)
    }
  }

  const doReject = async (id: number) => {
    setBusy(id)
    try {
      await rejectRoleUpgrade(id, rejectMotif)
      setRejectId(null)
      setRejectMotif('')
      await load()
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Refus API')
    } finally {
      setBusy(null)
    }
  }

  if (!user || !isStaffRole(user)) return null

  const filterBtns: { key: string; label: string }[] = [
    { key: 'PENDING', label: 'En attente' },
    { key: 'APPROVED', label: 'Approuvées' },
    { key: 'REJECTED', label: 'Refusées' },
    { key: '', label: 'Toutes' },
  ]

  return (
    <div className="container admin-role-page" style={{ paddingTop: 24, paddingBottom: 48 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 4 }}>
        <span
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: 44,
            height: 44,
            borderRadius: 12,
            background: 'rgba(99, 102, 241, 0.15)',
            color: '#818cf8',
          }}
          aria-hidden
        >
          <UserPlus size={22} />
        </span>
        <div>
          <h1>Demandes vendeur / livreur</h1>
          <p className="meta" style={{ margin: 0 }}>
            Consultez les pièces, puis approuvez ou refusez avec un motif si besoin.
          </p>
        </div>
      </div>

      <div className="admin-role-filters" role="tablist" aria-label="Filtrer les demandes">
        {filterBtns.map(({ key, label }) => (
          <button
            key={key || 'all'}
            type="button"
            role="tab"
            aria-selected={filter === key}
            className={`admin-role-filter${filter === key ? ' admin-role-filter--on' : ''}`}
            onClick={() => setFilter(key)}
          >
            {label}
          </button>
        ))}
      </div>

      {err && (
        <p role="alert" style={{ color: 'var(--danger)', marginBottom: 16 }}>
          {err}
        </p>
      )}

      {rows.length === 0 ? (
        <div className="livreur-empty">Aucune demande dans cette catégorie.</div>
      ) : (
        rows.map((r) => (
          <article key={r.id} className="admin-role-card">
            <div className="admin-role-card-head">
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap', marginBottom: 8 }}>
                  <span className={tagClass(r.status)}>{r.status}</span>
                  <strong style={{ fontSize: '1.05rem' }}>
                    #{r.id} · {r.roleDemande}
                  </strong>
                </div>
                <div className="meta" style={{ fontSize: '0.9rem', lineHeight: 1.6 }}>
                  <div>
                    <strong>{r.emailDemandeur}</strong>
                  </div>
                  {r.idtypeVendeur != null && <div>Catégorie vendeur (id) : {r.idtypeVendeur}</div>}
                  {r.roleDemande === 'VENDEUR' && (
                    <div>Marché international : {r.vendeurInternational ? 'oui' : 'non'}</div>
                  )}
                  {r.typeEnginLivreur && <div>Engin : {r.typeEnginLivreur}</div>}
                  {(r.latitude != null || r.longitude != null) && (
                    <div>
                      GPS : {r.latitude ?? '—'}, {r.longitude ?? '—'}
                    </div>
                  )}
                  {r.createdAt && <div>Créée : {new Date(r.createdAt).toLocaleString('fr-FR')}</div>}
                  {r.adminMotif && (
                    <div style={{ marginTop: 8, color: 'var(--danger)' }}>Motif : {r.adminMotif}</div>
                  )}
                </div>
              </div>
              {r.status === 'PENDING' && (
                <div className="admin-role-actions">
                  <a href={adminRoleUpgradeCnibUrl(r.id)} target="_blank" rel="noreferrer">
                    Pièce d’identité →
                  </a>
                  <a href={adminRoleUpgradePhotoUrl(r.id)} target="_blank" rel="noreferrer">
                    Photo →
                  </a>
                  <button type="button" className="button-primary" disabled={busy === r.id} onClick={() => void doApprove(r.id)}>
                    Approuver
                  </button>
                  {rejectId === r.id ? (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 8, width: '100%', maxWidth: 260 }}>
                      <input
                        placeholder="Motif du refus (optionnel)"
                        value={rejectMotif}
                        onChange={(e) => setRejectMotif(e.target.value)}
                        style={{ width: '100%' }}
                      />
                      <button type="button" disabled={busy === r.id} onClick={() => void doReject(r.id)}>
                        Confirmer le refus
                      </button>
                      <button type="button" className="link-button" onClick={() => setRejectId(null)}>
                        Annuler
                      </button>
                    </div>
                  ) : (
                    <button type="button" className="link-button" onClick={() => setRejectId(r.id)}>
                      Refuser
                    </button>
                  )}
                </div>
              )}
            </div>
          </article>
        ))
      )}
    </div>
  )
}
