<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { formatPrice } from '@/utils/format'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import { useRecentlyViewed } from '@/composables/useRecentlyViewed'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import type { ViewedProduct } from '@/types'

const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const { items, remove, clear } = useRecentlyViewed()
const batchAddLoading = ref(false)

const sortBy = ref<'newest' | 'oldest' | 'priceAsc' | 'priceDesc'>('newest')

const sortedItems = computed(() => {
  const list = [...items.value]
  switch (sortBy.value) {
    case 'oldest': return list.reverse()
    case 'priceAsc': return list.sort((a, b) => a.price - b.price)
    case 'priceDesc': return list.sort((a, b) => b.price - a.price)
    default: return list
  }
})

const groups = computed(() => {
  const day = 86400000
  const todayStart = new Date().setHours(0, 0, 0, 0)
  const yesterdayStart = todayStart - day

  const today: ViewedProduct[] = []
  const yesterday: ViewedProduct[] = []
  const earlier: ViewedProduct[] = []

  for (const p of sortedItems.value) {
    const t = typeof p.viewedAt === 'string' ? new Date(p.viewedAt).getTime() : p.viewedAt
    if (t >= todayStart) today.push(p)
    else if (t >= yesterdayStart) yesterday.push(p)
    else earlier.push(p)
  }

  const result: { label: string; items: ViewedProduct[] }[] = []
  if (today.length) result.push({ label: t('recent.today'), items: today })
  if (yesterday.length) result.push({ label: t('recent.yesterday'), items: yesterday })
  if (earlier.length) result.push({ label: t('recent.earlier'), items: earlier })
  return result
})

const sortOptions = [
  { key: 'newest' as const, label: t('recent.sortNewest') },
  { key: 'oldest' as const, label: t('recent.sortOldest') },
  { key: 'priceAsc' as const, label: t('recent.sortPriceAsc') },
  { key: 'priceDesc' as const, label: t('recent.sortPriceDesc') },
]

function goDetail(id: number) { router.push(`/product/${id}`) }
function removeItem(e: Event, id: number) { e.stopPropagation(); remove(id) }

async function quickAdd(e: Event, p: ViewedProduct) {
  e.stopPropagation()
  try { await addToCart(p.id, 1); toast.success(t('toast.addedToCart')) }
  catch { toast.error(t('toast.addCartFail')) }
}

async function batchAddAll() {
  batchAddLoading.value = true
  let success = 0
  for (const p of items.value) {
    try { await addToCart(p.id, 1); success++ } catch { /* skip */ }
  }
  if (success > 0) toast.success(t('recent.batchAddDone', { n: success }))
  else toast.error(t('toast.addCartFail'))
  batchAddLoading.value = false
}

