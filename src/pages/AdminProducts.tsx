import { useAuth, isStaffRole } from '../contexts/AuthContext'
import { useProducts, mapArticleDto, type Product } from '../contexts/ProductContext'
import { useNavigate } from 'react-router-dom'
import { useEffect, useState, useCallback } from 'react'
import { AlertTriangle, Ban, Check, Trash2 } from 'lucide-react'
import { fetchAdminArticles } from '../services/articleApi'

export default function AdminProducts() {
  const { user } = useAuth()
  const { deleteProduct, blockProduct, unblockProduct, addWarning, removeWarning, refreshProducts } =
    useProducts()
  const navigate = useNavigate()
  const [adminProducts, setAdminProducts] = useState<Product[]>([])
  const [adminLoading, setAdminLoading] = useState(true)
  const [warningProductId, setWarningProductId] = useState<string | null>(null)
  const [warningMessage, setWarningMessage] = useState('')
  const [sortBy, setSortBy] = useState<'recent' | 'oldest'>('recent')

  const loadAdminCatalog = useCallback(async () => {
    setAdminLoading(true)
    try {
      const dtos = await fetchAdminArticles()
      setAdminProducts(dtos.map(mapArticleDto))
    } catch {
      setAdminProducts([])
    } finally {
      setAdminLoading(false)
    }
  }, [])

  useEffect(() => {
    if (!user || !isStaffRole(user)) {
      navigate('/')
    }
  }, [user, navigate])

  useEffect(() => {
    if (user && isStaffRole(user)) {
      void loadAdminCatalog()
    }
  }, [user, loadAdminCatalog])

  if (!user) return null

  const sortedProducts = [...adminProducts].sort((a, b) => {
    const timeA = parseInt(a.id, 10)
    const timeB = parseInt(b.id, 10)
    return sortBy === 'recent' ? timeB - timeA : timeA - timeB
  })

  const syncAfterMutation = async () => {
    await loadAdminCatalog()
    await refreshProducts()
  }

  const handleRefresh = () => {
    void loadAdminCatalog()
    void refreshProducts()
    const btn = document.getElementById('refresh-btn')
    if (btn) {
      btn.style.transform = 'rotate(360deg)'
      setTimeout(() => {
        btn.style.transform = 'rotate(0deg)'
      }, 500)
    }
  }

  const handleDelete = async (id: string, title: string) => {
    if (!window.confirm(`Êtes-vous sûr de vouloir supprimer "${title}" ?`)) return
    try {
      await deleteProduct(id)
      await syncAfterMutation()
      alert('Publication supprimée avec succès')
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Erreur')
    }
  }

  const handleBlock = async (id: string, title: string) => {
    if (!window.confirm(`Bloquer la publication "${title}" ?`)) return
    try {
      await blockProduct(id)
      await syncAfterMutation()
      alert('Publication bloquée')
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Erreur')
    }
  }

  const handleUnblock = async (id: string) => {
    try {
      await unblockProduct(id)
      await syncAfterMutation()
      alert('Publication débloquée')
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Erreur')
    }
  }

  const handleAddWarning = async (id: string) => {
    if (!warningMessage.trim()) return
    try {
      await addWarning(id, warningMessage)
      setWarningProductId(null)
      setWarningMessage('')
      await syncAfterMutation()
      alert('Avertissement ajouté')
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Erreur')
    }
  }

  const handleRemoveWarning = async (id: string) => {
    try {
      await removeWarning(id)
      if (warningProductId === id) {
        setWarningProductId(null)
        setWarningMessage('')
      }
      await syncAfterMutation()
      alert('Avertissement retiré')
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Erreur')
    }
  }

  return (
    <div className="container" style={{ paddingTop: 20, paddingBottom: 40 }}>
      <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', marginBottom: 24 }}>
        <button
          id="refresh-btn"
          type="button"
          onClick={handleRefresh}
          style={{
            background: 'var(--admin-accent)',
            border: 'none',
            padding: '10px 20px',
            display: 'flex',
            alignItems: 'center',
            gap: 8,
            cursor: 'pointer',
            transition: 'transform 0.5s ease',
          }}
        >
          Rafraîchir
        </button>
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <div>
          <h1 style={{ margin: 0 }}>Gestion des articles</h1>
          <p style={{ color: 'var(--muted)', marginTop: 8, marginBottom: 0 }}>
            {adminLoading ? 'Chargement…' : `${adminProducts.length} article(s) (y compris bloqués)`}
          </p>
        </div>

        {/* Tri */}
        <select
          value={sortBy}
          onChange={(e) => setSortBy(e.target.value as 'recent' | 'oldest')}
          style={{
            padding: '8px 12px',
            borderRadius: 6,
            border: '1px solid var(--border)',
            background: 'var(--input-bg)',
            color: 'var(--text)',
            fontSize: '0.9em',
            cursor: 'pointer'
          }}
        >
          <option value="recent">Plus récentes</option>
          <option value="oldest">Plus anciennes</option>
        </select>
      </div>

      {!adminLoading && adminProducts.length === 0 ? (
        <p style={{ textAlign: 'center', color: 'var(--muted)', padding: '40px 0' }}>
          Aucune publication pour le moment
        </p>
      ) : adminLoading ? (
        <p className="meta" style={{ padding: '24px 0' }}>
          Chargement du catalogue administrateur…
        </p>
      ) : (
        <div style={{ display: 'grid', gap: 16 }}>
          {sortedProducts.map(product => (
            <div 
              key={product.id}
              className="card"
              style={{ 
                padding: 16,
                opacity: product.isBlocked ? 0.5 : 1,
                border: product.hasWarning ? '2px solid #f59e0b' : undefined
              }}
            >
              <div style={{ display: 'grid', gridTemplateColumns: 'auto 1fr auto', gap: 16, alignItems: 'start' }}>
                {/* Image */}
                <img 
                  src={product.image} 
                  alt={product.title}
                  style={{ 
                    width: 100, 
                    height: 100, 
                    objectFit: 'cover', 
                    borderRadius: 8 
                  }}
                />

                {/* Infos */}
                <div>
                  <h3 style={{ margin: '0 0 8px 0' }}>
                    {product.title}
                    {product.isBlocked && (
                      <span style={{ 
                        marginLeft: 8, 
                        background: '#da3633', 
                        color: 'white', 
                        padding: '2px 8px', 
                        borderRadius: 4, 
                        fontSize: '0.7em',
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 4,
                      }}>
                        <Ban size={12} strokeWidth={2} aria-hidden />
                        BLOQUÉE
                      </span>
                    )}
                    {product.hasWarning && (
                      <span style={{ 
                        marginLeft: 8, 
                        background: '#f59e0b', 
                        color: 'white', 
                        padding: '2px 8px', 
                        borderRadius: 4, 
                        fontSize: '0.7em',
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 4,
                      }}>
                        <AlertTriangle size={12} strokeWidth={2} aria-hidden />
                        ATTENTION
                      </span>
                    )}
                  </h3>
                  
                  <div style={{ fontSize: '1.2em', fontWeight: 600, color: 'var(--admin-accent)', marginBottom: 8 }}>
                    {product.price} {product.currency}
                  </div>

                  {product.description && (
                    <p style={{ 
                      color: 'var(--muted)', 
                      fontSize: '0.9em', 
                      marginBottom: 8,
                      maxWidth: '600px'
                    }}>
                      {product.description.substring(0, 100)}
                      {product.description.length > 100 ? '...' : ''}
                    </p>
                  )}

                  {product.hasWarning && product.warningMessage && (
                    <div style={{ 
                      background: '#fef3c7', 
                      color: '#92400e', 
                      padding: 8, 
                      borderRadius: 4,
                      fontSize: '0.85em',
                      marginTop: 8
                    }}>
                      <span style={{ display: 'inline-flex', alignItems: 'flex-start', gap: 6 }}>
                        <AlertTriangle size={16} strokeWidth={1.75} aria-hidden style={{ flexShrink: 0, marginTop: 2 }} />
                        {product.warningMessage}
                      </span>
                    </div>
                  )}

                  {/* Formulaire d'avertissement */}
                  {warningProductId === product.id && (
                    <div style={{ marginTop: 12 }}>
                      <textarea
                        value={warningMessage}
                        onChange={(e) => setWarningMessage(e.target.value)}
                        placeholder="Message d'avertissement..."
                        style={{
                          width: '100%',
                          minHeight: '60px',
                          padding: 8,
                          borderRadius: 4,
                          border: '1px solid var(--border)',
                          background: 'var(--input-bg)',
                          color: 'var(--text)',
                          marginBottom: 8
                        }}
                      />
                      <div style={{ display: 'flex', gap: 8 }}>
                        <button
                          onClick={() => handleAddWarning(product.id)}
                          style={{ fontSize: '0.85em', padding: '6px 12px' }}
                        >
                          Confirmer
                        </button>
                        <button
                          onClick={() => {
                            setWarningProductId(null)
                            setWarningMessage('')
                          }}
                          style={{ 
                            fontSize: '0.85em', 
                            padding: '6px 12px',
                            background: 'transparent',
                            border: '1px solid var(--border)'
                          }}
                        >
                          Annuler
                        </button>
                      </div>
                    </div>
                  )}
                </div>

                {/* Actions */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8, minWidth: 150 }}>
                  {!product.isBlocked ? (
                    <>
                      <button
                        type="button"
                        onClick={() => handleBlock(product.id, product.title)}
                        style={{
                          background: '#da3633',
                          color: '#fff',
                          border: 'none',
                          fontSize: '0.85em',
                          padding: '8px 12px',
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: 6,
                        }}
                      >
                        <Ban size={14} strokeWidth={2} aria-hidden />
                        Bloquer
                      </button>

                      {!product.hasWarning ? (
                        <button
                          type="button"
                          onClick={() => setWarningProductId(product.id)}
                          style={{
                            background: 'transparent',
                            border: '1px solid #f59e0b',
                            color: '#f59e0b',
                            fontSize: '0.85em',
                            padding: '8px 12px',
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: 6,
                          }}
                        >
                          <AlertTriangle size={14} strokeWidth={2} aria-hidden />
                          Avertir
                        </button>
                      ) : (
                        <button
                          type="button"
                          onClick={() => void handleRemoveWarning(product.id)}
                          style={{
                            background: 'transparent',
                            border: '1px solid var(--admin-accent)',
                            color: 'var(--admin-accent)',
                            fontSize: '0.85em',
                            padding: '8px 12px',
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: 6,
                          }}
                        >
                          <Check size={14} strokeWidth={2} aria-hidden />
                          Retirer avertissement
                        </button>
                      )}
                    </>
                  ) : (
                    <button
                      type="button"
                      onClick={() => handleUnblock(product.id)}
                      style={{
                        background: 'var(--admin-accent)',
                        border: 'none',
                        fontSize: '0.85em',
                        padding: '8px 12px',
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 6,
                        color: '#fff',
                      }}
                    >
                      <Check size={14} strokeWidth={2} aria-hidden />
                      Débloquer
                    </button>
                  )}

                  <button
                    type="button"
                    onClick={() => handleDelete(product.id, product.title)}
                    style={{
                      background: '#da3633',
                      border: 'none',
                      fontSize: '0.85em',
                      padding: '8px 12px',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: 6,
                      color: '#fff',
                    }}
                  >
                    <Trash2 size={14} strokeWidth={2} aria-hidden />
                    Supprimer
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
