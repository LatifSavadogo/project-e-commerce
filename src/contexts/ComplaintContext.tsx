/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react'
import {
  createComplaint,
  fetchAdminComplaints,
  fetchMyComplaints,
  patchComplaintLu,
} from '../services/complaintApi'
import type { ComplaintDtoJson } from '../types/backend'
import { dateFromDto } from '../utils/dateFromDto'
import { useAuth, isStaffRole } from './AuthContext'

export type Complaint = {
  id: string
  userId: string
  userName: string
  userEmail: string
  titre?: string
  message: string
  createdAt: string
  read: boolean
}

function mapDto(c: ComplaintDtoJson): Complaint {
  return {
    id: String(c.idplainte),
    userId: c.idAuteur != null ? String(c.idAuteur) : '',
    userName: c.auteurNom || '',
    userEmail: c.auteurEmail || '',
    titre: c.titre,
    message: c.description,
    createdAt: dateFromDto(c.datecreation),
    read: c.lu,
  }
}

type ComplaintContextType = {
  complaints: Complaint[]
  complaintsLoading: boolean
  refreshComplaints: () => Promise<void>
  addComplaint: (input: { titre: string; description: string; idArticle?: number }) => Promise<void>
  markAsRead: (id: string) => Promise<void>
  getUnreadCount: () => number
}

const ComplaintContext = createContext<ComplaintContextType | undefined>(undefined)

export function ComplaintProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const [complaints, setComplaints] = useState<Complaint[]>([])
  const [complaintsLoading, setComplaintsLoading] = useState(false)

  const refreshComplaints = useCallback(async () => {
    if (!user) {
      setComplaints([])
      return
    }
    setComplaintsLoading(true)
    try {
      const list = isStaffRole(user) ? await fetchAdminComplaints() : await fetchMyComplaints()
      setComplaints(list.map(mapDto))
    } catch (e) {
      console.error(e)
      setComplaints([])
    } finally {
      setComplaintsLoading(false)
    }
  }, [user])

  useEffect(() => {
    void refreshComplaints()
  }, [refreshComplaints])

  const addComplaint = useCallback(
    async (input: { titre: string; description: string; idArticle?: number }) => {
      await createComplaint(input)
      await refreshComplaints()
    },
    [refreshComplaints]
  )

  const markAsRead = useCallback(
    async (id: string) => {
      const numId = Number(id)
      if (user && isStaffRole(user)) {
        try {
          await patchComplaintLu(numId, true)
          await refreshComplaints()
          return
        } catch (e) {
          console.error(e)
        }
      }
      setComplaints((prev) => prev.map((c) => (c.id === id ? { ...c, read: true } : c)))
    },
    [user, refreshComplaints]
  )

  const getUnreadCount = useCallback(() => {
    return complaints.filter((c) => !c.read).length
  }, [complaints])

  return (
    <ComplaintContext.Provider
      value={{
        complaints,
        complaintsLoading,
        refreshComplaints,
        addComplaint,
        markAsRead,
        getUnreadCount,
      }}
    >
      {children}
    </ComplaintContext.Provider>
  )
}

export function useComplaints() {
  const context = useContext(ComplaintContext)
  if (!context) {
    throw new Error('useComplaints must be used within ComplaintProvider')
  }
  return context
}