function formatTime(ts: string | number): string {
  const d = new Date(typeof ts === 'string' ? ts : ts)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <div class="rc-page">
    <!-- Header -->
    <div class="rc-header">
      <div class="rc-header-left">
        <h2 class="rc-title">🕐 {{ t('recent.title') }}</h2>
        <span v-if="items.length" class="rc-count">{{ t('recent.count', { n: items.length }) }}</span>
      </div>
      <div class="rc-header-right">
        <select v-if="items.length > 1" v-model="sortBy" class="rc-sort">
          <option v-for="o in sortOptions" :key="o.key" :value="o.key">{{ o.label }}</option>
        </select>
        <JdButton
          v-if="items.length > 1"
          size="sm"
          type="outline"
          :loading="batchAddLoading"
          @click="batchAddAll"
        >
          🛒 {{ t('recent.batchAddCart') }}
        </JdButton>
        <button v-if="items.length" class="rc-clear" @click="clear">
          {{ t('recent.clear') }}
        </button>
      </div>
    </div>

    <!-- Grouped product list -->
    <template v-if="items.length">
      <div v-for="group in groups" :key="group.label" class="rc-group">
        <h3 class="rc-group-label">{{ group.label }}</h3>
        <div class="rc-grid">
          <div
            v-for="p in group.items" :key="p.id"
            class="rc-card"
            role="button"
            tabindex="0"
            @click="goDetail(p.id)"
            @keydown.enter.prevent="goDetail(p.id)"
            @keydown.space.prevent="goDetail(p.id)"
          >
            <button
              class="rc-remove"
              @click="(e: Event) => removeItem(e, p.id)"
              :aria-label="t('recent.remove')"
            >&times;</button>
            <LazyImage :src="p.imageUrl || ''" :alt="p.name" height="180px" />
            <div class="rc-info">
              <h4 class="rc-name">{{ p.name }}</h4>
              <div class="rc-bottom">
                <div>
                  <span class="rc-price">{{ formatPrice(p.price / 100, 2) }}</span>
                  <div class="rc-time">{{ formatTime(p.viewedAt) }}</div>
                </div>
                <button
                  class="rc-add"
                  @click="(e: Event) => quickAdd(e, p)"
                  :aria-label="t('product.addToCart')"
                >+</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- Empty -->
    <JdEmpty v-else icon="🕐" :title="t('recent.empty')" :description="t('recent.emptyDesc')">
      <JdButton @click="router.push('/')">{{ t('recent.goBrowse') }}</JdButton>
    </JdEmpty>
  </div>
</template>

<style scoped>
.rc-page { max-width: 900px; margin: 0 auto; }

/* Header */
.rc-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: var(--space-xl); flex-wrap: wrap; gap: var(--space-sm);
}
.rc-header-left { display: flex; align-items: baseline; gap: var(--space-sm); }
.rc-title { font-size: var(--font-xl); font-weight: 700; }
.rc-count { color: var(--text-tertiary); font-size: var(--font-sm); }
.rc-header-right { display: flex; align-items: center; gap: var(--space-sm); }

.rc-sort {
  padding: 5px 10px; border: 1px solid var(--border); border-radius: var(--radius-sm);
  background: var(--bg-white); color: var(--text-primary); font-size: var(--font-sm);
  cursor: pointer; outline: none;
}
.rc-sort:focus { border-color: var(--jd-red); }

.rc-clear {
  padding: 5px 14px; border: 1px solid var(--border); background: var(--bg-white);
  border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-sm);
  color: var(--text-tertiary); transition: all var(--transition-fast);
}
.rc-clear:hover { border-color: var(--jd-red); color: var(--jd-red); }

/* Groups */
.rc-group { margin-bottom: var(--space-xl); }
.rc-group-label {
  font-size: var(--font-md); font-weight: 600; color: var(--text-secondary);
  margin-bottom: var(--space-md); padding-left: var(--space-xs);
  border-left: 3px solid var(--jd-red);
}

/* Grid */
.rc-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }

.rc-card {
  background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden;
  cursor: pointer; transition: transform var(--transition); box-shadow: var(--shadow-sm);
  position: relative;
}
.rc-card:hover { transform: translateY(-4px); }
.rc-remove {
  position: absolute; top: 6px; right: 6px; width: 22px; height: 22px;
  border-radius: 50%; background: rgba(0,0,0,.4); color: #fff; border: none;
  cursor: pointer; font-size: 14px; line-height: 1; display: flex;
  align-items: center; justify-content: center; opacity: 0;
  transition: opacity var(--transition-fast); z-index: 1;
}
.rc-card:hover .rc-remove { opacity: 1; }
.rc-info { padding: 12px; }
.rc-name { font-size: var(--font-md); margin-bottom: 6px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rc-bottom { display: flex; align-items: center; justify-content: space-between; }
.rc-price { color: var(--jd-red); font-size: 16px; font-weight: 700; }
.rc-time { font-size: 10px; color: var(--text-placeholder); margin-top: 2px; }
.rc-add {
  width: 28px; height: 28px; border-radius: 50%; border: 2px solid var(--jd-red);
  background: var(--bg-white); color: var(--jd-red); cursor: pointer; font-size: 14px;
  display: flex; align-items: center; justify-content: center; transition: all var(--transition-fast);
}
.rc-add:hover { background: var(--jd-red); color: #fff; }

@media (max-width: 768px) {
  .rc-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>