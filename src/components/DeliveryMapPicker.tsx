import { useEffect, useRef } from 'react'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png'
import markerIcon from 'leaflet/dist/images/marker-icon.png'
import markerShadow from 'leaflet/dist/images/marker-shadow.png'

const DefaultIcon = L.icon({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
})
L.Marker.prototype.options.icon = DefaultIcon

type Props = {
  centerLat: number
  centerLng: number
  markerLat: number | null
  markerLng: number | null
  onPick: (lat: number, lng: number) => void
  height?: number
  /** Texte sous la carte (ex. domicile profil vs point pour une commande). */
  footerHint?: string
}

/**
 * Carte OpenStreetMap : clic pour placer le point et récupérer les coordonnées WGS84 (même référentiel que Google Maps).
 */
export default function DeliveryMapPicker({
  centerLat,
  centerLng,
  markerLat,
  markerLng,
  onPick,
  height = 240,
  footerHint = 'Zoomez, déplacez la carte puis cliquez sur le lieu. Les coordonnées sont au format WGS84 (comme Google Maps).',
}: Props) {
  const mapElRef = useRef<HTMLDivElement>(null)
  const mapRef = useRef<L.Map | null>(null)
  const markerRef = useRef<L.Marker | null>(null)
  const onPickRef = useRef(onPick)
  onPickRef.current = onPick

  useEffect(() => {
    const el = mapElRef.current
    if (!el || mapRef.current) return

    const map = L.map(el, {
      center: [centerLat, centerLng],
      zoom: 13,
    })
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap',
      maxZoom: 19,
    }).addTo(map)
    mapRef.current = map

    const placeMarker = (lat: number, lng: number) => {
      if (markerRef.current) {
        markerRef.current.setLatLng([lat, lng])
      } else {
        markerRef.current = L.marker([lat, lng]).addTo(map)
      }
      map.setView([lat, lng], Math.max(map.getZoom(), 15))
    }

    map.on('click', (e: L.LeafletMouseEvent) => {
      const { lat, lng } = e.latlng
      placeMarker(lat, lng)
      onPickRef.current(lat, lng)
    })

    const t = window.setTimeout(() => map.invalidateSize(), 250)

    return () => {
      window.clearTimeout(t)
      map.remove()
      mapRef.current = null
      markerRef.current = null
    }
  }, [centerLat, centerLng])

  useEffect(() => {
    const map = mapRef.current
    if (!map) return
    if (markerLat != null && markerLng != null) {
      if (markerRef.current) {
        markerRef.current.setLatLng([markerLat, markerLng])
      } else {
        markerRef.current = L.marker([markerLat, markerLng]).addTo(map)
      }
      map.setView([markerLat, markerLng], Math.max(map.getZoom(), 15))
    } else if (markerRef.current) {
      map.removeLayer(markerRef.current)
      markerRef.current = null
    }
  }, [markerLat, markerLng])

  return (
    <div>
      <div
        ref={mapElRef}
        style={{
          height,
          width: '100%',
          borderRadius: 12,
          border: '1px solid var(--border)',
          overflow: 'hidden',
        }}
      />
      <p className="form-hint" style={{ marginTop: 8, marginBottom: 0 }}>
        {footerHint}
      </p>
    </div>
  )
}
