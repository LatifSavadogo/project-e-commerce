import { useProducts } from '../contexts/ProductContext'

type Props = {
  selectedTypeId: string | null
  onSelectType: (typeId: string | null) => void
}

export default function TypeArticleFilter({ selectedTypeId, onSelectType }: Props) {
  const { typeArticles } = useProducts()

  return (
    <div style={{ marginTop: 24, marginBottom: 24 }}>
      <h2 style={{ marginBottom: 12 }}>Filtrer par type (API)</h2>
      <p className="meta" style={{ marginBottom: 16 }}>
        Types chargés depuis le serveur ; les annonces sont filtrées par identifiant de catégorie.
      </p>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
        <button
          type="button"
          onClick={() => onSelectType(null)}
          style={{
            padding: '10px 14px',
            borderRadius: 8,
            border: selectedTypeId === null ? '2px solid var(--accent)' : '1px solid var(--border)',
            background: selectedTypeId === null ? 'var(--surface-muted)' : 'transparent',
            color: 'var(--text)',
            cursor: 'pointer',
          }}
        >
          Tous
        </button>
        {typeArticles.map((t) => (
          <button
            key={t.idtype}
            type="button"
            onClick={() => onSelectType(String(t.idtype))}
            style={{
              padding: '10px 14px',
              borderRadius: 8,
              border: selectedTypeId === String(t.idtype) ? '2px solid var(--accent)' : '1px solid var(--border)',
              background: selectedTypeId === String(t.idtype) ? 'var(--surface-muted)' : 'transparent',
              color: 'var(--text)',
              cursor: 'pointer',
              fontSize: '0.9em',
            }}
          >
            {t.libfamille ? `${t.libfamille} — ` : ''}
            {t.libtype}
          </button>
        ))}
      </div>
    </div>
  )
}
