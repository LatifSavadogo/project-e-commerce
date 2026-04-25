import { useAuth, isStaffRole, isLivreurRole } from '../contexts/AuthContext'
import { useNavigate, Link } from 'react-router-dom'
import { useEffect, useState, useCallback, type ReactNode } from 'react'
import {
  UserRound,
  Mail,
  MapPin,
  Shield,
  Briefcase,
  Calendar,
  ShoppingBag,
  Store,
  Home,
  Truck,
  LocateFixed,
} from 'lucide-react'
import { fetchMyPurchases, fetchMySales, downloadPaymentReceipt, type PaymentResultDto } from '../services/paymentApi'
import { postTransactionSellerRating } from '../services/sellerRatingApi'
import { dateFromDto } from '../utils/dateFromDto'
import { iconSm } from '../components/ui/iconProps'
import LivraisonBuyerModal from '../components/LivraisonBuyerModal'
import DomicileLocationMapModal from '../components/DomicileLocationMapModal'

const MOYEN_LABEL: Record<string, string> = {
  PAYDUNYA: 'PayDunya',
  ORANGE_MONEY: 'Orange Money',
  MOOV_MONEY: 'Moov Money',
  VIREMENT: 'Virement',
  ESPECES: 'Espèces',
}

/** QR + suivi Maps uniquement tant que la livraison n’est pas clôturée. */
function livraisonQrSuiviEncoreActifs(statut: string | null | undefined): boolean {
  if (statut == null || statut === '') return true
  return statut === 'EN_ATTENTE' || statut === 'EN_COURS'
}

function livraisonStatutAcheteurLibelle(statut: string | null | undefined): string | null {
  if (statut === 'LIVREE') return 'Livraison effectuée'
  if (statut === 'ANNULEE') return 'Livraison annulée'
  return null
}

function AccountField({
  label,
  value,
  icon,
}: {
  label: string
  value: string
  icon: ReactNode
}) {
  return (
    <div className="account-field">
      <div className="account-field-label">
        {icon}
        {label}
      </div>
      <div className="account-field-value">{value}</div>
    </div>
  )
}

