import { Apple, BookOpen, Home, Laptop, Shirt, type LucideIcon } from 'lucide-react'
import type { CategoryIconKey } from '../../contexts/ProductContext'
import { iconSm } from './iconProps'

const MAP: Record<CategoryIconKey, LucideIcon> = {
  home: Home,
  shirt: Shirt,
  laptop: Laptop,
  apple: Apple,
  books: BookOpen,
}

type Props = {
  name: CategoryIconKey
  className?: string
  size?: number
}

export default function CategoryIcon({ name, className, size }: Props) {
  const Icon = MAP[name]
  return <Icon {...iconSm} size={size ?? iconSm.size} className={className} aria-hidden />
}
