import { useCallback, useEffect, useRef, useState } from 'react'
import { BrowserQRCodeReader } from '@zxing/browser'
import { ExternalLink, LocateFixed, MapPin, RefreshCw, Route, ScanLine } from 'lucide-react'
import { ApiError } from '../../services/apiClient'
import LivreurQrCameraScanner from '../../components/LivreurQrCameraScanner'
import { fetchLivreurDashboard, publierPositionLivreur, terminerLivraisonParScan } from '../../services/livreurApi'
import type { LivraisonLivreurDtoJson } from '../../types/backend'

const POLL_MS = 10_000

function parseClientQrPayload(raw: string): { idlivraison: number; tokenTail: string } | null {
  const t = raw.trim()
  if (!t.startsWith('ECOM;')) return null
  const parts = t.split(';', 3)
  if (parts.length !== 3 || !parts[1].trim() || !parts[2].trim()) return null
  const idlivraison = parseInt(parts[1].trim(), 10)
  if (Number.isNaN(idlivraison)) return null
  const token = parts[2].trim()
  const tokenTail = token.length > 4 ? `…${token.slice(-4)}` : '•••'
  return { idlivraison, tokenTail }
}

async function tryDecodeQrFromImageFile(file: File): Promise<string | null> {
  const url = URL.createObjectURL(file)
  try {
    const reader = new BrowserQRCodeReader()
    const result = await reader.decodeFromImageUrl(url)
    return result.getText() || null
  } catch {
    return null
  } finally {
    URL.revokeObjectURL(url)
  }
}

