/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useCallback, useRef, type ReactNode } from 'react'
import type { ArticleDtoJson } from '../types/backend'
import {
  fetchArticles,
  fetchArticleById,
  createArticleMultipart,
  updateArticleJson,
  deleteArticle,
  adminPatchArticle,
} from '../services/articleApi'
import { fetchTypeArticles } from '../services/referenceApi'
import { articleGalleryPhotoUrl, articleMainPhotoUrl } from '../utils/articleUrls'
import { dateFromDto } from '../utils/dateFromDto'

export type SubCategory = {
  id: string
  name: string
  parentId: string
}

export type CategoryIconKey = 'home' | 'shirt' | 'laptop' | 'apple' | 'books'

export type Category = {
  id: string
  name: string
  iconKey: CategoryIconKey
  description: string
  examples: string[]
  subCategories: SubCategory[]
}

export const CATEGORIES: Category[] = [
  {
    id: 'maison-bricolage',
    name: 'Maison, Bricolage & Construction',
    iconKey: 'home',
    description: 'Habitat, rénovation, gros œuvres',
    examples: [
      'Matériaux de construction (ciment, bois, tuiles)',
      'Outillage et équipement de chantier',
      'Équipement de jardinage',
      'Quincaillerie',
    ],
    subCategories: [
      { id: 'materiaux-construction', name: 'Matériaux de construction', parentId: 'maison-bricolage' },
      { id: 'outillage', name: 'Outillage et équipement', parentId: 'maison-bricolage' },
      { id: 'jardinage', name: 'Jardinage', parentId: 'maison-bricolage' },
      { id: 'quincaillerie', name: 'Quincaillerie', parentId: 'maison-bricolage' },
      { id: 'mobilier', name: 'Mobilier et décoration', parentId: 'maison-bricolage' },
    ],
  },
  {
    id: 'mode-textile',
    name: 'Mode, Habillement & Textile',
    iconKey: 'shirt',
    description: 'Prêt-à-porter, accessoires personnels et tissus',
    examples: [
      'Vêtements, chaussures, maroquinerie',
      'Tissus simples ou complexes',
      'Bijoux et montres',
      'Linge de maison',
    ],
    subCategories: [
      { id: 'vetements-homme', name: 'Vêtements homme', parentId: 'mode-textile' },
      { id: 'vetements-femme', name: 'Vêtements femme', parentId: 'mode-textile' },
      { id: 'chaussures', name: 'Chaussures', parentId: 'mode-textile' },
      { id: 'accessoires-mode', name: 'Accessoires et maroquinerie', parentId: 'mode-textile' },
      { id: 'bijoux', name: 'Bijoux et montres', parentId: 'mode-textile' },
      { id: 'tissus', name: 'Tissus et mercerie', parentId: 'mode-textile' },
    ],
  },
  {
    id: 'electronique-informatique',
    name: 'Électronique & Informatique',
    iconKey: 'laptop',
    description: 'Appareils technologiques, composants et gadgets',
    examples: [
      'Matériels informatiques (ordinateurs, périphériques)',
      'Téléphones mobiles et accessoires',
      'Télévisions, systèmes audio, caméras',
      'Électroménager (petit et gros)',
    ],
    subCategories: [
      { id: 'ordinateurs', name: 'Ordinateurs et tablettes', parentId: 'electronique-informatique' },
      { id: 'telephones', name: 'Téléphones mobiles', parentId: 'electronique-informatique' },
      { id: 'peripheriques', name: 'Périphériques', parentId: 'electronique-informatique' },
      { id: 'tv-audio', name: 'TV et Audio', parentId: 'electronique-informatique' },
      { id: 'photo-video', name: 'Photo et Vidéo', parentId: 'electronique-informatique' },
      { id: 'electromenager', name: 'Électroménager', parentId: 'electronique-informatique' },
    ],
  },
  {
    id: 'consommation-alimentation',
    name: 'Produits de Consommation & Alimentation',
    iconKey: 'apple',
    description: 'Produits essentiels consommés régulièrement',
    examples: [
      'Épicerie fine, boissons, produits frais',
      "Produits d'hygiène et de beauté",
      'Articles pour animaux de compagnie',
      "Produits d'entretien",
    ],
    subCategories: [
      { id: 'alimentation', name: 'Alimentation et boissons', parentId: 'consommation-alimentation' },
      { id: 'hygiene-beaute', name: 'Hygiène et beauté', parentId: 'consommation-alimentation' },
      { id: 'animaux', name: 'Articles pour animaux', parentId: 'consommation-alimentation' },
      { id: 'entretien', name: "Produits d'entretien", parentId: 'consommation-alimentation' },
      { id: 'sante', name: 'Santé et bien-être', parentId: 'consommation-alimentation' },
    ],
  },
  {
    id: 'culture-loisirs',
    name: 'Culture, Loisirs & Services',
    iconKey: 'books',
    description: 'Divertissement, éducation et bien-être',
    examples: [
      'Livres, médias (CD, DVD, vinyles)',
      'Articles de sport et de plein air',
      'Jouets et jeux',
      "Matériel d'artisanat et fournitures de bureau",
    ],
    subCategories: [
      { id: 'livres-medias', name: 'Livres et médias', parentId: 'culture-loisirs' },
      { id: 'sport', name: 'Sport et plein air', parentId: 'culture-loisirs' },
      { id: 'jouets', name: 'Jouets et jeux', parentId: 'culture-loisirs' },
      { id: 'artisanat', name: 'Artisanat et loisirs créatifs', parentId: 'culture-loisirs' },
      { id: 'fournitures-bureau', name: 'Fournitures de bureau', parentId: 'culture-loisirs' },
      { id: 'instruments-musique', name: 'Instruments de musique', parentId: 'culture-loisirs' },
    ],
  },
]

