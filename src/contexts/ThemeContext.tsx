/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'

const STORAGE_KEY = 'ecomarket-theme'

export type ThemeMode = 'dark' | 'light'

export type ThemeChartColors = {
  axis: string
  grid: string
  tooltipBg: string
  tooltipBorder: string
  tooltipText: string
  tooltipMuted: string
}

type ThemeContextValue = {
  theme: ThemeMode
  setTheme: (mode: ThemeMode) => void
  toggleTheme: () => void
  chart: ThemeChartColors
}

const ThemeContext = createContext<ThemeContextValue | undefined>(undefined)

function readStoredTheme(): ThemeMode {
  try {
    const v = localStorage.getItem(STORAGE_KEY)
    if (v === 'light' || v === 'dark') return v
  } catch {
    /* ignore */
  }
  if (typeof window !== 'undefined' && window.matchMedia('(prefers-color-scheme: light)').matches) {
    return 'light'
  }
  return 'dark'
}

function applyDocumentTheme(mode: ThemeMode) {
  document.documentElement.setAttribute('data-theme', mode)
  document.documentElement.style.colorScheme = mode === 'light' ? 'light' : 'dark'
}

const CHART_DARK: ThemeChartColors = {
  axis: '#8b92a3',
  grid: '#2d3548',
  tooltipBg: '#1a1e28',
  tooltipBorder: '#2d3548',
  tooltipText: '#e8eaef',
  tooltipMuted: '#8b92a3',
}

const CHART_LIGHT: ThemeChartColors = {
  axis: '#64748b',
  grid: '#cbd5e1',
  tooltipBg: '#f1f5f9',
  tooltipBorder: '#cbd5e1',
  tooltipText: '#0f172a',
  tooltipMuted: '#64748b',
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setThemeState] = useState<ThemeMode>(() => {
    if (typeof document === 'undefined') return 'dark'
    return readStoredTheme()
  })

  useEffect(() => {
    applyDocumentTheme(theme)
    try {
      localStorage.setItem(STORAGE_KEY, theme)
    } catch {
      /* ignore */
    }
  }, [theme])

  const setTheme = useCallback((mode: ThemeMode) => {
    setThemeState(mode)
  }, [])

  const toggleTheme = useCallback(() => {
    document.documentElement.classList.add('theme-switching')
    setThemeState((t) => (t === 'dark' ? 'light' : 'dark'))
    window.setTimeout(() => {
      document.documentElement.classList.remove('theme-switching')
    }, 400)
  }, [])

  const chart = theme === 'light' ? CHART_LIGHT : CHART_DARK

  const value = useMemo(
    () => ({ theme, setTheme, toggleTheme, chart }),
    [theme, setTheme, toggleTheme, chart]
  )

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>
}

export function useTheme() {
  const ctx = useContext(ThemeContext)
  if (!ctx) {
    throw new Error('useTheme must be used within ThemeProvider')
  }
  return ctx
}

/** Pour du code hors arbre React (rare) */
export function getThemeFromDocument(): ThemeMode {
  if (typeof document === 'undefined') return 'dark'
  const a = document.documentElement.getAttribute('data-theme')
  return a === 'light' ? 'light' : 'dark'
}
