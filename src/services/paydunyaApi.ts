import { apiJson } from './apiClient'
import type { PaymentResultDto } from './paymentApi'

export type VendorCertificationPlan = 'MONTHLY' | 'YEARLY'

export type PaydunyaOrderCheckoutBody = {
  idArticle: number
  quantite: number
  prixUnitaireNegocie?: number | null
  livraisonLatitude?: number | null
  livraisonLongitude?: number | null
}

export type PaydunyaCheckoutResponse = {
  checkoutUrl: string
  invoiceToken: string
  description?: string | null
}

export type PaydunyaCompleteResponse = {
  outcome: string
  message?: string | null
  payment?: PaymentResultDto | null
  certification?: VendorCertificationStatusDto | null
}

export type VendorCertificationStatusDto = {
  active: boolean
  certifieJusqua?: string | null
  monthlyPriceFcfa: number
  yearlyPriceFcfa: number
}

export async function createPaydunyaOrderInvoice(
  body: PaydunyaOrderCheckoutBody
): Promise<PaydunyaCheckoutResponse> {
  return apiJson<PaydunyaCheckoutResponse>('/api/v1/paydunya/invoices/order', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function createPaydunyaCertificationInvoice(plan: VendorCertificationPlan): Promise<PaydunyaCheckoutResponse> {
  return apiJson<PaydunyaCheckoutResponse>('/api/v1/paydunya/invoices/certification', {
    method: 'POST',
    body: JSON.stringify({ plan }),
  })
}

export async function completePaydunyaInvoice(invoiceToken: string): Promise<PaydunyaCompleteResponse> {
  return apiJson<PaydunyaCompleteResponse>('/api/v1/paydunya/complete', {
    method: 'POST',
    body: JSON.stringify({ invoiceToken }),
  })
}

export async function fetchVendorCertificationStatus(): Promise<VendorCertificationStatusDto> {
  return apiJson<VendorCertificationStatusDto>('/api/v1/me/vendor-certification')
}
