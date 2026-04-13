import { apiJson } from './apiClient'
import type { ComplaintDtoJson } from '../types/backend'

export async function createComplaint(body: {
  titre: string
  description: string
  idArticle?: number
}): Promise<ComplaintDtoJson> {
  return apiJson<ComplaintDtoJson>('/api/v1/complaints', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function fetchMyComplaints(): Promise<ComplaintDtoJson[]> {
  return apiJson<ComplaintDtoJson[]>('/api/v1/complaints/mine')
}

export async function fetchAdminComplaints(): Promise<ComplaintDtoJson[]> {
  return apiJson<ComplaintDtoJson[]>('/api/v1/admin/complaints')
}

export async function patchComplaintLu(id: number, lu: boolean): Promise<ComplaintDtoJson> {
  return apiJson<ComplaintDtoJson>(`/api/v1/admin/complaints/${id}/lu`, {
    method: 'PATCH',
    body: JSON.stringify({ lu }),
  })
}
