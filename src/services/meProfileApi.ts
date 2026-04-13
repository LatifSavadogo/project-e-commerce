import { ApiError, apiJson } from './apiClient'
import type { UserDtoJson } from '../types/backend'

const DELIVERY_PATHS = ['/api/v1/users/me/delivery-location', '/api/v1/me/delivery-location'] as const

/**
 * Enregistre le GPS du domicile (POST). Essaie d’abord `/users/me/...` (même famille que l’export RGPD), puis `/me/...`
 * si 404 — certains déploiements ou proxies ne routent qu’une des deux URLs.
 */
export async function patchMyDeliveryLocation(latitude: number, longitude: number): Promise<UserDtoJson> {
  const body = JSON.stringify({ latitude, longitude })
  let last: ApiError | null = null
  for (const path of DELIVERY_PATHS) {
    try {
      return await apiJson<UserDtoJson>(path, { method: 'POST', body })
    } catch (e) {
      if (e instanceof ApiError && e.status === 404) {
        last = e
        continue
      }
      throw e
    }
  }
  throw last ?? new ApiError(404, 'Aucune route GPS domicile trouvée. Redémarrez le backend Spring à jour (UserController ou MeDeliveryLocationController).')
}
