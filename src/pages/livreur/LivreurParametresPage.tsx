import { useCallback, useEffect, useState } from 'react'
import { useAuth } from '../../contexts/AuthContext'
import { fetchLivreurDashboard, patchLivreurEngin } from '../../services/livreurApi'

export default function LivreurParametresPage() {
  const { user, refreshMe } = useAuth()
  const [enginProfil, setEnginProfil] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [msg, setMsg] = useState<string | null>(null)

  const load = useCallback(async () => {
    try {
      const d = await fetchLivreurDashboard()
      setEnginProfil(d.enginProfil || user?.typeEnginLivreur || null)
    } catch {
      setEnginProfil(user?.typeEnginLivreur || null)
    }
  }, [user?.typeEnginLivreur])

  useEffect(() => {
    void load()
  }, [load])

  const setEngin = async (engin: 'MOTO' | 'VEHICULE') => {
    setBusy(true)
    setMsg(null)
    try {
      await patchLivreurEngin(engin)
      await refreshMe()
      setEnginProfil(engin)
      setMsg('Engin enregistré sur votre profil.')
    } catch (e) {
      setMsg(e instanceof Error ? e.message : 'Erreur')
    } finally {
      setBusy(false)
    }
  }

  const actif = (enginProfil || '').toUpperCase()

  return (
    <div className="container" style={{ paddingTop: 28, paddingBottom: 48, maxWidth: 640 }}>
      <h1 style={{ margin: '0 0 8px', fontSize: '1.55rem' }}>Engin par défaut</h1>
      <p className="meta" style={{ marginBottom: 24 }}>
        Suggestion pour votre compte ; à chaque course vous choisissez encore moto ou véhicule au moment d’accepter.
      </p>

      <div className="livreur-panel">
        <p className="livreur-panel-hint">Actuellement : {actif || 'non défini'}</p>
        <div className="livreur-pill-row">
          <button
            type="button"
            className={`livreur-pill${actif === 'MOTO' ? ' livreur-pill--active' : ''}`}
            disabled={busy}
            onClick={() => void setEngin('MOTO')}
          >
            Moto
          </button>
          <button
            type="button"
            className={`livreur-pill${actif === 'VEHICULE' ? ' livreur-pill--active' : ''}`}
            disabled={busy}
            onClick={() => void setEngin('VEHICULE')}
          >
            Véhicule
          </button>
        </div>
        {msg && (
          <p className="meta" style={{ marginTop: 16, marginBottom: 0 }}>
            {msg}
          </p>
        )}
      </div>
    </div>
  )
}
