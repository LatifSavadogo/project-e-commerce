import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import basicSsl from '@vitejs/plugin-basic-ssl'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

/** Certificat incluant l’IP LAN (généré par scripts/gen-vite-ssl-cert.ps1). Sinon basic-ssl = localhost seulement → erreur sur https://192.168.x.x */
function readLanDevHttps(): { key: Buffer; cert: Buffer } | null {
  const keyPath = path.join(__dirname, 'certs', 'vite-dev-key.pem')
  const certPath = path.join(__dirname, 'certs', 'vite-dev.pem')
  try {
    if (fs.existsSync(keyPath) && fs.existsSync(certPath)) {
      return { key: fs.readFileSync(keyPath), cert: fs.readFileSync(certPath) }
    }
  } catch {
    /* ignore */
  }
  return null
}

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const devHttps = env.VITE_DEV_HTTPS === 'true'
  /** Même origine : les appels /api/* sont proxifiés vers Spring (évite mixed content HTTPS → HTTP). */
  const useProxy = env.VITE_USE_DEV_PROXY === 'true'
  const lanHttps = devHttps ? readLanDevHttps() : null

  return {
    /** Sans certs/vite-dev*.pem, basic-ssl = SAN localhost uniquement → erreur si vous ouvrez https://IP_LAN */
    plugins: [react(), ...(devHttps && !lanHttps ? [basicSsl()] : [])],
    server: {
      host: true,
      port: Number(env.VITE_DEV_PORT || 5173),
      /** Cert LAN explicite ; sinon laisser vide : @vitejs/plugin-basic-ssl injecte HTTPS au build. */
      ...(lanHttps ? { https: lanHttps } : {}),
      proxy: useProxy
        ? {
            '/api': {
              target: env.VITE_PROXY_TARGET || 'http://127.0.0.1:8080',
              changeOrigin: true,
              secure: false,
              configure: (proxy) => {
                proxy.on('proxyReq', (proxyReq, req) => {
                  if (devHttps) {
                    proxyReq.setHeader('X-Forwarded-Proto', 'https')
                    const h = req.headers.host
                    if (typeof h === 'string') proxyReq.setHeader('X-Forwarded-Host', h)
                  }
                })
              },
            },
          }
        : undefined,
    },
  }
})
