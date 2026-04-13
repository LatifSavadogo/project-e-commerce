import { useAuth, isStaffRole } from '../contexts/AuthContext'
import { useNavigate } from 'react-router-dom'
import { useEffect, useState, useCallback } from 'react'
import { fetchAdminComplaints, patchComplaintLu } from '../services/complaintApi'
import type { ComplaintDtoJson } from '../types/backend'
import { dateFromDto } from '../utils/dateFromDto'

function mapDto(c: ComplaintDtoJson) {
  return {
    id: String(c.idplainte),
    userName: c.auteurNom || '',
    userEmail: c.auteurEmail || '',
    titre: c.titre,
    message: c.description,
    createdAt: dateFromDto(c.datecreation),
    read: c.lu,
  }
}

export default function AdminComplaints() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [list, setList] = useState<ReturnType<typeof mapDto>[]>([])
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState<string | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setErr(null)
    try {
      const raw = await fetchAdminComplaints()
      setList(raw.map(mapDto))
    } catch {
      setErr('Impossible de charger les plaintes.')
      setList([])
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

  const setRead = async (id: string, lu: boolean) => {
    try {
      await patchComplaintLu(Number(id), lu)
      setList((prev) => prev.map((x) => (x.id === id ? { ...x, read: lu } : x)))
    } catch {
      alert('Mise à jour impossible.')
    }
  }

  if (!user || !isStaffRole(user)) return null

  return (
    <div className="container" style={{ paddingTop: 20, paddingBottom: 40 }}>
      <h1 style={{ marginTop: 0 }}>Plaintes et signalements</h1>
      <p className="meta">Traitement de toutes les plaintes déposées sur la plateforme.</p>
      {err && <p style={{ color: '#f87171' }}>{err}</p>}
      {loading && <p className="meta">Chargement…</p>}
      {!loading && list.length === 0 && (
        <p style={{ color: 'var(--muted)', textAlign: 'center', padding: '40px 0' }}>Aucune plainte.</p>
      )}
      {!loading && list.length > 0 && (
        <div style={{ display: 'grid', gap: 16 }}>
          {list.map((complaint) => (
            <div
              key={complaint.id}
              className="card"
              style={{
                padding: 16,
                background: complaint.read ? 'var(--input-bg)' : '#1a2332',
                border: `1px solid ${complaint.read ? 'var(--border)' : 'var(--admin-accent)'}`,
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: 12 }}>
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
                          borderRadius: 12,
                          fontSize: '0.75em',
                        }}
                      >
                        Non lu
                      </span>
                    )}
                  </div>
                  <div style={{ fontSize: '0.85em', color: 'var(--muted)' }}>{complaint.userEmail}</div>
                </div>
                <div style={{ fontSize: '0.8em', color: 'var(--muted)' }}>
                  {new Date(complaint.createdAt).toLocaleString('fr-FR')}
                </div>
              </div>
              {complaint.titre && <p style={{ fontWeight: 600 }}>{complaint.titre}</p>}
              <p style={{ whiteSpace: 'pre-wrap', lineHeight: 1.6 }}>{complaint.message}</p>
              <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 12 }}>
                {!complaint.read && (
                  <button type="button" className="button-primary" onClick={() => void setRead(complaint.id, true)}>
                    Marquer comme lu
                  </button>
                )}
                {complaint.read && (
                  <button
                    type="button"
                    onClick={() => void setRead(complaint.id, false)}
                    style={{ border: '1px solid var(--border)', background: 'transparent' }}
                  >
                    Marquer non lu
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
