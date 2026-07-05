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

export function formatFileSize(bytes: number): string {
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / (1024 * 1024)).toFixed(1) + 'MB'
}
