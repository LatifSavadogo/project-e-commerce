import { useEffect } from 'react'
import { CheckCircle2, Hand, Info, X, XCircle } from 'lucide-react'
import { iconMd } from './ui/iconProps'

type ToastProps = {
  message: string
  type?: 'success' | 'error' | 'info' | 'welcome'
  onClose: () => void
  duration?: number
}

export default function Toast({ message, type = 'info', onClose, duration = 5000 }: ToastProps) {
  useEffect(() => {
    const timer = setTimeout(() => {
      onClose()
    }, duration)

    return () => clearTimeout(timer)
  }, [duration, onClose])

  const accent =
    type === 'error'
      ? 'var(--danger)'
      : type === 'info'
        ? 'var(--link)'
        : 'var(--accent)'

  const Icon =
    type === 'success'
      ? CheckCircle2
      : type === 'error'
        ? XCircle
        : type === 'welcome'
          ? Hand
          : Info

  return (
    <div
      style={{
        position: 'fixed',
        top: 20,
        right: 20,
        background: 'var(--surface)',
        border: `2px solid ${accent}`,
        borderRadius: 12,
        padding: '16px 20px',
        minWidth: 300,
        maxWidth: 400,
        boxShadow: '0 8px 32px rgba(0,0,0,0.5)',
        zIndex: 9999,
        animation: 'slideIn 0.3s ease-out',
        display: 'flex',
        alignItems: 'center',
        gap: 12,
      }}
    >
      <style>
        {`
          @keyframes slideIn {
            from {
              transform: translateX(400px);
              opacity: 0;
            }
            to {
              transform: translateX(0);
              opacity: 1;
            }
          }
        `}
      </style>

      <div style={{ color: accent, lineHeight: 0, flexShrink: 0 }}>
        <Icon {...iconMd} strokeWidth={1.75} aria-hidden />
      </div>

      <div style={{ flex: 1 }}>
        <div
          style={{
            color: 'var(--text)',
            fontSize: '1em',
            lineHeight: 1.4,
          }}
        >
          {message}
        </div>
      </div>

      <button
        type="button"
        onClick={onClose}
        style={{
          background: 'transparent',
          border: 'none',
          color: 'var(--muted)',
          cursor: 'pointer',
          padding: 4,
          lineHeight: 0,
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          borderRadius: 8,
        }}
        aria-label="Fermer"
      >
        <X size={20} strokeWidth={1.65} aria-hidden />
      </button>
    </div>
  )
}
