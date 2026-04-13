import { apiFetch, apiJson } from './apiClient'
import type { UserDtoJson } from '../types/backend'

export type MeResponse =
  | { authenticated: false }
  | { authenticated: true; user: UserDtoJson }

export type LoginResponse = { authenticated: true; user: UserDtoJson }

export async function loginRequest(email: string, password: string): Promise<LoginResponse> {
  return apiJson<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email: email.trim(), password }),
  })
}

export async function logoutRequest(): Promise<void> {
  await apiJson<unknown>('/api/auth/logout', { method: 'POST' })
}

export async function meRequest(): Promise<MeResponse> {
  return apiJson<MeResponse>('/api/auth/me')
}

export type RegisterParams = {
  nom: string
  prenom: string
  email: string
  password: string
  idpays?: number
  ville?: string
  /** Optionnel : l’inscription crée toujours un compte acheteur ; pièces complètes lors d’une demande vendeur/livreur. */
  cnib?: File
}

export async function forgotPasswordRequest(email: string): Promise<{ message: string }> {
  return apiJson<{ message: string }>('/api/auth/forgot-password', {
    method: 'POST',
    body: JSON.stringify({ email: email.trim() }),
  })
}

export async function resetPasswordRequest(token: string, newPassword: string): Promise<{ message: string }> {
  return apiJson<{ message: string }>('/api/auth/reset-password', {
    method: 'POST',
    body: JSON.stringify({ token: token.trim(), newPassword }),
  })
}

export async function registerRequest(p: RegisterParams): Promise<UserDtoJson> {
  const fd = new FormData()
  fd.append('nom', p.nom.trim())
  fd.append('prenom', p.prenom.trim())
  fd.append('email', p.email.trim())
  fd.append('password', p.password)
  if (p.idpays != null) fd.append('idpays', String(p.idpays))
  if (p.ville != null && p.ville.trim() !== '') fd.append('ville', p.ville.trim())
  if (p.cnib) fd.append('cnib', p.cnib)

  const res = await apiFetch('/api/auth/register', { method: 'POST', body: fd })
  const text = await res.text()
  let data: unknown = {}
  if (text) {
    try {
      data = JSON.parse(text) as unknown
    } catch {
      data = {}
    }
  }
  if (!res.ok) {
    const o = data as { error?: string }
    const msg =
      typeof o.error === 'string'
        ? o.error
        : res.status === 413
          ? 'Fichier ou formulaire trop volumineux pour le serveur.'
          : res.statusText
    throw new Error(msg)
  }
  return data as UserDtoJson
}
