import { useAuth, isStaffRole, isSuperAdminRole } from '../contexts/AuthContext'
import { useNavigate } from 'react-router-dom'
import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../services/apiClient'
import { fetchRoles, createRole, updateRole, deleteRole } from '../services/referenceApi'
import type { RoleDtoJson } from '../types/backend'

function apiErr(e: unknown): string {
  if (e instanceof ApiError && e.body && typeof e.body === 'object') {
    const m = (e.body as Record<string, unknown>).message
    if (typeof m === 'string') return m
  }
  if (e instanceof ApiError && e.status === 403) {
    return 'Action réservée au super-administrateur.'
  }
  return e instanceof Error ? e.message : 'Erreur'
}

export default function AdminSettingsRoles() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const superAdmin = isSuperAdminRole(user)
  const [rows, setRows] = useState<RoleDtoJson[]>([])
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState<string | null>(null)
  const [lib, setLib] = useState('')
  const [desc, setDesc] = useState('')
  const [editId, setEditId] = useState<number | null>(null)

  const reload = useCallback(async () => {
    setLoading(true)
    setErr(null)
    try {
      setRows(await fetchRoles())
    } catch (e) {
      setErr(apiErr(e))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    if (!user || !isStaffRole(user)) {
      navigate('/')
      return
    }
    void reload()
  }, [user, navigate, reload])

  const startEdit = (r: RoleDtoJson) => {
    setEditId(r.idrole)
    setLib(r.librole)
    setDesc(r.descrole || '')
  }

  const reset = () => {
    setEditId(null)
    setLib('')
    setDesc('')
  }

  const submit = async () => {
    if (!superAdmin) return
    try {
      if (editId != null) {
        await updateRole(editId, { librole: lib.trim(), descrole: desc.trim() })
      } else {
        await createRole({ librole: lib.trim(), descrole: desc.trim() })
      }
      reset()
      await reload()
    } catch (e) {
      alert(apiErr(e))
    }
  }

  const remove = async (id: number) => {
    if (!superAdmin) return
    if (!window.confirm('Supprimer ce rôle ? (impossible si des utilisateurs l’utilisent.)')) return
    try {
      const r = await deleteRole(id)
      if (r.supprimé === false && r.message) {
        alert(r.message)
        return
      }
      await reload()
    } catch (e) {
      alert(apiErr(e))
    }
  }

  if (!user || !isStaffRole(user)) return null

  return (
    <div className="container" style={{ paddingTop: 20, paddingBottom: 40 }}>
      <h1 style={{ marginTop: 0 }}>Rôles</h1>
      <p className="meta">Liste des rôles applicatifs. La création, la modification et la suppression sont réservées au super-admin.</p>
      {!superAdmin && (
        <p
          style={{
            padding: '12px 14px',
            borderRadius: 10,
            border: '1px solid rgba(245, 158, 11, 0.45)',
            background: 'rgba(245, 158, 11, 0.08)',
            fontSize: '0.9rem',
          }}
        >
          Vous êtes connecté en tant qu’administrateur : vous pouvez consulter les rôles, mais pas les modifier côté API.
        </p>
      )}
      {err && <p style={{ color: '#f87171' }}>{err}</p>}

      <div className="admin-settings-panels">
        <div className="card" style={{ padding: 20, opacity: superAdmin ? 1 : 0.65 }}>
          <h2 style={{ marginTop: 0, fontSize: '1.1rem' }}>{editId != null ? 'Modifier le rôle' : 'Nouveau rôle'}</h2>
          <input
            type="text"
            placeholder="Libellé du rôle"
            value={lib}
            onChange={(e) => setLib(e.target.value)}
            disabled={!superAdmin}
            style={{ width: '100%', marginBottom: 10 }}
          />
          <textarea
            placeholder="Description"
            value={desc}
            onChange={(e) => setDesc(e.target.value)}
            disabled={!superAdmin}
            rows={3}
            style={{ width: '100%', marginBottom: 12 }}
          />
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            <button type="button" className="button-primary" disabled={!superAdmin} onClick={() => void submit()}>
              Enregistrer
            </button>
            {editId != null && superAdmin && (
              <button type="button" onClick={reset}>
                Annuler
              </button>
            )}
          </div>
        </div>
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          {loading ? (
            <p className="meta" style={{ padding: 16 }}>
              Chargement…
            </p>
          ) : (
            <table className="admin-ref-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Rôle</th>
                  <th>Description</th>
                  {superAdmin && <th />}
                </tr>
              </thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.idrole}>
                    <td>{r.idrole}</td>
                    <td>{r.librole}</td>
                    <td style={{ maxWidth: 320, fontSize: '0.88rem', color: '#94a3b8' }}>{r.descrole ?? '—'}</td>
                    {superAdmin && (
                      <td style={{ whiteSpace: 'nowrap' }}>
                        <button type="button" className="link-button" onClick={() => startEdit(r)}>
                          Modifier
                        </button>
                        <button type="button" className="link-button" onClick={() => void remove(r.idrole)}>
                          Supprimer
                        </button>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  )
}
