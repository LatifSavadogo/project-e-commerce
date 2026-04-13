import { apiJson } from './apiClient'
import type { AdminLivraisonListDtoJson, CommandeSuiviDtoJson } from '../types/backend'

export async function fetchAdminLivraisons(): Promise<AdminLivraisonListDtoJson[]> {
  return apiJson<AdminLivraisonListDtoJson[]>('/api/v1/admin/livraisons')
}

export async function fetchAdminLivraisonSuivi(idlivraison: number): Promise<CommandeSuiviDtoJson> {
  return apiJson<CommandeSuiviDtoJson>(`/api/v1/admin/livraisons/${idlivraison}/suivi`)
}
