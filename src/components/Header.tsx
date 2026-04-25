import { Link, useNavigate, useLocation, NavLink } from 'react-router-dom'
import { Globe, Menu, ShoppingCart, UserRound, X } from 'lucide-react'
import { useAuth, isStaffRole, isLivreurRole } from '../contexts/AuthContext'
import { useCart } from '../contexts/CartContext'
import { useTheme } from '../contexts/ThemeContext'
import ThemeToggleButton from './ThemeToggleButton'
import { useState, useEffect, useRef } from 'react'
import { useNotifications } from '../hooks/useNotifications'
import NotificationBadge from './NotificationBadge'
import { iconMd } from './ui/iconProps'

export default function Header() {
  const { theme, toggleTheme } = useTheme()
  const { isAuthenticated, user, logout } = useAuth()
  const { itemCount } = useCart()
  const { unreadCount } = useNotifications()
  const navigate = useNavigate()
  const location = useLocation()
  const [showMenu, setShowMenu] = useState(false)
  const [mobileNavOpen, setMobileNavOpen] = useState(false)
  const [query, setQuery] = useState('')
  const [showSellToast, setShowSellToast] = useState(false)
  const accountRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (location.pathname === '/listings') {
      const q = new URLSearchParams(location.search).get('q') || ''
      setQuery(q)
    }
  }, [location.pathname, location.search])

  useEffect(() => {
    setMobileNavOpen(false)
  }, [location.pathname, location.search])

  useEffect(() => {
    if (!showMenu) return
    const onDown = (e: MouseEvent) => {
      const el = accountRef.current
      if (el && !el.contains(e.target as Node)) setShowMenu(false)
    }
    document.addEventListener('mousedown', onDown)
    return () => document.removeEventListener('mousedown', onDown)
  }, [showMenu])

  const handleSellClick = () => {
    setMobileNavOpen(false)
    if (!isAuthenticated) {
      setShowSellToast(true)
      setTimeout(() => {
        setShowSellToast(false)
        navigate('/auth?mode=register')
      }, 3000)
      return
    }
    if (user && isLivreurRole(user)) {
      navigate('/livreur')
      return
    }
    if (user?.accountType === 'seller') {
      navigate('/vendor/add-product')
      return
    }
    navigate('/demande-upgrade')
  }

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    const q = query.trim()
    setMobileNavOpen(false)
    navigate(q ? `/listings?q=${encodeURIComponent(q)}` : '/listings')
  }

  const navClass = (isActive: boolean) => (isActive ? 'nav-link-active' : undefined)

  return (
    <header className="site-header">
      {showSellToast && (
        <div
          style={{
            position: 'fixed',
            bottom: '-80px',
            left: '50%',
            transform: 'translateX(-50%)',
            background: 'var(--surface)',
            borderRadius: 12,
            padding: '14px 20px',
            color: 'var(--text)',
            boxShadow: '0 10px 30px rgba(0,0,0,0.6)',
            zIndex: 2000,
            minWidth: 260,
            textAlign: 'center',
            border: '1px solid var(--accent)',
            animation: 'sellToastSlide 3s ease-in-out forwards',
          }}
        >
          <style>
            {`
              @keyframes sellToastSlide {
                0% { transform: translate(-50%, 80px); opacity: 0; }
                20% { transform: translate(-50%, -40vh); opacity: 1; }
                80% { transform: translate(-50%, -40vh); opacity: 1; }
                100% { transform: translate(-50%, -120vh); opacity: 0; }
              }
            `}
          </style>
          <strong style={{ display: 'block', marginBottom: 4 }}>Connexion requise</strong>
          <span style={{ fontSize: '0.9em', color: 'var(--text-secondary)' }}>
            Veuillez vous connecter ou créer un compte avant de publier un article.
          </span>
        </div>
      )}
      <div className="header-inner container">
        <div className="header-brand-row">
          <Link className="brand" to="/">
            Ecomarket
          </Link>
          <button
            type="button"
            className="nav-toggle"
            aria-expanded={mobileNavOpen}
            aria-controls="site-nav"
            onClick={() => setMobileNavOpen((o) => !o)}
          >
            {mobileNavOpen ? <X {...iconMd} aria-hidden /> : <Menu {...iconMd} aria-hidden />}
            <span className="visually-hidden">Ouvrir ou fermer le menu</span>
          </button>
        </div>

        <form className="search-form" role="search" onSubmit={handleSearch}>
          <label htmlFor="header-search">Rechercher sur Ecomarket</label>
          <div className="search-inner">
            <input
              id="header-search"
              type="search"
              name="q"
              autoComplete="off"
              placeholder="Articles, marques, mots-clés…"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
            <button type="submit">Rechercher</button>
          </div>
        </form>

        <nav
          id="site-nav"
          className={`nav ${mobileNavOpen ? 'nav--open' : ''}`}
          aria-label="Navigation principale"
        >
          <NavLink to="/listings" className={({ isActive }) => navClass(isActive)} end>
            Parcourir
          </NavLink>
          <NavLink
            to={{ pathname: '/listings', search: '?international=1' }}
            className={() =>
              navClass(location.pathname === '/listings' && location.search.includes('international=1'))
            }
            onClick={() => setMobileNavOpen(false)}
            title="Annonces des vendeurs internationaux"
          >
            <Globe {...iconMd} aria-hidden style={{ verticalAlign: 'middle' }} />
            <span style={{ marginLeft: 6 }}>International</span>
          </NavLink>
          <NavLink to="/help" className={({ isActive }) => navClass(isActive)}>
            Aide
          </NavLink>
          <ThemeToggleButton
            theme={theme}
            onClick={() => {
              toggleTheme()
              setMobileNavOpen(false)
            }}
          />
          {isAuthenticated ? (
            <>
              {user != null && !isStaffRole(user) && !isLivreurRole(user) && (
                <NavLink
                  to="/panier"
                  className={({ isActive }) => navClass(isActive)}
                  onClick={() => setMobileNavOpen(false)}
                  style={{ position: 'relative' }}
                >
                  <ShoppingCart {...iconMd} aria-hidden style={{ verticalAlign: 'middle' }} />
                  <span style={{ marginLeft: 6 }}>Panier</span>
                  {itemCount > 0 && (
                    <span className="nav-cart-badge">{itemCount > 99 ? '99+' : itemCount}</span>
                  )}
                </NavLink>
              )}
              <button type="button" onClick={handleSellClick}>
                {user && isLivreurRole(user) ? 'Livraisons' : 'Vendre'}
              </button>
              {user != null && isLivreurRole(user) && (
                <NavLink to="/livreur" className={({ isActive }) => navClass(isActive)} onClick={() => setMobileNavOpen(false)}>
                  Espace livreur
                </NavLink>
              )}
              <Link to="/chat" className="nav-messages-link" onClick={() => setMobileNavOpen(false)}>
                Messages
                <NotificationBadge count={unreadCount} />
              </Link>
              <div className="account-menu-wrap" ref={accountRef}>
                <button
                  type="button"
                  className="account-menu-trigger"
                  aria-expanded={showMenu}
                  aria-haspopup="true"
                  onClick={(e) => {
                    e.stopPropagation()
                    setShowMenu((m) => !m)
                  }}
                >
                  <UserRound {...iconMd} aria-hidden />
                  <span className="visually-hidden">Menu compte</span>
                </button>
                {showMenu && (
                  <div className="account-dropdown">
                    <div className="account-dropdown-header">
                      <div style={{ fontWeight: 600, marginBottom: 4 }}>
                        {user?.prenoms} {user?.nom}
                      </div>
                      <div style={{ fontSize: '0.85em', color: 'var(--muted)' }}>{user?.email}</div>
                      <div style={{ fontSize: '0.8em', color: 'var(--muted)', marginTop: 4 }}>
                        {user?.accountType === 'seller'
                          ? 'Vendeur'
                          : user && isLivreurRole(user)
                            ? 'Livreur'
                            : 'Acheteur'}
                      </div>
                    </div>
                    {isStaffRole(user) ? (
                      <Link to="/admin" onClick={() => setShowMenu(false)}>
                        Dashboard admin
                      </Link>
                    ) : user && isLivreurRole(user) ? (
                      <>
                        <Link to="/livreur" onClick={() => setShowMenu(false)}>
                          Espace livreur
                        </Link>
                        <Link to="/profile" onClick={() => setShowMenu(false)}>
                          Mon compte
                        </Link>
                      </>
                    ) : user?.accountType === 'seller' ? (
                      <>
                        <Link to="/vendor" onClick={() => setShowMenu(false)}>
                          Dashboard vendeur
                        </Link>
                        <Link to="/profile" onClick={() => setShowMenu(false)}>
                          Mon compte
                        </Link>
                      </>
                    ) : (
                      <>
                        <Link to="/demande-upgrade" onClick={() => setShowMenu(false)}>
                          Devenir vendeur / livreur
                        </Link>
                        <Link to="/profile" onClick={() => setShowMenu(false)}>
                          Mon compte
                        </Link>
                      </>
                    )}
                    <button
                      type="button"
                      className="account-dropdown-logout"
                      onClick={() => {
                        logout()
                        setShowMenu(false)
                      }}
                    >
                      Déconnexion
                    </button>
                  </div>
                )}
              </div>
            </>
          ) : (
            <>
              <Link to="/auth?mode=login" className="link-button" onClick={() => setMobileNavOpen(false)}>
                Se connecter
              </Link>
              <Link to="/auth?mode=register" className="button-primary" onClick={() => setMobileNavOpen(false)}>
                S&apos;inscrire
              </Link>
              <button type="button" onClick={handleSellClick}>
                Vendre
              </button>
            </>
          )}
        </nav>
      </div>
    </header>
  )
}
