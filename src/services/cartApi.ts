import { apiJson } from './apiClient'
import type { CartDtoJson } from '../types/backend'

export async function fetchCart(): Promise<CartDtoJson> {
  return apiJson<CartDtoJson>('/api/v1/cart')
}

export async function addCartItem(idArticle: number, quantity: number): Promise<CartDtoJson> {
  return apiJson<CartDtoJson>('/api/v1/cart/items', {
    method: 'POST',
    body: JSON.stringify({ idArticle, quantity }),
  })
}

export async function patchCartItem(idcartitem: number, quantity: number): Promise<CartDtoJson> {
  return apiJson<CartDtoJson>(`/api/v1/cart/items/${idcartitem}`, {
    method: 'PATCH',
    body: JSON.stringify({ quantity }),
  })
}

export async function removeCartItem(idcartitem: number): Promise<CartDtoJson> {
  return apiJson<CartDtoJson>(`/api/v1/cart/items/${idcartitem}`, { method: 'DELETE' })
}

export async function clearCart(): Promise<CartDtoJson> {
  return apiJson<CartDtoJson>('/api/v1/cart', { method: 'DELETE' })
}

export async function addCartFromNegotiation(conversationId: number, messageId: number): Promise<CartDtoJson> {
  return apiJson<CartDtoJson>('/api/v1/cart/items/from-negotiation', {
    method: 'POST',
    body: JSON.stringify({ conversationId, messageId }),
  })
}
