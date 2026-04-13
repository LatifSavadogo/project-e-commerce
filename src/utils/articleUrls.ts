import { API_BASE } from '../config/api'

export function articleMainPhotoUrl(articleId: number): string {
  return `${API_BASE}/api/v1/articles/${articleId}/photo`
}

export function articleGalleryPhotoUrl(articleId: number, filename: string): string {
  return `${API_BASE}/api/v1/articles/${articleId}/photo/${encodeURIComponent(filename)}`
}
