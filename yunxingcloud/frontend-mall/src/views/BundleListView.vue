<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBundles } from '@/api/bundle'
import { formatPrice } from '@/utils/format'
import LazyImage from '@/components/LazyImage.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const bundles = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  try { const r = await getBundles(); bundles.value = r.data || [] } catch { toast.error(t('bundle.loadFail')) } finally { loading.value = false }
})

function goDetail(id: number) { router.push(`/bundle/${id}`) }
</script>

<template>
  <div class="bundles-page">
    <div class="bundles-hero">
      <h1>{{ t('bundle.title') }}</h1>
      <p>{{ t('bundle.subtitle') }}</p>
    </div>
    <div v-if="loading" class="bundles-grid">
      <div v-for="i in 6" :key="i" class="bskel"><div class="sk-img" /><div class="sk-line" /></div>
    </div>
    <div v-else-if="bundles.length" class="bundles-grid">
      <div v-for="b in bundles" :key="b.id" class="bundle-card" role="button" tabindex="0" @click="goDetail(b.id)" @keydown.enter.prevent="goDetail(b.id)" @keydown.space.prevent="goDetail(b.id)">
        <LazyImage :src="b.imageUrl || ''" :alt="b.name" height="180px" />
        <div class="bundle-info">
          <h3 class="bundle-name">{{ b.name }}</h3>
          <div class="bundle-prices">
            <span class="bundle-price">{{ formatPrice((b.bundlePrice || b.price || 0) / 100, 2) }}</span>
            <span v-if="b.originalPrice" class="bundle-original">{{ formatPrice(b.originalPrice / 100) }}</span>
          </div>
          <span v-if="b.originalPrice && b.originalPrice > (b.bundlePrice || b.price || 0)" class="bundle-tag">{{ t('bundle.save') }} ¥{{ formatPrice(((b.originalPrice || 0) - (b.bundlePrice || b.price || 0)) / 100) }}</span>
        </div>
      </div>
    </div>
    <JdEmpty v-else icon="🎁" :title="t('bundle.empty')" />
  </div>
</template>

<style scoped>
.bundles-page { max-width: 1000px; margin: 0 auto; }
.bundles-hero { background: linear-gradient(135deg, #f093fb, #f5576c); color: #fff; border-radius: var(--radius-xl); padding: var(--space-xxxl); margin-bottom: var(--space-xxl); text-align: center; }
.bundles-hero h1 { font-size: var(--font-h1); font-weight: 800; margin-bottom: var(--space-sm); }
.bundles-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-lg); }
.bundle-card { background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition); }
.bundle-card:hover { transform: translateY(-4px); }
.bundle-info { padding: var(--space-lg); }
.bundle-name { font-size: 15px; font-weight: 600; margin-bottom: var(--space-sm); }
.bundle-prices { display: flex; gap: var(--space-sm); align-items: baseline; }
.bundle-price { color: var(--jd-red); font-size: var(--font-xxl); font-weight: 700; }
.bundle-original { color: var(--text-tertiary); font-size: var(--font-sm); text-decoration: line-through; }
.bundle-tag { display: inline-block; margin-top: var(--space-xs); padding: 2px 8px; background: var(--jd-red-light); color: var(--jd-red); border-radius: var(--radius-sm); font-size: 12px; }
.bskel { background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden; }
.sk-img { height: 180px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; }
.sk-line { height: 16px; width: 60%; background: var(--border-light); border-radius: var(--radius-sm); margin: var(--space-lg); }
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

@media (max-width: 768px) {
  .bundles-page { padding: 0 var(--space-md) 80px; }
  .bundles-hero { padding: var(--space-xl); }
  .bundles-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-md); }
}
</style>
