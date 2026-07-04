/**
 * Format number for display.
 * @example formatPrice(1299) => "¥1,299"
 * @example formatCount(12345) => "1.2万"
 * @example formatCount(9999) => "9,999"
 */
export function formatPrice(price: number): string {
  return `¥${price.toLocaleString('zh-CN')}`
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

export function formatDate(date: string | Date): string {
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

export function formatDateTime(date: string | Date): string {
  const d = typeof date === 'string' ? new Date(date) : date
  return d.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}
