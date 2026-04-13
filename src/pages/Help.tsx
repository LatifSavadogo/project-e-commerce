import { Link } from 'react-router-dom'
import { AlertTriangle, BookOpen, Bot, ChevronRight, FileText } from 'lucide-react'
import { iconLg } from '../components/ui/iconProps'

export default function Help() {
  return (
    <section className="container" style={{ paddingTop: 24, paddingBottom: 48 }}>
      <h1 style={{ marginBottom: 8 }}>Centre d&apos;aide</h1>
      <p className="meta" style={{ maxWidth: 560, lineHeight: 1.6 }}>
        Guides, assistant automatisé et contact pour les problèmes sur la plateforme.
      </p>

      <div className="help-grid">
        <Link to="/guide" className="help-card">
          <div className="help-card-head">
            <BookOpen {...iconLg} aria-hidden />
            <div>
              <h2>Guide d&apos;utilisation</h2>
              <p>Acheter, vendre, négocier et finaliser un achat en toute sécurité.</p>
            </div>
          </div>
          <span className="help-card-cta">
            Ouvrir le guide <ChevronRight size={16} strokeWidth={1.65} style={{ verticalAlign: 'middle' }} />
          </span>
        </Link>

        <Link to="/help/assistant" className="help-card">
          <div className="help-card-head">
            <Bot {...iconLg} aria-hidden />
            <div>
              <h2>Assistant (FAQ)</h2>
              <p>Réponses rapides aux questions fréquentes, connecté au service assistant.</p>
            </div>
          </div>
          <span className="help-card-cta">
            Poser une question <ChevronRight size={16} strokeWidth={1.65} style={{ verticalAlign: 'middle' }} />
          </span>
        </Link>

        <Link to="/help/complaint" className="help-card">
          <div className="help-card-head">
            <AlertTriangle {...iconLg} aria-hidden />
            <div>
              <h2>Plainte ou signalement</h2>
              <p>Signaler un problème, une annonce ou un comportement inapproprié.</p>
            </div>
          </div>
          <span className="help-card-cta">
            Envoyer un message <ChevronRight size={16} strokeWidth={1.65} style={{ verticalAlign: 'middle' }} />
          </span>
        </Link>

        <Link to="/terms" className="help-card">
          <div className="help-card-head">
            <FileText {...iconLg} aria-hidden />
            <div>
              <h2>Conditions &amp; sécurité</h2>
              <p>Conditions d&apos;utilisation et bonnes pratiques sur Ecomarket.</p>
            </div>
          </div>
          <span className="help-card-cta">
            Lire les conditions <ChevronRight size={16} strokeWidth={1.65} style={{ verticalAlign: 'middle' }} />
          </span>
        </Link>
      </div>
    </section>
  )
}
