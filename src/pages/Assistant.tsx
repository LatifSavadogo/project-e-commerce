import { useState, useRef, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { chatbotReply } from '../services/chatbotApi'
import { useAuth } from '../contexts/AuthContext'

type Line = { role: 'user' | 'bot'; text: string }

export default function Assistant() {
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [lines, setLines] = useState<Line[]>([
    { role: 'bot', text: 'Bonjour ! Je peux répondre à des questions courantes sur Ecomarket.' },
  ])
  const [input, setInput] = useState('')
  const [busy, setBusy] = useState(false)
  const endRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/auth?mode=login')
    }
  }, [isAuthenticated, navigate])

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [lines])

  const send = async (e: React.FormEvent) => {
    e.preventDefault()
    const t = input.trim()
    if (!t || busy) return
    setInput('')
    setLines((prev) => [...prev, { role: 'user', text: t }])
    setBusy(true)
    try {
      const r = await chatbotReply(t)
      setLines((prev) => [...prev, { role: 'bot', text: r.reply }])
    } catch {
      setLines((prev) => [
        ...prev,
        { role: 'bot', text: 'Service temporairement indisponible. Essayez plus tard ou contactez le support.' },
      ])
    } finally {
      setBusy(false)
    }
  }

  if (!isAuthenticated) {
    return null
  }

  return (
    <section className="container" style={{ paddingTop: 20, paddingBottom: 40, maxWidth: 720 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <h1 style={{ margin: 0 }}>Assistant Ecomarket</h1>
        <Link to="/help" className="link-button">
          ← Aide
        </Link>
      </div>
      <p className="meta">Posez une question sur la plateforme ; les réponses sont générées automatiquement.</p>

      <div
        style={{
          border: '1px solid var(--border)',
          borderRadius: 12,
          height: 420,
          overflowY: 'auto',
          padding: 16,
          background: 'var(--surface)',
          marginBottom: 12,
        }}
      >
        {lines.map((l, i) => (
          <div
            key={i}
            style={{
              marginBottom: 12,
              textAlign: l.role === 'user' ? 'right' : 'left',
            }}
          >
            <span
              style={{
                display: 'inline-block',
                maxWidth: '85%',
                padding: '10px 14px',
                borderRadius: 12,
                background: l.role === 'user' ? '#2a9d8f' : 'var(--surface-elevated)',
                color: 'var(--text)',
                whiteSpace: 'pre-wrap',
                textAlign: 'left',
              }}
            >
              {l.text}
            </span>
          </div>
        ))}
        <div ref={endRef} />
      </div>

      <form onSubmit={send} style={{ display: 'flex', gap: 8 }}>
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Posez votre question…"
          disabled={busy}
          style={{
            flex: 1,
            padding: 12,
            borderRadius: 8,
            border: '1px solid var(--border)',
            background: 'var(--input-bg)',
            color: 'var(--text)',
          }}
        />
        <button type="submit" disabled={busy || !input.trim()} style={{ padding: '0 20px' }}>
          {busy ? '…' : 'Envoyer'}
        </button>
      </form>
    </section>
  )
}
