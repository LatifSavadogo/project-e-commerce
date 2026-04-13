import { Link } from 'react-router-dom'

export default function Footer() {
  const year = new Date().getFullYear()
  return (
    <footer className="site-footer">
      <div className="footer-inner container">
        <span>© {year} Ecomarket</span>
        <nav className="footer-links" aria-label="Liens de pied de page">
          <Link to="/help">Aide</Link>
          <Link to="/guide">Guide</Link>
          <Link to="/terms">Conditions</Link>
          <Link to="/listings">Catalogue</Link>
        </nav>
      </div>
    </footer>
  )
}
