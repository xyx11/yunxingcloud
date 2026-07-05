/**
 * Format price with thousand separators and optional decimal places.
 * @example formatPrice(1299) => "¥1,299"
 * @example formatPrice(1299.50, 2) => "¥1,299.50"
 * @example formatCount(12345) => "1.2万"
 * @example formatCount(9999) => "9,999"
 */
export function formatPrice(price: number, decimals: number = 0): string {
  return `¥${Number(price).toLocaleString('zh-CN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  })}`
}

export function formatCount(n: number): string {
  if (n >= 10000) return (Math.floor(n / 100) / 100).toFixed(1) + '万'
  if (n >= 1000) return n.toLocaleString('zh-CN')
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
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

export function formatDateTime(date: string | Date | number): string {
  const d = date instanceof Date ? date : new Date(date)
  return d.toLocaleString('zh-CN', {
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
  if (diff < min) return '刚刚'
  if (diff < hour) return Math.floor(diff / min) + '分钟前'
  if (diff < day) return Math.floor(diff / hour) + '小时前'
  if (diff < 2 * day) return '昨天'
  if (diff < 7 * day) return Math.floor(diff / day) + '天前'
  return formatDate(date)
}
