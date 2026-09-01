import { t, currentLocale } from '@/locales'
import type { Product } from '@/types'

export function getProductImage(p: Product | { imageUrl?: string; images?: string[] }): string {
  return p.imageUrl || p.images?.[0] || ''
}

export function formatPrice(price: number, decimals: number = 0): string {
  const loc = currentLocale.value === 'en' ? 'en-US' : 'zh-CN'
  return `¥${Number(price).toLocaleString(loc, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  })}`
}

export function formatCount(n: number): string {
  if (n >= 100000000) return (n / 100000000).toFixed(1).replace(/\.0$/, '') + t('format.hundredMillion')
  if (n >= 10000) return (n / 10000).toFixed(1).replace(/\.0$/, '') + t('format.tenThousand')
  if (n >= 1000) return n.toLocaleString(currentLocale.value === 'en' ? 'en-US' : 'zh-CN')
  return String(n)
}

export function formatRating(rating: number): string {
  return rating.toFixed(1)
}

export function formatPercent(n: number, total: number): string {
  if (total === 0) return '0%'
  return Math.round((n / total) * 100) + '%'
}

export function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / (1024 * 1024)).toFixed(1) + 'MB'
}

export function formatDate(date: string | Date | number): string {
  const d = date instanceof Date ? date : new Date(date)
  const loc = currentLocale.value === 'en' ? 'en-US' : 'zh-CN'
  return d.toLocaleDateString(loc, { year: 'numeric', month: '2-digit', day: '2-digit' })
}

export function formatDateTime(date: string | Date | number): string {
  const d = date instanceof Date ? date : new Date(date)
  const loc = currentLocale.value === 'en' ? 'en-US' : 'zh-CN'
  return d.toLocaleString(loc, {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

export function formatRelativeTime(date: string | Date | number | number): string {
  const now = Date.now()
  const then = date instanceof Date ? date.getTime() : new Date(date).getTime()
  if (isNaN(then)) return String(date)
  const diff = now - then
  const min = 60 * 1000
  const hour = 60 * min
  const day = 24 * hour
  if (diff < min) return t('format.justNow')
  if (diff < hour) return t('format.minutesAgo', { n: Math.floor(diff / min) })
  if (diff < day) return t('format.hoursAgo', { n: Math.floor(diff / hour) })
  if (diff < 2 * day) return t('format.yesterday')
  if (diff < 7 * day) return t('format.daysAgo', { n: Math.floor(diff / day) })
  return formatDate(date)
}

export function formatLargeNumber(n: number): string {
  if (n >= 100000000) return (n / 100000000).toFixed(1).replace(/\.0$/, '') + t('format.hundredMillion')
  if (n >= 10000) return (n / 10000).toFixed(1).replace(/\.0$/, '') + t('format.tenThousand')
  return String(n)
}

export function formatDuration(ms: number): string {
  if (ms < 60000) return t('format.second', { n: Math.floor(ms / 1000) })
  if (ms < 3600000) return t('format.minute', { n: Math.floor(ms / 60000) })
  if (ms < 86400000) return t('format.hour', { n: Math.floor(ms / 3600000) })
  return t('format.day', { n: Math.floor(ms / 86400000) })
}

export function formatStock(stock: number): { text: string; urgent: boolean } {
  if (stock <= 0) return { text: t('format.soldOut'), urgent: true }
  if (stock <= 5) return { text: t('format.stockLeft', { n: stock }), urgent: true }
  if (stock <= 20) return { text: t('format.stockCount', { n: stock }), urgent: false }
  return { text: t('format.stockSufficient'), urgent: false }
}

export function formatOrderNo(no: string): string {
  if (no.length > 16) return no.substring(0, 4) + '...' + no.substring(no.length - 4)
  return no
}