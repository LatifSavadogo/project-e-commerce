import { API_BASE } from '../config/api'

export class ApiError extends Error {
  status: number
  body?: unknown

  constructor(status: number, message: string, body?: unknown) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

function buildUrl(path: string): string {
  if (path.startsWith('http')) return path
  const p = path.startsWith('/') ? path : `/${path}`
  if (!API_BASE) return p
  const base = API_BASE.replace(/\/$/, '')
  return `${base}${p}`
}

export async function apiFetch(path: string, init?: RequestInit): Promise<Response> {
  const headers = new Headers(init?.headers)
  if (init?.body && !(init.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  return fetch(buildUrl(path), {
    ...init,
    credentials: 'include',
    headers,
  })
}

async function readBody(res: Response): Promise<unknown> {
  const text = await res.text()
  if (!text) return null
  try {
    return JSON.parse(text) as unknown
  } catch {
    return text
  }
}

function errorMessage(data: unknown, fallback: string): string {
  if (data && typeof data === 'object') {
    const o = data as Record<string, unknown>
    if (typeof o.error === 'string') return o.error
    if (typeof o.message === 'string') return o.message
  }
  if (typeof data === 'string' && data.length > 0) {
    const t = data.trim()
    if (/^<!DOCTYPE|^<html/i.test(t) || t.includes('<title>')) {
      return fallback === 'Internal Server Error'
        ? 'Erreur serveur (réponse HTML). Vérifiez l’URL de l’API, les logs Spring Boot et que le backend est à jour.'
        : fallback
    }
    return t.length > 200 ? `${t.slice(0, 200)}…` : t
  }
  return fallback
}

export async function apiJson<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await apiFetch(path, init)
  const data = await readBody(res)
  if (!res.ok) {
    throw new ApiError(res.status, errorMessage(data, res.statusText), data)
  }
  return data as T
}