export default function LivreurCoursesPage() {
  const [enCours, setEnCours] = useState<LivraisonLivreurDtoJson[]>([])
  const [busyId, setBusyId] = useState<number | null>(null)
  const [posBusyId, setPosBusyId] = useState<number | null>(null)
  const [err, setErr] = useState<string | null>(null)
  const [scanForId, setScanForId] = useState<number | null>(null)
  const [scanStep, setScanStep] = useState<'capture' | 'recap'>('capture')
  const [scanText, setScanText] = useState('')
  /** Sur http://IP locale le navigateur n’expose pas la caméra : on évite d’afficher l’erreur par défaut. */
  const [cameraOn, setCameraOn] = useState(() =>
    typeof window !== 'undefined' ? window.isSecureContext : false
  )
  const [camHint, setCamHint] = useState<string | null>(null)
  const [scanSuccess, setScanSuccess] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const load = useCallback(async () => {
    setErr(null)
    try {
      const d = await fetchLivreurDashboard()
      const list = (d.dernieresCourses || []).filter((c) => c.statut === 'EN_COURS')
      setEnCours(list)
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'Erreur')
    }
  }, [])

  useEffect(() => {
    if (!scanSuccess) return
    const t = window.setTimeout(() => setScanSuccess(null), 5000)
    return () => window.clearTimeout(t)
  }, [scanSuccess])

  useEffect(() => {
    void load()
    const t = window.setInterval(() => void load(), POLL_MS)
    return () => window.clearInterval(t)
  }, [load])

  const openScan = (idlivraison: number) => {
    setScanForId(idlivraison)
    setScanText('')
    setScanStep('capture')
    setCameraOn(typeof window !== 'undefined' && window.isSecureContext)
    setCamHint(null)
  }

  const closeScan = () => {
    setScanForId(null)
    setScanText('')
    setScanStep('capture')
    setCameraOn(false)
    setCamHint(null)
  }

  const tryGoRecap = (): boolean => {
    if (!scanText.trim()) {
      alert('Collez ou scannez le contenu du QR du client (format ECOM;…).')
      return false
    }
    const p = parseClientQrPayload(scanText)
    if (!p) {
      alert('QR invalide : le texte doit commencer par ECOM; et contenir l’identifiant et le jeton.')
      return false
    }
    if (scanForId != null && p.idlivraison !== scanForId) {
      alert(`Ce QR correspond à la livraison #${p.idlivraison}, pas à la livraison #${scanForId}.`)
      return false
    }
    setScanStep('recap')
    setCamHint(null)
    return true
  }

  const applyDecodedPayload = (text: string) => {
    setScanText(text)
    setCameraOn(false)
    const p = parseClientQrPayload(text)
    if (!p) {
      setCamHint('QR détecté mais format invalide (attendu ECOM;id;jeton).')
      return
    }
    if (scanForId != null && p.idlivraison !== scanForId) {
      setCamHint(`Ce QR est pour la livraison #${p.idlivraison}, pas #${scanForId}.`)
      return
    }
    setCamHint(null)
    setScanStep('recap')
  }

  const onTerminerScan = async () => {
    if (!scanText.trim()) return
    setBusyId(scanForId)
    try {
      await terminerLivraisonParScan(scanText.trim())
      closeScan()
      await load()
      setScanSuccess('Livraison exécutée.')
    } catch (e) {
      alert(e instanceof ApiError ? e.message : e instanceof Error ? e.message : 'Erreur')
    } finally {
      setBusyId(null)
    }
  }

  const onPickPhoto = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    const decoded = await tryDecodeQrFromImageFile(file)
    if (decoded) {
      applyDecodedPayload(decoded)
      return
    }
    alert(
      'QR non détecté dans la photo. Utilisez un autre cliché ou collez le texte du QR à la main (ou un lecteur externe).'
    )
  }

  const courseForScan = scanForId != null ? enCours.find((c) => c.idlivraison === scanForId) : undefined
  const parsedScan = parseClientQrPayload(scanText)

  const sendMyPosition = (idlivraison: number) => {
    if (!navigator.geolocation) {
      alert('La géolocalisation n’est pas disponible sur cet appareil.')
      return
    }
    setPosBusyId(idlivraison)
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        void publierPositionLivreur(idlivraison, pos.coords.latitude, pos.coords.longitude)
          .then(() => {
            alert('Position envoyée. Le client peut ouvrir le suivi de commande pour voir l’itinéraire vers chez lui sur Google Maps.')
          })
          .catch((e) => {
            alert(e instanceof ApiError ? e.message : e instanceof Error ? e.message : 'Erreur envoi position')
          })
          .finally(() => setPosBusyId(null))
      },
      () => {
        alert('Impossible d’obtenir votre position. Vérifiez les autorisations du navigateur.')
        setPosBusyId(null)
      },
      { enableHighAccuracy: true, timeout: 20_000, maximumAge: 0 }
    )
  }

  return (
    <div className="container" style={{ paddingTop: 28, paddingBottom: 48, maxWidth: 960 }}>
      <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
        <div>
          <h1 style={{ margin: '0 0 6px', fontSize: '1.55rem', display: 'flex', alignItems: 'center', gap: 10 }}>
            <Route size={26} strokeWidth={2} aria-hidden />
            Mes courses en cours
          </h1>
          <p className="meta" style={{ margin: 0 }}>
            Ouvrez l’itinéraire après acceptation. Scannez le QR du client, vérifiez le récapitulatif, puis terminez la
            livraison. Vous pouvez envoyer votre position pour que le client vous suive sur Maps.
          </p>
        </div>
        <button type="button" className="livreur-btn-ghost" onClick={() => void load()}>
          <RefreshCw size={16} aria-hidden />
          Actualiser
        </button>
      </div>

      {err && (
        <p role="alert" style={{ color: 'var(--danger)', marginTop: 20 }}>
          {err}
        </p>
      )}

      {scanSuccess && (
        <p
          role="status"
          style={{
            marginTop: 16,
            padding: '10px 12px',
            borderRadius: 10,
            background: 'var(--input-bg)',
            border: '1px solid var(--border)',
            fontSize: '0.92rem',
            fontWeight: 600,
          }}
        >
          {scanSuccess}
        </p>
      )}

      {enCours.length === 0 ? (
        <div className="livreur-empty" style={{ marginTop: 28 }}>
          Vous n’avez aucune course active. Acceptez une offre depuis « Offres à saisir ».
        </div>
      ) : (
        <ul className="livreur-run-list" style={{ marginTop: 24 }}>
          {enCours.map((c) => (
            <li key={c.idlivraison} className="livreur-run-card livreur-run-card--active">
              <div style={{ flex: 1, minWidth: 0 }}>
                <strong>{c.articleLibelle || `Livraison #${c.idlivraison}`}</strong>
                <div className="livreur-run-meta">
                  <MapPin size={14} style={{ verticalAlign: '-2px', marginRight: 4 }} aria-hidden />
                  {c.acheteurVille || '—'}
                  <br />
                  Engin : <strong>{c.typeEnginUtilise || '—'}</strong>
                </div>
                {c.navigation && (
                  <div style={{ marginTop: 12, display: 'flex', flexDirection: 'column', gap: 6, fontSize: '0.88rem' }}>
                    {c.navigation.itineraireComplet && (
                      <a
                        href={c.navigation.itineraireComplet}
                        target="_blank"
                        rel="noreferrer"
                        style={{ display: 'inline-flex', alignItems: 'center', gap: 6, color: 'var(--link)' }}
                      >
                        <ExternalLink size={14} aria-hidden />
                        Itinéraire complet (vous → vendeur → client)
                      </a>
                    )}
                    {c.navigation.etapeRetraitVendeur && (
                      <a
                        href={c.navigation.etapeRetraitVendeur}
                        target="_blank"
                        rel="noreferrer"
                        style={{ display: 'inline-flex', alignItems: 'center', gap: 6, color: 'var(--link)' }}
                      >
                        <ExternalLink size={14} aria-hidden />
                        Retrait chez le vendeur
                      </a>
                    )}
                    {c.navigation.etapeDepotClient && (
                      <a
                        href={c.navigation.etapeDepotClient}
                        target="_blank"
                        rel="noreferrer"
                        style={{ display: 'inline-flex', alignItems: 'center', gap: 6, color: 'var(--link)' }}
                      >
                        <ExternalLink size={14} aria-hidden />
                        Livraison chez le client
                      </a>
                    )}
                  </div>
                )}
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8, alignItems: 'stretch' }}>
                <button
                  type="button"
                  className="livreur-btn-secondary"
                  style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 8 }}
                  disabled={posBusyId === c.idlivraison}
                  onClick={() => sendMyPosition(c.idlivraison)}
                >
                  <LocateFixed size={18} aria-hidden />
                  {posBusyId === c.idlivraison ? 'Position…' : 'Envoyer ma position (suivi client)'}
                </button>
                <button
                  type="button"
                  className="button-primary"
                  style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 8 }}
                  disabled={busyId === c.idlivraison}
                  onClick={() => openScan(c.idlivraison)}
                >
                  <ScanLine size={18} aria-hidden />
                  Scanner QR &amp; terminer
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {scanForId != null && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="scan-livr-title"
          style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(0,0,0,0.5)',
            zIndex: 150,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: 16,
          }}
          onClick={(e) => e.target === e.currentTarget && closeScan()}
        >
          <div
            className="card"
            style={{
              maxWidth: 440,
              width: '100%',
              padding: 20,
              background: 'var(--surface-elevated)',
              borderRadius: 14,
              border: '1px solid var(--border)',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h2 id="scan-livr-title" style={{ margin: '0 0 12px', fontSize: '1.1rem' }}>
              {scanStep === 'capture' ? `Scanner le QR — livraison #${scanForId}` : `Récapitulatif — livraison #${scanForId}`}
            </h2>

            {scanStep === 'recap' && courseForScan && (
              <div
                style={{
                  marginBottom: 16,
                  padding: 12,
                  borderRadius: 10,
                  background: 'var(--input-bg)',
                  border: '1px solid var(--border)',
                  fontSize: '0.9rem',
                  lineHeight: 1.5,
                }}
              >
                <p style={{ margin: '0 0 8px' }}>
                  <strong>Article</strong> : {courseForScan.articleLibelle || '—'}
                </p>
                <p style={{ margin: '0 0 8px' }}>
                  <strong>Quantité</strong> : {courseForScan.quantite ?? '—'}
                </p>
                <p style={{ margin: '0 0 8px' }}>
                  <strong>Client</strong> : {courseForScan.acheteurVille || courseForScan.acheteurEmail || '—'}
                </p>
                {courseForScan.vendeurEmail && (
                  <p style={{ margin: '0 0 8px' }}>
                    <strong>Vendeur</strong> : {courseForScan.vendeurEmail}
                  </p>
                )}
                {parsedScan && (
                  <p style={{ margin: 0 }} className="meta">
                    QR reconnu : livraison #{parsedScan.idlivraison}, jeton {parsedScan.tokenTail}
                  </p>
                )}
              </div>
            )}

            {scanStep === 'capture' && (
              <>
                <p className="meta" style={{ marginBottom: 12 }}>
                  Utilisez la caméra, importez une photo du QR, ou collez le texte exact (ECOM;…).
                </p>
                {!window.isSecureContext && (
                  <p className="meta" style={{ marginBottom: 10, fontSize: '0.82rem' }}>
                    En <strong>http</strong> (sauf localhost), la caméra est souvent bloquée par le navigateur. Utilisez{' '}
                    <strong>https</strong> ou <strong>localhost</strong>, ou « Choisir une photo » / collage du texte du QR.
                  </p>
                )}
                <div style={{ display: 'flex', gap: 8, marginBottom: 10, flexWrap: 'wrap' }}>
                  <button
                    type="button"
                    className={cameraOn ? 'button-primary' : 'livreur-btn-secondary'}
                    style={{ flex: '1 1 140px' }}
                    onClick={() => {
                      setCameraOn(true)
                      setCamHint(null)
                    }}
                  >
                    Caméra en direct
                  </button>
                  <button
                    type="button"
                    className={!cameraOn ? 'button-primary' : 'livreur-btn-secondary'}
                    style={{ flex: '1 1 140px' }}
                    onClick={() => setCameraOn(false)}
                  >
                    Sans caméra
                  </button>
                </div>
                {camHint && (
                  <p role="status" style={{ color: 'var(--muted)', fontSize: '0.82rem', marginBottom: 8 }}>
                    {camHint}
                  </p>
                )}
                {cameraOn && (
                  <LivreurQrCameraScanner
                    key={scanForId}
                    active
                    onDecoded={(text) => applyDecodedPayload(text)}
                    onScannerError={(msg) => setCamHint(msg)}
                  />
                )}
                <input ref={fileInputRef} type="file" accept="image/*" capture="environment" className="visually-hidden" onChange={(e) => void onPickPhoto(e)} />
                <button
                  type="button"
                  className="livreur-btn-secondary"
                  style={{ marginBottom: 10, width: '100%' }}
                  onClick={() => fileInputRef.current?.click()}
                >
                  Choisir une photo du QR (galerie ou appareil photo)
                </button>
                <textarea
                  value={scanText}
                  onChange={(e) => {
                    setScanText(e.target.value)
                    setScanStep('capture')
                  }}
                  placeholder="Contenu du QR…"
                  rows={4}
                  style={{
                    width: '100%',
                    boxSizing: 'border-box',
                    padding: 10,
                    borderRadius: 10,
                    border: '1px solid var(--border)',
                    background: 'var(--input-bg)',
                    color: 'var(--text)',
                    fontFamily: 'monospace',
                    fontSize: '0.82rem',
                    marginBottom: 12,
                  }}
                />
              </>
            )}

            <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end', flexWrap: 'wrap' }}>
              <button type="button" className="livreur-btn-ghost" onClick={closeScan}>
                Annuler
              </button>
              {scanStep === 'capture' ? (
                <button type="button" className="button-primary" disabled={busyId != null} onClick={() => tryGoRecap()}>
                  Voir le récapitulatif
                </button>
              ) : (
                <>
                  <button type="button" className="livreur-btn-secondary" onClick={() => setScanStep('capture')}>
                    Modifier le scan
                  </button>
                  <button
                    type="button"
                    className="button-primary"
                    disabled={busyId != null}
                    onClick={() => void onTerminerScan()}
                  >
                    Terminer la livraison
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
