import { apiJson } from './apiClient'
import { API_BASE } from '../config/api'
import type { UserDtoJson } from '../types/backend'

export async function fetchAllUsers(): Promise<UserDtoJson[]> {
  return apiJson<UserDtoJson[]>('/api/v1/users')
}

export type AdminUserPatchBody = {
  idrole?: number
  idpays?: number
  idtypeVendeur?: number
  vendeurInternational?: boolean
}

export async function patchAdminUserProfile(
  iduser: number,
  body: AdminUserPatchBody
): Promise<UserDtoJson> {
  return apiJson<UserDtoJson>(`/api/v1/users/${iduser}/admin-profile`, {
    method: 'PATCH',
    body: JSON.stringify(body),
  })
}

/** Ouvrir dans un nouvel onglet (cookies de session envoyés vers le backend). */
export function adminCnibUrl(iduser: number): string {
  return `${API_BASE}/api/v1/users/${iduser}/cnib`
}

export async function deleteUserById(iduser: number): Promise<void> {
  await apiJson<unknown>(`/api/v1/users/${iduser}`, { method: 'DELETE' })
}

export async function adminResetPassword(iduser: number, newPassword: string): Promise<UserDtoJson> {
  return apiJson<UserDtoJson>(`/api/v1/users/${iduser}/admin-password`, {
    method: 'PUT',
    body: JSON.stringify({ newPassword }),
  })
}
