import { apiFetch, apiJson } from './apiClient'
import type { ClientLivraisonQrDtoJson, CommandeSuiviDtoJson } from '../types/backend'

export type PaymentMethod = 'ORANGE_MONEY' | 'MOOV_MONEY' | 'VIREMENT' | 'ESPECES' | 'PAYDUNYA'

export type PaymentResultDto = {
  idtransaction: number
  idArticle: number
  articleLibelle?: string
  quantite: number
  prixUnitaire: number
  montantTotal: number
  frais: number
  moyenPaiement: PaymentMethod
  datecreation?: string
  message?: string
  idLivraison?: number | null
  livraisonStatut?: string | null
  livraisonTypeEngin?: string | null
  /** Vendeur / staff uniquement */
  vendorPickupCode?: string | null
  vendorPackedReferenceBase64?: string | null
}

export type PaymentCreateBody = {
  idArticle: number
  quantite: number
  moyenPaiement: PaymentMethod
  referenceExterne: string
  prixUnitaireNegocie?: number | null
  /** Si défini : lieu de dépôt pour cette commande (sinon domicile du profil). */
  livraisonLatitude?: number | null
  livraisonLongitude?: number | null
}

export async function createPayment(body: PaymentCreateBody): Promise<PaymentResultDto> {
  const payload: Record<string, unknown> = {
    idArticle: body.idArticle,
    quantite: body.quantite,
    moyenPaiement: body.moyenPaiement,
    referenceExterne: body.referenceExterne,
    prixUnitaireNegocie: body.prixUnitaireNegocie ?? null,
  }
  if (body.livraisonLatitude != null && body.livraisonLongitude != null) {
    payload.livraisonLatitude = body.livraisonLatitude
    payload.livraisonLongitude = body.livraisonLongitude
  }
  return apiJson<PaymentResultDto>('/api/v1/payments', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export type CartCheckoutBody = {
  moyenPaiement: PaymentMethod
  referenceExterne: string
  cartItemIds: number[]
  livraisonLatitude?: number | null
  livraisonLongitude?: number | null
}

/** Règle chaque ligne sélectionnée (référence externe suffixée par |cartItem=id côté serveur). */
export async function cartCheckout(body: CartCheckoutBody): Promise<PaymentResultDto[]> {
  const payload: Record<string, unknown> = {
    moyenPaiement: body.moyenPaiement,
    referenceExterne: body.referenceExterne,
    cartItemIds: body.cartItemIds,
  }
  if (body.livraisonLatitude != null && body.livraisonLongitude != null) {
    payload.livraisonLatitude = body.livraisonLatitude
    payload.livraisonLongitude = body.livraisonLongitude
  }
  return apiJson<PaymentResultDto[]>('/api/v1/payments/cart-checkout', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function fetchMyPurchases(): Promise<PaymentResultDto[]> {
  return apiJson<PaymentResultDto[]>('/api/v1/payments/mine')
}

export async function fetchMySales(): Promise<PaymentResultDto[]> {
  return apiJson<PaymentResultDto[]>('/api/v1/payments/sales')
}

export type VendorSalesTimePoint = {
  date: string
  revenue: number
  orderCount: number
}

export type VendorPaymentMethodStat = {
  method: string
  transactionCount: number
  revenue: number
}

export type VendorTopArticle = {
  idArticle: number
  libelle?: string
  revenue: number
  quantitySold: number
}

export type VendorSalesDashboard = {
  transactionCount: number
  totalQuantitySold: number
  revenueTotal: number
  averageOrderValue: number
  revenueLast7Days: number
  ordersLast7Days: number
  revenueLast30Days: number
  ordersLast30Days: number
  revenueByDay: VendorSalesTimePoint[]
  byPaymentMethod: VendorPaymentMethodStat[]
  topArticles: VendorTopArticle[]
}

export async function fetchVendorSalesDashboard(): Promise<VendorSalesDashboard> {
  return apiJson<VendorSalesDashboard>('/api/v1/payments/sales/dashboard')
}

/** Reçu de paiement (PDF, même URL qu’avant ; le contenu est désormais application/pdf). */
export async function downloadPaymentReceipt(transactionId: number): Promise<Blob> {
  const res = await apiFetch(`/api/v1/payments/${transactionId}/receipt`, {
    headers: { Accept: 'application/pdf' },
  })
  if (!res.ok) {
    const t = await res.text()
    throw new Error(t || res.statusText)
  }
  return res.blob()
}

/** QR + image PNG (acheteur uniquement). */
export async function fetchClientLivraisonQr(transactionId: number): Promise<ClientLivraisonQrDtoJson> {
  return apiJson<ClientLivraisonQrDtoJson>(`/api/v1/payments/${transactionId}/livraison/qr`)
}

/** Suivi livraison (acheteur). */
export async function fetchCommandeSuivi(transactionId: number): Promise<CommandeSuiviDtoJson> {
  return apiJson<CommandeSuiviDtoJson>(`/api/v1/payments/${transactionId}/suivi`)
}