export default function Profile() {
  const { user, isAuthenticated, refreshMe } = useAuth()
  const navigate = useNavigate()
  const [purchases, setPurchases] = useState<PaymentResultDto[]>([])
  const [sales, setSales] = useState<PaymentResultDto[]>([])
  const [loadErr, setLoadErr] = useState<string | null>(null)
  const [buyerModal, setBuyerModal] = useState<{ mode: 'qr' | 'suivi'; transactionId: number } | null>(null)
  const [mapModalOpen, setMapModalOpen] = useState(false)
  const [gpsOk, setGpsOk] = useState<string | null>(null)
  const [ratingStars, setRatingStars] = useState<Record<number, string>>({})
  const [ratingBusy, setRatingBusy] = useState<number | null>(null)

  const loadPayments = useCallback(async () => {
    if (!user) return
    setLoadErr(null)
    try {
      const [mine, s] = await Promise.all([fetchMyPurchases(), fetchMySales()])
      setPurchases(mine)
      if (user.accountType === 'seller') setSales(s)
      else setSales([])
    } catch {
      setLoadErr('Impossible de charger l’historique des paiements.')
    }
  }, [user])

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/auth?mode=login')
    }
  }, [isAuthenticated, navigate])

  useEffect(() => {
    void loadPayments()
  }, [loadPayments])

  const handleReceipt = async (id: number) => {
    try {
      const blob = await downloadPaymentReceipt(id)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `recu-ecomarket-${id}.pdf`
      a.click()
      URL.revokeObjectURL(url)
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Téléchargement impossible')
    }
  }

  if (!user) return null

  const initial = (user.prenoms?.trim()?.[0] || user.nom?.trim()?.[0] || '?').toUpperCase()
  const roleLabel =
    user.librole ||
    (user.accountType === 'seller' ? 'VENDEUR' : isLivreurRole(user) ? 'LIVREUR' : 'ACHETEUR')

  return (
    <div className="account-page-wrap">
      <div className="account-page-inner">
        <div className="account-page-card">
          <div className="account-hero">
            <div className="account-hero-avatar" aria-hidden>
              {initial}
            </div>
            <div className="account-hero-text">
              <h1>
                {user.prenoms} {user.nom}
              </h1>
              <p>{user.email}</p>
            </div>
          </div>

          <div className="account-field-grid">
            <AccountField label="Nom" value={user.nom} icon={<UserRound {...iconSm} aria-hidden />} />
            <AccountField label="Prénoms" value={user.prenoms} icon={<UserRound {...iconSm} aria-hidden />} />
            <AccountField label="Email" value={user.email} icon={<Mail {...iconSm} aria-hidden />} />
            <AccountField label="Rôle" value={roleLabel} icon={<Shield {...iconSm} aria-hidden />} />
            <AccountField
              label="Type de compte"
              value={
                user.accountType === 'seller' ? 'Vendeur' : isLivreurRole(user) ? 'Livreur' : 'Acheteur'
              }
              icon={<Briefcase {...iconSm} aria-hidden />}
            />
            {isLivreurRole(user) && user.typeEnginLivreur && (
              <AccountField
                label="Engin"
                value={user.typeEnginLivreur}
                icon={<Truck {...iconSm} aria-hidden />}
              />
            )}
            {(user.libtypeVendeur || user.productType) && (
              <AccountField
                label="Catégorie vendeur"
                value={user.libtypeVendeur || user.productType || '—'}
                icon={<Store {...iconSm} aria-hidden />}
              />
            )}
            {user.accountType === 'seller' && (
              <AccountField
                label="Marché international"
                value={
                  user.vendeurInternational
                    ? 'Oui — vos annonces figurent sur le catalogue international'
                    : 'Non — catalogue général uniquement'
                }
                icon={<Shield {...iconSm} aria-hidden />}
              />
            )}
            {user.accountType === 'seller' && (
              <AccountField
                label="Vendeur certifié"
                value={
                  user.vendeurCertifieActif && user.vendeurCertifieJusqua
                    ? `Oui — jusqu’au ${new Date(user.vendeurCertifieJusqua).toLocaleString('fr-FR', {
                        dateStyle: 'long',
                        timeStyle: 'short',
                      })}`
                    : 'Non — souscrire depuis le dashboard (certification PayDunya)'
                }
                icon={<Shield {...iconSm} aria-hidden />}
              />
            )}
            {user.country && (
              <AccountField label="Pays" value={user.country} icon={<MapPin {...iconSm} aria-hidden />} />
            )}
            {user.city && (
              <AccountField label="Ville" value={user.city} icon={<MapPin {...iconSm} aria-hidden />} />
            )}
            <AccountField
              label="Membre depuis"
              value={new Date(user.registeredAt).toLocaleDateString('fr-FR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
              icon={<Calendar {...iconSm} aria-hidden />}
            />
            {!isStaffRole(user) && user.latitude != null && user.longitude != null && (
              <AccountField
                label="GPS livraison / profil"
                value={`${user.latitude}, ${user.longitude}`}
                icon={<MapPin {...iconSm} aria-hidden />}
              />
            )}
          </div>

          {!isStaffRole(user) && (
            <section className="account-section" style={{ marginTop: 20 }} aria-labelledby="delivery-gps-heading">
              <h2
                id="delivery-gps-heading"
                style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: '1.05rem', marginBottom: 8 }}
              >
                <MapPin {...iconSm} aria-hidden />
                Lieu de livraison (obligatoire pour payer)
              </h2>
              <p className="meta" style={{ marginTop: 0 }}>
                Ouvrez la carte, cliquez sur votre domicile (point de remise habituel), puis validez. Les vendeurs ont
                déjà renseigné leur position à la demande de statut vendeur. Au paiement, vous pourrez encore choisir un
                autre lieu sur la carte sans modifier ce domicile.
              </p>
              {user.latitude != null && user.longitude != null && (
                <p className="form-hint" style={{ marginTop: 12, marginBottom: 0 }}>
                  Domicile enregistré :{' '}
                  <strong>
                    {Number(user.latitude).toFixed(6)}, {Number(user.longitude).toFixed(6)}
                  </strong>{' '}
                  <a
                    href={`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(String(user.latitude))}%2C${encodeURIComponent(String(user.longitude))}`}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    Google Maps
                  </a>
                </p>
              )}
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, marginTop: 14, alignItems: 'center' }}>
                <button
                  type="button"
                  className="button-primary"
                  style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}
                  onClick={() => {
                    setGpsOk(null)
                    setMapModalOpen(true)
                  }}
                >
                  <LocateFixed size={18} aria-hidden />
                  {user.latitude != null && user.longitude != null ? 'Modifier sur la carte' : 'Choisir sur la carte'}
                </button>
              </div>
              {gpsOk && (
                <p className="meta" style={{ marginTop: 10, marginBottom: 0 }}>
                  {gpsOk}
                </p>
              )}
            </section>
          )}

          {user.accountType === 'buyer' && !isStaffRole(user) && (
            <p className="meta" style={{ marginTop: 16, marginBottom: 0 }}>
              <Link to="/demande-upgrade" style={{ color: 'var(--link)', fontWeight: 600 }}>
                Demander le statut vendeur ou livreur (validation admin)
              </Link>
            </p>
          )}

          <p className="meta" style={{ marginTop: 20, marginBottom: 0 }}>
            <Link to="/help/complaint" style={{ color: 'var(--link)', fontWeight: 600 }}>
              Signaler un problème (plainte)
            </Link>
          </p>

          <section className="account-section" aria-labelledby="purchases-heading">
            <h2 id="purchases-heading">
              <ShoppingBag {...iconSm} aria-hidden />
              Mes achats
            </h2>
            {loadErr && (
              <p style={{ color: 'var(--danger, #e05252)', marginBottom: 12 }} role="alert">
                {loadErr}
              </p>
            )}
            {purchases.length === 0 && !loadErr ? (
              <p className="meta account-section-intro" style={{ marginBottom: 0 }}>
                Aucun paiement enregistré pour le moment.
              </p>
            ) : (
              purchases.map((p) => (
                <div key={p.idtransaction} className="account-payment-card">
                  <div className="account-payment-main">
                    <strong>{p.articleLibelle || `Article #${p.idArticle}`}</strong>
                    <div className="account-payment-meta">
                      {p.quantite} × {p.prixUnitaire} FCFA = {p.montantTotal} FCFA (frais {p.frais}) —{' '}
                      {MOYEN_LABEL[p.moyenPaiement] || p.moyenPaiement}
                    </div>
                    <div className="account-payment-meta" style={{ marginTop: 4, fontSize: '0.78rem' }}>
                      {p.datecreation ? new Date(dateFromDto(p.datecreation)).toLocaleString('fr-FR') : ''}
                    </div>
                    {p.idLivraison != null && (
                      <div
                        style={{
                          marginTop: 10,
                          display: 'flex',
                          flexWrap: 'wrap',
                          gap: '8px 14px',
                          alignItems: 'center',
                          fontSize: '0.85rem',
                        }}
                      >
                        <span className="meta">
                          Livraison #{p.idLivraison}
                          {p.livraisonStatut ? ` · ${p.livraisonStatut}` : ''}
                        </span>
                        {livraisonQrSuiviEncoreActifs(p.livraisonStatut) ? (
                          <>
                            <button
                              type="button"
                              className="link-button"
                              style={{ fontSize: '0.85rem' }}
                              onClick={() => setBuyerModal({ mode: 'suivi', transactionId: p.idtransaction })}
                            >
                              Suivi &amp; Maps
                            </button>
                            <button
                              type="button"
                              className="link-button"
                              style={{ fontSize: '0.85rem' }}
                              onClick={() => setBuyerModal({ mode: 'qr', transactionId: p.idtransaction })}
                            >
                              QR livraison
                            </button>
                          </>
                        ) : (
                          livraisonStatutAcheteurLibelle(p.livraisonStatut) && (
                            <span style={{ color: 'var(--muted)', fontSize: '0.82rem' }}>
                              {livraisonStatutAcheteurLibelle(p.livraisonStatut)}
                            </span>
                          )
                        )}
                      </div>
                    )}
                  </div>
                  <button type="button" className="link-button" onClick={() => void handleReceipt(p.idtransaction)}>
                    Reçu PDF
                  </button>
                  {p.livraisonStatut === 'LIVREE' &&
                    typeof sessionStorage !== 'undefined' &&
                    !sessionStorage.getItem(`seller_rating_done_${p.idtransaction}`) && (
                      <div
                        style={{
                          marginTop: 12,
                          paddingTop: 12,
                          borderTop: '1px solid var(--border)',
                          fontSize: '0.88rem',
                        }}
                      >
                        <div style={{ marginBottom: 6, fontWeight: 600 }}>Noter le vendeur</div>
                        <label style={{ display: 'block', marginBottom: 6 }}>
                          Étoiles (1 à 5){' '}
                          <input
                            type="number"
                            min={1}
                            max={5}
                            value={ratingStars[p.idtransaction] ?? '5'}
                            onChange={(e) =>
                              setRatingStars((prev) => ({ ...prev, [p.idtransaction]: e.target.value }))
                            }
                            style={{ width: 64, marginLeft: 8 }}
                          />
                        </label>
                        <button
                          type="button"
                          className="link-button"
                          disabled={ratingBusy === p.idtransaction}
                          onClick={() => {
                            void (async () => {
                              const raw = (ratingStars[p.idtransaction] ?? '5').trim()
                              const stars = parseInt(raw, 10)
                              if (!stars || stars < 1 || stars > 5) {
                                alert('Indiquez une note entre 1 et 5.')
                                return
                              }
                              setRatingBusy(p.idtransaction)
                              try {
                                await postTransactionSellerRating(p.idtransaction, { stars })
                                sessionStorage.setItem(`seller_rating_done_${p.idtransaction}`, '1')
                                await loadPayments()
                              } catch (e) {
                                alert(e instanceof Error ? e.message : 'Envoi impossible')
                              } finally {
                                setRatingBusy(null)
                              }
                            })()
                          }}
                        >
                          {ratingBusy === p.idtransaction ? 'Envoi…' : 'Envoyer la note'}
                        </button>
                      </div>
                    )}
                </div>
              ))
            )}
          </section>

          {user.accountType === 'seller' && !isStaffRole(user) && (
            <section className="account-section" aria-labelledby="sales-heading">
              <h2 id="sales-heading">
                <Store {...iconSm} aria-hidden />
                Mes ventes
              </h2>
              {sales.length === 0 ? (
                <p className="meta account-section-intro" style={{ marginBottom: 0 }}>
                  Aucune vente enregistrée.
                </p>
              ) : (
                sales.map((p) => (
                  <div key={p.idtransaction} className="account-payment-card">
                    <div className="account-payment-main">
                      <strong>{p.articleLibelle || `Article #${p.idArticle}`}</strong>
                      <div className="account-payment-meta">
                        {p.quantite} × {p.prixUnitaire} FCFA = {p.montantTotal} FCFA —{' '}
                        {MOYEN_LABEL[p.moyenPaiement] || p.moyenPaiement}
                      </div>
                      {p.vendorPickupCode && (
                        <div
                          className="account-payment-meta"
                          style={{
                            marginTop: 10,
                            padding: '8px 10px',
                            background: 'var(--input-bg)',
                            borderRadius: 8,
                            border: '1px solid var(--border)',
                          }}
                        >
                          <span style={{ color: 'var(--muted)', fontSize: '0.78rem' }}>Code retrait livreur</span>
                          <div style={{ fontWeight: 700, letterSpacing: '0.1em', fontFamily: 'monospace' }}>
                            {p.vendorPickupCode}
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                ))
              )}
            </section>
          )}

          <div className="account-footer-actions">
            <button
              type="button"
              className="button-primary"
              onClick={() => navigate('/')}
              style={{ minWidth: 160, display: 'inline-flex', alignItems: 'center', gap: 8 }}
            >
              <Home {...iconSm} aria-hidden />
              Accueil
            </button>
            {user.accountType === 'seller' && !isStaffRole(user) && (
              <button
                type="button"
                className="link-button"
                onClick={() => navigate('/vendor')}
                style={{ minWidth: 180, display: 'inline-flex', alignItems: 'center', gap: 8 }}
              >
                <Store {...iconSm} aria-hidden />
                Dashboard vendeur
              </button>
            )}
            {isLivreurRole(user) && !isStaffRole(user) && (
              <button
                type="button"
                className="link-button"
                onClick={() => navigate('/livreur')}
                style={{ minWidth: 180, display: 'inline-flex', alignItems: 'center', gap: 8 }}
              >
                <Truck {...iconSm} aria-hidden />
                Espace livreur
              </button>
            )}
          </div>
        </div>
      </div>

      <LivraisonBuyerModal
        mode={buyerModal?.mode ?? null}
        transactionId={buyerModal?.transactionId ?? null}
        onClose={() => {
          setBuyerModal(null)
          void loadPayments()
        }}
      />
      <DomicileLocationMapModal
        open={mapModalOpen}
        onClose={() => setMapModalOpen(false)}
        savedLat={user.latitude}
        savedLng={user.longitude}
        onSaved={async () => {
          setGpsOk('Domicile enregistré.')
          await refreshMe()
        }}
      />
    </div>
  )
}
