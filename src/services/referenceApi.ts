import { apiJson } from './apiClient'
import type {
  FamilleArticleDtoJson,
  PaysDtoJson,
  RoleDtoJson,
  TypeArticleDtoJson,
} from '../types/backend'

export async function fetchRoles(): Promise<RoleDtoJson[]> {
  return apiJson<RoleDtoJson[]>('/api/v1/roles')
}

export async function fetchPays(): Promise<PaysDtoJson[]> {
  return apiJson<PaysDtoJson[]>('/api/v1/pays')
}

export async function fetchTypeArticles(): Promise<TypeArticleDtoJson[]> {
  return apiJson<TypeArticleDtoJson[]>('/api/v1/typeArticles')
}

export async function fetchFamilleArticles(): Promise<FamilleArticleDtoJson[]> {
  return apiJson<FamilleArticleDtoJson[]>('/api/v1/familleArticles')
}

export type MutateResponse = { supprimé?: boolean; message?: string }

export async function createFamilleArticle(body: {
  libfamille: string
  description: string
}): Promise<FamilleArticleDtoJson> {
  return apiJson<FamilleArticleDtoJson>('/api/v1/familleArticles', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function updateFamilleArticle(
  idfamille: number,
  body: { libfamille: string; description: string },
): Promise<FamilleArticleDtoJson> {
  return apiJson<FamilleArticleDtoJson>(`/api/v1/familleArticles/${idfamille}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function deleteFamilleArticle(idfamille: number): Promise<MutateResponse> {
  return apiJson<MutateResponse>(`/api/v1/familleArticles/${idfamille}`, { method: 'DELETE' })
}

export async function createTypeArticle(body: {
  libtype: string
  desctype: string
  idfamille: number
}): Promise<TypeArticleDtoJson> {
  return apiJson<TypeArticleDtoJson>('/api/v1/typeArticles', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function updateTypeArticle(
  idtype: number,
  body: { libtype: string; desctype: string; idfamille: number },
): Promise<TypeArticleDtoJson> {
  return apiJson<TypeArticleDtoJson>(`/api/v1/typeArticles/${idtype}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function deleteTypeArticle(idtype: number): Promise<MutateResponse> {
  return apiJson<MutateResponse>(`/api/v1/typeArticles/${idtype}`, { method: 'DELETE' })
}

export async function createPays(body: { libpays: string; descpays: string }): Promise<PaysDtoJson> {
  return apiJson<PaysDtoJson>('/api/v1/pays', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function updatePays(
  idpays: number,
  body: { libpays: string; descpays: string },
): Promise<PaysDtoJson> {
  return apiJson<PaysDtoJson>(`/api/v1/pays/${idpays}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function deletePays(idpays: number): Promise<MutateResponse> {
  return apiJson<MutateResponse>(`/api/v1/pays/${idpays}`, { method: 'DELETE' })
}

export async function createRole(body: { librole: string; descrole: string }): Promise<RoleDtoJson> {
  return apiJson<RoleDtoJson>('/api/v1/roles', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export async function updateRole(
  idrole: number,
  body: { librole: string; descrole: string },
): Promise<RoleDtoJson> {
  return apiJson<RoleDtoJson>(`/api/v1/roles/${idrole}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export async function deleteRole(idrole: number): Promise<MutateResponse> {
  return apiJson<MutateResponse>(`/api/v1/roles/${idrole}`, { method: 'DELETE' })
}
