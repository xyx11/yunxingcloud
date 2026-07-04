import { ref } from 'vue'

const MAX_COMPARE = 3
const STORAGE_KEY = 'mall_compare_list'

interface CompareItem { id: number; name: string; price: number; imageUrl?: string; sales?: number; description?: string }

const items = ref<CompareItem[]>([])
const overflowCount = ref(0)

function load() {
  try { const raw = localStorage.getItem(STORAGE_KEY); items.value = raw ? JSON.parse(raw) : [] } catch { items.value = [] }
}

function save() { localStorage.setItem(STORAGE_KEY, JSON.stringify(items.value)) }

export function useCompare() {
  if (!items.value.length) load()

  function toggle(p: CompareItem): boolean {
    const idx = items.value.findIndex(i => i.id === p.id)
    if (idx >= 0) { items.value.splice(idx, 1); save(); return false }
    if (items.value.length >= MAX_COMPARE) { items.value.shift(); overflowCount.value++ }
    items.value.push(p); save(); return true
  }

  function remove(id: number) { items.value = items.value.filter(i => i.id !== id); save() }
  function clear() { items.value = []; localStorage.removeItem(STORAGE_KEY) }
  function isSelected(id: number) { return items.value.some(i => i.id === id) }

  return { items, toggle, remove, clear, isSelected, overflowCount }
}