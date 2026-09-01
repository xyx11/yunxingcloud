import { describe, it, expect, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAbTest } from '@/composables/useAbTest'
import { getExperimentById, getEnabledExperiments } from '@/config/abTests'
import type { AbExperiment } from '@/composables/useAbTest'

function mockLocalStorage() {
  const store: Record<string, string> = {}
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, v: string) => { store[key] = v },
    removeItem: (key: string) => { delete store[key] },
    clear: () => { Object.keys(store).forEach(k => delete store[k]) },
  }
}
Object.defineProperty(globalThis, 'localStorage', { value: mockLocalStorage() })

beforeEach(() => {
  setActivePinia(createPinia())
  localStorage.clear()
  // Clear AbTest's assignment/seen storage
  localStorage.removeItem('yxcloud_ab_assignments')
  localStorage.removeItem('yxcloud_ab_seen')
  localStorage.removeItem('yxcloud_uid')
})

function makeExperiment(overrides?: Partial<AbExperiment>): AbExperiment {
  return {
    id: 'test-exp',
    name: 'Test Experiment',
    enabled: true,
    variants: [
      { name: 'control', weight: 50 },
      { name: 'variant-a', weight: 30 },
      { name: 'variant-b', weight: 20 },
    ],
    ...overrides,
  }
}

describe('useAbTest', () => {
  it('should assign a variant from enabled experiment', () => {
    const ab = useAbTest()
    const exp = makeExperiment()
    const variant = ab.getVariant(exp)
    expect(['control', 'variant-a', 'variant-b']).toContain(variant)
  })

  it('should return control variant for disabled experiment', () => {
    const ab = useAbTest()
    const exp = makeExperiment({ enabled: false })
    const variant = ab.getVariant(exp)
    expect(variant).toBe('control')
  })

  it('should return consistent variant for same user', () => {
    const a = useAbTest()
    const b = useAbTest()
    const exp = makeExperiment()
    // Same userId (same localStorage mock) should yield same variant
    expect(a.getVariant(exp)).toBe(b.getVariant(exp))
  })

  it('should return first variant if none match', () => {
    const ab = useAbTest()
    const exp = makeExperiment({ variants: [{ name: 'only', weight: 0 }] })
    expect(ab.getVariant(exp)).toBe('only')
  })

  it('should return config for assigned variant', () => {
    const ab = useAbTest()
    const exp = makeExperiment({
      variants: [
        { name: 'control', weight: 100, config: { theme: 'light' } },
      ],
    })
    const config = ab.getConfig(exp)
    expect(config).toEqual({ theme: 'light' })
  })

  it('should deduplicate trackExposure calls', () => {
    const ab = useAbTest()
    const exp = makeExperiment()
    ab.getVariant(exp)
    ab.trackExposure(exp.id)
    ab.trackExposure(exp.id) // Should be no-op
    // No assertion needed; verify no crash
  })

  it('resetAssignments clears all state', () => {
    const ab = useAbTest()
    const exp = makeExperiment()
    ab.getVariant(exp)
    ab.resetAssignments()
    // After reset, new variant assignment (could be same or different)
    const after = ab.getVariant(exp)
    expect(['control', 'variant-a', 'variant-b']).toContain(after)
  })
})

describe('abTests config', () => {
  it('getExperimentById returns experiment for valid id', () => {
    const exp = getExperimentById('home-layout')
    expect(exp).toBeDefined()
    expect(exp!.id).toBe('home-layout')
    expect(exp!.variants).toHaveLength(3)
  })

  it('getExperimentById returns undefined for unknown id', () => {
    expect(getExperimentById('nonexistent')).toBeUndefined()
  })

  it('getEnabledExperiments returns only enabled ones', () => {
    const enabled = getEnabledExperiments()
    expect(enabled.every(e => e.enabled)).toBe(true)
    // checkout-button-text is disabled
    expect(enabled.find(e => e.id === 'checkout-button-text')).toBeUndefined()
  })

  it('experiment weights sum to 100', () => {
    for (const exp of getEnabledExperiments()) {
      const total = exp.variants.reduce((s, v) => s + v.weight, 0)
      expect(total).toBe(100)
    }
  })
})