import { useState, useEffect, useCallback, useMemo } from 'react'
import { useSearchParams, Link, useNavigate, Navigate } from 'react-router-dom'
import { Check, LogIn } from 'lucide-react'
import { iconLg } from '../components/ui/iconProps'
import { useAuth, isStaffRole, isLivreurRole } from '../contexts/AuthContext'
import Toast from '../components/Toast'
import { API_BASE } from '../config/api'
import { fetchPays } from '../services/referenceApi'
import type { PaysDtoJson } from '../types/backend'
import { getCitiesForPaysLibelle } from '../data/citiesByCountry'

function RequiredAsterisk() {
  return (
    <span aria-hidden="true" style={{ color: '#dc2626', marginLeft: 4, fontWeight: 700 }}>
      *
    </span>
  )
}

export default function Auth() {
  const [searchParams] = useSearchParams()
  const { user, authLoading } = useAuth()
  const [mode, setMode] = useState<'login' | 'register'>('login')

  useEffect(() => {
    const urlMode = searchParams.get('mode')
    if (urlMode === 'register' || urlMode === 'login') {
      setMode(urlMode)
    }
  }, [searchParams])

  if (authLoading) {
    return (
      <div className="container" style={{ padding: '48px 20px', textAlign: 'center', color: 'var(--muted)' }}>
        Chargement…
      </div>
    )
  }

  if (user != null) {
    const dest = isStaffRole(user) ? '/admin' : isLivreurRole(user) ? '/livreur' : '/'
    return <Navigate to={dest} replace />
  }

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'flex-start',
        minHeight: 'calc(100vh - 80px)',
        padding: '40px 20px',
        background: 'var(--page-bg)',
      }}
    >
      <div
        className="card"
        style={{
          padding: '32px',
          width: '100%',
          maxWidth: mode === 'register' ? '600px' : '480px',
          boxSizing: 'border-box',
          marginTop: 40,
        }}
      >
        {mode === 'login' ? <LoginForm /> : <RegisterForm />}
      </div>
    </div>
  )
}

function LoginForm() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [loginData, setLoginData] = useState({ email: '', password: '' })
  const [showToast, setShowToast] = useState(false)
  const [toastMessage, setToastMessage] = useState('')
  const [toastType, setToastType] = useState<'success' | 'error' | 'welcome'>('success')
  const [loginPending, setLoginPending] = useState(false)

  const handleLoginChange = (field: string, value: string) => {
    setLoginData({ ...loginData, [field]: value })
  }

  const isLoginValid = () => {
    return loginData.email && loginData.password && /.+@.+\..+/.test(loginData.email)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoginPending(true)
    try {
      const result = await login(loginData.email, loginData.password)
      if (result.ok) {
        const firstName = loginData.email.split('@')[0]
        setToastMessage(`Bienvenue ${firstName} ! Connexion réussie.`)
        setToastType('welcome')
        setShowToast(true)
        const target = result.staff ? '/admin' : result.livreur ? '/livreur' : '/'
        setTimeout(() => navigate(target), 1500)
      } else {
        setToastMessage('Email ou mot de passe incorrect')
        setToastType('error')
        setShowToast(true)
      }
    } finally {
      setLoginPending(false)
    }
  }

  return (
    <>
      {showToast && (
        <Toast
          message={toastMessage}
          type={toastType}
          onClose={() => setShowToast(false)}
          duration={toastType === 'welcome' ? 3000 : 5000}
        />
      )}

      <h2 className="form-card-title" style={{ marginBottom: 20 }}>
        <LogIn {...iconLg} aria-hidden />
        Connexion
      </h2>
      <form onSubmit={handleSubmit} className="form-stack">
        <div className="form-field">
          <label className="form-label" htmlFor="login-email">
            Email
          </label>
          <input
            id="login-email"
            placeholder="votre@email.com"
            type="email"
            required
            value={loginData.email}
            onChange={(e) => handleLoginChange('email', e.target.value)}
          />
        </div>
        <div className="form-field">
          <label className="form-label" htmlFor="login-password">
            Mot de passe
          </label>
          <input
            id="login-password"
            placeholder="Votre mot de passe"
            type="password"
            required
            value={loginData.password}
            onChange={(e) => handleLoginChange('password', e.target.value)}
          />
        </div>
        <div style={{ textAlign: 'right', marginTop: -4 }}>
          <Link to="/forgot-password" style={{ color: 'var(--link)', fontSize: '0.9em' }}>
            Mot de passe oublié ?
          </Link>
        </div>
        <button
          type="submit"
          style={{
            marginTop: 8,
            opacity: isLoginValid() && !loginPending ? 1 : 0.5,
            cursor: isLoginValid() && !loginPending ? 'pointer' : 'not-allowed',
          }}
          disabled={!isLoginValid() || loginPending}
        >
          {loginPending ? 'Connexion…' : 'Se connecter'}
        </button>
        <p style={{ textAlign: 'center', fontSize: '0.9em', marginTop: 12 }}>
          Pas encore de compte ?{' '}
          <Link to="/auth?mode=register" style={{ color: 'var(--link)', textDecoration: 'underline' }}>
            S&apos;inscrire
          </Link>
        </p>
      </form>
    </>
  )
}