export type Product = {
  id: string
  title: string
  price: number
  currency: string
  image: string
  images?: string[]
  description?: string
  size?: string
  brand?: string
  category?: string
  idtype?: number
  isBlocked?: boolean
  hasWarning?: boolean
  warningMessage?: string
  sellerId?: string
  /** Vendeur international (badge catalogue). */
  sellerInternational?: boolean
  /** Moyenne avis 1–5 (null si aucun avis). */
  sellerRatingAvg?: number | null
  sellerRatingCount?: number
  /** Abonnement « vendeur certifié » actif. */
  sellerCertified?: boolean
  views?: number
  country?: string
  city?: string
  listedAt?: string
}

export type TypeArticleOption = {
  idtype: number
  libtype: string
  libfamille?: string
}

export function mapArticleDto(a: ArticleDtoJson): Product {
  const id = String(a.idarticle)
  const rawPhotos =
    a.photos && a.photos.length > 0 ? a.photos : a.photo ? [a.photo] : []
  const images = rawPhotos
    .filter(Boolean)
    .map((fn) => articleGalleryPhotoUrl(a.idarticle, fn as string))
  const image = images[0] ?? articleMainPhotoUrl(a.idarticle)
  return {
    id,
    title: a.libarticle,
    price: a.prixunitaire,
    currency: 'XOF',
    image,
    images: images.length ? images : [image],
    description: a.descarticle,
    category: a.typearticle,
    idtype: a.idtype,
    sellerId: a.idVendeur != null ? String(a.idVendeur) : undefined,
    sellerInternational: a.vendeurInternational === true,
    sellerRatingAvg: a.vendeurNoteMoyenne ?? null,
    sellerRatingCount: a.vendeurNombreAvis ?? 0,
    sellerCertified: a.vendeurCertifieActif === true,
    views: a.viewCount,
    isBlocked: a.blocked,
    warningMessage: a.warningMessage || undefined,
    hasWarning: !!(a.warningMessage && a.warningMessage.length > 0),
    listedAt: a.dateupdate ? dateFromDto(a.dateupdate) : undefined,
  }
}

type ProductContextType = {
  products: Product[]
  productsLoading: boolean
  typeArticles: TypeArticleOption[]
  /** Sans argument : recharge selon le dernier mode (tout le catalogue ou international). */
  refreshProducts: (mode?: 'all' | 'international') => Promise<void>
  addProduct: (input: {
    title: string
    price: number
    description: string
    photos: File[]
    idtype: number
  }) => Promise<void>
  deleteProduct: (id: string) => Promise<void>
  blockProduct: (id: string) => Promise<void>
  unblockProduct: (id: string) => Promise<void>
  addWarning: (id: string, message: string) => Promise<void>
  removeWarning: (id: string) => Promise<void>
  updateProductPrice: (id: string, newPrice: number) => Promise<void>
  incrementViews: (id: string) => Promise<void>
  getProductByIdFromApi: (id: string) => Promise<Product | null>
}

