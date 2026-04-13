/* eslint-disable react-refresh/only-export-components */
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { useAuth } from './AuthContext'
import type { CartDtoJson } from '../types/backend'
import { fetchCart, addCartItem, patchCartItem, removeCartItem, clearCart } from '../services/cartApi'
import { ApiError } from '../services/apiClient'

type CartContextValue = {
  cart: CartDtoJson | null
  cartLoading: boolean
  itemCount: number
  refreshCart: () => Promise<void>
  addToCart: (idArticle: number, quantity: number) => Promise<void>
  setLineQuantity: (idcartitem: number, quantity: number) => Promise<void>
  removeLine: (idcartitem: number) => Promise<void>
  emptyCart: () => Promise<void>
}

const CartContext = createContext<CartContextValue | undefined>(undefined)

export function CartProvider({ children }: { children: ReactNode }) {
  const { user, isAuthenticated } = useAuth()
  const [cart, setCart] = useState<CartDtoJson | null>(null)
  const [cartLoading, setCartLoading] = useState(false)

  const refreshCart = useCallback(async () => {
    if (!isAuthenticated || !user) {
      setCart(null)
      return
    }
    setCartLoading(true)
    try {
      const c = await fetchCart()
      setCart(c)
    } catch (e) {
      if (e instanceof ApiError && e.status === 401) {
        setCart(null)
      } else {
        setCart({ idcart: 0, items: [], montantTotalEstime: 0 })
      }
    } finally {
      setCartLoading(false)
    }
  }, [isAuthenticated, user])

  useEffect(() => {
    void refreshCart()
  }, [refreshCart])

  const addToCart = useCallback(
    async (idArticle: number, quantity: number) => {
      const updated = await addCartItem(idArticle, quantity)
      setCart(updated)
    },
    []
  )

  const setLineQuantity = useCallback(async (idcartitem: number, quantity: number) => {
    const updated = await patchCartItem(idcartitem, quantity)
    setCart(updated)
  }, [])

  const removeLine = useCallback(async (idcartitem: number) => {
    const updated = await removeCartItem(idcartitem)
    setCart(updated)
  }, [])

  const emptyCart = useCallback(async () => {
    const updated = await clearCart()
    setCart(updated)
  }, [])

  const itemCount = useMemo(
    () => (cart?.items ?? []).reduce((s, i) => s + i.quantity, 0),
    [cart]
  )

  const value = useMemo(
    () => ({
      cart,
      cartLoading,
      itemCount,
      refreshCart,
      addToCart,
      setLineQuantity,
      removeLine,
      emptyCart,
    }),
    [cart, cartLoading, itemCount, refreshCart, addToCart, setLineQuantity, removeLine, emptyCart]
  )

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) {
    throw new Error('useCart must be used within CartProvider')
  }
  return ctx
}
