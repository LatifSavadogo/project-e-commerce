/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react'
import type { UserDtoJson } from '../types/backend'
import { loginRequest, logoutRequest, meRequest, registerRequest, type RegisterParams } from '../services/authApi'
import { deleteUserById, fetchAllUsers } from '../services/adminUserApi'

export type User = {
  id: string
  nom: string
  prenoms: string
  email: string
  accountType: 'buyer' | 'seller' | 'livreur'
  /** Libellé rôle backend (ACHETEUR, VENDEUR, LIVREUR, ADMIN, SUPER_ADMIN) */
  librole?: string
  idrole?: number
  productType?: string
  idtypeVendeur?: number
  libtypeVendeur?: string
  typeEnginLivreur?: string
  registeredAt: string
  country?: string
  idpays?: number
  city?: string
  /** Point de livraison / localisation (WGS84), requis avant paiement. */
  latitude?: number | null
  longitude?: number | null
  /** Fichier pièce d’identité (nom stocké côté serveur), si présent. */
  cnib?: string | null
}

export function mapUserDtoToUser(d: UserDtoJson): User {
  const lib = (d.librole || '').toUpperCase()
  let accountType: 'buyer' | 'seller' | 'livreur' = 'buyer'
  if (lib === 'VENDEUR') accountType = 'seller'
  else if (lib === 'LIVREUR') accountType = 'livreur'
  return {
    id: String(d.iduser),
    nom: d.nom,
    prenoms: d.prenom,
    email: d.email,
    accountType,
    librole: d.librole,
    idrole: d.idrole,
    idtypeVendeur: d.idtypeVendeur ?? undefined,
    libtypeVendeur: d.libtypeVendeur,
    productType: d.libtypeVendeur || undefined,
    typeEnginLivreur: d.typeEnginLivreur ?? undefined,
    registeredAt: d.dateupdate || new Date().toISOString(),
    country: d.libpays,
    idpays: d.idpays ?? undefined,
    city: d.ville?.trim() || undefined,
    latitude: d.latitude ?? undefined,
    longitude: d.longitude ?? undefined,
    cnib: d.cnib,
  }
}

export function isStaffRole(user: User | null): boolean {
  if (!user?.librole) return false
  const l = user.librole.toUpperCase()
  return l === 'ADMIN' || l === 'SUPER_ADMIN'
}

export function isSuperAdminRole(user: User | null): boolean {
  return (user?.librole || '').toUpperCase() === 'SUPER_ADMIN'
}

export function isLivreurRole(user: User | null): boolean {
  return (user?.librole || '').toUpperCase() === 'LIVREUR'
}

export type LoginResult = { ok: true; staff: boolean; livreur: boolean } | { ok: false }

type AuthContextType = {
  user: User | null
  authLoading: boolean
  login: (email: string, password: string) => Promise<LoginResult>
  register: (params: RegisterParams) => Promise<{ ok: true } | { ok: false; error: string }>
  logout: () => Promise<void>
  isAuthenticated: boolean
  refreshMe: () => Promise<void>
  /** Cache pour l’admin (liste serveur). */
  getAllUsers: () => User[]
  setAdminUsersCache: (users: User[]) => void
  refreshAdminUsers: () => Promise<User[]>
  getConnectedUsersCount: () => number
  deleteUser: (userId: string) => Promise<boolean>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [authLoading, setAuthLoading] = useState(true)
  const [adminUsersCache, setAdminUsersCache] = useState<User[]>([])

  const refreshMe = useCallback(async () => {
    try {
      const res = await meRequest()
      if (res.authenticated && res.user) {
        setUser(mapUserDtoToUser(res.user))
      } else {
        setUser(null)
      }
    } catch {
      setUser(null)
    }
  }, [])

  useEffect(() => {
    let alive = true
    ;(async () => {
      setAuthLoading(true)
      await refreshMe()
      if (alive) setAuthLoading(false)
    })()
    return () => {
      alive = false
    }
  }, [refreshMe])

  const login = useCallback(async (email: string, password: string): Promise<LoginResult> => {
    try {
      const res = await loginRequest(email, password)
      if (res.authenticated && res.user) {
        const u = mapUserDtoToUser(res.user)
        setUser(u)
        return { ok: true, staff: isStaffRole(u), livreur: isLivreurRole(u) }
      }
      return { ok: false }
    } catch {
      return { ok: false }
    }
  }, [])

  const register = useCallback(
    async (params: RegisterParams): Promise<{ ok: true } | { ok: false; error: string }> => {
      try {
        await registerRequest(params)
        return { ok: true }
      } catch (e) {
        const msg = e instanceof Error ? e.message : 'Inscription impossible'
        return { ok: false, error: msg }
      }
    },
    []
  )

  const logout = useCallback(async () => {
    try {
      await logoutRequest()
    } catch {
      /* session peut être déjà invalide */
    }
    setUser(null)
  }, [])

  const getAllUsers = useCallback((): User[] => adminUsersCache, [adminUsersCache])

  const refreshAdminUsers = useCallback(async (): Promise<User[]> => {
    try {
      const list = await fetchAllUsers()
      const mapped = list.map(mapUserDtoToUser)
      setAdminUsersCache(mapped)
      return mapped
    } catch {
      return adminUsersCache
    }
  }, [adminUsersCache])

  const getConnectedUsersCount = useCallback((): number => {
    return 0
  }, [])

  const deleteUser = useCallback(async (userId: string): Promise<boolean> => {
    try {
      await deleteUserById(Number(userId))
      setAdminUsersCache((prev) => prev.filter((u) => u.id !== userId))
      if (user?.id === userId) {
        setUser(null)
      }
      return true
    } catch {
      return false
    }
  }, [user?.id])

  return (
    <AuthContext.Provider
      value={{
        user,
        authLoading,
        login,
        register,
        logout,
        isAuthenticated: !!user,
        refreshMe,
        getAllUsers,
        setAdminUsersCache,
        refreshAdminUsers,
        getConnectedUsersCount,
        deleteUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
