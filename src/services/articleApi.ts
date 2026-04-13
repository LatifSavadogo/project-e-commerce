import { apiFetch, apiJson } from './apiClient'
import type { ArticleDtoJson } from '../types/backend'

export async function fetchArticles(): Promise<ArticleDtoJson[]> {
  return apiJson<ArticleDtoJson[]>('/api/v1/articles')
}

/** Toutes les annonces (y compris bloquées) — admin / super-admin. */
export async function fetchAdminArticles(): Promise<ArticleDtoJson[]> {
  return apiJson<ArticleDtoJson[]>('/api/v1/admin/articles')
}

export async function fetchArticleById(id: number): Promise<ArticleDtoJson> {
  return apiJson<ArticleDtoJson>(`/api/v1/articles/${id}`)
}

export type CreateArticleParams = {
  libarticle: string
  descarticle: string
  prixunitaire: number
  idtype?: number
  photos: File[]
}

export async function createArticleMultipart(p: CreateArticleParams): Promise<ArticleDtoJson> {
  const fd = new FormData()
  fd.append('libarticle', p.libarticle)
  fd.append('descarticle', p.descarticle)
  fd.append('prixunitaire', String(Math.round(p.prixunitaire)))
  if (p.idtype != null) fd.append('idtype', String(p.idtype))
  for (const file of p.photos) {
    fd.append('photos', file)
  }
  const res = await apiFetch('/api/v1/articles', { method: 'POST', body: fd })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) {
    const msg = typeof data === 'string' ? data : (data as { error?: string }).error || res.statusText
    throw new Error(msg)
  }
  return data as ArticleDtoJson
}

export async function updateArticleJson(id: number, body: Partial<ArticleDtoJson>): Promise<ArticleDtoJson> {
  return apiJson<ArticleDtoJson>(`/api/v1/articles/${id}/json`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function deleteArticle(id: number): Promise<void> {
  const res = await apiFetch(`/api/v1/articles/${id}`, { method: 'DELETE' })
  if (!res.ok) {
    const t = await res.text()
    throw new Error(t || res.statusText)
  }
}

export async function adminPatchArticle(
  id: number,
  body: { blocked?: boolean; warningMessage?: string | null; clearWarning?: boolean }
): Promise<void> {
  const res = await apiFetch(`/api/v1/admin/articles/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(body),
  })
  if (!res.ok) {
    const t = await res.text()
    throw new Error(t || res.statusText)
  }
}
