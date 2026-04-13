import { useState } from 'react'
import { Check, ChevronRight, Info, LayoutGrid, Minus } from 'lucide-react'
import { CATEGORIES, type Category } from '../contexts/ProductContext'
import CategoryIcon from './ui/CategoryIcon'
import { iconSm } from './ui/iconProps'

type CategoryFilterProps = {
  selectedCategory: string | null
  onSelectCategory: (categoryId: string | null) => void
}

export default function CategoryFilter({ selectedCategory, onSelectCategory }: CategoryFilterProps) {
  const [expandedCategory, setExpandedCategory] = useState<string | null>(null)
  const [showSubCategories, setShowSubCategories] = useState<string | null>(null)

  const toggleExpand = (categoryId: string) => {
    setExpandedCategory(expandedCategory === categoryId ? null : categoryId)
  }

  const toggleSubCategories = (categoryId: string) => {
    setShowSubCategories(showSubCategories === categoryId ? null : categoryId)
  }

  const accentBtn = (active: boolean) =>
    ({
      width: '100%',
      marginBottom: 12,
      padding: '14px 16px',
      background: active ? 'var(--accent)' : 'transparent',
      border: active ? '2px solid var(--accent)' : '1px solid var(--border)',
      borderRadius: 12,
      textAlign: 'left' as const,
      fontWeight: active ? 600 : 400,
      fontSize: '1em',
      cursor: 'pointer',
      color: active ? '#fff' : 'var(--text)',
      display: 'flex',
      alignItems: 'center',
      gap: 12,
    }) as const

  return (
    <div className="category-filter-block" style={{ marginTop: 24, marginBottom: 32 }}>
      <h2 style={{ marginBottom: 16 }}>Catégories</h2>
      <p className="meta" style={{ marginBottom: 20 }}>
        Filtrez les annonces par catégorie de produits
      </p>

      <button type="button" onClick={() => onSelectCategory(null)} style={accentBtn(selectedCategory === null)}>
        <LayoutGrid {...iconSm} aria-hidden style={{ flexShrink: 0 }} />
        Toutes les catégories
      </button>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        {CATEGORIES.map((category: Category) => {
          const isSelected = selectedCategory === category.id
          const isExpanded = expandedCategory === category.id

          return (
            <div
              key={category.id}
              style={{
                border: isSelected ? '2px solid var(--accent)' : '1px solid var(--border)',
                borderRadius: 12,
                overflow: 'hidden',
                background: isSelected ? 'var(--surface-muted)' : 'transparent',
              }}
            >
              <div
                style={{
                  padding: '16px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  cursor: 'pointer',
                  gap: 12,
                }}
                onClick={() => {
                  onSelectCategory(isSelected ? null : category.id)
                  toggleSubCategories(category.id)
                }}
              >
                <div style={{ flex: 1, display: 'flex', alignItems: 'flex-start', gap: 12, minWidth: 0 }}>
                  <span style={{ marginTop: 2, color: 'var(--accent)', flexShrink: 0 }}>
                    <CategoryIcon name={category.iconKey} size={20} />
                  </span>
                  <div style={{ minWidth: 0 }}>
                    <div
                      style={{
                        fontWeight: isSelected ? 600 : 500,
                        fontSize: '1em',
                        marginBottom: 4,
                      }}
                    >
                      {category.name}
                    </div>
                    <div className="meta" style={{ fontSize: '0.85em' }}>
                      {category.description}
                    </div>
                  </div>
                </div>
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation()
                    toggleExpand(category.id)
                  }}
                  style={{
                    background: 'transparent',
                    border: 'none',
                    cursor: 'pointer',
                    padding: 8,
                    color: 'var(--muted)',
                    display: 'inline-flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    borderRadius: 8,
                  }}
                  title={isExpanded ? 'Masquer les exemples' : 'Voir les exemples'}
                >
                  {isExpanded ? <Minus {...iconSm} aria-hidden /> : <Info {...iconSm} aria-hidden />}
                </button>
              </div>

              {showSubCategories === category.id && (
                <div
                  style={{
                    padding: '0 16px 16px 16px',
                    borderTop: '1px solid var(--border)',
                    background: 'var(--surface)',
                  }}
                >
                  <p
                    className="meta"
                    style={{
                      fontSize: '0.85em',
                      fontWeight: 600,
                      marginTop: 12,
                      marginBottom: 12,
                    }}
                  >
                    Sous-catégories :
                  </p>
                  <div
                    style={{
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '8px',
                    }}
                  >
                    {category.subCategories.map((subCat) => {
                      const isSubSelected = selectedCategory === subCat.id
                      return (
                        <button
                          type="button"
                          key={subCat.id}
                          onClick={(e) => {
                            e.stopPropagation()
                            onSelectCategory(isSubSelected ? null : subCat.id)
                          }}
                          style={{
                            padding: '12px 16px',
                            background: isSubSelected ? 'var(--accent)' : 'var(--surface-elevated)',
                            border: isSubSelected ? '2px solid var(--accent)' : '1px solid var(--border)',
                            borderRadius: 8,
                            textAlign: 'left',
                            cursor: 'pointer',
                            fontSize: '0.9em',
                            fontWeight: isSubSelected ? 600 : 400,
                            transition: 'border-color 0.2s, background 0.2s',
                            color: isSubSelected ? '#fff' : 'var(--text)',
                            display: 'flex',
                            alignItems: 'center',
                            gap: 8,
                          }}
                        >
                          {isSubSelected ? (
                            <Check {...iconSm} aria-hidden style={{ flexShrink: 0 }} />
                          ) : (
                            <ChevronRight {...iconSm} aria-hidden style={{ flexShrink: 0, opacity: 0.7 }} />
                          )}
                          {subCat.name}
                        </button>
                      )
                    })}
                  </div>
                </div>
              )}

              {isExpanded && (
                <div
                  style={{
                    padding: '0 16px 16px 16px',
                    borderTop: '1px solid var(--border)',
                  }}
                >
                  <p
                    className="meta"
                    style={{
                      fontSize: '0.85em',
                      fontWeight: 600,
                      marginTop: 12,
                      marginBottom: 8,
                    }}
                  >
                    Exemples de produits :
                  </p>
                  <ul
                    style={{
                      margin: 0,
                      paddingLeft: 20,
                      color: 'var(--muted)',
                      fontSize: '0.85em',
                    }}
                  >
                    {category.examples.map((example, index) => (
                      <li key={index} style={{ marginBottom: 6 }}>
                        {example}
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}
