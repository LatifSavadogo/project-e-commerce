import { apiJson } from './apiClient'

export type AdminDashboardStats = {
  usersTotal: number
  articlesTotal: number
  complaintsUnread: number
  paymentsTotal: number
  sessionsActives: number
}

export async function fetchAdminDashboardStats(): Promise<AdminDashboardStats> {
  return apiJson<AdminDashboardStats>('/api/v1/admin/dashboard/stats')
}