function RegisterForm() {
  const navigate = useNavigate()
  const { register, login, refreshMe } = useAuth()
  const [idCardFile, setIdCardFile] = useState<File | null>(null)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [paysList, setPaysList] = useState<PaysDtoJson[]>([])
  const [refsLoading, setRefsLoading] = useState(true)
  const [refsError, setRefsError] = useState<string | null>(null)
  const [selectedPaysId, setSelectedPaysId] = useState<number | null>(null)
  const [registerSubmitting, setRegisterSubmitting] = useState(false)

  const isValidEmail = (email: string) => {
    const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
    return emailRegex.test(email.trim())
  }

  const isStrongPassword = (password: string) => {
    const pwdRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-={}[\]|;:'",.<>/?]).{8,}$/
    return pwdRegex.test(password)
  }
  const [citySelect, setCitySelect] = useState('')
  const [cityOther, setCityOther] = useState('')
  const [cityFreeText, setCityFreeText] = useState('')

  const selectedPaysLibelle = useMemo(
    () => paysList.find((p) => p.idpays === selectedPaysId)?.libpays,
    [paysList, selectedPaysId],
  )
  const cityOptions = useMemo(() => getCitiesForPaysLibelle(selectedPaysLibelle), [selectedPaysLibelle])
  const [registrationStep, setRegistrationStep] = useState<number>(1)
  const [showToast, setShowToast] = useState(false)
  const [toastMessage, setToastMessage] = useState('')
  const [toastType, setToastType] = useState<'success' | 'error'>('success')
  const [formData, setFormData] = useState({
    nom: '',
    prenoms: '',
    email: '',
    password: '',
    confirmPassword: '',
  })

  const loadPays = useCallback(async () => {
    setRefsLoading(true)
    setRefsError(null)
    try {
      const pays = await fetchPays()
      setPaysList(pays)
    } catch {
      setRefsError(
        `Impossible de joindre l’API (${API_BASE}). Démarrez le backend sur le port 8080 ou définissez VITE_API_BASE_URL dans .env`,
      )
      setPaysList([])
    } finally {
      setRefsLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadPays()
  }, [loadPays])

  const steps = [
    { id: 1, label: 'Informations personnelles' },
    { id: 2, label: 'Identifiants' },
    { id: 3, label: 'Localisation' },
  ]

  const getProgressPercentage = () => (registrationStep / 3) * 100

  const handleInputChange = (field: string, value: string) => {
    setFormData({ ...formData, [field]: value })
  }

  const handleNextStep = async (e: React.FormEvent) => {
    e.preventDefault()

    if (registrationStep === 1) {
      if (!formData.nom.trim() || !formData.prenoms.trim()) {
        alert('Veuillez renseigner votre nom et prénoms')
        return
      }
      setRegistrationStep(2)
      return
    }

    if (registrationStep === 2) {
      if (!formData.email.trim()) {
        alert('Veuillez renseigner votre email')
        return
      }
      if (!isValidEmail(formData.email)) {
        alert('Veuillez saisir une adresse email valide (ex: nom.prenom@domaine.com)')
        return
      }
      if (!formData.password || !formData.confirmPassword) {
        alert('Veuillez renseigner vos mots de passe')
        return
      }
      if (!isStrongPassword(formData.password)) {
        alert(
          'Votre mot de passe doit contenir au minimum 8 caractères, avec au moins une majuscule, une minuscule, un chiffre et un caractère spécial.',
        )
        return
      }
      if (formData.password !== formData.confirmPassword) {
        alert('Les mots de passe ne correspondent pas')
        return
      }
      setRegistrationStep(3)
      return
    }

    if (registrationStep === 3) {
      if (selectedPaysId == null) {
        alert('Veuillez sélectionner votre pays dans la liste officielle')
        return
      }
      let villeFinal = ''
      if (cityOptions.length > 0) {
        if (!citySelect) {
          alert('Veuillez choisir votre ville dans la liste')
          return
        }
        if (citySelect === '__OTHER__') {
          if (!cityOther.trim()) {
            alert('Veuillez préciser le nom de votre ville')
            return
          }
          villeFinal = cityOther.trim()
        } else {
          villeFinal = citySelect
        }
      } else {
        if (!cityFreeText.trim()) {
          alert('Veuillez saisir votre ville')
          return
        }
        villeFinal = cityFreeText.trim()
      }

      setRegisterSubmitting(true)
      try {
        const reg = await register({
          nom: formData.nom,
          prenom: formData.prenoms,
          email: formData.email,
          password: formData.password,
          idpays: selectedPaysId,
          ville: villeFinal,
          cnib: idCardFile ?? undefined,
        })
        if (!reg.ok) {
          alert(reg.error)
          return
        }
        const logged = await login(formData.email, formData.password)
        if (!logged.ok) {
          alert('Compte créé. Connectez-vous avec votre email et mot de passe.')
          navigate('/auth?mode=login')
          return
        }
        await refreshMe()
        setToastMessage('Inscription réussie ! Vous êtes enregistré comme acheteur. Vous pourrez demander le statut vendeur ou livreur depuis votre profil.')
        setToastType('success')
        setShowToast(true)
        setTimeout(() => navigate('/'), 2000)
      } finally {
        setRegisterSubmitting(false)
      }
    }
  }

  return (
    <>
      {showToast && <Toast message={toastMessage} type={toastType} onClose={() => setShowToast(false)} />}

      <div style={{ marginBottom: 24 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
          {steps.map((s) => (
            <div
              key={s.id}
              style={{
                fontSize: '0.8em',
                color: registrationStep >= s.id ? '#2a9d8f' : 'var(--muted)',
                fontWeight: registrationStep === s.id ? 600 : 400,
              }}
            >
              {s.label}
            </div>
          ))}
        </div>
        <div
          style={{
            height: 6,
            background: 'var(--border-subtle)',
            borderRadius: 10,
            overflow: 'hidden',
          }}
        >
          <div
            style={{
              height: '100%',
              background: 'linear-gradient(90deg, #2a9d8f, #25b09b)',
              width: `${getProgressPercentage()}%`,
              transition: 'width 0.3s ease',
            }}
          />
        </div>
      </div>

      <p
        className="meta"
        style={{ textAlign: 'center', marginBottom: 16, padding: '10px 12px', background: 'var(--surface-elevated)', borderRadius: 8 }}
      >
        Nouveau compte = profil <strong>acheteur</strong>. Pièce d’identité et photo seront demandées si vous sollicitez le statut{' '}
        <strong>vendeur</strong> ou <strong>livreur</strong> (validation admin).
      </p>

      <h2 style={{ marginBottom: 20, textAlign: 'center' }}>{steps[registrationStep - 1].label}</h2>

      <form onSubmit={handleNextStep} className="grid" style={{ gridTemplateColumns: '1fr', gap: 16 }}>
        {registrationStep === 1 && (
          <>
            <div>
              <label style={{ display: 'block', marginBottom: 4, fontSize: '0.9em' }}>
                Nom
                <RequiredAsterisk />
              </label>
              <input
                placeholder="Nom"
                type="text"
                required
                value={formData.nom}
                onChange={(e) => handleInputChange('nom', e.target.value)}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 4, fontSize: '0.9em' }}>
                Prénoms
                <RequiredAsterisk />
              </label>
              <input
                placeholder="Prénoms"
                type="text"
                required
                value={formData.prenoms}
                onChange={(e) => handleInputChange('prenoms', e.target.value)}
              />
            </div>
          </>
        )}

        {registrationStep === 2 && (
          <>
            <div>
              <label style={{ display: 'block', marginBottom: 4, fontSize: '0.9em' }}>
                Email
                <RequiredAsterisk />
              </label>
              <input
                placeholder="votre@email.com"
                type="email"
                required
                value={formData.email}
                onChange={(e) => handleInputChange('email', e.target.value)}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 4, fontSize: '0.9em' }}>
                Mot de passe
                <RequiredAsterisk />
              </label>
              <div style={{ position: 'relative' }}>
                <input
                  placeholder="Choisissez un mot de passe"
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={formData.password}
                  onChange={(e) => handleInputChange('password', e.target.value)}
                  style={{ paddingRight: 80 }}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  style={{
                    position: 'absolute',
                    right: 8,
                    top: '50%',
                    transform: 'translateY(-50%)',
                    background: 'transparent',
                    border: 'none',
                    color: 'var(--muted)',
                    fontSize: '0.8em',
                    cursor: 'pointer',
                  }}
                >
                  {showPassword ? 'Masquer' : 'Afficher'}
                </button>
              </div>
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 4, fontSize: '0.9em' }}>
                Confirmer le mot de passe
                <RequiredAsterisk />
              </label>
              <div style={{ position: 'relative' }}>
                <input
                  placeholder="Confirmez votre mot de passe"
                  type={showConfirmPassword ? 'text' : 'password'}
                  required
                  value={formData.confirmPassword}
                  onChange={(e) => handleInputChange('confirmPassword', e.target.value)}
                  style={{ paddingRight: 80 }}
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  style={{
                    position: 'absolute',
                    right: 8,
                    top: '50%',
                    transform: 'translateY(-50%)',
                    background: 'transparent',
                    border: 'none',
                    color: 'var(--muted)',
                    fontSize: '0.8em',
                    cursor: 'pointer',
                  }}
                >
                  {showConfirmPassword ? 'Masquer' : 'Afficher'}
                </button>
              </div>
            </div>
          </>
        )}

        {registrationStep === 3 && (
          <>
            <div>
              <label style={{ display: 'block', marginBottom: 4, fontSize: '0.9em' }}>
                Pays (référentiel serveur)
                <RequiredAsterisk />
              </label>
              {refsLoading && <p className="meta">Chargement des pays…</p>}
              {refsError && (
                <div
                  style={{
                    marginBottom: 10,
                    padding: '10px 12px',
                    borderRadius: 8,
                    border: '1px solid #f59e0b',
                    background: 'rgba(245, 158, 11, 0.1)',
                    fontSize: '0.88rem',
                  }}
                >
                  <p style={{ margin: '0 0 8px 0' }}>{refsError}</p>
                  <button type="button" className="button-primary" onClick={() => void loadPays()}>
                    Réessayer
                  </button>
                </div>
              )}
              <select
                required
                disabled={refsLoading || paysList.length === 0}
                value={selectedPaysId ?? ''}
                onChange={(e) => {
                  const v = e.target.value ? Number(e.target.value) : null
                  setSelectedPaysId(v)
                  setCitySelect('')
                  setCityOther('')
                  setCityFreeText('')
                }}
              >
                <option value="">{refsLoading ? 'Chargement…' : 'Sélectionnez votre pays…'}</option>
                {paysList.map((p) => (
                  <option key={p.idpays} value={p.idpays}>
                    {p.libpays}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 4, fontSize: '0.9em' }}>
                Ville
                <RequiredAsterisk />
              </label>
              {!selectedPaysId ? (
                <p className="form-hint" style={{ marginTop: 4 }}>
                  Choisissez d’abord un pays pour afficher les villes proposées (ou la saisie libre).
                </p>
              ) : cityOptions.length > 0 ? (
                <div className="form-stack form-stack--tight">
                  <select
                    required
                    value={citySelect}
                    onChange={(e) => {
                      setCitySelect(e.target.value)
                      if (e.target.value !== '__OTHER__') setCityOther('')
                    }}
                  >
                    <option value="">Choisissez une ville…</option>
                    {cityOptions.map((c) => (
                      <option key={c} value={c}>
                        {c}
                      </option>
                    ))}
                    <option value="__OTHER__">Autre (saisie libre)</option>
                  </select>
                  {citySelect === '__OTHER__' && (
                    <input
                      type="text"
                      placeholder="Indiquez votre ville"
                      required
                      value={cityOther}
                      onChange={(e) => setCityOther(e.target.value)}
                      autoComplete="address-level2"
                    />
                  )}
                </div>
              ) : (
                <>
                  <input
                    type="text"
                    placeholder="Ex. nom de votre ville"
                    required
                    value={cityFreeText}
                    onChange={(e) => setCityFreeText(e.target.value)}
                    autoComplete="address-level2"
                  />
                </>
              )}
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: 4, fontSize: '0.9em' }}>
                Pièce d’identité (optionnel à l’inscription)
              </label>
              <input type="file" accept="image/*,.pdf,.docx" onChange={(e) => setIdCardFile(e.target.files?.[0] || null)} />
              {idCardFile && (
                <p
                  style={{
                    fontSize: '0.85em',
                    color: 'var(--accent)',
                    marginTop: 4,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 6,
                  }}
                >
                  <Check size={16} strokeWidth={2} aria-hidden />
                  {idCardFile.name}
                </p>
              )}
              <p className="form-hint" style={{ marginTop: 6 }}>
                Vous pourrez fournir CNIB + photo lors d’une demande vendeur/livreur.
              </p>
            </div>
          </>
        )}

        <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
          {registrationStep > 1 && (
            <button
              type="button"
              onClick={() => setRegistrationStep(registrationStep - 1)}
              style={{
                flex: 1,
                background: 'transparent',
                border: '1px solid var(--border)',
              }}
            >
              Précédent
            </button>
          )}
          <button type="submit" style={{ flex: 1 }} disabled={registerSubmitting}>
            {registerSubmitting ? 'Inscription…' : registrationStep === 3 ? 'Créer mon compte acheteur' : 'Suivant'}
          </button>
        </div>
        <p style={{ textAlign: 'center', fontSize: '0.9em', marginTop: 12 }}>
          Déjà inscrit ?{' '}
          <Link to="/auth?mode=login" style={{ color: '#0066cc', textDecoration: 'underline' }}>
            Se connecter
          </Link>
        </p>
        <p style={{ textAlign: 'center', fontSize: '0.85em', marginTop: 12, color: 'var(--muted)' }}>
          En vous inscrivant, vous acceptez nos{' '}
          <Link to="/terms" target="_blank" style={{ color: '#2a9d8f', textDecoration: 'underline' }}>
            Conditions d&apos;utilisation
          </Link>
        </p>
      </form>
    </>
  )
}
