<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPresales, payDeposit } from '@/api/presale'
import { formatPrice } from '@/utils/format'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import CountdownTimer from '@/components/CountdownTimer.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import type { PresaleProduct } from '@/types'

const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const presales = ref<PresaleProduct[]>([])
const loading = ref(true)
const loadError = ref(false)
const paying = ref<Set<number>>(new Set())

async function load() {
  loading.value = true
  loadError.value = false
  try {
    const r = await getPresales()
    presales.value = (r.data || r.data?.content || [])
  } catch {
    toast.error(t('presale.loadFail'))
    loadError.value = true
  } finally {
    loading.value = false
  }
}

async function doDeposit(id: number) {
  if (paying.value.has(id)) return
  paying.value.add(id)
  try { await payDeposit(id); toast.success(t('presale.depositSuccess')); load() } catch { toast.error(t('presale.depositFail')) } finally { paying.value.delete(id) }
}

function goDetail(id: number) { router.push(`/presale/${id}`) }
function presaleExpired(p: PresaleProduct): boolean { return !!p.endTime && new Date(p.endTime).getTime() <= Date.now() }

onMounted(load)
</script>

<template>
  <div class="presale-page">
    <div class="presale-hero">
      <h1 class="presale-hero-title">{{ t('presale.title') }}</h1>
      <p class="presale-hero-sub">{{ t('presale.subtitle') }}</p>
    </div>

    <div v-if="loading" class="presale-grid">
      <SkeletonBox variant="card" :columns="3" :count="6" height="320px" />
    </div>

    <JdEmpty v-else-if="loadError" icon="⚠️" :title="t('presale.loadFail')">
      <JdButton @click="load">{{ t('common.retry') }}</JdButton>
    </JdEmpty>


    <div v-else-if="presales.length" class="presale-grid">
      <div v-for="p in presales" :key="p.id" class="presale-card" role="button" tabindex="0" @click="goDetail(p.id)" @keydown.enter.prevent="goDetail(p.id)" @keydown.space.prevent="goDetail(p.id)">
        <LazyImage :src="p.imageUrl || p.productImage || ''" :alt="p.productName || p.name" height="200px" />
        <div class="presale-badge">{{ t('presale.badge') }}</div>
        <div v-if="(p.depositCount || 0) > 100" class="presale-hot">🔥 热门</div>
        <div class="presale-info">
          <h3 class="presale-name">{{ p.productName || p.name }}</h3>
          <div class="presale-prices">
            <span class="presale-deposit">{{ t('presale.deposit') }} ¥{{ formatPrice((p.depositAmount || 0) / 100) }}</span>
            <span class="presale-full">{{ t('presale.fullPrice') }} ¥{{ formatPrice((p.fullAmount || p.price || 0) / 100) }}</span>
          </div>
          <!-- Phase indicator -->
          <div class="presale-phase" v-if="p.depositEndTime && p.finalPayStartTime">
            <span class="phase-tag" :class="new Date(p.depositEndTime).getTime() > Date.now() ? 'phase-active' : ''">
              {{ new Date(p.depositEndTime).getTime() > Date.now() ? t('presale.depositPhase') : t('presale.finalPhase') }}
            </span>
            <span class="phase-date" v-if="new Date(p.depositEndTime).getTime() > Date.now()">
              定金截止 {{ new Date(p.depositEndTime).toLocaleDateString() }}
            </span>
            <span class="phase-date" v-else-if="p.finalPayStartTime">
              尾款开始 {{ new Date(p.finalPayStartTime).toLocaleDateString() }}
            </span>
          </div>
          <div class="presale-progress">
            <div class="presale-bar"><div class="presale-bar-fill" :style="{ width: Math.min(100, ((p.depositCount || 0) / Math.max(1, p.stock || 1)) * 100) + '%' }" /></div>
            <span class="presale-count">{{ p.depositCount || 0 }}/{{ p.stock || 0 }} {{ t('presale.booked') }}</span>
          </div>
          <CountdownTimer v-if="p.endTime" :end-time="p.endTime" />
          <JdButton size="sm" class="presale-btn" :loading="paying.has(p.id)" :disabled="presaleExpired(p)" @click.stop="doDeposit(p.id)">{{ presaleExpired(p) ? t('countdown.ended') : t('presale.payDeposit') }}</JdButton>
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
.presale-hot { position: absolute; top: 10px; right: 10px; background: rgba(0,0,0,.6); color: #fff; padding: 2px 10px; border-radius: var(--radius-round); font-size: 11px; font-weight: 600; }
.presale-info { padding: var(--space-lg); }
.presale-name { font-size: 15px; font-weight: 600; margin-bottom: var(--space-sm); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.presale-prices { display: flex; gap: var(--space-md); align-items: baseline; margin-bottom: var(--space-sm); }
.presale-deposit { color: var(--jd-red); font-size: var(--font-lg); font-weight: 700; }
.presale-full { color: var(--text-tertiary); font-size: var(--font-sm); text-decoration: line-through; }
.presale-progress { margin-bottom: var(--space-sm); }
.presale-bar { background: var(--bg-hover); border-radius: var(--radius-sm); height: 4px; overflow: hidden; margin-bottom: 4px; }
.presale-bar-fill { height: 100%; background: var(--jd-red); border-radius: var(--radius-sm); transition: width .6s; }
.presale-count { font-size: 11px; color: var(--text-tertiary); }
.presale-phase { display: flex; align-items: center; gap: var(--space-sm); margin-bottom: var(--space-sm); }
.phase-tag { font-size: 11px; padding: 2px 8px; border-radius: var(--radius-round); background: var(--bg-hover); color: var(--text-tertiary); font-weight: 600; }
.phase-tag.phase-active { background: var(--jd-red); color: #fff; animation: pulse-phase 1.5s ease-in-out infinite; }
@keyframes pulse-phase { 0%, 100% { opacity: 1; } 50% { opacity: .7; } }
.phase-date { font-size: 11px; color: var(--text-placeholder); }
.presale-btn { margin-top: var(--space-sm); width: 100%; }

@media (max-width: 768px) {
  .presale-page { padding: 0 var(--space-md) calc(80px + env(safe-area-inset-bottom, 0px)); }
  .presale-hero { padding: var(--space-xl); }
  .presale-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-md); }
}
</style>
