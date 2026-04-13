import type { Product } from '../contexts/ProductContext'
import { Link } from 'react-router-dom'
import { useProducts } from '../contexts/ProductContext'

const currencySymbols: Record<string, string> = {
  XOF: 'FCFA',
  EUR: '€',
  USD: '$',
  GBP: '£',
  JPY: '¥',
  CHF: 'CHF',
  CAD: 'CAD',
  AUD: 'AUD',
  CNY: '¥',
  INR: '₹',
  BRL: 'R$',
  ZAR: 'ZAR',
  NGN: '₦',
  GHS: '₵',
  KES: 'KES',
  MAD: 'MAD',
  TND: 'TND',
  DZD: 'DZD',
}

function excerpt(text: string, max: number) {
  const t = text.replace(/\s+/g, ' ').trim()
  if (t.length <= max) return t
  return `${t.slice(0, max - 1)}…`
}

export default function ProductCard({ p }: { p: Product }) {
  const { incrementViews } = useProducts()
  const currencyDisplay = currencySymbols[p.currency] || p.currency
  const priceDisplay =
    p.currency === 'XOF' ? `${p.price.toFixed(0)} ${currencyDisplay}` : `${p.price.toFixed(2)} ${currencyDisplay}`

  const handleClick = () => {
    incrementViews(p.id)
  }

  const desc = p.description ? excerpt(p.description, 120) : ''
  const metaLine = [p.brand, p.size].filter(Boolean).join(' · ')

  return (
    <Link
      to={`/product/${p.id}`}
      onClick={handleClick}
      className="product-card-link"
    >
      <article className="card product-card">
        <div className="product-card__media">
          <img src={p.image} alt={p.title} className="product-card__img" />
        </div>
        <div className="product-card__body">
          <h3 className="product-card__title">{p.title}</h3>
          {desc ? <p className="product-card__desc">{desc}</p> : null}
          {metaLine ? <p className="product-card__meta">{metaLine}</p> : null}
          <div className="product-card__footer">
            <span className="product-card__price">{priceDisplay}</span>
          </div>
        </div>
      </article>
    </Link>
  )
}
