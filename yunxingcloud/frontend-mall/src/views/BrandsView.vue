<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBrands } from '@/api/product'
import LazyImage from '@/components/LazyImage.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const brands = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  loading.value = true
  try { const r = await getBrands(); brands.value = r.data || [] } catch { toast.error(t('brands.loadFail')) } finally { loading.value = false }
})

function goBrand(id: number) { router.push(`/brand/${id}`) }
</script>

<template>
  <div class="brands-page">
    <h1 class="brands-title">{{ t('brands.title') }}</h1>
    <div v-if="loading" class="brands-grid">
      <div v-for="i in 8" :key="i" class="brands-skel">
        <div class="sk-avatar" />
        <div class="sk-line" />
      </div>
    </div>
    <div v-else-if="brands.length" class="brands-grid">
      <div v-for="b in brands" :key="b.id" class="brand-card" role="button" tabindex="0" @click="goBrand(b.id)" @keydown.enter.prevent="goBrand(b.id)" @keydown.space.prevent="goBrand(b.id)">
        <LazyImage :src="b.logo || b.imageUrl || ''" :alt="b.name" height="80px" width="80px" rounded="50%" />
        <div class="brand-name">{{ b.name }}</div>
      </div>
    </div>
    <JdEmpty v-else icon="🏷️" :title="t('brands.empty')" />
  </div>
</template>

<style scoped>
.brands-page { max-width: 1000px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }
.brands-title { font-size: var(--font-xl); font-weight: 700; margin-bottom: var(--space-xl); }
.brands-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-lg); }
.brand-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); text-align: center; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition); }
.brand-card:hover { transform: translateY(-4px); }
.brand-name { font-size: 15px; font-weight: 600; margin-top: var(--space-md); }
.brands-skel { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); text-align: center; box-shadow: var(--shadow-sm); }
.sk-avatar { width: 80px; height: 80px; border-radius: 50%; background: var(--border-light); margin: 0 auto; }
.sk-line { height: 16px; width: 60%; background: var(--border-light); border-radius: var(--radius-sm); margin: var(--space-md) auto 0; }

@media (max-width: 768px) {
  .brands-page { padding: var(--space-lg) var(--space-md) 80px; }
  .brands-grid { grid-template-columns: repeat(3, 1fr); gap: var(--space-md); }
}
</style>
