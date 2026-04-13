import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { useChat } from '../contexts/ChatContext'

export function useNotifications() {
  const { user } = useAuth()
  const { conversations, getVendorConversations, getBuyerConversations } = useChat()
  const [unreadCount, setUnreadCount] = useState(0)

  useEffect(() => {
    if (!user) {
      setUnreadCount(0)
      return
    }

    // Calculer le total de messages non lus
    // Temporairement commenté car role n'existe pas encore dans User type
    const userConversations = getVendorConversations(user.id).length > 0
      ? getVendorConversations(user.id)
      : getBuyerConversations(user.id)

    const total = userConversations.reduce((sum, conv) => sum + conv.unreadCount, 0)
    setUnreadCount(total)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user, conversations])

  return {
    unreadCount,
    hasUnread: unreadCount > 0
  }
}