const ProductContext = createContext<ProductContextType | undefined>(undefined)

export function ProductProvider({ children }: { children: ReactNode }) {
  const [products, setProducts] = useState<Product[]>([])
  const [productsLoading, setProductsLoading] = useState(true)
  const [typeArticles, setTypeArticles] = useState<TypeArticleOption[]>([])
  const catalogModeRef = useRef<'all' | 'international'>('all')

  const refreshProducts = useCallback(async (mode?: 'all' | 'international') => {
    if (mode !== undefined) {
      catalogModeRef.current = mode
    }
    setProductsLoading(true)
    try {
      const intl = catalogModeRef.current === 'international'
      const [articlesRes, typesRes] = await Promise.allSettled([
        fetchArticles(intl ? { international: true } : {}),
        fetchTypeArticles(),
      ])
      if (articlesRes.status === 'fulfilled') {
        setProducts(articlesRes.value.map(mapArticleDto))
      } else {
        console.error(articlesRes.reason)
        setProducts([])
      }
      if (typesRes.status === 'fulfilled') {
        setTypeArticles(
          typesRes.value.map((t) => ({
            idtype: t.idtype,
            libtype: t.libtype,
            libfamille: t.libfamille,
          }))
        )
      } else {
        console.error(typesRes.reason)
        setTypeArticles([])
      }
    } finally {
      setProductsLoading(false)
    }
  }, [])

  const addProduct = useCallback(
    async (input: {
      title: string
      price: number
      description: string
      photos: File[]
      idtype: number
    }) => {
      await createArticleMultipart({
        libarticle: input.title,
        descarticle: input.description || ' ',
        prixunitaire: Math.round(input.price),
        idtype: input.idtype,
        photos: input.photos,
      })
      await refreshProducts()
    },
    [refreshProducts]
  )

  const deleteProduct = useCallback(
    async (id: string) => {
      await deleteArticle(Number(id))
      await refreshProducts()
    },
    [refreshProducts]
  )

  const blockProduct = useCallback(
    async (id: string) => {
      await adminPatchArticle(Number(id), { blocked: true })
      await refreshProducts()
    },
    [refreshProducts]
  )

  const unblockProduct = useCallback(
    async (id: string) => {
      await adminPatchArticle(Number(id), { blocked: false })
      await refreshProducts()
    },
    [refreshProducts]
  )

  const addWarning = useCallback(
    async (id: string, message: string) => {
      await adminPatchArticle(Number(id), { warningMessage: message })
      await refreshProducts()
    },
    [refreshProducts]
  )

  const removeWarning = useCallback(
    async (id: string) => {
      await adminPatchArticle(Number(id), { clearWarning: true })
      await refreshProducts()
    },
    [refreshProducts]
  )

  const updateProductPrice = useCallback(
    async (id: string, newPrice: number) => {
      const numId = Number(id)
      const current = await fetchArticleById(numId)
      await updateArticleJson(numId, {
        libarticle: current.libarticle,
        descarticle: current.descarticle,
        prixunitaire: Math.round(newPrice),
        photo: current.photo || '',
        idtype: current.idtype ?? undefined,
      })
      await refreshProducts()
    },
    [refreshProducts]
  )

  const incrementViews = useCallback(async (id: string) => {
    try {
      await fetchArticleById(Number(id))
      setProducts((prev) =>
        prev.map((p) => (p.id === id ? { ...p, views: (p.views || 0) + 1 } : p))
      )
    } catch {
      /* ignore */
    }
  }, [])

  const getProductByIdFromApi = useCallback(async (id: string): Promise<Product | null> => {
    try {
      const a = await fetchArticleById(Number(id))
      return mapArticleDto(a)
    } catch {
      return null
    }
  }, [])

  return (
    <ProductContext.Provider
      value={{
        products,
        productsLoading,
        typeArticles,
        refreshProducts,
        addProduct,
        deleteProduct,
        blockProduct,
        unblockProduct,
        addWarning,
        removeWarning,
        updateProductPrice,
        incrementViews,
        getProductByIdFromApi,
      }}
    >
      {children}
    </ProductContext.Provider>
  )
}

export function useProducts() {
  const context = useContext(ProductContext)
  if (!context) {
    throw new Error('useProducts must be used within ProductProvider')
  }
  return context
}
