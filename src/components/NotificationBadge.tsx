interface NotificationBadgeProps {
  count: number
  style?: React.CSSProperties
}

export default function NotificationBadge({ count, style }: NotificationBadgeProps) {
  if (count === 0) return null

  return (
    <span style={{
      position: 'absolute',
      top: -4,
      right: -4,
      background: '#dc2626',
      color: 'white',
      fontSize: '0.7em',
      fontWeight: 700,
      padding: '2px 6px',
      borderRadius: '50%',
      minWidth: 18,
      height: 18,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      boxShadow: '0 2px 8px rgba(220, 38, 38, 0.5)',
      ...style
    }}>
      {count > 99 ? '99+' : count}
    </span>
  )
}
