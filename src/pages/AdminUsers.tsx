import { useAuth, isStaffRole, type User } from '../contexts/AuthContext'
import { adminResetPassword, patchAdminUserProfile, adminCnibUrl } from '../services/adminUserApi'
import { useNavigate } from 'react-router-dom'
import { useEffect, useState, useRef, useCallback } from 'react'
import { fetchRoles, fetchPays, fetchTypeArticles } from '../services/referenceApi'
import type { RoleDtoJson, PaysDtoJson, TypeArticleDtoJson } from '../types/backend'

export default function AdminUsers() {
  const { user, getAllUsers, deleteUser, refreshAdminUsers } = useAuth()
  const navigate = useNavigate()
  const [users, setUsers] = useState(getAllUsers())
  const [filterType, setFilterType] = useState<'all' | 'buyer' | 'seller' | 'livreur'>('all')
  const [searchQuery, setSearchQuery] = useState('')
  const [resetPasswordUserId, setResetPasswordUserId] = useState<string | null>(null)
  const [newPassword, setNewPassword] = useState('')
  const [showSuccessMessage, setShowSuccessMessage] = useState(false)
  const [successUserName, setSuccessUserName] = useState('')
  const [deleteConfirmUserId, setDeleteConfirmUserId] = useState<string | null>(null)
  const [showDeleteSuccess, setShowDeleteSuccess] = useState(false)
  const [deletedUserName, setDeletedUserName] = useState('')
  const searchInputRef = useRef<HTMLInputElement>(null)

  const [roles, setRoles] = useState<RoleDtoJson[]>([])
  const [paysList, setPaysList] = useState<PaysDtoJson[]>([])
  const [types, setTypes] = useState<TypeArticleDtoJson[]>([])
  const [profileTarget, setProfileTarget] = useState<User | null>(null)
  const [editRoleId, setEditRoleId] = useState('')
  const [editPaysId, setEditPaysId] = useState('')
  const [editTypeId, setEditTypeId] = useState('')
  const [editVendeurInternational, setEditVendeurInternational] = useState(false)
  const [profileSaving, setProfileSaving] = useState(false)

  const isSuperAdmin = user?.librole?.toUpperCase() === 'SUPER_ADMIN'

  const loadRefs = useCallback(async () => {
    try {
      const [r, p, t] = await Promise.all([fetchRoles(), fetchPays(), fetchTypeArticles()])
      setRoles(r)
      setPaysList(p)
      setTypes(t)
    } catch {
      /* ignore */
    }
  }, [])

  useEffect(() => {
    if (!user || !isStaffRole(user)) {
      navigate('/')
    }
  }, [user, navigate])

  useEffect(() => {
    void refreshAdminUsers().then(() => setUsers(getAllUsers()))
  }, [refreshAdminUsers, getAllUsers])

  useEffect(() => {
    if (user && isStaffRole(user)) void loadRefs()
  }, [user, loadRefs])

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'f') {
        e.preventDefault()
        searchInputRef.current?.focus()
      }
    }
    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [])

  if (!user) return null

  const isProtectedUser = (u: (typeof users)[0]) => u.librole?.toUpperCase() === 'SUPER_ADMIN'
  const canEditProfile = (u: (typeof users)[0]) => {
    if (!isStaffRole(user)) return false
    if (isSuperAdmin) return true
    return !isProtectedUser(u) && u.librole?.toUpperCase() !== 'ADMIN'
  }

  const openProfileModal = (u: User) => {
    setProfileTarget(u)
    setEditRoleId(u.idrole != null ? String(u.idrole) : '')
    setEditPaysId(u.idpays != null ? String(u.idpays) : '')
    setEditTypeId(u.idtypeVendeur != null ? String(u.idtypeVendeur) : '')
    setEditVendeurInternational(!!u.vendeurInternational)
  }

  const roleOptions = roles.filter((r) => {
    if (isSuperAdmin) return true
    const lib = r.librole.toUpperCase()
    return lib !== 'ADMIN' && lib !== 'SUPER_ADMIN'
  })

  const saveProfile = async () => {
    if (!profileTarget) return
    const uid = Number(profileTarget.id)
    const body: { idrole?: number; idpays?: number; idtypeVendeur?: number; vendeurInternational?: boolean } = {}
    if (editRoleId) {
      const nr = Number(editRoleId)
      if (nr !== profileTarget.idrole) body.idrole = nr
    }
    if (editPaysId) {
      const np = Number(editPaysId)
      if (np !== profileTarget.idpays) body.idpays = np
    }
    const selectedRole = roles.find((r) => r.idrole === Number(editRoleId))
    const isVendeur = selectedRole?.librole.toUpperCase() === 'VENDEUR'
    if (isVendeur) {
      const hasType = editTypeId !== '' || profileTarget.idtypeVendeur != null
      if (!hasType) {
        alert('Sélectionnez une catégorie vendeur pour un compte VENDEUR.')
        return
      }
      if (editTypeId) {
        const nt = Number(editTypeId)
        if (nt !== profileTarget.idtypeVendeur) body.idtypeVendeur = nt
      }
      if (editVendeurInternational !== !!profileTarget.vendeurInternational) {
        body.vendeurInternational = editVendeurInternational
      }
    }
    if (Object.keys(body).length === 0) {
      setProfileTarget(null)
      return
    }
    setProfileSaving(true)
    try {
      await patchAdminUserProfile(uid, body)
      await refreshAdminUsers()
      setUsers(getAllUsers())
      setProfileTarget(null)
      alert('Profil mis à jour.')
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Mise à jour impossible (droits ou données invalides).')
    } finally {
      setProfileSaving(false)
    }
  }

  const handleResetPassword = async (userId: string) => {
    if (!newPassword || newPassword.length < 6) {
      alert('Le mot de passe doit contenir au moins 6 caractères')
      return
    }
    const userToUpdate = getAllUsers().find((u) => u.id === userId)
    if (!userToUpdate) {
      alert('Utilisateur introuvable')
      return
    }
    try {
      await adminResetPassword(Number(userId), newPassword)
      setSuccessUserName(`${userToUpdate.prenoms} ${userToUpdate.nom}`)
      setShowSuccessMessage(true)
      setResetPasswordUserId(null)
      setNewPassword('')
      setTimeout(() => setShowSuccessMessage(false), 5000)
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Réinitialisation impossible')
    }
  }

  const handleDeleteUser = async (userId: string) => {
    const userToDelete = getAllUsers().find((u) => u.id === userId)
    if (!userToDelete) {
      alert('Utilisateur introuvable')
      return
    }
    if (userToDelete.librole?.toUpperCase() === 'SUPER_ADMIN') {
      alert('Impossible de supprimer un compte SUPER_ADMIN')
      return
    }
    const success = await deleteUser(userId)
    if (success) {
      await refreshAdminUsers()
      setUsers(getAllUsers())
      setDeletedUserName(`${userToDelete.prenoms} ${userToDelete.nom}`)
      setShowDeleteSuccess(true)
      setDeleteConfirmUserId(null)
      setTimeout(() => setShowDeleteSuccess(false), 5000)
    } else {
      alert('Erreur lors de la suppression du compte')
    }
  }

  let filteredUsers = filterType === 'all' ? users : users.filter((u) => u.accountType === filterType)
  if (searchQuery.trim()) {
    const query = searchQuery.toLowerCase()
    filteredUsers = filteredUsers.filter(
      (u) =>
        u.nom.toLowerCase().includes(query) ||
        u.prenoms.toLowerCase().includes(query) ||
        u.email.toLowerCase().includes(query) ||
        u.id.toLowerCase().includes(query) ||
        (u.city && u.city.toLowerCase().includes(query)) ||
        (u.country && u.country.toLowerCase().includes(query)) ||
        (u.librole && u.librole.toLowerCase().includes(query))
    )
  }

  const buyersCount = users.filter((u) => u.accountType === 'buyer').length
  const sellersCount = users.filter((u) => u.accountType === 'seller').length
  const livreursCount = users.filter((u) => u.accountType === 'livreur').length

  return (
    <div className="container" style={{ paddingTop: 20, paddingBottom: 40 }}>
      {showSuccessMessage && (
        <div
          style={{
            position: 'fixed',
            top: 20,
            right: 20,
            background: 'var(--admin-accent)',
            color: 'white',
            padding: '16px 24px',
            borderRadius: 8,
            boxShadow: '0 4px 12px rgba(0,0,0,0.3)',
            zIndex: 1000,
            maxWidth: '400px',
          }}
        >
          <strong>Mot de passe réinitialisé</strong>
          <div style={{ fontSize: '0.9em', marginTop: 4 }}>{successUserName}</div>
          <button type="button" onClick={() => setShowSuccessMessage(false)} style={{ marginTop: 8 }}>
            OK
          </button>
        </div>
      )}

      {showDeleteSuccess && (
        <div
          style={{
            position: 'fixed',
            top: 20,
            right: 20,
            background: '#dc2626',
            color: 'white',
            padding: '16px 24px',
            borderRadius: 8,
            zIndex: 1000,
          }}
        >
          Compte supprimé : {deletedUserName}
          <button type="button" onClick={() => setShowDeleteSuccess(false)} style={{ marginLeft: 12 }}>
            OK
          </button>
        </div>
      )}

      {profileTarget && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(0,0,0,0.65)',
            zIndex: 2000,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: 16,
          }}
          role="dialog"
          aria-modal="true"
          aria-labelledby="admin-profile-title"
        >
          <div
            className="card"
            style={{ maxWidth: 440, width: '100%', padding: 24, maxHeight: '90vh', overflowY: 'auto' }}
          >
            <h2 id="admin-profile-title" style={{ marginTop: 0 }}>
              Rôle et référentiels
            </h2>
            <p className="meta">
              {profileTarget.prenoms} {profileTarget.nom} — {profileTarget.email}
            </p>
            <div style={{ display: 'grid', gap: 14, marginTop: 16 }}>
              <div>
                <label style={{ display: 'block', marginBottom: 6, fontSize: '0.85em' }}>Rôle</label>
                <select value={editRoleId} onChange={(e) => setEditRoleId(e.target.value)} style={{ width: '100%' }}>
                  <option value="">—</option>
                  {roleOptions.map((r) => (
                    <option key={r.idrole} value={r.idrole}>
                      {r.librole}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: 6, fontSize: '0.85em' }}>Pays</label>
                <select value={editPaysId} onChange={(e) => setEditPaysId(e.target.value)} style={{ width: '100%' }}>
                  <option value="">—</option>
                  {paysList.map((p) => (
                    <option key={p.idpays} value={p.idpays}>
                      {p.libpays}
                    </option>
                  ))}
                </select>
              </div>
              {roles.find((r) => r.idrole === Number(editRoleId))?.librole.toUpperCase() === 'VENDEUR' && (
                <>
                  <div>
                    <label style={{ display: 'block', marginBottom: 6, fontSize: '0.85em' }}>Catégorie vendeur</label>
                    <select value={editTypeId} onChange={(e) => setEditTypeId(e.target.value)} style={{ width: '100%' }}>
                      <option value="">—</option>
                      {types.map((t) => (
                        <option key={t.idtype} value={t.idtype}>
                          {t.libtype}
                        </option>
                      ))}
                    </select>
                  </div>
                  <label style={{ display: 'flex', alignItems: 'center', gap: 10, fontSize: '0.9em', cursor: 'pointer' }}>
                    <input
                      type="checkbox"
                      checked={editVendeurInternational}
                      onChange={(e) => setEditVendeurInternational(e.target.checked)}
                    />
                    Vendeur international (marché international)
                  </label>
                </>
              )}
            </div>
            <div style={{ display: 'flex', gap: 10, marginTop: 20, flexWrap: 'wrap' }}>
              <button type="button" className="button-primary" disabled={profileSaving} onClick={() => void saveProfile()}>
                {profileSaving ? '…' : 'Enregistrer'}
              </button>
              <button type="button" onClick={() => setProfileTarget(null)} disabled={profileSaving}>
                Annuler
              </button>
            </div>
          </div>
        </div>
      )}

      <div style={{ marginBottom: 24 }}>
        <h1 style={{ marginTop: 0 }}>Utilisateurs</h1>
        <p style={{ color: 'var(--muted)', marginTop: 8, marginBottom: 0 }}>
          {users.length} compte(s) • {buyersCount} acheteur(s) • {sellersCount} vendeur(s)
        </p>
      </div>

      <div style={{ marginBottom: 24 }}>
        <input
          ref={searchInputRef}
          type="search"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Rechercher (nom, email, rôle…) — Ctrl+F"
          style={{
            width: '100%',
            padding: '12px 16px',
            borderRadius: 8,
            border: '1px solid var(--border)',
            background: 'var(--input-bg)',
            color: 'var(--text)',
          }}
        />
      </div>

      <div
        style={{
          display: 'flex',
          gap: 12,
          marginBottom: 24,
          borderBottom: '1px solid var(--border)',
          paddingBottom: 12,
          flexWrap: 'wrap',
        }}
      >
        {(['all', 'buyer', 'seller', 'livreur'] as const).map((ft) => (
          <button
            key={ft}
            type="button"
            onClick={() => setFilterType(ft)}
            style={{
              background: filterType === ft ? 'var(--admin-accent)' : 'transparent',
              border: filterType === ft ? 'none' : '1px solid var(--border)',
              padding: '8px 16px',
            }}
          >
            {ft === 'all'
              ? `Tous (${users.length})`
              : ft === 'buyer'
                ? `Acheteurs (${buyersCount})`
                : ft === 'seller'
                  ? `Vendeurs (${sellersCount})`
                  : `Livreurs (${livreursCount})`}
          </button>
        ))}
      </div>

      {filteredUsers.length === 0 ? (
        <p style={{ textAlign: 'center', color: 'var(--muted)', padding: '40px 0' }}>Aucun utilisateur</p>
      ) : (
        <div style={{ display: 'grid', gap: 16 }}>
          {filteredUsers.map((u) => (
            <div key={u.id} className="card" style={{ padding: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', flexWrap: 'wrap', gap: 12 }}>
                <div>
                  <h3 style={{ margin: '0 0 4px 0' }}>
                    {u.prenoms} {u.nom}
                  </h3>
                  <div style={{ color: 'var(--muted)', fontSize: '0.9em' }}>{u.email}</div>
                  <div style={{ marginTop: 8, fontSize: '0.85em' }}>
                    <span style={{ color: 'var(--muted)' }}>Rôle :</span> {u.librole || '—'} ·{' '}
                    <span style={{ color: 'var(--muted)' }}>ID :</span> {u.id}
                  </div>
                  {u.country && (
                    <div style={{ fontSize: '0.85em', marginTop: 4 }}>
                      Pays : {u.country}
                    </div>
                  )}
                  {u.libtypeVendeur && (
                    <div style={{ fontSize: '0.85em', marginTop: 4 }}>
                      Catégorie vendeur : {u.libtypeVendeur}
                    </div>
                  )}
                </div>
              </div>

              <div
                style={{
                  marginTop: 16,
                  paddingTop: 16,
                  borderTop: '1px solid var(--border)',
                  display: 'flex',
                  gap: 10,
                  flexWrap: 'wrap',
                }}
              >
                {resetPasswordUserId === u.id ? (
                  <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', width: '100%', alignItems: 'center' }}>
                    <input
                      type="password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      placeholder="Nouveau mot de passe (min 6)"
                      style={{ flex: 1, minWidth: 160, padding: 8 }}
                    />
                    <button type="button" onClick={() => handleResetPassword(u.id)}>
                      Confirmer
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setResetPasswordUserId(null)
                        setNewPassword('')
                      }}
                    >
                      Annuler
                    </button>
                  </div>
                ) : deleteConfirmUserId === u.id ? (
                  <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center', width: '100%' }}>
                    <span style={{ color: '#f87171' }}>Supprimer définitivement ce compte ?</span>
                    <button type="button" onClick={() => handleDeleteUser(u.id)}>
                      Supprimer
                    </button>
                    <button type="button" onClick={() => setDeleteConfirmUserId(null)}>
                      Annuler
                    </button>
                  </div>
                ) : (
                  <>
                    {canEditProfile(u) && (
                      <button type="button" onClick={() => openProfileModal(u)}>
                        Rôle / pays / catégorie
                      </button>
                    )}
                    {u.cnib && (
                      <button type="button" onClick={() => window.open(adminCnibUrl(Number(u.id)), '_blank', 'noopener,noreferrer')}>
                        Pièce d’identité
                      </button>
                    )}
                    <button type="button" onClick={() => setResetPasswordUserId(u.id)}>
                      Réinitialiser mot de passe
                    </button>
                    <button
                      type="button"
                      onClick={() => setDeleteConfirmUserId(u.id)}
                      disabled={isProtectedUser(u)}
                      style={{ opacity: isProtectedUser(u) ? 0.5 : 1 }}
                    >
                      Supprimer
                    </button>
                  </>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
