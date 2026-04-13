interface QuickSuggestionsProps {
  suggestions: string[]
  onSelect: (suggestion: string) => void
}

export default function QuickSuggestions({ suggestions, onSelect }: QuickSuggestionsProps) {
  if (suggestions.length === 0) return null

  return (
    <div style={{
      display: 'flex',
      flexWrap: 'wrap',
      gap: 8,
      marginTop: 12,
      marginBottom: 8
    }}>
      {suggestions.map((suggestion, index) => (
        <button
          key={index}
          type="button"
          className="quick-suggestion-chip"
          onClick={() => onSelect(suggestion)}
        >
          {suggestion}
        </button>
      ))}
    </div>
  )
}
