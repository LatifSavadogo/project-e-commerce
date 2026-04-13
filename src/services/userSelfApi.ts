import { apiFetch } from './apiClient'

/** Export JSON RGPD (téléchargement navigateur). */
export async function downloadMyDataExport(): Promise<void> {
  const res = await apiFetch('/api/v1/users/me/data-export')
  if (!res.ok) {
    const t = await res.text()
    throw new Error(t || res.statusText)
  }
  const blob = await res.blob()
  const cd = res.headers.get('Content-Disposition')
  let filename = 'ecomarket-mes-donnees.json'
  const m = cd?.match(/filename="([^"]+)"/)
  if (m) filename = m[1]
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}
