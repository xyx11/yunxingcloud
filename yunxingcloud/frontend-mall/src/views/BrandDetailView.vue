<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProducts } from '@/api/product'
import { formatPrice } from '@/utils/format'
import LazyImage from '@/components/LazyImage.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import JdButton from '@/components/JdButton.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const products = ref<any[]>([])
const loading = ref(true)
const brandId = computed(() => Number(route.params.id))

async function load() {
  loading.value = true
  try { const r = await getProducts({ brandId: brandId.value, size: 50 }); products.value = r.data?.content || r.data || [] } catch { toast.error(t('brandDetail.loadFail')) } finally { loading.value = false }
}

function goDetail(id: number) { router.push(`/product/${id}`) }

onMounted(load)
</script>

<template>
  <div class="brand-detail-page">
    <div class="bd-back"><button class="back-btn" @click="router.back()">{{ t('brandDetail.back') }}</button></div>
    <h2 class="bd-title">{{ t('brandDetail.title') }}</h2>

    <div v-if="loading" class="bd-grid">
      <div v-for="i in 6" :key="i" class="bd-skel"><div class="sk-img" /><div class="sk-line" /></div>
    </div>

    <div v-else-if="products.length" class="bd-grid">
      <div v-for="p in products" :key="p.id" class="bd-card" role="button" tabindex="0" @click="goDetail(p.id)" @keydown.enter.prevent="goDetail(p.id)" @keydown.space.prevent="goDetail(p.id)">
        <LazyImage :src="p.imageUrl || ''" :alt="p.name" height="180px" />
        <div class="bd-info">
          <h4 class="bd-name">{{ p.name }}</h4>
          <span class="bd-price">{{ formatPrice(p.price / 100, 2) }}</span>
        </div>
      </div>
    </div>

    <JdEmpty v-else icon="🏷️" :title="t('brandDetail.empty')">
      <JdButton @click="router.push('/brands')">{{ t('brandDetail.viewAllBrands') }}</JdButton>
    </JdEmpty>
  </div>
</template>

<style scoped>
.brand-detail-page { max-width: 1000px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }
.bd-back { margin-bottom: var(--space-lg); }
.back-btn { background: none; border: none; color: var(--text-secondary); cursor: pointer; font-size: var(--font-md); }
.bd-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-xl); }
.bd-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-lg); }
.bd-card { background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition); }
.bd-card:hover { transform: translateY(-4px); }
.bd-info { padding: var(--space-md); }
.bd-name { font-size: 14px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 4px; }
.bd-price { color: var(--jd-red); font-size: var(--font-lg); font-weight: 700; }
.bd-skel { background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow-sm); }
.sk-img { height: 180px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; }
.sk-line { height: 16px; width: 60%; background: var(--border-light); border-radius: var(--radius-sm); margin: var(--space-md); }
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

@media (max-width: 768px) {
  .brand-detail-page { padding: var(--space-lg) var(--space-md) 80px; }
  .bd-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-md); }
}
</style>
