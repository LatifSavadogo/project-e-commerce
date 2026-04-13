import { apiJson } from './apiClient'
import { API_BASE } from '../config/api'
import type { RoleUpgradeRequestDtoJson, UserDtoJson } from '../types/backend'

export async function fetchAdminRoleUpgrades(status?: string): Promise<RoleUpgradeRequestDtoJson[]> {
  const q = status != null && status !== '' ? `?status=${encodeURIComponent(status)}` : ''
  return apiJson<RoleUpgradeRequestDtoJson[]>(`/api/v1/admin/role-upgrades${q}`)
}

export async function approveRoleUpgrade(id: number): Promise<UserDtoJson> {
  return apiJson<UserDtoJson>(`/api/v1/admin/role-upgrades/${id}/approve`, { method: 'POST' })
}

export async function rejectRoleUpgrade(id: number, motif?: string): Promise<void> {
  await apiJson<unknown>(`/api/v1/admin/role-upgrades/${id}/reject`, {
    method: 'POST',
    body: JSON.stringify({ motif: motif?.trim() || undefined }),
  })
}

export function adminRoleUpgradeCnibUrl(id: number): string {
  return `${API_BASE}/api/v1/admin/role-upgrades/${id}/fichiers/cnib`
}

export function adminRoleUpgradePhotoUrl(id: number): string {
  return `${API_BASE}/api/v1/admin/role-upgrades/${id}/fichiers/photo`
}
