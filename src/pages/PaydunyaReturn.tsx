import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { completePaydunyaInvoice, type PaydunyaCompleteResponse } from '../services/paydunyaApi'
import { ApiError } from '../services/apiClient'

/**
 * Page de retour après paiement PayDunya : l’URL contient ?token=… (ajouté par PayDunya).
 * Appelle l’API pour enregistrer la commande ou la certification.
 */
export default function PaydunyaReturn() {
  const [status, setStatus] = useState<'loading' | 'ok' | 'err'>('loading')
  const [detail, setDetail] = useState<PaydunyaCompleteResponse | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const token = params.get('token')?.trim()
    if (!token) {
      setStatus('err')
      setMessage('Lien de retour invalide (paramètre token manquant).')
      return
    }
    let cancelled = false
    ;(async () => {
      try {
        const res = await completePaydunyaInvoice(token)
        if (cancelled) return
        setDetail(res)
        if (res.outcome === 'ORDER_SETTLED' || res.outcome === 'CERT_APPLIED') {
          setStatus('ok')
        } else {
          setStatus('err')
          setMessage(res.message || 'Paiement non confirmé.')
        }
      } catch (e) {
        if (cancelled) return
        setStatus('err')
        setMessage(e instanceof ApiError ? e.message : e instanceof Error ? e.message : 'Erreur')
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 48, maxWidth: 520 }}>
      <h1 style={{ marginTop: 0 }}>Paiement PayDunya</h1>
      {status === 'loading' && <p>Vérification du paiement…</p>}
      {status === 'ok' && detail?.outcome === 'ORDER_SETTLED' && detail.payment && (
        <div>
          <p style={{ color: 'var(--muted)' }}>Commande enregistrée.</p>
          <p>
            Réf. transaction interne : <strong>#{detail.payment.idtransaction}</strong>
          </p>
          <Link to="/profile" style={{ fontWeight: 600 }}>
            Voir mes achats
          </Link>
        </div>
      )}
      {status === 'ok' && detail?.outcome === 'CERT_APPLIED' && (
        <div>
          <p style={{ color: 'var(--muted)' }}>Abonnement vendeur certifié mis à jour.</p>
          {detail.certification?.certifieJusqua && (
            <p>
              Valide jusqu’au{' '}
              <strong>
                {new Date(detail.certification.certifieJusqua).toLocaleString('fr-FR', {
                  dateStyle: 'long',
                  timeStyle: 'short',
                })}
              </strong>
              .
            </p>
          )}
          <Link to="/vendor" style={{ fontWeight: 600 }}>
            Dashboard vendeur
          </Link>
        </div>
      )}
      {status === 'err' && (
        <div>
          <p style={{ color: 'var(--danger, #e05252)' }} role="alert">
            {message || 'Une erreur est survenue.'}
          </p>
          <p className="meta">
            Si vous avez bien payé sur PayDunya, attendez quelques instants (notification IPN) ou contactez le support.
          </p>
          <Link to="/profile">Mon compte</Link>
        </div>
      )}
    </div>
  )
}
