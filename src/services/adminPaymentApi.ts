import { apiJson } from './apiClient'
import type { PaymentResultDto } from './paymentApi'

export async function fetchAdminPayments(): Promise<PaymentResultDto[]> {
  return apiJson<PaymentResultDto[]>('/api/v1/admin/payments')
}
