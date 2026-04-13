/** Normalise une date renvoyée par le backend (souvent ISO string). */
export function dateFromDto(value: unknown): string {
  if (value == null) return new Date().toISOString()
  if (typeof value === 'string') {
    const d = new Date(value)
    return Number.isNaN(d.getTime()) ? new Date().toISOString() : d.toISOString()
  }
  if (Array.isArray(value) && value.length >= 3) {
    const [y, m, day, h = 0, min = 0, sec = 0] = value as number[]
    return new Date(y, (m as number) - 1, day, h, min, sec).toISOString()
  }
  return new Date().toISOString()
}
