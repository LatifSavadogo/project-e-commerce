import { useEffect, useState } from 'react'
import { NavLink, Outlet, Link, useLocation, useNavigate } from 'react-router-dom'
import { History, LayoutDashboard, ListTodo, Route, Settings2, Store, UserRound } from 'lucide-react'
import { useAuth, isLivreurRole } from '../contexts/AuthContext'
import { useTheme } from '../contexts/ThemeContext'
import ThemeToggleButton from './ThemeToggleButton'
import { iconSm } from './ui/iconProps'

const navItems = [
  { to: '/livreur', end: true, label: 'Tableau de bord', Icon: LayoutDashboard },
  { to: '/livreur/offres', end: false, label: 'Offres à saisir', Icon: ListTodo },
  { to: '/livreur/courses', end: false, label: 'Mes courses', Icon: Route },
  { to: '/livreur/historique', end: false, label: 'Historique', Icon: History },
  { to: '/livreur/parametres', end: false, label: 'Engin par défaut', Icon: Settings2 },
] as const

export default function LivreurLayout() {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, authLoading, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const [navOpen, setNavOpen] = useState(false)

  useEffect(() => {
    if (authLoading) return
    if (!user) {
      navigate('/auth?mode=login')
      return
    }
    if (!isLivreurRole(user)) {
      navigate('/profile')
    }
  }, [user, authLoading, navigate])

  const handleLogout = async () => {
    await logout()
    navigate('/auth?mode=login')
  }

  useEffect(() => {
    setNavOpen(false)
  }, [location.pathname])

  if (authLoading || !user || !isLivreurRole(user)) {
    return (
      <div className="admin-main" style={{ padding: 40, textAlign: 'center', color: 'var(--muted)' }}>
        Chargement…
      </div>
    )
  }

  return (
    <div className="admin-shell" data-console="livreur">
      <button
        type="button"
        className="admin-sidebar-toggle"
        aria-expanded={navOpen}
        aria-controls="livreur-sidebar-nav"
        onClick={() => setNavOpen((o) => !o)}
      >
        <span className="admin-sidebar-toggle-bars" aria-hidden />
        <span className="visually-hidden">Menu livreur</span>
      </button>

      <div
        className={`admin-sidebar-backdrop${navOpen ? ' is-visible' : ''}`}
        aria-hidden
        onClick={() => setNavOpen(false)}
      />

      <aside className={`admin-sidebar${navOpen ? ' is-open' : ''}`} aria-label="Console livreur">
        <div className="admin-sidebar-head">
          <Link to="/livreur" className="admin-sidebar-brand" onClick={() => setNavOpen(false)}>
            <span className="admin-sidebar-brand-mark livreur-sidebar-mark" aria-hidden />
            <span className="admin-sidebar-brand-text">
              <span className="admin-sidebar-brand-title">Ecomarket</span>
              <span className="admin-sidebar-brand-sub">Console livreur</span>
            </span>
          </Link>
        </div>

        <nav id="livreur-sidebar-nav" className="admin-sidebar-nav" aria-label="Sections livreur">
          {navItems.map(({ to, end, label, Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) => `admin-sidebar-link${isActive ? ' admin-sidebar-link--active' : ''}`}
              onClick={() => setNavOpen(false)}
            >
              <Icon {...iconSm} aria-hidden />
              {label}
            </NavLink>
          ))}
          <p className="admin-sidebar-section-label">Raccourcis</p>
          <Link to="/" className="admin-sidebar-exit" onClick={() => setNavOpen(false)}>
            <Store {...iconSm} aria-hidden />
            Boutique
          </Link>
          <Link to="/profile" className="admin-sidebar-exit" onClick={() => setNavOpen(false)}>
            <UserRound {...iconSm} aria-hidden />
            Mon compte
          </Link>
        </nav>

        <div className="admin-sidebar-foot">
          <ThemeToggleButton theme={theme} onClick={() => toggleTheme()} className="theme-toggle-btn--block" />
          <button type="button" className="admin-sidebar-logout" onClick={() => void handleLogout()}>
            Se déconnecter
          </button>
        </div>
      </aside>

      <div className="admin-main">
        <Outlet />
      </div>
    </div>
  )
}
