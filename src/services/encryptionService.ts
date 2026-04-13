// Service de chiffrement des transactions supprimé pour repartir de zéro sur la gestion du paiement.
// Placeholder minimal pour éviter les erreurs TypeScript.

export type Transaction = Record<string, unknown>

export function saveTransaction(_transaction: Transaction): void {
  // no-op
}

export function getAllTransactions(): Transaction[] {
  return []
}

export function getTransactionById(_id: string): Transaction | null {
  return null
}

export function getUserTransactions(_userId: string): Transaction[] {
  return []
}

export function updateTransactionStatus(
  _id: string,
  _status: 'pending' | 'completed' | 'cancelled'
): boolean {
  return false
}

export function transactionIdExists(_transactionId: string): boolean {
  return false
}

export function exportTransactions(): string {
  return '[]'
}
