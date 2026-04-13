import { useParams, useNavigate, Link } from 'react-router-dom'
import { MapPin, MessageCircle, ShoppingCart } from 'lucide-react'
import { useProducts } from '../contexts/ProductContext'
import { useAuth } from '../contexts/AuthContext'
import { useCart } from '../contexts/CartContext'
import { ApiError } from '../services/apiClient'
import { useState, useEffect } from 'react'
import type { Product } from '../contexts/ProductContext'
import PaymentPanel from '../components/PaymentPanel'
import { iconSm } from '../components/ui/iconProps'

const currencySymbols: Record<string, string> = {
  XOF: 'FCFA',
  EUR: '€',
  USD: '$',
  GBP: '£',
  JPY: '¥',
  CHF: 'CHF',
  CAD: 'CAD',
  AUD: 'AUD',
  CNY: '¥',
  INR: '₹',
  BRL: 'R$',
  ZAR: 'ZAR',
  NGN: '₦',
  GHS: '₵',
  KES: 'KES',
  MAD: 'MAD',
  TND: 'TND',
  DZD: 'DZD'
}

export default function ProductDetail() {
  const { id } = useParams<{ id: string }>()
  const { products, getProductByIdFromApi, incrementViews } = useProducts()
  const { user } = useAuth()
  const { addToCart } = useCart()
  const navigate = useNavigate()
  const [product, setProduct] = useState<Product | null>(null)
  const [selectedImage, setSelectedImage] = useState<string>('')
  const [wideLayout, setWideLayout] = useState(() =>
    typeof window !== 'undefined' ? window.innerWidth > 768 : true
  )
  const [payOkId, setPayOkId] = useState<number | null>(null)
  const [cartQty, setCartQty] = useState('1')
  const [cartBusy, setCartBusy] = useState(false)
  const [cartMsg, setCartMsg] = useState<string | null>(null)

  useEffect(() => {
    const onResize = () => setWideLayout(window.innerWidth > 768)
    window.addEventListener('resize', onResize)
    return () => window.removeEventListener('resize', onResize)
  }, [])

  useEffect(() => {
    if (!id) return
    const foundProduct = products.find((p) => p.id === id)
    if (foundProduct) {
      setProduct(foundProduct)
      setSelectedImage(foundProduct.image)
      void incrementViews(id)
      return
    }
    let cancelled = false
    ;(async () => {
      const fetched = await getProductByIdFromApi(id)
      if (cancelled) return
      if (fetched) {
        setProduct(fetched)
        setSelectedImage(fetched.image)
        void incrementViews(id)
      } else {
        navigate('/listings')
      }
    })()
    return () => {
      cancelled = true
    }
  }, [id, products, navigate, getProductByIdFromApi, incrementViews])

  if (!product) return null

  const currencyDisplay = currencySymbols[product.currency] || product.currency
  const priceDisplay = product.currency === 'XOF' 
    ? `${product.price.toFixed(0)} ${currencyDisplay}` 
    : `${product.price.toFixed(2)} ${currencyDisplay}`

  const images = product.images || [product.image]

  const handleContactVendor = () => {
    if (!user) {
      navigate('/auth')
      return
    }
    navigate(`/chat?productId=${product.id}`)
  }

  const isOwner = !!(user && product.sellerId && user.id === product.sellerId)
  const canPay = !!(user && !isOwner && !product.isBlocked)
  const canAddToCart = canPay

  return (
    <div className="container" style={{ paddingTop: 20, paddingBottom: 40 }}>
      <button 
        onClick={() => navigate(-1)} 
        style={{ 
          marginBottom: 20, 
          background: 'transparent', 
          border: '1px solid var(--border)',
          padding: '8px 16px'
        }}
      >
        ← Retour
      </button>

      {product.isBlocked && (
        <div
          style={{
            marginBottom: 16,
            padding: 12,
            borderRadius: 8,
            background: 'rgba(220,38,38,0.12)',
            border: '1px solid rgba(220,38,38,0.4)',
            color: '#fca5a5',
          }}
        >
          Cette annonce n’est plus disponible à l’achat.
        </div>
      )}
      {product.hasWarning && product.warningMessage && (
        <div
          style={{
            marginBottom: 16,
            padding: 12,
            borderRadius: 8,
            background: 'rgba(245,158,11,0.12)',
            border: '1px solid rgba(245,158,11,0.4)',
            color: '#fcd34d',
          }}
        >
          Avertissement : {product.warningMessage}
        </div>
      )}

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: wideLayout ? '1fr 1fr' : '1fr',
          gap: 40,
        }}
      >
        {/* Galerie de photos */}
        <div>
          {/* Image principale */}
          <div style={{ 
            background: 'var(--surface)', 
            border: '1px solid var(--border)', 
            borderRadius: 12,
            overflow: 'hidden',
            marginBottom: 16
          }}>
            <img 
              src={selectedImage} 
              alt={product.title}
              style={{ 
                width: '100%', 
                height: '400px', 
                objectFit: 'contain',
                display: 'block',
                background: 'var(--input-bg)'
              }}
            />
          </div>

          {/* Miniatures */}
          {images.length > 1 && (
            <div style={{ 
              display: 'grid', 
              gridTemplateColumns: 'repeat(auto-fill, minmax(80px, 1fr))',
              gap: 8
            }}>
              {images.map((img, index) => (
                <div 
                  key={index}
                  onClick={() => setSelectedImage(img)}
                  style={{
                    border: selectedImage === img ? '2px solid #2a9d8f' : '1px solid var(--border)',
                    borderRadius: 8,
                    overflow: 'hidden',
                    cursor: 'pointer',
                    transition: 'border-color 0.2s'
                  }}
                >
                  <img 
                    src={img} 
                    alt={`${product.title} - ${index + 1}`}
                    style={{ 
                      width: '100%', 
                      height: '80px', 
                      objectFit: 'cover',
                      display: 'block'
                    }}
                  />
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Informations du produit */}
        <div>
          <h1 style={{ fontSize: '2em', marginBottom: 16, marginTop: 0 }}>{product.title}</h1>
          
          <div style={{ 
            fontSize: '2em', 
            fontWeight: 700, 
            color: '#2a9d8f',
            marginBottom: 24
          }}>
            {priceDisplay}
          </div>

          {product.description && (
            <div style={{ marginBottom: 24 }}>
              <h3 style={{ fontSize: '1.2em', marginBottom: 12, color: 'var(--text)' }}>Description</h3>
              <p style={{ 
                lineHeight: 1.6, 
                color: 'var(--muted)',
                whiteSpace: 'pre-wrap'
              }}>
                {product.description}
              </p>
            </div>
          )}

          {(product.brand || product.size || product.category || product.city || product.country) && (
            <div style={{ marginBottom: 24 }}>
              <h3 style={{ fontSize: '1.2em', marginBottom: 12, color: 'var(--text)' }}>Détails</h3>
              <div style={{ display: 'grid', gap: 8 }}>
                {product.brand && (
                  <div style={{ display: 'flex', gap: 8 }}>
                    <span style={{ color: 'var(--muted)' }}>Marque:</span>
                    <span style={{ color: 'var(--text)', fontWeight: 600 }}>{product.brand}</span>
                  </div>
                )}
                {product.size && (
                  <div style={{ display: 'flex', gap: 8 }}>
                    <span style={{ color: 'var(--muted)' }}>Taille:</span>
                    <span style={{ color: 'var(--text)', fontWeight: 600 }}>{product.size}</span>
                  </div>
                )}
                {product.category && (
                  <div style={{ display: 'flex', gap: 8 }}>
                    <span style={{ color: 'var(--muted)' }}>Catégorie:</span>
                    <span style={{ color: 'var(--text)', fontWeight: 600 }}>{product.category}</span>
                  </div>
                )}
                {(product.city || product.country) && (
                  <div style={{ display: 'flex', gap: 8 }}>
                    <span style={{ color: 'var(--muted)' }}>Localisation:</span>
                    <span style={{ color: 'var(--text)', fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                      <MapPin {...iconSm} aria-hidden style={{ flexShrink: 0, opacity: 0.85 }} />
                      {product.city}{product.city && product.country ? ', ' : ''}{product.country}
                    </span>
                  </div>
                )}
              </div>
            </div>
          )}

          <div style={{ display: 'grid', gap: 12, marginTop: 32 }}>
            {!user && (
              <p className="meta" style={{ margin: 0 }}>
                Connectez-vous pour proposer un prix (négociation structurée) ou payer au prix affiché.
              </p>
            )}
            {isOwner && (
              <p className="meta" style={{ margin: 0 }}>
                Ceci est votre annonce.
              </p>
            )}
            {canAddToCart && (
              <div
                style={{
                  display: 'flex',
                  flexWrap: 'wrap',
                  gap: 10,
                  alignItems: 'center',
                  padding: 14,
                  borderRadius: 12,
                  border: '1px solid color-mix(in srgb, var(--accent) 28%, transparent)',
                  background: 'color-mix(in srgb, var(--accent) 6%, transparent)',
                }}
              >
                <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: '0.9em' }}>
                  Quantité
                  <input
                    type="number"
                    min={1}
                    max={100}
                    value={cartQty}
                    onChange={(e) => setCartQty(e.target.value)}
                    style={{ width: 72, padding: '8px 10px', borderRadius: 8 }}
                  />
                </label>
                <button
                  type="button"
                  className="button-primary"
                  disabled={cartBusy}
                  onClick={() => {
                    void (async () => {
                      setCartMsg(null)
                      const q = parseInt(cartQty, 10)
                      if (!q || q < 1 || q > 100) {
                        setCartMsg('Quantité entre 1 et 100.')
                        return
                      }
                      setCartBusy(true)
                      try {
                        await addToCart(Number(product.id), q)
                        setCartMsg('Article ajouté au panier.')
                      } catch (e) {
                        setCartMsg(e instanceof ApiError ? e.message : e instanceof Error ? e.message : 'Erreur panier')
                      } finally {
                        setCartBusy(false)
                      }
                    })()
                  }}
                  style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}
                >
                  <ShoppingCart {...iconSm} aria-hidden />
                  {cartBusy ? 'Ajout…' : 'Ajouter au panier'}
                </button>
                <Link to="/panier" style={{ fontWeight: 600, fontSize: '0.9em' }}>
                  Voir le panier →
                </Link>
                {cartMsg && (
                  <span style={{ flex: '1 1 100%', fontSize: '0.88em', color: 'var(--accent)' }}>{cartMsg}</span>
                )}
              </div>
            )}
            <button
              type="button"
              onClick={handleContactVendor}
              disabled={!user || isOwner || product.isBlocked}
              style={{
                background: 'var(--accent)',
                border: 'none',
                padding: '14px',
                fontSize: '1.1em',
                fontWeight: 600,
                cursor: !user || isOwner || product.isBlocked ? 'not-allowed' : 'pointer',
                opacity: !user || isOwner || product.isBlocked ? 0.5 : 1,
                color: '#fff',
                borderRadius: 12,
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 10,
              }}
            >
              <MessageCircle {...iconSm} aria-hidden />
              Contacter le vendeur (contre-proposition)
            </button>
            {canPay && (
              <PaymentPanel
                articleId={Number(product.id)}
                prixCatalogue={Math.round(product.price)}
                onSuccess={(id) => {
                  setPayOkId(id)
                  setTimeout(() => setPayOkId(null), 8000)
                }}
              />
            )}
            {payOkId != null && (
              <p style={{ color: 'var(--accent)', margin: 0, fontSize: '0.9em' }}>
                Paiement enregistré (transaction #{payOkId}). Téléchargez le reçu depuis votre profil → Mes achats.
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
