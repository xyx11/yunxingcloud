import { ref } from 'vue'
import { getCompareList, addCompare, removeCompare, clearCompare } from '@/api/compare'
import { useAuthStore } from '@/stores/auth'

const MAX_COMPARE = 3
const STORAGE_KEY = 'mall_compare_list'

interface CompareItem { id: number; name: string; price: number; imageUrl?: string; sales?: number; description?: string }

const items = ref<CompareItem[]>([])
const overflowCount = ref(0)
let inited = false

function loadLocal(): CompareItem[] {
  try { const raw = localStorage.getItem(STORAGE_KEY); return raw ? JSON.parse(raw) : [] } catch { return [] }
}

function saveLocal() { localStorage.setItem(STORAGE_KEY, JSON.stringify(items.value)) }

function isAuthenticated() { return useAuthStore().isLoggedIn }

async function syncFromServer() {
  if (!isAuthenticated()) return
  try {
    const r = await getCompareList()
    const serverItems: CompareItem[] = r.data || []
    if (serverItems.length) {
      const localItems = loadLocal()
      const merged = [...serverItems]
      for (const li of localItems) {
        if (!merged.find(mi => mi.id === li.id)) merged.push(li)
      }
      items.value = merged.slice(0, MAX_COMPARE)
    } else {
      items.value = loadLocal()
    }
    saveLocal()
  } catch { items.value = loadLocal() }
}

export function useCompare() {
  if (!items.value.length && !inited) {
    inited = true
    syncFromServer()
  }

  function toggle(p: CompareItem): boolean {
    const idx = items.value.findIndex(i => i.id === p.id)
    if (idx >= 0) {
      items.value.splice(idx, 1)
      saveLocal()
      if (isAuthenticated()) removeCompare(p.id).catch(() => {})
      return false
    }
    if (items.value.length >= MAX_COMPARE) { items.value.shift(); overflowCount.value++ }
    items.value.push(p)
    saveLocal()
    if (isAuthenticated()) addCompare(p.id).catch(() => {})
    return true
  }

  function remove(id: number) {
    items.value = items.value.filter(i => i.id !== id)
    saveLocal()
    if (isAuthenticated()) removeCompare(id).catch(() => {})
  }

  function clear() {
    items.value = []
    localStorage.removeItem(STORAGE_KEY)
    if (isAuthenticated()) clearCompare().catch(() => {})
  }

  function isSelected(id: number) { return items.value.some(i => i.id === id) }

  return { items, toggle, remove, clear, isSelected, overflowCount }
}
