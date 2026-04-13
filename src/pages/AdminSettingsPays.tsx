import { useAuth, isStaffRole } from '../contexts/AuthContext'
import { useNavigate } from 'react-router-dom'
import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../services/apiClient'
import { fetchPays, createPays, updatePays, deletePays } from '../services/referenceApi'
import type { PaysDtoJson } from '../types/backend'

function apiErr(e: unknown): string {
  if (e instanceof ApiError && e.body && typeof e.body === 'object') {
    const m = (e.body as Record<string, unknown>).message
    if (typeof m === 'string') return m
  }
  return e instanceof Error ? e.message : 'Erreur'
}

export default function AdminSettingsPays() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [rows, setRows] = useState<PaysDtoJson[]>([])
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState<string | null>(null)
  const [lib, setLib] = useState('')
  const [desc, setDesc] = useState('')
  const [editId, setEditId] = useState<number | null>(null)

  const reload = useCallback(async () => {
    setLoading(true)
    setErr(null)
    try {
      setRows(await fetchPays())
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

  const startEdit = (p: PaysDtoJson) => {
    setEditId(p.idpays)
    setLib(p.libpays)
    setDesc(p.descpays || '')
  }

  const reset = () => {
    setEditId(null)
    setLib('')
    setDesc('')
  }

  const submit = async () => {
    try {
      if (editId != null) {
        await updatePays(editId, { libpays: lib.trim(), descpays: desc.trim() })
      } else {
        await createPays({ libpays: lib.trim(), descpays: desc.trim() })
      }
      reset()
      await reload()
    } catch (e) {
      alert(apiErr(e))
    }
  }

  const remove = async (id: number) => {
    if (!window.confirm('Supprimer ce pays ? (impossible si des utilisateurs y sont rattachés.)')) return
    try {
      const r = await deletePays(id)
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
      <h1 style={{ marginTop: 0 }}>Pays</h1>
      <p className="meta">Pays disponibles à l’inscription et sur les profils.</p>
      {err && <p style={{ color: '#f87171' }}>{err}</p>}

      <div className="admin-settings-panels">
        <div className="card" style={{ padding: 20 }}>
          <h2 style={{ marginTop: 0, fontSize: '1.1rem' }}>{editId != null ? 'Modifier le pays' : 'Nouveau pays'}</h2>
          <input
            type="text"
            placeholder="Libellé (2–100 car.)"
            value={lib}
            onChange={(e) => setLib(e.target.value)}
            style={{ width: '100%', marginBottom: 10 }}
          />
          <textarea
            placeholder="Description (5–500 car.)"
            value={desc}
            onChange={(e) => setDesc(e.target.value)}
            rows={3}
            style={{ width: '100%', marginBottom: 12 }}
          />
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            <button type="button" className="button-primary" onClick={() => void submit()}>
              Enregistrer
            </button>
            {editId != null && (
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
                  <th>Pays</th>
                  <th>Description</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {rows.map((p) => (
                  <tr key={p.idpays}>
                    <td>{p.idpays}</td>
                    <td>{p.libpays}</td>
                    <td style={{ maxWidth: 280, fontSize: '0.88rem', color: '#94a3b8' }}>{p.descpays ?? '—'}</td>
                    <td style={{ whiteSpace: 'nowrap' }}>
                      <button type="button" className="link-button" onClick={() => startEdit(p)}>
                        Modifier
                      </button>
                      <button type="button" className="link-button" onClick={() => void remove(p.idpays)}>
                        Supprimer
                      </button>
                    </td>
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
