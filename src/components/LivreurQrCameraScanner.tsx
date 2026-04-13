import { useCallback, useEffect, useRef, useState } from 'react'
import { BrowserQRCodeReader } from '@zxing/browser'

type Props = {
  /** Quand false, la caméra est arrêtée et le flux libéré. */
  active: boolean
  /** Appelé une fois un QR lu (le parent arrête en général la caméra). */
  onDecoded: (text: string) => void
  onScannerError?: (message: string) => void
}

type Phase = 'idle' | 'starting' | 'scanning'

/**
 * Caméra pour scanner le QR client.
 * Sur iOS / Safari, l’accès caméra doit être déclenché par un **tap** utilisateur : pas de démarrage auto dans un effet.
 */
export default function LivreurQrCameraScanner({ active, onDecoded, onScannerError }: Props) {
  const videoRef = useRef<HTMLVideoElement>(null)
  const streamRef = useRef<MediaStream | null>(null)
  const scanControlsRef = useRef<{ stop: () => void } | null>(null)
  const onDecodedRef = useRef(onDecoded)
  onDecodedRef.current = onDecoded

  const [phase, setPhase] = useState<Phase>('idle')

  const stopScanOnly = useCallback(() => {
    try {
      scanControlsRef.current?.stop()
    } catch {
      /* ignore */
    }
    scanControlsRef.current = null
  }, [])

  const stopAll = useCallback(() => {
    stopScanOnly()
    const v = videoRef.current
    if (v) {
      v.srcObject = null
    }
    streamRef.current?.getTracks().forEach((t) => t.stop())
    streamRef.current = null
    setPhase('idle')
  }, [stopScanOnly])

  useEffect(() => {
    if (!active) {
      stopAll()
    }
    return () => {
      stopAll()
    }
  }, [active, stopAll])

  const requestCamera = useCallback(async () => {
    if (!window.isSecureContext) {
      onScannerError?.('HTTPS ou localhost requis pour la caméra sur ce navigateur.')
      return
    }
    if (!navigator.mediaDevices?.getUserMedia) {
      onScannerError?.('Caméra non disponible (navigateur trop ancien ou contexte restreint).')
      return
    }
    setPhase('starting')
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: {
          facingMode: { ideal: 'environment' },
          width: { ideal: 1280 },
          height: { ideal: 720 },
        },
        audio: false,
      })
      streamRef.current = stream
      const video = videoRef.current
      if (!video) {
        stream.getTracks().forEach((t) => t.stop())
        setPhase('idle')
        return
      }
      video.setAttribute('playsinline', 'true')
      video.setAttribute('webkit-playsinline', 'true')
      video.muted = true
      video.srcObject = stream
      await video.play()
      setPhase('scanning')
    } catch (e) {
      setPhase('idle')
      const msg =
        e instanceof DOMException && e.name === 'NotAllowedError'
          ? 'Caméra refusée. Appuyez à nouveau sur le bouton et acceptez la permission quand le téléphone ou le navigateur la demande (réglages → autorisations si besoin).'
          : e instanceof DOMException && e.name === 'NotFoundError'
            ? 'Aucune caméra détectée sur cet appareil.'
            : e instanceof Error
              ? e.message
              : 'Impossible d’ouvrir la caméra.'
      onScannerError?.(msg)
    }
  }, [onScannerError])

  useEffect(() => {
    if (phase !== 'scanning' || !active) return
    const video = videoRef.current
    if (!video) return

    const reader = new BrowserQRCodeReader()
    let controls: { stop: () => void }
    try {
      controls = reader.scan(
        video,
        (result, _error, ctrl) => {
          const text = result?.getText()?.trim()
          if (!text) return
          try {
            ctrl.stop()
          } catch {
            /* ignore */
          }
          scanControlsRef.current = null
          onDecodedRef.current(text)
        },
        () => {
          scanControlsRef.current = null
        }
      )
      scanControlsRef.current = controls
    } catch (e) {
      setPhase('idle')
      onScannerError?.(e instanceof Error ? e.message : 'Démarrage du scan impossible.')
      stopAll()
      return
    }

    return () => {
      try {
        controls.stop()
      } catch {
        /* ignore */
      }
      scanControlsRef.current = null
    }
  }, [phase, active, onScannerError, stopAll])

  if (!active) return null

  if (!window.isSecureContext) {
    return (
      <p className="meta" style={{ fontSize: '0.82rem', marginBottom: 12 }}>
        La caméra n’est pas autorisée en <strong>HTTP</strong> sur cette adresse (sauf localhost). Utilisez{' '}
        <strong>HTTPS</strong>, ou la photo / le texte du QR.
      </p>
    )
  }

  return (
    <div style={{ marginBottom: 12 }}>
      {phase === 'idle' && (
        <button
          type="button"
          className="button-primary"
          style={{ width: '100%', padding: '14px 16px', marginBottom: 12 }}
          onClick={() => void requestCamera()}
        >
          Autoriser la caméra et scanner
        </button>
      )}
      {phase === 'starting' && (
        <p className="meta" style={{ marginBottom: 10, fontSize: '0.85rem' }}>
          Ouverture de la caméra… Si une boîte de dialogue apparaît, choisissez <strong>Autoriser</strong>.
        </p>
      )}
      <div
        style={{
          position: 'relative',
          borderRadius: 12,
          overflow: 'hidden',
          background: '#000',
          aspectRatio: '4 / 3',
          maxHeight: 280,
        }}
      >
        <video
          ref={videoRef}
          muted
          playsInline
          autoPlay
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            display: 'block',
            opacity: phase === 'scanning' ? 1 : 0,
            position: phase === 'scanning' ? 'relative' : 'absolute',
            pointerEvents: 'none',
          }}
        />
        {phase !== 'scanning' && (
          <div
            style={{
              position: 'absolute',
              inset: 0,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'rgba(255,255,255,0.55)',
              fontSize: '0.85rem',
              padding: 16,
              textAlign: 'center',
            }}
          >
            L’aperçu s’affiche après autorisation
          </div>
        )}
      </div>
      {phase === 'scanning' && (
        <p className="meta" style={{ marginTop: 8, marginBottom: 0, fontSize: '0.78rem' }}>
          Cadrez le QR sur l’écran du client ; la lecture est automatique.
        </p>
      )}
    </div>
  )
}
