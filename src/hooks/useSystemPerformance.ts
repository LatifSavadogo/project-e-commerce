import { useState, useEffect } from 'react'

export type SystemPerformance = {
  cpuUsage: number
  memoryUsage: number
  totalMemory: number
  usedMemory: number
  uptime: number
  timestamp: number
}

export function useSystemPerformance() {
  const [performance, setPerformance] = useState<SystemPerformance>({
    cpuUsage: 0,
    memoryUsage: 0,
    totalMemory: 0,
    usedMemory: 0,
    uptime: 0,
    timestamp: Date.now()
  })

  useEffect(() => {
    const updatePerformance = () => {
      // Utilisation de l'API Performance et Memory (disponible dans certains navigateurs)
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const perf = (performance as any)
      
      // Estimation CPU basée sur le temps de réponse
      const cpuEstimate = Math.random() * 30 + 20 // Simulation entre 20-50%
      
      // Mémoire utilisée par la page
      let memoryUsage = 0
      let usedMemory = 0
      let totalMemory = 0
      
      if (perf.memory) {
        usedMemory = perf.memory.usedJSHeapSize / 1048576 // Convertir en MB
        totalMemory = perf.memory.jsHeapSizeLimit / 1048576
        memoryUsage = (usedMemory / totalMemory) * 100
      } else {
        // Valeurs simulées si l'API n'est pas disponible
        totalMemory = 8192 // 8GB simulé
        usedMemory = Math.random() * 2048 + 1024 // Entre 1-3GB
        memoryUsage = (usedMemory / totalMemory) * 100
      }

      // Uptime du navigateur/session
      const uptime = perf.now() / 1000 // En secondes

      setPerformance({
        cpuUsage: cpuEstimate,
        memoryUsage,
        totalMemory,
        usedMemory,
        uptime,
        timestamp: Date.now()
      })
    }

    // Mise à jour initiale
    updatePerformance()

    // Mise à jour toutes les 2 secondes
    const interval = setInterval(updatePerformance, 2000)

    return () => clearInterval(interval)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return performance
}
