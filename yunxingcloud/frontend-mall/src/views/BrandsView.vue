<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBrands } from '@/api/product'
import LazyImage from '@/components/LazyImage.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import JdButton from '@/components/JdButton.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const brands = ref<any[]>([])
const loading = ref(true)
const loadError = ref(false)
const searchQuery = ref('')
const sortBy = ref<'default' | 'az'>('default')

const filteredBrands = computed(() => {
  let list = [...brands.value]
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(b => b.name?.toLowerCase().includes(q))
  }
  if (sortBy.value === 'az') list.sort((a, b) => (a.name || '').localeCompare(b.name || ''))
  return list
})

// Group brands alphabetically
const groupedBrands = computed(() => {
  const groups: Record<string, any[]> = {}
  filteredBrands.value.forEach(b => {
    const letter = (b.name?.[0] || '#').toUpperCase()
    const key = /[A-Z]/.test(letter) ? letter : '#'
    if (!groups[key]) groups[key] = []
    groups[key].push(b)
  })
  return Object.entries(groups).sort(([a], [b]) => a === '#' ? 1 : b === '#' ? -1 : a.localeCompare(b))
})

onMounted(load)

async function load() {
  loading.value = true
  loadError.value = false
  try {
    const r = await getBrands()
    brands.value = r.data || []
  } catch {
    toast.error(t('brands.loadFail'))
    loadError.value = true
  } finally {
    loading.value = false
  }
}

function goBrand(id: number) { router.push(`/brand/${id}`) }
</script>

<template>
  <div class="brands-page">
    <div class="brands-hero">
      <h1 class="brands-hero-title">{{ t('brands.title') }}</h1>
      <p class="brands-hero-sub">{{ t('brands.heroSub') }}</p>
    </div>

    <!-- Search + Sort -->
    <div class="brands-toolbar">
      <div class="brands-search">
        <input v-model="searchQuery" :placeholder="t('brands.searchPlaceholder')" class="brands-search-input" />
        <span v-if="searchQuery" class="brands-search-clear" @click="searchQuery = ''">✕</span>
      </div>
      <div class="brands-sort">
        <span class="sort-opt" :class="{ active: sortBy === 'default' }" @click="sortBy = 'default'">{{ t('brands.sortDefault') }}</span>
        <span class="sort-opt" :class="{ active: sortBy === 'az' }" @click="sortBy = 'az'">A-Z</span>
      </div>
    </div>

    <SkeletonBox v-if="loading" variant="card" :columns="4" :count="8" height="120px" />

    <template v-else-if="loadError">
      <JdEmpty icon="⚠️" :title="t('brands.loadFail')">
        <JdButton @click="load">{{ t('common.retry') }}</JdButton>
      </JdEmpty>
    </template>

    <template v-else-if="filteredBrands.length">
      <!-- Alphabetical groups -->
      <div v-for="[letter, list] in groupedBrands" :key="letter" class="brands-group">
        <h3 class="brands-letter">{{ letter }}</h3>
        <div class="brands-grid">
          <div v-for="b in list" :key="b.id" class="brand-card" role="button" tabindex="0" @click="goBrand(b.id)" @keydown.enter.prevent="goBrand(b.id)" @keydown.space.prevent="goBrand(b.id)">
            <div class="brand-logo-wrap">
              <LazyImage :src="b.logo || b.imageUrl || ''" :alt="b.name" height="64px" width="64px" rounded="50%" />
            </div>
            <div class="brand-name">{{ b.name }}</div>
            <div v-if="b.productCount" class="brand-count">{{ t('brands.productCount', { n: String(b.productCount || 0) }) }}</div>
          </div>
        </div>
      </div>
    </template>

    <JdEmpty v-else icon="🏷️" :title="searchQuery ? t('brands.notFound') : t('brands.empty')">
      <JdButton v-if="searchQuery" @click="searchQuery = ''">{{ t('brands.clearSearch') }}</JdButton>
    </JdEmpty>
  </div>
</template>

<style scoped>
.brands-page { max-width: 1000px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }
.brands-hero { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; border-radius: var(--radius-xl); padding: var(--space-xxxl); margin-bottom: var(--space-xxl); text-align: center; }
.brands-hero-title { font-size: var(--font-h1); font-weight: 800; margin-bottom: var(--space-sm); }
.brands-hero-sub { font-size: var(--font-md); opacity: .85; }
[data-theme="dark"] .brands-hero { background: linear-gradient(135deg, #4a5ab9, #5c388e); }

.brands-toolbar { display: flex; justify-content: space-between; align-items: center; gap: var(--space-lg); margin-bottom: var(--space-xl); }
.brands-search { position: relative; flex: 1; max-width: 300px; }
.brands-search-input { width: 100%; padding: var(--space-sm) var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-round); font-size: var(--font-base); background: var(--bg-white); color: var(--text-primary); outline: none; box-sizing: border-box; }
.brands-search-input:focus { border-color: var(--jd-red); }
.brands-search-clear { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); cursor: pointer; color: var(--text-tertiary); font-size: 14px; }
.brands-sort { display: flex; gap: var(--space-sm); }
.sort-opt { padding: 4px 14px; border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-sm); color: var(--text-secondary); border: 1px solid var(--border); transition: all var(--transition-fast); }
.sort-opt.active { background: var(--jd-red); color: #fff; border-color: var(--jd-red); }

.brands-group { margin-bottom: var(--space-xxl); }
.brands-letter { font-size: var(--font-lg); font-weight: 700; color: var(--text-primary); margin-bottom: var(--space-md); padding-bottom: var(--space-sm); border-bottom: 2px solid var(--jd-red); display: inline-block; }

.brands-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-md); }
.brand-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); text-align: center; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition), box-shadow var(--transition); }
.brand-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-md); }
.brand-logo-wrap { margin-bottom: var(--space-md); }
.brand-name { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.brand-count { font-size: var(--font-xs); color: var(--text-tertiary); margin-top: 4px; }

@media (max-width: 768px) {
  .brands-page { padding: var(--space-lg) var(--space-md) 80px; }
  .brands-hero { padding: var(--space-xl); }
  .brands-hero-title { font-size: var(--font-xl); }
  .brands-toolbar { flex-direction: column; align-items: stretch; }
  .brands-search { max-width: 100%; }
  .brands-grid { grid-template-columns: repeat(3, 1fr); gap: var(--space-sm); }
}
</style>
