/** Champs JSON renvoyés par le backend (Jackson). */

export type UserDtoJson = {
  iduser: number
  nom: string
  prenom: string
  email: string
  cnib?: string | null
  photoProfil?: string | null
  latitude?: number | null
  longitude?: number | null
  idrole?: number
  librole?: string
  idpays?: number
  libpays?: string
  ville?: string | null
  idtypeVendeur?: number
  libtypeVendeur?: string
  typeEnginLivreur?: string | null
  dateupdate?: string
}

export type RoleUpgradeRequestDtoJson = {
  id: number
  iduser?: number
  emailDemandeur?: string
  roleDemande: string
  status: string
  latitude?: number | null
  longitude?: number | null
  idtypeVendeur?: number | null
  typeEnginLivreur?: string | null
  adminMotif?: string | null
  createdAt?: string
  decidedAt?: string | null
  fichierCnib?: string
  fichierPhoto?: string
}

/** Admin / détail complet (montants possibles). */
export type LivraisonDtoJson = {
  idlivraison: number
  idtransaction?: number
  statut: string
  typeEnginUtilise?: string | null
  idlivreur?: number | null
  livreurEmail?: string
  livreurNomComplet?: string
  idArticle?: number | null
  articleLibelle?: string
  quantite?: number
  montantTotal?: number
  acheteurEmail?: string
  acheteurVille?: string
  vendeurEmail?: string
  datecreation?: string
  datePriseEnCharge?: string | null
  dateLivraison?: string | null
}

/** API livreur : pas de montant. */
export type LivreurNavigationDtoJson = {
  itineraireComplet?: string | null
  etapeRetraitVendeur?: string | null
  etapeDepotClient?: string | null
}

export type LivraisonLivreurDtoJson = {
  idlivraison: number
  idtransaction?: number
  statut: string
  typeEnginUtilise?: string | null
  idlivreur?: number | null
  livreurEmail?: string
  livreurNomComplet?: string
  idArticle?: number | null
  articleLibelle?: string
  quantite?: number
  acheteurEmail?: string
  acheteurVille?: string
  vendeurEmail?: string
  datecreation?: string
  datePriseEnCharge?: string | null
  dateLivraison?: string | null
  navigation?: LivreurNavigationDtoJson | null
  /** Google Maps (recherche) vers le point de dépôt client enregistré pour la commande */
  lieuDepotCarteUrl?: string | null
}

export type LivreurMesLivraisonsDtoJson = {
  enCours: LivraisonLivreurDtoJson[]
  terminees: LivraisonLivreurDtoJson[]
  limiteChargee: number
}

/** Liste admin : GET /api/v1/admin/livraisons (même forme que LivraisonDTO côté API). */
export type AdminLivraisonListDtoJson = {
  idlivraison: number
  idtransaction?: number
  statut: string
  typeEnginUtilise?: string | null
  idlivreur?: number | null
  livreurEmail?: string | null
  livreurNomComplet?: string | null
  idArticle?: number | null
  articleLibelle?: string | null
  quantite?: number | null
  montantTotal?: number | null
  acheteurEmail?: string | null
  acheteurVille?: string | null
  vendeurEmail?: string | null
  datecreation?: string | null
  datePriseEnCharge?: string | null
  dateLivraison?: string | null
}

export type LivreurDashboardDtoJson = {
  livraisonsEnCours: number
  livraisonsLivrees: number
  livraisonsLivreesMoto: number
  livraisonsLivreesVehicule: number
  enginProfil?: string | null
  dernieresCourses: LivraisonLivreurDtoJson[]
}

export type SuiviEtapeDtoJson = {
  code: string
  libelle: string
  date?: string | null
}

export type CommandeSuiviDtoJson = {
  idtransaction: number
  idlivraison?: number | null
  statutLivraison?: string | null
  articleLibelle?: string | null
  quantite?: number | null
  montantTotal?: number | null
  etapes: SuiviEtapeDtoJson[]
  vendorPickupCode?: string | null
  vendorPackedReferenceBase64?: string | null
  livreurPrenom?: string | null
  livreurNom?: string | null
  livreurAssigne: boolean
  navigationDisponible: boolean
  lienRetraitChezVendeur?: string | null
  lienLivraisonChezClient?: string | null
  lienTrajetVendeurVersClient?: string | null
  /** Itinéraire Google Maps : position livreur (profil ou dernière synchro) → client. */
  lienLivreurVersClient?: string | null
  livreurPositionMiseAJourAt?: string | null
}

export type ClientLivraisonQrDtoJson = {
  idtransaction: number
  idlivraison: number
  articleLibelle?: string | null
  quantite?: number | null
  qrPayload: string
  qrImagePngBase64: string
  message?: string | null
}

export type CartItemDtoJson = {
  idcartitem: number
  idArticle: number
  libelleArticle: string
  quantity: number
  prixUnitaireCatalogue: number
  prixUnitaireNegocie?: number | null
  idMessageAccord?: number | null
  negociationVerrouillee: boolean
}

export type CartDtoJson = {
  idcart: number
  items: CartItemDtoJson[]
  montantTotalEstime: number
}

export type ArticleDtoJson = {
  idarticle: number
  libarticle: string
  descarticle: string
  prixunitaire: number
  photo: string | null
  photos?: string[]
  idtype?: number
  typearticle?: string
  idVendeur?: number
  vendeurNom?: string
  vendeurPrenom?: string
  blocked?: boolean
  warningMessage?: string | null
  viewCount?: number
  dateupdate?: string
}

export type RoleDtoJson = {
  idrole: number
  librole: string
  descrole?: string
}

export type PaysDtoJson = {
  idpays: number
  libpays: string
  descpays?: string
}

export type TypeArticleDtoJson = {
  idtype: number
  libtype: string
  desctype?: string
  idfamille?: number
  libfamille?: string
}

export type FamilleArticleDtoJson = {
  idfamille: number
  libfamille: string
  description: string
  dateupdate?: string
  userupdate?: string
  typeArticles?: TypeArticleDtoJson[]
}

export type ConversationDtoJson = {
  idconversation: number
  idAcheteur?: number
  acheteurNom?: string
  idVendeur?: number
  vendeurNom?: string
  idArticle?: number
  articleLibelle?: string
  datecreation?: string
  dateupdate?: string
}

export type ChatMessageDtoJson = {
  idmessage: number
  idconversation?: number
  idAuteur?: number
  auteurNom?: string
  contenu: string
  dateenvoi?: string
  prixPropose?: number | null
  quantiteProposee?: number | null
  statutOffre?: string | null
  /** Dernier prix fixe par le vendeur après 2 refus (paiement à ce montant). */
  offreFinaleVendeur?: boolean | null
}

export type ComplaintDtoJson = {
  idplainte: number
  idAuteur?: number
  auteurNom?: string
  auteurEmail?: string
  idArticle?: number | null
  titre: string
  description: string
  lu: boolean
  datecreation?: string
}
