import { BookOpen, Check, LifeBuoy, Lightbulb, List, Mail, MapPin, MessageSquare, Phone, Plus, Sparkles } from 'lucide-react'
import { iconLg } from '../components/ui/iconProps'

const accent = 'var(--accent)'

const linkStyle = { color: accent } as const

export default function Guide() {
  return (
    <div className="container" style={{ maxWidth: '900px', paddingTop: 40, paddingBottom: 60 }}>
      <h1 style={{ fontSize: '2.5em', marginBottom: 8, display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
        <BookOpen {...iconLg} aria-hidden style={{ color: accent }} />
        Guide d&apos;utilisation Ecomarket
      </h1>
      <p className="meta" style={{ fontSize: '1.1em', marginBottom: 40 }}>
        Bienvenue sur Ecomarket, votre plateforme de vente et d&apos;achat d&apos;articles d&apos;occasion.
      </p>

      <div className="card" style={{ padding: 24, marginBottom: 40, background: 'var(--surface)' }}>
        <h2 style={{ marginBottom: 16, display: 'flex', alignItems: 'center', gap: 10 }}>
          <List {...iconLg} aria-hidden style={{ color: accent }} />
          Table des matières
        </h2>
        <ul style={{ lineHeight: 2 }}>
          <li><a href="#acheteurs" style={linkStyle}>Pour les acheteurs</a></li>
          <li><a href="#vendeurs" style={linkStyle}>Pour les vendeurs</a></li>
          <li><a href="#connexion" style={linkStyle}>Connexion</a></li>
          <li><a href="#faq" style={linkStyle}>FAQ</a></li>
          <li><a href="#support" style={linkStyle}>Support</a></li>
        </ul>
      </div>

      <section id="acheteurs" style={{ marginBottom: 60 }}>
        <h2 style={{ fontSize: '2em', marginBottom: 24, color: accent }}>Pour les acheteurs</h2>

        <div className="card" style={{ padding: 24, marginBottom: 24 }}>
          <h3 style={{ marginBottom: 16 }}>Créer un compte acheteur</h3>
          <ol style={{ lineHeight: 1.8 }}>
            <li><strong>Étape 1</strong> : Cliquez sur &quot;Connexion&quot; puis &quot;S&apos;inscrire&quot;</li>
            <li><strong>Étape 2</strong> : Renseignez votre nom et prénoms</li>
            <li><strong>Étape 3</strong> : Créez vos identifiants (email et mot de passe)</li>
            <li><strong>Étape 4</strong> :
              <ul style={{ marginTop: 8 }}>
                <li>Sélectionnez &quot;Acheteur&quot; comme type de compte</li>
                <li>Téléchargez votre CNI ou passeport</li>
                <li>Choisissez votre pays et ville</li>
              </ul>
            </li>
          </ol>
          <p style={{ marginTop: 16, color: accent, fontWeight: 600, display: 'flex', alignItems: 'center', gap: 8 }}>
            <Check size={18} strokeWidth={2} aria-hidden />
            Votre compte est créé.
          </p>
        </div>

        <div className="card" style={{ padding: 24, marginBottom: 24 }}>
          <h3 style={{ marginBottom: 16 }}>Rechercher des articles</h3>
          <ul style={{ lineHeight: 1.8 }}>
            <li><strong>Page d&apos;accueil</strong> : découvrez les articles en vedette</li>
            <li><strong>Catalogue</strong> : consultez tous les articles disponibles</li>
            <li><strong>Filtres</strong> : filtrez par catégorie, devise ou localisation</li>
            <li><strong>Recherche</strong> : utilisez la barre de recherche pour un article précis</li>
          </ul>
        </div>

        <div className="card" style={{ padding: 24 }}>
          <h3 style={{ marginBottom: 16 }}>Contacter un vendeur</h3>
          <p style={{ lineHeight: 1.8 }}>
            Sur la page détail d&apos;un article, utilisez le bouton <strong>« Contacter le vendeur »</strong> pour échanger
            directement et négocier si besoin.
          </p>
        </div>
      </section>

      <section id="vendeurs" style={{ marginBottom: 60 }}>
        <h2 style={{ fontSize: '2em', marginBottom: 24, color: accent }}>Pour les vendeurs</h2>

        <div className="card" style={{ padding: 24, marginBottom: 24 }}>
          <h3 style={{ marginBottom: 16 }}>Créer un compte vendeur</h3>
          <ol style={{ lineHeight: 1.8 }}>
            <li><strong>Étape 1 à 3</strong> : mêmes étapes que pour un acheteur</li>
            <li><strong>Étape 4</strong> :
              <ul style={{ marginTop: 8 }}>
                <li>Sélectionnez <strong>&quot;Vendeur&quot;</strong></li>
                <li>Choisissez votre type de produits (électronique, vêtements, électroménager…)</li>
                <li>Téléchargez votre CNI ou passeport</li>
                <li>Indiquez votre pays et ville</li>
              </ul>
            </li>
            <li><strong>Étape 5</strong> : publiez votre premier article avec photos, description et prix</li>
          </ol>
        </div>

        <div className="card" style={{ padding: 24, marginBottom: 24 }}>
          <h3 style={{ marginBottom: 16 }}>Tableau de bord vendeur</h3>
          <p style={{ lineHeight: 1.8, marginBottom: 16 }}>
            Une fois connecté, accédez à votre espace vendeur où vous pouvez notamment :
          </p>
          <ul style={{ lineHeight: 1.8 }}>
            <li><Plus size={14} strokeWidth={2} style={{ verticalAlign: 'middle', marginRight: 6 }} aria-hidden /> <strong>Ajouter de nouveaux articles</strong></li>
            <li><strong>Voir vos statistiques</strong> (nombre de vues par article)</li>
            <li><strong>Modifier les prix</strong> de vos articles</li>
            <li><strong>Gérer vos publications</strong></li>
          </ul>
        </div>

        <div className="card" style={{ padding: 24, marginBottom: 24 }}>
          <h3 style={{ marginBottom: 16 }}>Publier un article</h3>
          <div style={{ lineHeight: 1.8 }}>
            <p style={{ marginBottom: 12 }}><strong>Libellé</strong> : titre court et clair</p>
            <p style={{ marginBottom: 12 }}><strong>Photos</strong> : 1 à 6 photos (JPG, PNG, WEBP)</p>
            <div style={{ background: 'var(--input-bg)', padding: 12, borderRadius: 8, marginBottom: 12 }}>
              <p style={{ fontSize: '0.9em', color: 'var(--muted)', display: 'flex', alignItems: 'center', gap: 8 }}>
                <Lightbulb size={16} strokeWidth={1.75} aria-hidden />
                Conseils photos
              </p>
              <ul style={{ fontSize: '0.9em', color: 'var(--muted)', marginTop: 8 }}>
                <li>Bon éclairage naturel</li>
                <li>Plusieurs angles de vue</li>
                <li>Montrez les défauts si présents</li>
                <li>Fond neutre de préférence</li>
              </ul>
            </div>
            <p style={{ marginBottom: 12 }}><strong>Description</strong> : état, caractéristiques, défauts éventuels</p>
            <p style={{ marginBottom: 12 }}><strong>Prix</strong> : montant et devise (FCFA, €, $, etc.)</p>
            <p style={{ marginTop: 16, color: 'var(--muted)', fontSize: '0.9em', display: 'flex', alignItems: 'flex-start', gap: 8 }}>
              <MapPin size={16} strokeWidth={1.75} aria-hidden style={{ flexShrink: 0, marginTop: 2 }} />
              Votre pays et ville sont automatiquement associés à l&apos;article.
            </p>
          </div>
        </div>

        <div className="card" style={{ padding: 24 }}>
          <h3 style={{ marginBottom: 16 }}>Suivre les performances</h3>
          <p style={{ lineHeight: 1.8 }}>
            Chaque article affiche un <strong>compteur de vues</strong>. Utilisez cette information pour ajuster vos prix
            et améliorer vos annonces.
          </p>
        </div>
      </section>

      <section id="connexion" style={{ marginBottom: 60 }}>
        <h2 style={{ fontSize: '2em', marginBottom: 24, color: accent }}>Connexion</h2>

        <div className="card" style={{ padding: 24 }}>
          <ol style={{ lineHeight: 1.8 }}>
            <li>Cliquez sur <strong>&quot;Connexion&quot;</strong> dans l&apos;en-tête</li>
            <li>Saisissez votre <strong>email</strong> et <strong>mot de passe</strong></li>
            <li>Validez avec <strong>&quot;Se connecter&quot;</strong></li>
          </ol>
          <p style={{ marginTop: 16, color: accent, display: 'flex', alignItems: 'center', gap: 8 }}>
            <Check size={18} strokeWidth={2} aria-hidden />
            Une notification de bienvenue peut s&apos;afficher après connexion.
          </p>
        </div>
      </section>

      <section id="faq" style={{ marginBottom: 60 }}>
        <h2 style={{ fontSize: '2em', marginBottom: 24, color: accent }}>FAQ</h2>

        <div style={{ display: 'grid', gap: 16 }}>
          <div className="card" style={{ padding: 20 }}>
            <h4 style={{ marginBottom: 8, color: 'var(--text)' }}>La plateforme est-elle gratuite ?</h4>
            <p className="meta">Oui, l&apos;inscription et la publication d&apos;articles sont gratuites.</p>
          </div>

          <div className="card" style={{ padding: 20 }}>
            <h4 style={{ marginBottom: 8, color: 'var(--text)' }}>Dans quels pays puis-je vendre ou acheter ?</h4>
            <p className="meta">14 pays disponibles : Burkina Faso, Côte d&apos;Ivoire, Sénégal, Mali, Bénin, Togo, Niger, Guinée, Ghana, Nigeria, Cameroun, France, Canada, États-Unis.</p>
          </div>

          <div className="card" style={{ padding: 20 }}>
            <h4 style={{ marginBottom: 8, color: 'var(--text)' }}>Combien d&apos;articles puis-je publier ?</h4>
            <p className="meta">Aucune limite fixe : publiez selon vos besoins.</p>
          </div>

          <div className="card" style={{ padding: 20 }}>
            <h4 style={{ marginBottom: 8, color: 'var(--text)' }}>Comment se fait le paiement ?</h4>
            <p className="meta">Le paiement s&apos;organise entre acheteur et vendeur selon les moyens proposés sur la plateforme.</p>
          </div>

          <div className="card" style={{ padding: 20 }}>
            <h4 style={{ marginBottom: 8, color: 'var(--text)' }}>Puis-je modifier un article après publication ?</h4>
            <p className="meta">Vous pouvez notamment modifier le prix. Pour d&apos;autres changements, contactez le support.</p>
          </div>

          <div className="card" style={{ padding: 20 }}>
            <h4 style={{ marginBottom: 8, color: 'var(--text)' }}>Mes données sont-elles protégées ?</h4>
            <p className="meta">Des mesures de sécurité sont appliquées ; consultez aussi les conditions d&apos;utilisation.</p>
          </div>
        </div>
      </section>

      <section id="support" style={{ marginBottom: 40 }}>
        <h2 style={{ fontSize: '2em', marginBottom: 24, color: accent, display: 'flex', alignItems: 'center', gap: 10 }}>
          <LifeBuoy {...iconLg} aria-hidden />
          Support et assistance
        </h2>

        <div className="card" style={{ padding: 24 }}>
          <h3 style={{ marginBottom: 16 }}>Besoin d&apos;aide ?</h3>
          <div style={{ lineHeight: 2 }}>
            <p style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Mail size={16} strokeWidth={1.75} aria-hidden />
              <strong>Email</strong> : support@ecomarket.com
            </p>
            <p style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <MessageSquare size={16} strokeWidth={1.75} aria-hidden />
              <strong>Messagerie</strong> : disponible sur la plateforme
            </p>
            <p style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Phone size={16} strokeWidth={1.75} aria-hidden />
              <strong>Téléphone</strong> : +226 XX XX XX XX
            </p>
            <p className="meta" style={{ marginTop: 16 }}>Horaires : lundi – vendredi, 8h – 18h (GMT)</p>
          </div>
        </div>
      </section>

      <div
        className="card"
        style={{
          padding: 24,
          background: 'linear-gradient(135deg, var(--surface) 0%, var(--surface-elevated) 100%)',
          border: `2px solid ${accent}`,
        }}
      >
        <h2 style={{ marginBottom: 16, color: accent }}>Conseils pour réussir</h2>

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
            gap: 24,
          }}
        >
          <div>
            <h3 style={{ fontSize: '1.2em', marginBottom: 12, color: 'var(--text)' }}>Pour les vendeurs</h3>
            <ul style={{ lineHeight: 1.8, paddingLeft: 0, listStyle: 'none' }}>
              {['Photos nettes et représentatives', 'Prix cohérents avec le marché', 'Descriptions complètes', 'Réponses rapides aux acheteurs', "Transparence sur l'état du produit"].map((t) => (
                <li key={t} style={{ display: 'flex', gap: 8, alignItems: 'flex-start', marginBottom: 6 }}>
                  <Check size={16} strokeWidth={2} aria-hidden style={{ flexShrink: 0, marginTop: 3, color: accent }} />
                  {t}
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h3 style={{ fontSize: '1.2em', marginBottom: 12, color: 'var(--text)' }}>Pour les acheteurs</h3>
            <ul style={{ lineHeight: 1.8, paddingLeft: 0, listStyle: 'none' }}>
              {['Vérifiez titre, photos et description', 'Posez vos questions avant paiement', 'Comparez les offres', 'Privilégiez les échanges clairs', 'Privilégiez des remises en main propres sûres'].map((t) => (
                <li key={t} style={{ display: 'flex', gap: 8, alignItems: 'flex-start', marginBottom: 6 }}>
                  <Check size={16} strokeWidth={2} aria-hidden style={{ flexShrink: 0, marginTop: 3, color: accent }} />
                  {t}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>

      <div style={{ marginTop: 40, textAlign: 'center', padding: 24, borderTop: '1px solid var(--border)' }}>
        <p style={{ fontSize: '1.2em', marginBottom: 8, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10, flexWrap: 'wrap' }}>
          <Sparkles size={22} strokeWidth={1.55} aria-hidden style={{ color: accent }} />
          Merci d&apos;utiliser Ecomarket.
        </p>
        <p className="meta">Bonne vente et bon achat.</p>
      </div>
    </div>
  )
}
