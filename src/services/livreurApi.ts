import { apiJson } from './apiClient'
import type { LivraisonLivreurDtoJson, LivreurDashboardDtoJson, LivreurMesLivraisonsDtoJson } from '../types/backend'

export async function fetchLivreurDashboard(): Promise<LivreurDashboardDtoJson> {
  return apiJson<LivreurDashboardDtoJson>('/api/v1/livreur/dashboard')
}

export async function fetchLivraisonsDisponibles(): Promise<LivraisonLivreurDtoJson[]> {
  return apiJson<LivraisonLivreurDtoJson[]>('/api/v1/livreur/livraisons/disponibles')
}

/** En cours + livrées / annulées (historique), jusqu’à la limite renvoyée par le serveur. */
export async function fetchMesLivraisonsLivreur(): Promise<LivreurMesLivraisonsDtoJson> {
  return apiJson<LivreurMesLivraisonsDtoJson>('/api/v1/livreur/livraisons/mes-livraisons')
}

export async function prendreLivraison(
  id: number,
  typeEngin: 'MOTO' | 'VEHICULE'
): Promise<LivraisonLivreurDtoJson> {
  return apiJson<LivraisonLivreurDtoJson>(`/api/v1/livreur/livraisons/${id}/prendre`, {
    method: 'POST',
    body: JSON.stringify({ typeEngin }),
  })
}

/** Finalisation après scan du QR client (texte lu dans le code-barres). */
export async function terminerLivraisonParScan(clientQrPayload: string): Promise<LivraisonLivreurDtoJson> {
  return apiJson<LivraisonLivreurDtoJson>('/api/v1/livreur/livraisons/terminer-par-scan', {
    method: 'POST',
    body: JSON.stringify({ clientQrPayload: clientQrPayload.trim() }),
  })
}

/** Envoie la position GPS actuelle pour le suivi client (Google Maps). */
export async function publierPositionLivreur(idlivraison: number, latitude: number, longitude: number): Promise<void> {
  await apiJson<{ ok: boolean }>(`/api/v1/livreur/livraisons/${idlivraison}/position`, {
    method: 'POST',
    body: JSON.stringify({ latitude, longitude }),
  })
}

export async function patchLivreurEngin(engin: 'MOTO' | 'VEHICULE'): Promise<unknown> {
  return apiJson<unknown>('/api/v1/livreur/profil/engin', {
    method: 'PATCH',
    body: JSON.stringify({ typeEnginLivreur: engin }),
  })
}

/** Masque l’offre pour ce livreur uniquement ; les autres la voient encore. */
export async function ignorerLivraison(id: number): Promise<void> {
  await apiJson<{ ok: boolean }>(`/api/v1/livreur/livraisons/${id}/ignorer`, { method: 'POST' })
}
