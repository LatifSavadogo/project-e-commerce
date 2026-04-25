import { apiJson } from './apiClient'

export type VendorRatingSummary = {
  idVendeur: number
  moyenneEtoiles: number
  nombreAvis: number
  certifie: boolean
}

export async function fetchVendorRatingSummary(idVendeur: number): Promise<VendorRatingSummary> {
  return apiJson<VendorRatingSummary>(`/api/v1/vendors/${idVendeur}/rating`)
}
