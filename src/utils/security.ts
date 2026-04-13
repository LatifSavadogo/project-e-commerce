// Utilitaires de sécurité pour Ecomarket

/**
 * Nettoie les entrées utilisateur pour prévenir les attaques XSS
 * @param input - Chaîne à nettoyer
 * @returns Chaîne nettoyée
 */
export function sanitizeInput(input: string): string {
  if (!input) return ''
  
  // Remplace les caractères dangereux
  return input
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')
    .replace(/\//g, '&#x2F;')
}

/**
 * Valide une adresse email
 * @param email - Email à valider
 * @returns true si l'email est valide
 */
export function validateEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * Valide un mot de passe (minimum 8 caractères, avec lettres et chiffres)
 * @param password - Mot de passe à valider
 * @returns objet avec isValid et message d'erreur
 */
export function validatePassword(password: string): { isValid: boolean; message: string } {
  if (password.length < 8) {
    return { isValid: false, message: 'Le mot de passe doit contenir au moins 8 caractères' }
  }
  
  if (!/[a-zA-Z]/.test(password)) {
    return { isValid: false, message: 'Le mot de passe doit contenir des lettres' }
  }
  
  if (!/[0-9]/.test(password)) {
    return { isValid: false, message: 'Le mot de passe doit contenir des chiffres' }
  }
  
  return { isValid: true, message: '' }
}

/**
 * Hash un mot de passe (simulation simple - EN PRODUCTION, utiliser bcrypt côté serveur)
 * @param password - Mot de passe à hasher
 * @returns Hash du mot de passe
 */
export async function hashPassword(password: string): Promise<string> {
  // En production, ceci doit être fait côté serveur avec bcrypt
  // Ici, on fait une simulation simple
  const encoder = new TextEncoder()
  const data = encoder.encode(password)
  const hashBuffer = await crypto.subtle.digest('SHA-256', data)
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
}

/**
 * Limite le nombre de tentatives (protection contre brute force)
 */
class RateLimiter {
  private attempts: Map<string, { count: number; resetTime: number }> = new Map()
  private maxAttempts: number
  private windowMs: number

  constructor(maxAttempts = 5, windowMs = 15 * 60 * 1000) {
    this.maxAttempts = maxAttempts
    this.windowMs = windowMs
  }

  canAttempt(identifier: string): boolean {
    const now = Date.now()
    const record = this.attempts.get(identifier)

    if (!record) {
      this.attempts.set(identifier, { count: 1, resetTime: now + this.windowMs })
      return true
    }

    if (now > record.resetTime) {
      this.attempts.set(identifier, { count: 1, resetTime: now + this.windowMs })
      return true
    }

    if (record.count >= this.maxAttempts) {
      return false
    }

    record.count++
    return true
  }

  getRemainingTime(identifier: string): number {
    const record = this.attempts.get(identifier)
    if (!record) return 0
    
    const remaining = record.resetTime - Date.now()
    return remaining > 0 ? remaining : 0
  }

  reset(identifier: string): void {
    this.attempts.delete(identifier)
  }
}

export const loginRateLimiter = new RateLimiter(5, 15 * 60 * 1000) // 5 tentatives par 15 minutes

/**
 * Vérifie si une URL est sûre (protection contre les redirections malveillantes)
 * @param url - URL à vérifier
 * @returns true si l'URL est sûre
 */
export function isSafeUrl(url: string): boolean {
  try {
    const parsed = new URL(url, window.location.origin)
    return parsed.origin === window.location.origin
  } catch {
    return false
  }
}

/**
 * Génère un token CSRF (Cross-Site Request Forgery protection)
 * @returns Token CSRF
 */
export function generateCsrfToken(): string {
  const array = new Uint8Array(32)
  crypto.getRandomValues(array)
  return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('')
}

/**
 * Nettoie le localStorage (suppression des données sensibles)
 */
export function secureLogout(): void {
  // Supprime toutes les données sensibles
  localStorage.removeItem('user')
  localStorage.removeItem('authToken')
  sessionStorage.clear()
}

/**
 * Détecte les tentatives d'injection de scripts
 * @param input - Chaîne à vérifier
 * @returns true si l'entrée semble malveillante
 */
export function detectScriptInjection(input: string): boolean {
  const dangerousPatterns = [
    /<script/i,
    /javascript:/i,
    /on\w+=/i, // onclick, onerror, etc.
    /<iframe/i,
    /eval\(/i,
    /document\.cookie/i,
    /document\.write/i
  ]
  
  return dangerousPatterns.some(pattern => pattern.test(input))
}

/**
 * Limite la taille des uploads (protection contre DoS via gros fichiers)
 * @param file - Fichier à vérifier
 * @param maxSizeMB - Taille maximale en MB
 * @returns true si le fichier est acceptable
 */
export function validateFileSize(file: File, maxSizeMB = 5): boolean {
  const maxSizeBytes = maxSizeMB * 1024 * 1024
  return file.size <= maxSizeBytes
}

/**
 * Valide le type de fichier (protection contre upload de fichiers malveillants)
 * @param file - Fichier à vérifier
 * @param allowedTypes - Types MIME autorisés
 * @returns true si le type est autorisé
 */
export function validateFileType(file: File, allowedTypes: string[]): boolean {
  return allowedTypes.includes(file.type)
}

/**
 * Protection contre le clickjacking
 */
export function preventClickjacking(): void {
  if (window.self !== window.top) {
    window.top!.location.href = window.self.location.href
  }
}

// Appliquer la protection au chargement
if (typeof window !== 'undefined') {
  preventClickjacking()
}
