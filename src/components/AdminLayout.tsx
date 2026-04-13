import { useEffect, useState } from 'react'
import { NavLink, Outlet, Link, useLocation, useNavigate } from 'react-router-dom'
import {
  AlertTriangle,
  CreditCard,
  Globe,
  LayoutDashboard,
  Layers,
  Package,
  Truck,
  Shield,
  UserPlus,
  Users,
} from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { useTheme } from '../contexts/ThemeContext'
import ThemeToggleButton from './ThemeToggleButton'
import { iconSm } from './ui/iconProps'

const mainItems = [
  { to: '/admin', end: true, label: 'Tableau de bord', Icon: LayoutDashboard },
  { to: '/admin/users', end: false, label: 'Utilisateurs', Icon: Users },
  { to: '/admin/articles', end: false, label: 'Articles', Icon: Package },
  { to: '/admin/payments', end: false, label: 'Paiements', Icon: CreditCard },
  { to: '/admin/complaints', end: false, label: 'Plaintes', Icon: AlertTriangle },
  { to: '/admin/livraisons', end: false, label: 'Livraisons', Icon: Truck },
] as const

const settingsItems = [
  { to: '/admin/settings/catalog', label: 'Familles & types', Icon: Layers },
  { to: '/admin/settings/pays', label: 'Pays', Icon: Globe },
  { to: '/admin/settings/roles', label: 'Rôles', Icon: Shield },
  { to: '/admin/settings/role-upgrades', label: 'Demandes vendeur/livreur', Icon: UserPlus },
] as const

export default function AdminLayout() {
  const location = useLocation()
  const navigate = useNavigate()
  const { logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const [navOpen, setNavOpen] = useState(false)

  const handleLogout = async () => {
    await logout()
    navigate('/auth?mode=login')
  }

  useEffect(() => {
    setNavOpen(false)
  }, [location.pathname])

  return (
    <div className="admin-shell">
      <button
        type="button"
        className="admin-sidebar-toggle"
        aria-expanded={navOpen}
        aria-controls="admin-sidebar-nav"
        onClick={() => setNavOpen((o) => !o)}
      >
        <span className="admin-sidebar-toggle-bars" aria-hidden />
        <span className="visually-hidden">Ouvrir ou fermer le menu admin</span>
      </button>

      <div
        className={`admin-sidebar-backdrop${navOpen ? ' is-visible' : ''}`}
        aria-hidden
        onClick={() => setNavOpen(false)}
      />

      <aside className={`admin-sidebar${navOpen ? ' is-open' : ''}`} aria-label="Administration">
        <div className="admin-sidebar-head">
          <Link to="/admin" className="admin-sidebar-brand" onClick={() => setNavOpen(false)}>
            <span className="admin-sidebar-brand-mark" aria-hidden />
            <span className="admin-sidebar-brand-text">
              <span className="admin-sidebar-brand-title">Ecomarket</span>
              <span className="admin-sidebar-brand-sub">Console admin</span>
            </span>
          </Link>
        </div>

        <nav id="admin-sidebar-nav" className="admin-sidebar-nav" aria-label="Sections">
          {mainItems.map(({ to, end, label, Icon }) => (
            <NavLink
              key={to + String(end)}
              to={to}
              end={end}
              className={({ isActive }) => `admin-sidebar-link${isActive ? ' admin-sidebar-link--active' : ''}`}
              onClick={() => setNavOpen(false)}
            >
              <Icon {...iconSm} aria-hidden />
              {label}
            </NavLink>
          ))}
          <p className="admin-sidebar-section-label">Référentiels</p>
          {settingsItems.map(({ to, label, Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) => `admin-sidebar-link${isActive ? ' admin-sidebar-link--active' : ''}`}
              onClick={() => setNavOpen(false)}
            >
              <Icon {...iconSm} aria-hidden />
              {label}
            </NavLink>
          ))}
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
