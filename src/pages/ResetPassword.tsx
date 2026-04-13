import { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { resetPasswordRequest } from '../services/authApi'

export default function ResetPassword() {
  const [searchParams] = useSearchParams()
  const [token, setToken] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [msg, setMsg] = useState<string | null>(null)
  const [err, setErr] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  useEffect(() => {
    const t = searchParams.get('token')
    if (t) setToken(t)
  }, [searchParams])

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErr(null)
    setMsg(null)
    if (password.length < 6) {
      setErr('Le mot de passe doit faire au moins 6 caractères.')
      return
    }
    if (password !== confirm) {
      setErr('Les mots de passe ne correspondent pas.')
      return
    }
    setPending(true)
    try {
      const r = await resetPasswordRequest(token, password)
      setMsg(r.message)
    } catch {
      setErr('Lien invalide ou expiré.')
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="container" style={{ paddingTop: 40, paddingBottom: 40, maxWidth: 480 }}>
      <div className="card" style={{ padding: 28 }}>
        <h1 style={{ marginTop: 0 }}>Nouveau mot de passe</h1>
        {msg ? (
          <>
            <p style={{ color: '#2a9d8f' }}>{msg}</p>
            <Link to="/auth?mode=login">Se connecter</Link>
          </>
        ) : (
          <form onSubmit={submit} className="grid" style={{ gap: 14 }}>
            <div>
              <label style={{ display: 'block', marginBottom: 6 }}>Token (depuis l’e-mail)</label>
              <input
                type="text"
                required
                value={token}
                onChange={(e) => setToken(e.target.value)}
                style={{ width: '100%', padding: 10 }}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 6 }}>Nouveau mot de passe</label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{ width: '100%', padding: 10 }}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 6 }}>Confirmation</label>
              <input
                type="password"
                required
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
                style={{ width: '100%', padding: 10 }}
              />
            </div>
            {err && <p style={{ color: '#f87171', margin: 0 }}>{err}</p>}
            <button type="submit" disabled={pending}>
              {pending ? '…' : 'Mettre à jour'}
            </button>
          </form>
        )}
        <p style={{ marginTop: 16 }}>
          <Link to="/forgot-password">Demander un nouvel e-mail</Link>
        </p>
      </div>
    </div>
  )
}
