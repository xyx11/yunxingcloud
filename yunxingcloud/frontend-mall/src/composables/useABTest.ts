/**
 * A/B 测试分流 — 基于 userId 一致性哈希
 * 同一用户始终看到同一变体 (persistent across sessions)
 */
const STORAGE_KEY = 'mall_ab_assignments'

function getUserId(): string {
  let uid = localStorage.getItem('mall_user_id')
  if (!uid) {
    uid = 'u_' + Math.random().toString(36).substring(2, 10) + Date.now().toString(36)
    localStorage.setItem('mall_user_id', uid)
  }
  return uid
}

function loadAssignments(): Record<string, string> {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}') } catch { return {} }
}

export function useABTest() {
  const userId = getUserId()

  function assign(testName: string, variants: string[]): string {
    if (variants.length < 2) return variants[0] || 'control'
    const saved = loadAssignments()
    if (saved[testName]) return saved[testName]
    const hash = Math.abs(hashCode(userId + testName))
    const variant = variants[hash % variants.length]
    saved[testName] = variant
    localStorage.setItem(STORAGE_KEY, JSON.stringify(saved))
    return variant
  }

  function track(testName: string, variant: string) {
    console.log(`[AB] ${testName} → ${variant}`)
  }

  return { assign, track }
}

function hashCode(s: string): number {
  let h = 0
  for (let i = 0; i < s.length; i++) {
    h = ((h << 5) - h + s.charCodeAt(i)) | 0
  }
  return Math.abs(h)
}