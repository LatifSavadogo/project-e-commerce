import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Check, Package } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { iconLg, iconSm } from '../components/ui/iconProps'
import { useProducts } from '../contexts/ProductContext'

function RequiredAsterisk() {
  return (
    <span aria-hidden="true" style={{ color: '#dc2626', marginLeft: 4, fontWeight: 700 }}>
      *
    </span>
  )
}

export default function AddProduct() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const { addProduct } = useProducts()

  const [formData, setFormData] = useState({
    title: '',
    price: '',
    description: '',
    brand: '',
    size: '',
  })

  const [productPhotos, setProductPhotos] = useState<File[]>([])
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (!user || user.accountType !== 'seller') {
    navigate('/')
    return null
  }

  const handlePhotoUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || [])
    if (productPhotos.length + files.length <= 6) {
      setProductPhotos([...productPhotos, ...files])
    } else {
      alert('Vous ne pouvez télécharger que 6 photos maximum')
    }
  }

  const removePhoto = (index: number) => {
    setProductPhotos(productPhotos.filter((_, i) => i !== index))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsSubmitting(true)

    if (!formData.title.trim()) {
      alert('Veuillez entrer un titre')
      setIsSubmitting(false)
      return
    }

    if (!formData.price || parseFloat(formData.price) <= 0) {
      alert('Veuillez entrer un prix valide')
      setIsSubmitting(false)
      return
    }

    if (productPhotos.length === 0) {
      alert('Veuillez ajouter au moins une photo')
      setIsSubmitting(false)
      return
    }

    if (!user?.idtypeVendeur) {
      alert('Votre compte vendeur n’a pas de catégorie (type article). Contactez l’administrateur.')
      setIsSubmitting(false)
      return
    }

    try {
      await addProduct({
        title: formData.title.trim(),
        price: parseFloat(formData.price),
        description: formData.description.trim() || ' ',
        photos: productPhotos,
        idtype: user.idtypeVendeur,
      })
      alert('Votre produit a été publié avec succès !')
      navigate('/vendor')
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Erreur lors de la publication')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="container" style={{ paddingTop: 20, paddingBottom: 40 }}>
      <button onClick={() => navigate('/vendor')} style={{ marginBottom: 20 }}>
        ← Retour au Dashboard
      </button>

      <div style={{ maxWidth: 800, margin: '0 auto' }}>
        <h1 style={{ marginBottom: 24, display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
          <Package {...iconLg} aria-hidden style={{ color: 'var(--accent)' }} />
          Ajouter un nouveau produit
        </h1>

        <form onSubmit={handleSubmit} className="card" style={{ padding: 24 }}>
          <div style={{ display: 'grid', gap: 20 }}>
            {/* Titre */}
            <div>
              <label style={{ display: 'block', marginBottom: 8, fontSize: '0.9em', fontWeight: 600 }}>
                Titre du produit<RequiredAsterisk />
              </label>
              <input
                type="text"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                placeholder="Ex: iPhone 13 Pro Max 256 Go"
                required
                style={{
                  width: '100%',
                  padding: '12px 16px',
                  borderRadius: 8,
                  border: '1px solid var(--border)',
                  background: 'var(--input-bg)',
                  color: 'var(--text)',
                  fontSize: '0.95em'
                }}
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: 8, fontSize: '0.9em', fontWeight: 600 }}>
                Prix (FCFA)<RequiredAsterisk />
              </label>
              <input
                type="number"
                step="1"
                min="1"
                value={formData.price}
                onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                placeholder="Ex: 25000"
                required
                style={{
                  width: '100%',
                  padding: '12px 16px',
                  borderRadius: 8,
                  border: '1px solid var(--border)',
                  background: 'var(--input-bg)',
                  color: 'var(--text)',
                  fontSize: '0.95em',
                }}
              />
            </div>

            <div
              style={{
                padding: 12,
                borderRadius: 8,
                border: '1px solid #2a9d8f',
                background: 'rgba(42, 157, 143, 0.08)',
                fontSize: '0.9em',
              }}
            >
              <strong>Catégorie vendeur :</strong>{' '}
              {user.libtypeVendeur || user.productType || `Type #${user.idtypeVendeur}`} — les articles sont
              publiés uniquement dans ce type (règle serveur).
            </div>

            {/* Marque et Taille */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
              <div>
                <label style={{ display: 'block', marginBottom: 8, fontSize: '0.9em', fontWeight: 600 }}>
                  Marque
                </label>
                <input
                  type="text"
                  value={formData.brand}
                  onChange={(e) => setFormData({ ...formData, brand: e.target.value })}
                  placeholder="Ex: Apple, Samsung..."
                  style={{
                    width: '100%',
                    padding: '12px 16px',
                    borderRadius: 8,
                    border: '1px solid var(--border)',
                    background: 'var(--input-bg)',
                    color: 'var(--text)',
                    fontSize: '0.95em'
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: 8, fontSize: '0.9em', fontWeight: 600 }}>
                  Taille / Capacité
                </label>
                <input
                  type="text"
                  value={formData.size}
                  onChange={(e) => setFormData({ ...formData, size: e.target.value })}
                  placeholder="Ex: M, 256 Go..."
                  style={{
                    width: '100%',
                    padding: '12px 16px',
                    borderRadius: 8,
                    border: '1px solid var(--border)',
                    background: 'var(--input-bg)',
                    color: 'var(--text)',
                    fontSize: '0.95em'
                  }}
                />
              </div>
            </div>

            {/* Photos */}
            <div>
              <label style={{ display: 'block', marginBottom: 8, fontSize: '0.9em', fontWeight: 600 }}>
                Photos du produit<RequiredAsterisk /> (max 6)
              </label>
              <input
                type="file"
                accept="image/*"
                multiple
                onChange={handlePhotoUpload}
                style={{
                  width: '100%',
                  padding: '12px 16px',
                  borderRadius: 8,
                  border: '1px solid var(--border)',
                  background: 'var(--input-bg)',
                  color: 'var(--text)',
                  fontSize: '0.9em'
                }}
              />
              
              {productPhotos.length > 0 && (
                <div style={{ 
                  marginTop: 12, 
                  display: 'grid', 
                  gridTemplateColumns: 'repeat(auto-fill, minmax(100px, 1fr))', 
                  gap: 8 
                }}>
                  {productPhotos.map((photo, index) => (
                    <div key={index} style={{ position: 'relative', border: '2px solid var(--border)', borderRadius: 8, overflow: 'hidden' }}>
                      <img
                        src={URL.createObjectURL(photo)}
                        alt={`Photo ${index + 1}`}
                        style={{ width: '100%', height: '100px', objectFit: 'cover', display: 'block' }}
                      />
                      <button
                        type="button"
                        onClick={() => removePhoto(index)}
                        style={{
                          position: 'absolute',
                          top: 4,
                          right: 4,
                          background: 'rgba(0,0,0,0.7)',
                          color: 'white',
                          border: 'none',
                          borderRadius: '50%',
                          width: 24,
                          height: 24,
                          cursor: 'pointer',
                          fontSize: '14px',
                          padding: 0
                        }}
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              )}
              <p style={{ fontSize: '0.85em', color: 'var(--muted)', marginTop: 8 }}>
                {productPhotos.length}/6 photos
              </p>
            </div>

            {/* Description */}
            <div>
              <label style={{ display: 'block', marginBottom: 8, fontSize: '0.9em', fontWeight: 600 }}>
                Description
              </label>
              <textarea
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Décrivez votre produit en détail : état, caractéristiques, raison de la vente..."
                rows={5}
                style={{
                  width: '100%',
                  padding: '12px 16px',
                  borderRadius: 8,
                  border: '1px solid var(--border)',
                  background: 'var(--input-bg)',
                  color: 'var(--text)',
                  fontSize: '0.95em',
                  resize: 'vertical'
                }}
              />
            </div>

            {/* Boutons */}
            <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
              <button
                type="button"
                onClick={() => navigate('/vendor')}
                style={{
                  flex: 1,
                  padding: '14px',
                  borderRadius: 8,
                  border: '1px solid var(--border)',
                  background: 'transparent',
                  color: 'var(--text)',
                  fontSize: '1em',
                  fontWeight: 600,
                  cursor: 'pointer'
                }}
              >
                Annuler
              </button>
              <button
                type="submit"
                disabled={isSubmitting}
                style={{
                  flex: 1,
                  padding: '14px',
                  borderRadius: 10,
                  border: 'none',
                  background: isSubmitting ? 'var(--surface-elevated)' : 'var(--accent)',
                  color: isSubmitting ? 'var(--muted)' : 'white',
                  fontSize: '1em',
                  fontWeight: 600,
                  cursor: isSubmitting ? 'not-allowed' : 'pointer',
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: 8,
                }}
              >
                {isSubmitting ? (
                  'Publication...'
                ) : (
                  <>
                    <Check {...iconSm} aria-hidden />
                    Publier le produit
                  </>
                )}
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  )
}
