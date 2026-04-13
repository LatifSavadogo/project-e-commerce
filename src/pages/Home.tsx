import { Link } from 'react-router-dom'
import { Package, Plus, Sparkles } from 'lucide-react'
import ProductCard from '../components/ProductCard'
import { iconLg, iconSm } from '../components/ui/iconProps'
import { useAuth } from '../contexts/AuthContext'
import { useProducts } from '../contexts/ProductContext'
import type { Product } from '../contexts/ProductContext'

const featured: Product[] = [
  {
    id: '1',
    title: 'Veste en jean',
    price: 19,
    currency: 'EUR',
    image: 'https://images.unsplash.com/photo-1520975916090-3105956dac38?q=80&w=800&auto=format&fit=crop',
    size: 'M',
    brand: "Levi's",
    description: 'Coupe classique, denim bleu, idéale mi-saison.',
  },
  {
    id: '2',
    title: 'Robe fleurie',
    price: 12,
    currency: 'EUR',
    image: 'https://images.unsplash.com/photo-1516431882764-7cd4265f3bde?q=80&w=800&auto=format&fit=crop',
    size: 'S',
    brand: 'Zara',
    description: 'Imprimé floral léger, parfaite pour l’été.',
  },
  {
    id: '3',
    title: 'Baskets blanches',
    price: 35,
    currency: 'EUR',
    image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=800&auto=format&fit=crop',
    size: '40',
    brand: 'Nike',
    description: 'Semelle confort, look urbain intemporel.',
  },
  {
    id: '4',
    title: 'Sweat capuche',
    price: 15,
    currency: 'EUR',
    image: 'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?q=80&w=800&auto=format&fit=crop',
    size: 'L',
    brand: 'H&M',
    description: 'Molleton doux, capuche ajustable, coupe relax.',
  },
]

export default function Home() {
  const { user } = useAuth()
  const { products } = useProducts()

  const recentProducts = [...products].sort((a, b) => {
    return parseInt(b.id, 10) - parseInt(a.id, 10)
  }).slice(0, 8)

  const showSellerStrip = user != null && user.accountType === 'seller'

  return (
    <section className="container">
      {showSellerStrip && (
        <div
          className="card"
          style={{
            marginBottom: 24,
            padding: '14px 18px',
            display: 'flex',
            flexWrap: 'wrap',
            alignItems: 'center',
            gap: 12,
            justifyContent: 'space-between',
            border: '1px solid color-mix(in srgb, var(--accent) 35%, transparent)',
            background: 'color-mix(in srgb, var(--accent) 8%, transparent)',
          }}
        >
          <span style={{ fontSize: '0.9em', color: 'var(--text)' }}>
            Espace vendeur : publiez et gérez vos annonces depuis le tableau de bord.
          </span>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
            <Link
              to="/vendor"
              className="button-primary"
              style={{ display: 'inline-block', padding: '8px 16px', fontSize: '0.9em', textDecoration: 'none' }}
            >
              Tableau de bord
            </Link>
            <Link
              to="/vendor/add-product"
              style={{
                display: 'inline-block',
                padding: '8px 16px',
                fontSize: '0.9em',
                border: '1px solid var(--border)',
                borderRadius: 8,
                color: 'var(--text)',
                textDecoration: 'none',
              }}
            >
              <Plus {...iconSm} style={{ verticalAlign: 'middle', marginRight: 4 }} aria-hidden />
              Publier un article
            </Link>
          </div>
        </div>
      )}

      <div className="home-hero">
        <h1>Découvre les bonnes affaires</h1>
        <p className="meta">Inspire-toi et trouve ta prochaine pièce préférée.</p>
      </div>

      <div className="home-section-title">
        <h2 style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <Sparkles {...iconLg} aria-hidden style={{ color: 'var(--accent)' }} />
          Sélection du jour
        </h2>
      </div>
      <div className="home-grid" style={{ marginTop: 16 }}>
        {(products.length > 0 ? recentProducts.slice(0, 4) : featured).map((p) => (
          <ProductCard key={p.id} p={p} />
        ))}
      </div>

      {products.length > 0 && (
        <>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginTop: 48,
              marginBottom: 16,
              maxWidth: 900,
              marginLeft: 'auto',
              marginRight: 'auto',
            }}
          >
            <h2 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: 10 }}>
              <Package {...iconLg} aria-hidden style={{ color: 'var(--accent)' }} />
              Annonces récentes
            </h2>
            <Link
              to="/listings"
              style={{
                color: 'var(--link)',
                textDecoration: 'none',
                fontSize: '0.9em',
                fontWeight: 600,
              }}
            >
              Voir tout →
            </Link>
          </div>
          <p className="meta" style={{ marginBottom: 16, textAlign: 'center' }}>
            {products.length} nouvelle(s) annonce(s) de nos vendeurs
          </p>
          <div className="home-grid">
            {recentProducts.map((p) => (
              <ProductCard key={p.id} p={p} />
            ))}
          </div>
        </>
      )}
    </section>
  )
}
