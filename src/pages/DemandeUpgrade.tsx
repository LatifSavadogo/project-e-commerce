import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Store, Truck, ArrowLeft, Clock, MapPin, Upload, Shield } from 'lucide-react'
import { useAuth, isStaffRole, isLivreurRole } from '../contexts/AuthContext'
import { fetchTypeArticles } from '../services/referenceApi'
import { fetchMyRoleUpgrade, submitRoleUpgrade } from '../services/roleUpgradeApi'
import type { RoleUpgradeRequestDtoJson, TypeArticleDtoJson } from '../types/backend'

function statusLabel(s: string) {
  switch (s) {
    case 'PENDING':
      return 'en attente de validation'
    case 'APPROVED':
      return 'approuvée'
    case 'REJECTED':
      return 'refusée'
    default:
      return s
  }
}

export default function DemandeUpgrade() {
  const { user, isAuthenticated, refreshMe } = useAuth()
  const navigate = useNavigate()
  const [types, setTypes] = useState<TypeArticleDtoJson[]>([])
  const [demande, setDemande] = useState<RoleUpgradeRequestDtoJson | null | undefined>(undefined)
  const [roleDemande, setRoleDemande] = useState<'VENDEUR' | 'LIVREUR'>('VENDEUR')
  const [idtypeVendeur, setIdtypeVendeur] = useState<number | ''>('')
  const [typeEnginLivreur, setTypeEnginLivreur] = useState<'MOTO' | 'VEHICULE'>('MOTO')
  const [latitude, setLatitude] = useState('')
  const [longitude, setLongitude] = useState('')
  const [vendeurInternational, setVendeurInternational] = useState(false)
  const [cnib, setCnib] = useState<File | null>(null)
  const [photo, setPhoto] = useState<File | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [err, setErr] = useState<string | null>(null)

  const load = useCallback(async () => {
    try {
      const r = await fetchMyRoleUpgrade()
      setDemande(r.demande)
    } catch {
      setDemande(null)
    }
  }, [])

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/auth?mode=login')
      return
    }
    if (user && (isStaffRole(user) || isLivreurRole(user) || user.accountType === 'seller')) {
      navigate('/profile')
      return
    }
    void load()
    void fetchTypeArticles()
      .then(setTypes)
      .catch(() => setTypes([]))
  }, [isAuthenticated, user, navigate, load])

  const fillGeo = () => {
    if (!navigator.geolocation) {
      setErr('Géolocalisation non disponible dans ce navigateur.')
      return
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLatitude(String(pos.coords.latitude))
        setLongitude(String(pos.coords.longitude))
        setErr(null)
      },
      () => setErr('Impossible de lire la position. Saisissez latitude et longitude manuellement.'),
    )
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErr(null)
    if (!cnib || !photo) {
      setErr('CNIB et photo sont obligatoires.')
      return
    }
    if (roleDemande === 'VENDEUR' && idtypeVendeur === '') {
      setErr('Choisissez une catégorie vendeur.')
      return
    }
    if (roleDemande === 'VENDEUR' && (!latitude.trim() || !longitude.trim())) {
      setErr('La position GPS (latitude et longitude) est obligatoire pour une demande vendeur.')
      return
    }
    const fd = new FormData()
    fd.append('roleDemande', roleDemande)
    fd.append('cnib', cnib)
    fd.append('photo', photo)
    if (latitude.trim()) fd.append('latitude', latitude.trim().replace(',', '.'))
    if (longitude.trim()) fd.append('longitude', longitude.trim().replace(',', '.'))
    if (roleDemande === 'VENDEUR') {
      fd.append('idtypeVendeur', String(idtypeVendeur))
      fd.append('vendeurInternational', vendeurInternational ? 'true' : 'false')
    }
    if (roleDemande === 'LIVREUR') fd.append('typeEnginLivreur', typeEnginLivreur)

    setSubmitting(true)
    try {
      const saved = await submitRoleUpgrade(fd)
      setDemande(saved)
      setCnib(null)
      setPhoto(null)
      await refreshMe()
    } catch (er) {
      setErr(er instanceof Error ? er.message : 'Envoi impossible')
    } finally {
      setSubmitting(false)
    }
  }

  if (!user) return null

  return (
    <div className="upgrade-page">
      <header className="upgrade-hero">
        <h1>Devenir vendeur ou livreur</h1>
        <p className="upgrade-hero-lead">
          Votre compte reste d’abord <strong>acheteur</strong>. Envoyez un dossier complet : notre équipe valide les pièces
          avant d’activer le nouveau rôle. Les <strong>vendeurs</strong> doivent indiquer leur emplacement GPS (boutique /
          zone de vente).
        </p>
        <div className="upgrade-badge-row">
          <span className="upgrade-badge">
            <Shield size={14} aria-hidden />
            Validation admin
          </span>
          <span className="upgrade-badge">CNIB + photo</span>
          <span className="upgrade-badge">GPS vendeur</span>
        </div>
      </header>

      {demande === undefined ? (
        <div className="livreur-empty">Chargement de votre dossier…</div>
      ) : demande && demande.status === 'PENDING' ? (
        <div className="upgrade-pending">
          <div className="upgrade-pending-icon" aria-hidden>
            <Clock size={28} />
          </div>
          <h2>Demande envoyée</h2>
          <p>
            Profil demandé : <strong>{demande.roleDemande}</strong> — statut : {statusLabel(demande.status)}.
            {demande.createdAt && (
              <>
                <br />
                Envoyée le {new Date(demande.createdAt).toLocaleString('fr-FR')}.
              </>
            )}
          </p>
          <Link to="/profile" className="button-primary" style={{ display: 'inline-block', textDecoration: 'none' }}>
            Retour au profil
          </Link>
        </div>
      ) : (
        <form className="upgrade-form-card" onSubmit={(e) => void handleSubmit(e)}>
          {demande && demande.status === 'REJECTED' && (
            <div className="upgrade-alert" role="alert">
              <strong>Dernière demande refusée.</strong>
              {demande.adminMotif ? <p style={{ margin: '10px 0 0', fontSize: '0.92rem' }}>{demande.adminMotif}</p> : null}
            </div>
          )}

          <p style={{ margin: '0 0 18px', fontWeight: 600, fontSize: '0.95rem' }}>Je souhaite devenir :</p>
          <div className="upgrade-role-grid" role="group" aria-label="Type de profil">
            <button
              type="button"
              className={`upgrade-role-card${roleDemande === 'VENDEUR' ? ' upgrade-role-card--selected' : ''}`}
              onClick={() => setRoleDemande('VENDEUR')}
            >
              <div className="upgrade-role-card-icon" aria-hidden>
                <Store size={22} />
              </div>
              <h3>Vendeur</h3>
              <p>Publier des annonces dans une catégorie autorisée. GPS obligatoire.</p>
            </button>
            <button
              type="button"
              className={`upgrade-role-card${roleDemande === 'LIVREUR' ? ' upgrade-role-card--selected' : ''}`}
              onClick={() => setRoleDemande('LIVREUR')}
            >
              <div className="upgrade-role-card-icon" aria-hidden>
                <Truck size={22} />
              </div>
              <h3>Livreur</h3>
              <p>Effectuer les courses clients. Choix moto ou véhicule.</p>
            </button>
          </div>

          {roleDemande === 'VENDEUR' && (
            <>
              <div className="form-field">
                <label className="form-label" htmlFor="cat-vendeur">
                  Catégorie d’articles
                </label>
                <select
                  id="cat-vendeur"
                  required
                  value={idtypeVendeur}
                  onChange={(e) => setIdtypeVendeur(e.target.value ? Number(e.target.value) : '')}
                >
                  <option value="">Choisir…</option>
                  {types.map((t) => (
                    <option key={t.idtype} value={t.idtype}>
                      {t.libfamille ? `${t.libfamille} — ` : ''}
                      {t.libtype}
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-field" style={{ marginBottom: 4 }}>
                <label style={{ display: 'flex', alignItems: 'flex-start', gap: 10, cursor: 'pointer', fontSize: '0.95rem' }}>
                  <input
                    type="checkbox"
                    checked={vendeurInternational}
                    onChange={(e) => setVendeurInternational(e.target.checked)}
                    style={{ marginTop: 3 }}
                  />
                  <span>
                    <strong>Compte vendeur international</strong>
                    <span className="meta" style={{ display: 'block', marginTop: 4, fontSize: '0.88rem' }}>
                      Vos annonces seront visibles sur le marché international (validation admin inchangée).
                    </span>
                  </span>
                </label>
              </div>
              <div className="form-field">
                <span className="form-label">
                  <MapPin size={16} style={{ verticalAlign: '-2px', marginRight: 6 }} aria-hidden />
                  Position GPS (obligatoire)
                </span>
                <button type="button" className="livreur-btn-ghost" style={{ marginBottom: 10 }} onClick={fillGeo}>
                  Remplir avec ma position
                </button>
                <div className="upgrade-geo-row">
                  <input
                    placeholder="Latitude"
                    value={latitude}
                    onChange={(e) => setLatitude(e.target.value)}
                    autoComplete="off"
                  />
                  <input
                    placeholder="Longitude"
                    value={longitude}
                    onChange={(e) => setLongitude(e.target.value)}
                    autoComplete="off"
                  />
                </div>
              </div>
            </>
          )}

          {roleDemande === 'LIVREUR' && (
            <div className="form-field">
              <label className="form-label" htmlFor="engin">
                Type d’engin
              </label>
              <select
                id="engin"
                value={typeEnginLivreur}
                onChange={(e) => setTypeEnginLivreur(e.target.value as 'MOTO' | 'VEHICULE')}
              >
                <option value="MOTO">Moto</option>
                <option value="VEHICULE">Véhicule</option>
              </select>
              <p className="form-hint" style={{ marginTop: 8 }}>
                Le GPS n’est pas exigé pour le livreur (contrairement au vendeur).
              </p>
            </div>
          )}

          <div className="upgrade-file-field">
            <span className="upgrade-file-label">Pièce d’identité</span>
            <div className="upgrade-file-drop">
              <Upload size={22} color="var(--muted)" aria-hidden />
              <span className="meta" style={{ fontSize: '0.85rem' }}>
                CNIB, passeport… (image ou PDF)
              </span>
              <input
                id="cnib-up"
                type="file"
                accept="image/*,.pdf,.docx"
                onChange={(e) => setCnib(e.target.files?.[0] ?? null)}
              />
            </div>
            {cnib && <div className="upgrade-file-name">{cnib.name}</div>}
          </div>

          <div className="upgrade-file-field">
            <span className="upgrade-file-label">Photo portrait</span>
            <div className="upgrade-file-drop">
              <Upload size={22} color="var(--muted)" aria-hidden />
              <span className="meta" style={{ fontSize: '0.85rem' }}>
                Photo nette du visage (JPG, PNG…)
              </span>
              <input id="photo-up" type="file" accept="image/*" onChange={(e) => setPhoto(e.target.files?.[0] ?? null)} />
            </div>
            {photo && <div className="upgrade-file-name">{photo.name}</div>}
          </div>

          {err && (
            <p role="alert" style={{ color: 'var(--danger)', margin: '0 0 12px', fontSize: '0.92rem' }}>
              {err}
            </p>
          )}

          <button type="submit" className="button-primary upgrade-submit" disabled={submitting}>
            {submitting ? 'Envoi en cours…' : 'Envoyer la demande'}
          </button>
        </form>
      )}

      <Link to="/profile" className="livreur-back-link" style={{ marginTop: 32 }}>
        <ArrowLeft size={18} aria-hidden />
        Retour au profil
      </Link>
    </div>
  )
}
