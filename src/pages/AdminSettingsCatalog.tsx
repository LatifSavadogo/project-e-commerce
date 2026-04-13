import { useAuth, isStaffRole } from '../contexts/AuthContext'
import { useNavigate } from 'react-router-dom'
import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../services/apiClient'
import {
  fetchFamilleArticles,
  fetchTypeArticles,
  createFamilleArticle,
  updateFamilleArticle,
  deleteFamilleArticle,
  createTypeArticle,
  updateTypeArticle,
  deleteTypeArticle,
} from '../services/referenceApi'
import type { FamilleArticleDtoJson, TypeArticleDtoJson } from '../types/backend'

function apiErr(e: unknown): string {
  if (e instanceof ApiError && e.body && typeof e.body === 'object') {
    const m = (e.body as Record<string, unknown>).message
    if (typeof m === 'string') return m
  }
  return e instanceof Error ? e.message : 'Erreur'
}

export default function AdminSettingsCatalog() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [tab, setTab] = useState<'familles' | 'types'>('familles')
  const [familles, setFamilles] = useState<FamilleArticleDtoJson[]>([])
  const [types, setTypes] = useState<TypeArticleDtoJson[]>([])
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState<string | null>(null)

  const [fLib, setFLib] = useState('')
  const [fDesc, setFDesc] = useState('')
  const [editFamilleId, setEditFamilleId] = useState<number | null>(null)

  const [tLib, setTLib] = useState('')
  const [tDesc, setTDesc] = useState('')
  const [tFamille, setTFamille] = useState<number | ''>('')
  const [editTypeId, setEditTypeId] = useState<number | null>(null)

  const reload = useCallback(async () => {
    setLoading(true)
    setErr(null)
    try {
      const [f, t] = await Promise.all([fetchFamilleArticles(), fetchTypeArticles()])
      setFamilles(f)
      setTypes(t)
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

  const startEditFamille = (f: FamilleArticleDtoJson) => {
    setEditFamilleId(f.idfamille)
    setFLib(f.libfamille)
    setFDesc(f.description)
    setTab('familles')
  }

  const resetFamilleForm = () => {
    setEditFamilleId(null)
    setFLib('')
    setFDesc('')
  }

  const submitFamille = async () => {
    try {
      if (editFamilleId != null) {
        await updateFamilleArticle(editFamilleId, { libfamille: fLib.trim(), description: fDesc.trim() })
      } else {
        await createFamilleArticle({ libfamille: fLib.trim(), description: fDesc.trim() })
      }
      resetFamilleForm()
      await reload()
    } catch (e) {
      alert(apiErr(e))
    }
  }

  const removeFamille = async (id: number) => {
    if (!window.confirm('Supprimer cette famille ? (impossible si des types y sont rattachés.)')) return
    try {
      const r = await deleteFamilleArticle(id)
      if (r.supprimé === false && r.message) {
        alert(r.message)
        return
      }
      await reload()
    } catch (e) {
      alert(apiErr(e))
    }
  }

  const startEditType = (t: TypeArticleDtoJson) => {
    setEditTypeId(t.idtype)
    setTLib(t.libtype)
    setTDesc(t.desctype || '')
    setTFamille(t.idfamille ?? '')
    setTab('types')
  }

  const resetTypeForm = () => {
    setEditTypeId(null)
    setTLib('')
    setTDesc('')
    setTFamille('')
  }

  const submitType = async () => {
    if (tFamille === '') {
      alert('Choisissez une famille.')
      return
    }
    try {
      if (editTypeId != null) {
        await updateTypeArticle(editTypeId, {
          libtype: tLib.trim(),
          desctype: tDesc.trim(),
          idfamille: tFamille as number,
        })
      } else {
        await createTypeArticle({
          libtype: tLib.trim(),
          desctype: tDesc.trim(),
          idfamille: tFamille as number,
        })
      }
      resetTypeForm()
      await reload()
    } catch (e) {
      alert(apiErr(e))
    }
  }

  const removeType = async (id: number) => {
    if (!window.confirm('Supprimer ce type / catégorie ?')) return
    try {
      await deleteTypeArticle(id)
      await reload()
    } catch (e) {
      alert(apiErr(e))
    }
  }

  if (!user || !isStaffRole(user)) return null

  return (
    <div className="container" style={{ paddingTop: 20, paddingBottom: 40 }}>
      <h1 style={{ marginTop: 0 }}>Familles &amp; types d’annonces</h1>
      <p className="meta">
        Référentiel catalogue : familles regroupent les types visibles lors de la publication et du profil vendeur.
      </p>
      {err && <p style={{ color: '#f87171' }}>{err}</p>}

      <div className="admin-settings-tabs" role="tablist" aria-label="Sections référentiel">
        <button
          type="button"
          role="tab"
          aria-selected={tab === 'familles'}
          className={`admin-settings-tab${tab === 'familles' ? ' admin-settings-tab--active' : ''}`}
          onClick={() => setTab('familles')}
        >
          Familles
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={tab === 'types'}
          className={`admin-settings-tab${tab === 'types' ? ' admin-settings-tab--active' : ''}`}
          onClick={() => setTab('types')}
        >
          Types (catégories)
        </button>
      </div>

      {loading ? (
        <p className="meta">Chargement…</p>
      ) : tab === 'familles' ? (
        <div className="admin-settings-panels">
          <div className="card" style={{ padding: 20 }}>
            <h2 style={{ marginTop: 0, fontSize: '1.1rem' }}>
              {editFamilleId != null ? 'Modifier la famille' : 'Nouvelle famille'}
            </h2>
            <label className="visually-hidden" htmlFor="famille-lib">
              Libellé
            </label>
            <input
              id="famille-lib"
              type="text"
              placeholder="Libellé (2–100 car.)"
              value={fLib}
              onChange={(e) => setFLib(e.target.value)}
              style={{ width: '100%', marginBottom: 10 }}
            />
            <label className="visually-hidden" htmlFor="famille-desc">
              Description
            </label>
            <textarea
              id="famille-desc"
              placeholder="Description (5–500 car.)"
              value={fDesc}
              onChange={(e) => setFDesc(e.target.value)}
              rows={3}
              style={{ width: '100%', marginBottom: 12 }}
            />
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              <button type="button" className="button-primary" onClick={() => void submitFamille()}>
                Enregistrer
              </button>
              {editFamilleId != null && (
                <button type="button" onClick={resetFamilleForm}>
                  Annuler
                </button>
              )}
            </div>
          </div>
          <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <table className="admin-ref-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Libellé</th>
                  <th>Description</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {familles.map((f) => (
                  <tr key={f.idfamille}>
                    <td>{f.idfamille}</td>
                    <td>{f.libfamille}</td>
                    <td style={{ maxWidth: 280, fontSize: '0.88rem', color: '#94a3b8' }}>{f.description}</td>
                    <td style={{ whiteSpace: 'nowrap' }}>
                      <button type="button" className="link-button" onClick={() => startEditFamille(f)}>
                        Modifier
                      </button>
                      <button type="button" className="link-button" onClick={() => void removeFamille(f.idfamille)}>
                        Supprimer
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="admin-settings-panels">
          <div className="card" style={{ padding: 20 }}>
            <h2 style={{ marginTop: 0, fontSize: '1.1rem' }}>
              {editTypeId != null ? 'Modifier le type' : 'Nouveau type'}
            </h2>
            <label htmlFor="type-famille" style={{ display: 'block', marginBottom: 6, fontSize: '0.85rem' }}>
              Famille
            </label>
            <select
              id="type-famille"
              value={tFamille}
              onChange={(e) => setTFamille(e.target.value ? Number(e.target.value) : '')}
              style={{ width: '100%', marginBottom: 10 }}
            >
              <option value="">— Choisir —</option>
              {familles.map((f) => (
                <option key={f.idfamille} value={f.idfamille}>
                  {f.libfamille}
                </option>
              ))}
            </select>
            <input
              type="text"
              placeholder="Libellé type (2–100 car.)"
              value={tLib}
              onChange={(e) => setTLib(e.target.value)}
              style={{ width: '100%', marginBottom: 10 }}
            />
            <textarea
              placeholder="Description (5–500 car.)"
              value={tDesc}
              onChange={(e) => setTDesc(e.target.value)}
              rows={3}
              style={{ width: '100%', marginBottom: 12 }}
            />
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              <button type="button" className="button-primary" onClick={() => void submitType()}>
                Enregistrer
              </button>
              {editTypeId != null && (
                <button type="button" onClick={resetTypeForm}>
                  Annuler
                </button>
              )}
            </div>
          </div>
          <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <table className="admin-ref-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Type</th>
                  <th>Famille</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {types.map((t) => (
                  <tr key={t.idtype}>
                    <td>{t.idtype}</td>
                    <td>{t.libtype}</td>
                    <td style={{ fontSize: '0.88rem', color: '#94a3b8' }}>{t.libfamille ?? '—'}</td>
                    <td style={{ whiteSpace: 'nowrap' }}>
                      <button type="button" className="link-button" onClick={() => startEditType(t)}>
                        Modifier
                      </button>
                      <button type="button" className="link-button" onClick={() => void removeType(t.idtype)}>
                        Supprimer
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
