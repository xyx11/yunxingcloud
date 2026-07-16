<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPresales, payDeposit } from '@/api/presale'
import { formatPrice } from '@/utils/format'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import CountdownTimer from '@/components/CountdownTimer.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const presales = ref<any[]>([])
const loading = ref(true)
const paying = ref<Set<number>>(new Set())

async function load() {
  loading.value = true
  try { const r = await getPresales(); presales.value = (r.data || r.data?.content || []) } catch { toast.error(t('presale.loadFail')) } finally { loading.value = false }
}

async function doDeposit(id: number) {
  if (paying.value.has(id)) return
  paying.value.add(id)
  try { await payDeposit(id); toast.success(t('presale.depositSuccess')); load() } catch { toast.error(t('presale.depositFail')) } finally { paying.value.delete(id) }
}

function goDetail(id: number) { router.push(`/presale/${id}`) }

onMounted(load)
</script>

<template>
  <div class="presale-page">
    <div class="presale-hero">
      <h1 class="presale-hero-title">{{ t('presale.title') }}</h1>
      <p class="presale-hero-sub">{{ t('presale.subtitle') }}</p>
    </div>

    <div v-if="loading" class="presale-grid">
      <div v-for="i in 6" :key="i" class="presale-skel">
        <div class="sk-img" />
        <div class="sk-body"><div class="sk-line" /><div class="sk-line w60" /></div>
      </div>
    </div>

    <div v-else-if="presales.length" class="presale-grid">
      <div v-for="p in presales" :key="p.id" class="presale-card" role="button" tabindex="0" @click="goDetail(p.id)" @keydown.enter.prevent="goDetail(p.id)" @keydown.space.prevent="goDetail(p.id)">
        <LazyImage :src="p.imageUrl || p.productImage || ''" :alt="p.productName || p.name" height="200px" />
        <div class="presale-badge">{{ t('presale.badge') }}</div>
        <div class="presale-info">
          <h3 class="presale-name">{{ p.productName || p.name }}</h3>
          <div class="presale-prices">
            <span class="presale-deposit">{{ t('presale.deposit') }} ¥{{ formatPrice((p.depositAmount || 0) / 100) }}</span>
            <span class="presale-full">{{ t('presale.fullPrice') }} ¥{{ formatPrice((p.fullAmount || p.price || 0) / 100) }}</span>
          </div>
          <div class="presale-progress">
            <div class="presale-bar"><div class="presale-bar-fill" :style="{ width: Math.min(100, ((p.depositCount || 0) / Math.max(1, p.stock || 1)) * 100) + '%' }" /></div>
            <span class="presale-count">{{ p.depositCount || 0 }}/{{ p.stock || 0 }} {{ t('presale.booked') }}</span>
          </div>
          <CountdownTimer v-if="p.endTime" :end-time="p.endTime" />
          <JdButton size="sm" class="presale-btn" :loading="paying.has(p.id)" @click.stop="doDeposit(p.id)">{{ t('presale.payDeposit') }}</JdButton>
        </div>
      </div>
    </div>

    <JdEmpty v-else icon="🎯" :title="t('presale.empty')" :description="t('presale.emptyDesc')" />
  </div>
</template>

<style scoped>
.presale-page { max-width: 1000px; margin: 0 auto; }
.presale-hero { background: linear-gradient(135deg, #f093fb, #f5576c); color: #fff; border-radius: var(--radius-xl); padding: var(--space-xxxl); margin-bottom: var(--space-xxl); text-align: center; }
.presale-hero-title { font-size: var(--font-h1); font-weight: 800; margin-bottom: var(--space-sm); }
.presale-hero-sub { font-size: 15px; opacity: .9; }
.presale-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-lg); }
.presale-card { background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition); position: relative; }
.presale-card:hover { transform: translateY(-4px); }
.presale-badge { position: absolute; top: 10px; left: 10px; background: var(--jd-red); color: #fff; padding: 2px 10px; border-radius: var(--radius-round); font-size: 12px; font-weight: 600; }
.presale-info { padding: var(--space-lg); }
.presale-name { font-size: 15px; font-weight: 600; margin-bottom: var(--space-sm); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.presale-prices { display: flex; gap: var(--space-md); align-items: baseline; margin-bottom: var(--space-sm); }
.presale-deposit { color: var(--jd-red); font-size: var(--font-lg); font-weight: 700; }
.presale-full { color: var(--text-tertiary); font-size: var(--font-sm); text-decoration: line-through; }
.presale-progress { margin-bottom: var(--space-sm); }
.presale-bar { background: var(--bg-hover); border-radius: var(--radius-sm); height: 4px; overflow: hidden; margin-bottom: 4px; }
.presale-bar-fill { height: 100%; background: var(--jd-red); border-radius: var(--radius-sm); transition: width .6s; }
.presale-count { font-size: 11px; color: var(--text-tertiary); }
.presale-btn { margin-top: var(--space-sm); width: 100%; }
.presale-skel { background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden; box-shadow: var(--shadow-sm); }
.sk-img { height: 200px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; }
.sk-body { padding: var(--space-lg); display: flex; flex-direction: column; gap: var(--space-sm); }
.sk-line { height: 16px; background: var(--border-light); border-radius: var(--radius-sm); width: 100%; }
.sk-line.w60 { width: 60%; }
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

@media (max-width: 768px) {
  .presale-page { padding: 0 var(--space-md) 80px; }
  .presale-hero { padding: var(--space-xl); }
  .presale-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-md); }
}
</style>
