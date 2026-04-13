// Service de génération de reçus supprimé pour repartir de zéro sur la gestion du paiement.
// Placeholder minimal pour éviter les erreurs TypeScript.

export type Transaction = Record<string, unknown>

export function downloadReceipt(_transaction: Transaction): void {
  // no-op
}

export function previewReceipt(_transaction: Transaction): void {
  // no-op
}

export function generateTransactionId(): string {
  const timestamp = Date.now()
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
  return `ECO-${timestamp}-${random}`
}
