import { apiFetch, apiJson } from './apiClient'
import type { RoleUpgradeRequestDtoJson } from '../types/backend'

export type MyRoleUpgradeResponse = { demande: RoleUpgradeRequestDtoJson | null }

export async function fetchMyRoleUpgrade(): Promise<MyRoleUpgradeResponse> {
  return apiJson<MyRoleUpgradeResponse>('/api/v1/me/role-upgrade')
}

export async function submitRoleUpgrade(formData: FormData): Promise<RoleUpgradeRequestDtoJson> {
  const res = await apiFetch('/api/v1/me/role-upgrade', { method: 'POST', body: formData })
  const text = await res.text()
  let data: unknown = {}
  if (text) {
    try {
      data = JSON.parse(text) as unknown
    } catch {
      data = {}
    }
  }
  if (!res.ok) {
    const o = data as { error?: string }
    throw new Error(typeof o.error === 'string' ? o.error : res.statusText)
  }
  return data as RoleUpgradeRequestDtoJson
}
