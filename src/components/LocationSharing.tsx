import { useState, useEffect } from 'react'
import { Globe, Map, MapPin, RefreshCw, X } from 'lucide-react'
import type { LocationData } from '../types/chat'
import { iconLg, iconSm } from './ui/iconProps'

interface LocationSharingProps {
  onShare: (location: LocationData) => void
  onCancel: () => void
}

export default function LocationSharing({ onShare, onCancel }: LocationSharingProps) {
  const [location, setLocation] = useState<LocationData>({
    latitude: 0,
    longitude: 0,
    address: '',
    description: ''
  })
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Géolocalisation automatique
  const getCurrentLocation = () => {
    if (!navigator.geolocation) {
      setError('La géolocalisation n\'est pas supportée par votre navigateur')
      return
    }

    setIsLoading(true)
    setError(null)

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const { latitude, longitude } = position.coords

        // Reverse geocoding (simulé - dans une vraie app, utiliser Google Maps API ou Nominatim)
        const address = await reverseGeocode(latitude, longitude)

        setLocation({
          latitude,
          longitude,
          address,
          description: ''
        })
        setIsLoading(false)
      },
      (error) => {
        setError('Impossible de récupérer votre position. Vérifiez les permissions.')
        setIsLoading(false)
        console.error('Geolocation error:', error)
      },
      {
        enableHighAccuracy: true,
        timeout: 5000,
        maximumAge: 0
      }
    )
  }

  // Reverse geocoding simulé
  const reverseGeocode = async (lat: number, lng: number): Promise<string> => {
    // Dans une vraie application, utiliser une API comme :
    // - Google Maps Geocoding API
    // - Nominatim (OpenStreetMap)
    // - Mapbox
    
    // Simulation pour la démo
    return `${lat.toFixed(6)}, ${lng.toFixed(6)} - Ouagadougou, Burkina Faso`
  }

  // Auto-détection au montage
  useEffect(() => {
    getCurrentLocation()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleShare = () => {
    if (!location.address) {
      setError('Veuillez fournir une adresse')
      return
    }
    onShare(location)
  }

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      background: 'rgba(0, 0, 0, 0.8)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1000,
      padding: 20
    }}>
      <div style={{
        background: 'var(--surface)',
        borderRadius: 12,
        border: '1px solid var(--border)',
        maxWidth: 600,
        width: '100%',
        maxHeight: '90vh',
        overflow: 'auto'
      }}>
        {/* Header */}
        <div style={{
          padding: '20px',
          borderBottom: '1px solid var(--border)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <h2 style={{ margin: 0, fontSize: '1.2em', display: 'flex', alignItems: 'center', gap: 10 }}>
            <MapPin {...iconSm} aria-hidden style={{ color: 'var(--accent)' }} />
            Partager ma position
          </h2>
          <button
            type="button"
            onClick={onCancel}
            style={{
              background: 'transparent',
              border: 'none',
              cursor: 'pointer',
              color: 'var(--muted)',
              padding: 4,
              lineHeight: 0,
              borderRadius: 8,
            }}
            aria-label="Fermer"
          >
            <X size={22} strokeWidth={1.65} aria-hidden />
          </button>
        </div>

        {/* Contenu */}
        <div style={{ padding: '24px' }}>
          {isLoading && (
            <div style={{
              textAlign: 'center',
              padding: '40px',
              color: 'var(--muted)'
            }}>
              <div style={{ marginBottom: 12, color: 'var(--accent)', display: 'flex', justifyContent: 'center' }}>
                <Globe {...iconLg} size={40} aria-hidden />
              </div>
              <p>Récupération de votre position...</p>
            </div>
          )}

          {error && (
            <div style={{
              background: 'rgba(220, 38, 38, 0.1)',
              border: '1px solid rgba(220, 38, 38, 0.3)',
              borderRadius: 8,
              padding: '12px 16px',
              color: '#fca5a5',
              marginBottom: 20
            }}>
              {error}
            </div>
          )}

          {!isLoading && (
            <>
              {/* Carte simulée */}
              <div style={{
                background: 'var(--surface-elevated)',
                borderRadius: 8,
                border: '1px solid var(--border)',
                padding: 20,
                marginBottom: 20,
                textAlign: 'center'
              }}>
                <div style={{ marginBottom: 12, color: 'var(--muted)', display: 'flex', justifyContent: 'center' }}>
                  <Map size={44} strokeWidth={1.45} aria-hidden />
                </div>
                <div style={{ color: 'var(--muted)', fontSize: '0.9em' }}>
                  {location.latitude !== 0 && location.longitude !== 0 ? (
                    <>
                      <div>Latitude : <strong>{location.latitude.toFixed(6)}</strong></div>
                      <div>Longitude : <strong>{location.longitude.toFixed(6)}</strong></div>
                    </>
                  ) : (
                    <p>Position non disponible</p>
                  )}
                </div>
              </div>

              {/* Formulaire */}
              <div className="form-stack">
                <div className="form-field">
                  <label className="form-label">
                    Adresse de récupération <span style={{ color: 'var(--danger)' }}>*</span>
                  </label>
                  <input
                    type="text"
                    value={location.address}
                    onChange={(e) => setLocation({ ...location, address: e.target.value })}
                    placeholder="Ex: Secteur 15, Rue 12.45, Ouagadougou"
                  />
                </div>

                <div className="form-field">
                  <label className="form-label">Points de repère (optionnel)</label>
                  <textarea
                    value={location.description}
                    onChange={(e) => setLocation({ ...location, description: e.target.value })}
                    placeholder="Ex: Près de la pharmacie centrale, à 100m du carrefour..."
                    rows={3}
                  />
                </div>

                <button
                  type="button"
                  onClick={getCurrentLocation}
                  disabled={isLoading}
                  style={{
                    padding: '10px 16px',
                    borderRadius: 10,
                    border: '1px solid var(--border)',
                    background: 'var(--surface-elevated)',
                    color: 'var(--text)',
                    cursor: isLoading ? 'not-allowed' : 'pointer',
                    fontSize: '0.9em',
                    display: 'inline-flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: 8,
                  }}
                >
                  <RefreshCw {...iconSm} aria-hidden />
                  Actualiser ma position
                </button>
              </div>
            </>
          )}
        </div>

        {/* Footer */}
        <div style={{
          padding: '20px',
          borderTop: '1px solid var(--border)',
          display: 'flex',
          gap: 12,
          justifyContent: 'flex-end'
        }}>
          <button
            type="button"
            onClick={onCancel}
            style={{
              padding: '10px 20px',
              borderRadius: 8,
              border: '1px solid var(--border)',
              background: 'transparent',
              color: 'var(--text)',
              cursor: 'pointer',
              fontSize: '0.95em',
              fontWeight: 600
            }}
          >
            Annuler
          </button>
          <button
            type="button"
            onClick={handleShare}
            disabled={!location.address || isLoading}
            style={{
              padding: '10px 24px',
              borderRadius: 10,
              border: 'none',
              background: (!location.address || isLoading) ? 'var(--surface-elevated)' : 'var(--accent)',
              color: (!location.address || isLoading) ? 'var(--muted)' : 'white',
              cursor: (!location.address || isLoading) ? 'not-allowed' : 'pointer',
              fontSize: '0.95em',
              fontWeight: 600,
              display: 'inline-flex',
              alignItems: 'center',
              gap: 8,
            }}
          >
            <MapPin {...iconSm} aria-hidden />
            Partager la position
          </button>
        </div>
      </div>
    </div>
  )
}
