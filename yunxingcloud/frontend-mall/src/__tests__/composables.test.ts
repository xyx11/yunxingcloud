import { describe, it, expect, beforeEach } from 'vitest'
import { useCompare } from '@/composables/useCompare'
import { useRecentlyViewed } from '@/composables/useRecentlyViewed'

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

describe('useCompare', () => {
  beforeEach(() => {
    localStorage.clear()
    const c = useCompare()
    c.clear()
  })

  it('initializes with empty items', () => {
    const { items } = useCompare()
    expect(items.value).toEqual([])
  })

  it('adds item via toggle', () => {
    const { toggle, items } = useCompare()
    toggle({ id: 1, name: 'Product A', price: 99 })
    expect(items.value).toHaveLength(1)
    expect(items.value[0].name).toBe('Product A')
  })

  it('removes item on second toggle', () => {
    const { toggle, items } = useCompare()
    toggle({ id: 1, name: 'A', price: 10 })
    toggle({ id: 1, name: 'A', price: 10 })
    expect(items.value).toHaveLength(0)
  })

  it('returns true/false from toggle for add/remove', () => {
    const { toggle } = useCompare()
    expect(toggle({ id: 1, name: 'A', price: 10 })).toBe(true)
    expect(toggle({ id: 1, name: 'A', price: 10 })).toBe(false)
  })

  it('limits to MAX_COMPARE (3)', () => {
    const { toggle, items, overflowCount } = useCompare()
    toggle({ id: 1, name: 'A', price: 1 })
    toggle({ id: 2, name: 'B', price: 2 })
    toggle({ id: 3, name: 'C', price: 3 })
    toggle({ id: 4, name: 'D', price: 4 })
    expect(items.value).toHaveLength(3)
    expect(items.value[0].id).toBe(2) // oldest shifted out
    expect(items.value[2].id).toBe(4)
  })

  it('isSelected returns correct state', () => {
    const { toggle, isSelected } = useCompare()
    toggle({ id: 1, name: 'A', price: 10 })
    expect(isSelected(1)).toBe(true)
    expect(isSelected(999)).toBe(false)
  })

  it('remove deletes specific item', () => {
    const { toggle, remove, items } = useCompare()
    toggle({ id: 1, name: 'A', price: 1 })
    toggle({ id: 2, name: 'B', price: 2 })
    remove(1)
    expect(items.value).toHaveLength(1)
    expect(items.value[0].id).toBe(2)
  })

  it('clear empties everything', () => {
    const { toggle, clear, items } = useCompare()
    toggle({ id: 1, name: 'A', price: 1 })
    clear()
    expect(items.value).toEqual([])
  })
})

describe('useRecentlyViewed', () => {
  beforeEach(() => {
    localStorage.clear()
    const v = useRecentlyViewed()
    v.clear()
  })

  it('initializes with empty items', () => {
    const { items } = useRecentlyViewed()
    expect(items.value).toEqual([])
  })

  it('adds product to front', () => {
    const { add, items } = useRecentlyViewed()
    add({ id: 1, name: 'A', price: 10 })
    expect(items.value).toHaveLength(1)
    expect(items.value[0].name).toBe('A')
  })

  it('deduplicates by product id', () => {
    const { add, items } = useRecentlyViewed()
    add({ id: 1, name: 'A', price: 10 })
    add({ id: 1, name: 'A updated', price: 15 })
    expect(items.value).toHaveLength(1)
    expect(items.value[0].name).toBe('A updated')
  })

  it('moves re-added item to front', () => {
    const { add, items } = useRecentlyViewed()
    add({ id: 1, name: 'A', price: 10 })
    add({ id: 2, name: 'B', price: 20 })
    add({ id: 1, name: 'A again', price: 10 })
    expect(items.value).toHaveLength(2)
    expect(items.value[0].id).toBe(1)
  })

  it('limits to MAX_ITEMS (12)', () => {
    const { add, items } = useRecentlyViewed()
    for (let i = 1; i <= 15; i++) add({ id: i, name: `P${i}`, price: i })
    expect(items.value).toHaveLength(12)
    expect(items.value[0].id).toBe(15) // newest first
  })

  it('sets viewedAt timestamp', () => {
    const { add, items } = useRecentlyViewed()
    const before = Date.now()
    add({ id: 1, name: 'A', price: 10 })
    expect(items.value[0].viewedAt).toBeGreaterThanOrEqual(before)
  })

  it('clear removes all', () => {
    const { add, clear, items } = useRecentlyViewed()
    add({ id: 1, name: 'A', price: 10 })
    clear()
    expect(items.value).toEqual([])
  })
})
