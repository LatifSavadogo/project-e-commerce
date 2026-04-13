function stripTrailingSlash(url: string): string {
  return url.replace(/\/$/, '')
}

/**
 * URL du backend Spring Boot.
 *
 * Avec `VITE_USE_DEV_PROXY=true` (mode `npm run dev:https`), les requêtes passent par Vite
 * (`/api` → localhost:8080) : même origine HTTPS, pas de contenu mixte, session cookie OK.
 *
 * Sinon, si `VITE_API_BASE_URL` n’est pas défini et que la page est sur le LAN
 * (ex. `http://192.168.11.106:5173`), utiliser `http(s)://<même-hôte>:8080`.
 */
export function resolveApiBase(): string {
  if (import.meta.env.VITE_USE_DEV_PROXY === 'true') {
    return ''
  }
  const fromEnv = import.meta.env.VITE_API_BASE_URL
  if (typeof fromEnv === 'string' && fromEnv.trim() !== '') {
    return stripTrailingSlash(fromEnv.trim())
  }
  if (typeof window !== 'undefined') {
    const host = window.location.hostname
    if (host && host !== 'localhost' && host !== '127.0.0.1') {
      const proto = window.location.protocol === 'https:' ? 'https:' : 'http:'
      return stripTrailingSlash(`${proto}//${host}:8080`)
    }
  }
  return 'http://localhost:8080'
}

export const API_BASE = resolveApiBase()
