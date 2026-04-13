import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { useComplaints } from '../contexts/ComplaintContext'
import { iconSm } from '../components/ui/iconProps'

export default function Complaint() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const { addComplaint } = useComplaints()
  const [titre, setTitre] = useState('')
  const [description, setDescription] = useState('')
  const [pending, setPending] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!user) {
      alert('Vous devez être connecté pour envoyer une plainte')
      navigate('/auth?mode=login')
      return
    }

    if (!titre.trim() || !description.trim()) {
      alert('Veuillez renseigner un titre et une description')
      return
    }

    setPending(true)
    try {
      await addComplaint({
        titre: titre.trim(),
        description: description.trim(),
      })
      alert('Votre plainte a été envoyée aux administrateurs')
      setTitre('')
      setDescription('')
      navigate('/')
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Envoi impossible')
    } finally {
      setPending(false)
    }
  }

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        padding: '20px',
        background: 'var(--page-bg)',
      }}
    >
      <div
        style={{
          width: '100%',
          maxWidth: '600px',
          background: 'var(--surface)',
          border: '1px solid var(--border)',
          borderRadius: '12px',
          overflow: 'hidden',
        }}
      >
        <div
          style={{
            background: 'var(--accent)',
            padding: '16px',
            display: 'flex',
            alignItems: 'center',
            gap: 12,
          }}
        >
          <button
            type="button"
            onClick={() => navigate(-1)}
            style={{
              background: 'transparent',
              border: 'none',
              color: 'white',
              cursor: 'pointer',
              padding: 4,
              lineHeight: 0,
              borderRadius: 8,
            }}
            aria-label="Retour"
          >
            <ArrowLeft {...iconSm} aria-hidden />
          </button>
          <div>
            <div style={{ fontWeight: 600, fontSize: '1.1em', color: '#fff' }}>Envoyer une plainte</div>
            <div style={{ fontSize: '0.85em', opacity: 0.92, color: '#fff' }}>Transmission aux administrateurs</div>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="form-stack" style={{ padding: 24 }}>
          <div className="form-field">
            <label className="form-label" htmlFor="complaint-title">
              Titre
            </label>
            <input
              id="complaint-title"
              type="text"
              value={titre}
              onChange={(e) => setTitre(e.target.value)}
              required
              placeholder="Objet court de votre demande"
            />
          </div>
          <div className="form-field">
            <label className="form-label" htmlFor="complaint-body">
              Description
            </label>
            <textarea
              id="complaint-body"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
              rows={6}
              placeholder="Décrivez la situation en détail"
            />
          </div>
          <button
            type="submit"
            className="button-primary"
            disabled={pending}
            style={{ width: '100%', borderRadius: 12 }}
          >
            {pending ? 'Envoi…' : 'Envoyer'}
          </button>
        </form>
      </div>
    </div>
  )
}
