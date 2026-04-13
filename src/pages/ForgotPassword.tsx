import { useState } from 'react'
import { Link } from 'react-router-dom'
import { forgotPasswordRequest } from '../services/authApi'

export default function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [done, setDone] = useState(false)
  const [err, setErr] = useState<string | null>(null)
  const [pending, setPending] = useState(false)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErr(null)
    setPending(true)
    try {
      await forgotPasswordRequest(email)
      setDone(true)
    } catch {
      setErr('Impossible d’envoyer la demande pour le moment.')
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="container" style={{ paddingTop: 40, paddingBottom: 40, maxWidth: 480 }}>
      <div className="card" style={{ padding: 28 }}>
        <h1 style={{ marginTop: 0 }}>Mot de passe oublié</h1>
        <p className="meta">
          Si l’adresse est connue, un e-mail de réinitialisation sera envoyé (selon la configuration SMTP du
          serveur).
        </p>
        {done ? (
          <p style={{ color: '#2a9d8f' }}>Si cette adresse est inscrite, vérifiez votre boîte mail.</p>
        ) : (
          <form onSubmit={submit} className="grid" style={{ gap: 14 }}>
            <div>
              <label style={{ display: 'block', marginBottom: 6 }}>Email</label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                style={{ width: '100%', padding: 10 }}
              />
            </div>
            {err && <p style={{ color: '#f87171', margin: 0 }}>{err}</p>}
            <button type="submit" disabled={pending}>
              {pending ? 'Envoi…' : 'Envoyer le lien'}
            </button>
          </form>
        )}
        <p style={{ marginTop: 20 }}>
          <Link to="/auth?mode=login">← Retour à la connexion</Link>
        </p>
      </div>
    </div>
  )
}
