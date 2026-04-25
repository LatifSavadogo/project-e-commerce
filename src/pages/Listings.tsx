import { useState, useMemo, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { Sparkles } from 'lucide-react'
import ProductCard from '../components/ProductCard'
import { iconLg } from '../components/ui/iconProps'
import TypeArticleFilter from '../components/TypeArticleFilter'
import { useProducts } from '../contexts/ProductContext'

export default function Listings() {
  const { products, productsLoading, refreshProducts } = useProducts()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [selectedTypeId, setSelectedTypeId] = useState<string | null>(null)
  const international = searchParams.get('international') === '1'

  useEffect(() => {
    void refreshProducts(international ? 'international' : 'all')
  }, [international, refreshProducts])

  const searchRaw = (searchParams.get('q') || '').trim()
  const searchLower = searchRaw.toLowerCase()

  const sortedProducts = useMemo(
    () => [...products].sort((a, b) => Number(b.id) - Number(a.id)),
    [products]
  )

  const filteredProducts = useMemo(() => {
    let list = sortedProducts
    if (selectedTypeId) {
      list = list.filter((p) => p.idtype != null && String(p.idtype) === selectedTypeId)
    }
    if (searchLower) {
      list = list.filter((p) => {
        const title = p.title.toLowerCase()
        const brand = (p.brand || '').toLowerCase()
        const desc = (p.description || '').toLowerCase()
        return (
          title.includes(searchLower) ||
          brand.includes(searchLower) ||
          desc.includes(searchLower)
        )
      })
    }
    return list
  }, [sortedProducts, selectedTypeId, searchLower])

  const allProducts = filteredProducts

  return (
    <section className="container">
      <h1>{international ? 'Marché international' : 'Parcourir le catalogue'}</h1>
      <p className="meta" style={{ marginTop: 8 }}>
        {international
          ? 'Annonces publiées uniquement par les vendeurs inscrits sur le marché international.'
          : 'Découvrez les articles mis en vente. Utilisez la recherche dans l’en-tête pour filtrer par mot-clé.'}
      </p>
      {international && (
        <p className="meta" style={{ marginTop: 10 }}>
          <button type="button" className="link-button" onClick={() => navigate('/listings')} style={{ padding: 0 }}>
            Voir tout le catalogue
          </button>
        </p>
      )}
      {productsLoading && <p className="meta">Chargement du catalogue…</p>}

      {searchRaw && (
        <p className="meta" style={{ marginTop: 12 }}>
          Résultats pour « <strong style={{ color: 'var(--text)' }}>{searchRaw}</strong> » — {filteredProducts.length}{' '}
          annonce(s)
        </p>
      )}

      <TypeArticleFilter selectedTypeId={selectedTypeId} onSelectType={setSelectedTypeId} />
      
      {/* Annonces récentes */}
      {filteredProducts.length > 0 && (
        <>
          <div style={{
            background: 'var(--surface)',
            border: '1px solid var(--accent)',
            borderRadius: 12,
            padding: '12px 16px',
            marginTop: 20,
            marginBottom: 24,
            display: 'flex',
            alignItems: 'center',
            gap: 12
          }}>
            <Sparkles {...iconLg} aria-hidden style={{ color: 'var(--accent)', flexShrink: 0 }} />
            <div>
              <strong style={{ color: 'var(--accent)' }}>
                {selectedTypeId
                  ? `${filteredProducts.length} annonce(s) dans ce type`
                  : searchLower
                    ? `${filteredProducts.length} annonce(s) (recherche + filtres)`
                    : `${products.length} annonce(s) au catalogue`}
              </strong>
              <p className="meta" style={{ margin: '4px 0 0 0', fontSize: '0.9em' }}>
                {selectedTypeId
                  ? 'Articles correspondant au type sélectionné'
                  : searchLower
                    ? 'Affinez avec le filtre par type ci-dessous si besoin'
                    : international
                      ? 'Vendeurs internationaux'
                      : 'Articles publiés par les vendeurs'}
              </p>
            </div>
          </div>
        </>
      )}

      <div className="grid" style={{ marginTop: 16 }}>
        {allProducts.map((p) => (
          <ProductCard key={p.id} p={p} />
        ))}
      </div>

      {filteredProducts.length === 0 && products.length > 0 && (
        <div style={{
          textAlign: 'center',
          padding: '40px 20px',
          color: 'var(--muted)'
        }}>
          <p>
            {searchLower
              ? 'Aucun article ne correspond à cette recherche (essayez d’autres mots-clés).'
              : 'Aucune annonce dans cette catégorie.'}
          </p>
          <div style={{ display: 'flex', gap: 12, justifyContent: 'center', flexWrap: 'wrap', marginTop: 16 }}>
            {selectedTypeId && (
              <button type="button" onClick={() => setSelectedTypeId(null)}>
                Voir tous les types
              </button>
            )}
            {searchLower && (
              <button type="button" onClick={() => navigate('/listings')}>
                Effacer la recherche
              </button>
            )}
          </div>
        </div>
      )}
      
      {products.length === 0 && (
        <div style={{
          textAlign: 'center',
          padding: '40px 20px',
          color: 'var(--muted)'
        }}>
          <p>
            {international
              ? 'Aucune annonce sur le marché international pour le moment.'
              : 'Aucune annonce récente. Soyez le premier à publier !'}
          </p>
        </div>
      )}
    </section>
  )
}
