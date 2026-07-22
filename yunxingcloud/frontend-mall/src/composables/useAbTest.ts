import { ref } from 'vue'

export interface AbVariant {
  name: string
  weight: number       // 0-100, all variants should sum to 100
  config?: Record<string, unknown>
}

export interface AbExperiment {
  id: string
  name: string
  description?: string
  enabled: boolean
  variants: AbVariant[]
}

interface AbAssignment {
  variant: string
  assignedAt: number
}

const STORAGE_KEY = 'yxcloud_ab_assignments'
const SEEN_KEY = 'yxcloud_ab_seen'

function hashCode(str: string): number {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = ((hash << 5) - hash) + str.charCodeAt(i)
    hash |= 0
  }
  return Math.abs(hash)
}

function getUserId(): string {
  try {
    let uid = localStorage.getItem('yxcloud_uid')
    if (!uid) { uid = 'u_' + Date.now().toString(36) + Math.random().toString(36).slice(2, 8); localStorage.setItem('yxcloud_uid', uid) }
    return uid
  } catch { return 'fallback_' + Date.now() }
}

function loadAssignments(): Record<string, AbAssignment> {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}') } catch { return {} }
}

function saveAssignments(assignments: Record<string, AbAssignment>) {
  try { localStorage.setItem(STORAGE_KEY, JSON.stringify(assignments)) } catch {}
}

function loadSeen(): Set<string> {
  try { return new Set(JSON.parse(localStorage.getItem(SEEN_KEY) || '[]')) } catch { return new Set() }
}

function saveSeen(seen: Set<string>) {
  try { localStorage.setItem(SEEN_KEY, JSON.stringify([...seen])) } catch {}
}

function assignVariant(experiment: AbExperiment, userId: string): string {
  const hash = hashCode(experiment.id + userId)
  const bucket = hash % 100
  let cumulative = 0
  for (const v of experiment.variants) {
    cumulative += v.weight
    if (bucket < cumulative) return v.name
  }
  return experiment.variants[0]?.name || 'control'
}

export function useAbTest() {
  const userId = getUserId()
  const assignments = ref<Record<string, AbAssignment>>(loadAssignments())
  const seen = ref<Set<string>>(loadSeen())

  function getVariant(experiment: AbExperiment): string {
    if (!experiment.enabled) return experiment.variants[0]?.name || 'control'

    // Check existing assignment
    if (assignments.value[experiment.id]) {
      const existing = assignments.value[experiment.id].variant
      // Verify variant still exists
      if (experiment.variants.some(v => v.name === existing)) return existing
    }

    // New assignment
    const variant = assignVariant(experiment, userId)
    assignments.value = { ...assignments.value, [experiment.id]: { variant, assignedAt: Date.now() } }
    saveAssignments(assignments.value)
    return variant
  }

  function getConfig(experiment: AbExperiment): Record<string, unknown> {
    const variant = getVariant(experiment)
    return experiment.variants.find(v => v.name === variant)?.config || {}
  }

  function trackExposure(experimentId: string) {
    if (seen.value.has(experimentId)) return
    const s = new Set(seen.value)
    s.add(experimentId)
    seen.value = s
    saveSeen(s)

    const variant = assignments.value[experimentId]?.variant || 'unknown'
    // Fire-and-forget analytics beacon
    try {
      const data = JSON.stringify({ type: 'ab_exposure', experimentId, variant, userId, timestamp: Date.now() })
      if (navigator.sendBeacon) {
        navigator.sendBeacon('/api/analytics/ab-event', new Blob([data], { type: 'application/json' }))
      } else {
        fetch('/api/analytics/ab-event', { method: 'POST', body: data, headers: { 'Content-Type': 'application/json' }, keepalive: true }).catch(() => {})
      }
    } catch {}
  }

  function trackClick(experimentId: string, elementId: string) {
    try {
      const variant = assignments.value[experimentId]?.variant || 'unknown'
      const data = JSON.stringify({ type: 'ab_click', experimentId, variant, elementId, userId, timestamp: Date.now() })
      if (navigator.sendBeacon) {
        navigator.sendBeacon('/api/analytics/ab-event', new Blob([data], { type: 'application/json' }))
      } else {
        fetch('/api/analytics/ab-event', { method: 'POST', body: data, headers: { 'Content-Type': 'application/json' }, keepalive: true }).catch(() => {})
      }
    } catch {}
  }

  function resetAssignments() {
    assignments.value = {}
    seen.value = new Set()
    localStorage.removeItem(STORAGE_KEY)
    localStorage.removeItem(SEEN_KEY)
  }

  return { getVariant, getConfig, trackExposure, trackClick, resetAssignments, userId }
}
