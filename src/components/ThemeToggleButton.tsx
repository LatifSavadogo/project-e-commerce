import { Moon, Sun } from 'lucide-react'
import type { ThemeMode } from '../contexts/ThemeContext'
import { iconMd } from './ui/iconProps'

type Props = {
  theme: ThemeMode
  onClick: () => void
  /** Classe CSS additionnelle (ex. admin) */
  className?: string
}

/** Icône seule : soleil si mode sombre (→ passer au clair), lune si mode clair (→ passer au sombre). */
export default function ThemeToggleButton({ theme, onClick, className = '' }: Props) {
  const isDark = theme === 'dark'
  const label = isDark ? 'Activer le mode clair' : 'Activer le mode sombre'

  return (
    <button
      type="button"
      className={`theme-toggle-btn ${className}`.trim()}
      onClick={onClick}
      aria-label={label}
      title={label}
    >
      <span className="theme-toggle-btn__icon" aria-hidden>
        {isDark ? <Sun {...iconMd} /> : <Moon {...iconMd} />}
      </span>
    </button>
  )
}
