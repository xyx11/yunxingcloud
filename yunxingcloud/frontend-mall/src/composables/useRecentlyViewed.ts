import { ref } from 'vue'

interface ViewedProduct {
  id: number
  name: string
  price: number
  imageUrl?: string
  viewedAt: number
}

const STORAGE_KEY = 'mall_recently_viewed'
const MAX_ITEMS = 12

const items = ref<ViewedProduct[]>([])

function load() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    items.value = raw ? JSON.parse(raw) : []
  } catch { items.value = [] }
}

function save() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items.value))
}

export function useRecentlyViewed() {
  if (!items.value.length) load()

  function add(product: { id: number; name: string; price: number; imageUrl?: string }) {
    items.value = [
      { ...product, viewedAt: Date.now() },
      ...items.value.filter(p => p.id !== product.id)
    ].slice(0, MAX_ITEMS)
    save()
  }

  function clear() {
    items.value = []
    localStorage.removeItem(STORAGE_KEY)
  }

  return { items, add, clear }
}