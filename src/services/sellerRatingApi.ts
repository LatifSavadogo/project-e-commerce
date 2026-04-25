import { apiJson } from './apiClient'

export type SellerRatingCreateBody = {
  stars: number
  commentaire?: string | null
}

export async function postTransactionSellerRating(
  idtransaction: number,
  body: SellerRatingCreateBody
): Promise<{ id: number; stars: number; idtransaction: number }> {
  return apiJson(`/api/v1/payments/${idtransaction}/rating`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
}
