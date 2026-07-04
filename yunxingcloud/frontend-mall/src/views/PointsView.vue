<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { useI18n } from '@/locales'

const { t } = useI18n()
const account = ref<{balance:number,totalEarned:number,totalSpent:number} | null>(null)
const records = ref<any[]>([])
const loading = ref(true)

onMounted(async () => {
  try { const r = await request.get('/points/account'); account.value = r.data } catch {}
  try { const r = await request.get('/points/records'); records.value = r.data || [] } catch {}
  finally { loading.value = false }
})

const actions = [
  { icon: '🛒', label: t('points.shopReward'), desc: t('points.shopDesc') },
  { icon: '✍️', label: t('points.reviewReward'), desc: t('points.reviewDesc') },
  { icon: '🎁', label: t('points.checkinReward'), desc: t('points.checkinDesc') },
]
</script>

<template>
  <div class="pts-page">
    <div class="pts-hero">
      <div class="pts-hero-bg" /><div class="pts-hero-bg pts-hero-bg--small" />
      <div class="pts-hero-content">
        <p class="pts-label">⭐ {{ t('points.available') }}</p>
        <p class="pts-balance">{{ account?.balance || 0 }}</p>
        <div class="pts-stats">
          <span>{{ t('points.totalEarned') }} <b>{{ account?.totalEarned || 0 }}</b></span>
          <span>{{ t('points.totalSpent') }} <b>{{ account?.totalSpent || 0 }}</b></span>
        </div>
      </div>
    </div>

    <div class="pts-actions">
      <div v-for="a in actions" :key="a.label" class="pts-action">
        <div class="pts-action-icon">{{ a.icon }}</div>
        <div class="pts-action-label">{{ a.label }}</div>
        <div class="pts-action-desc">{{ a.desc }}</div>
      </div>
    </div>

    <div class="pts-card">
      <h3 class="pts-title">{{ t('points.detail') }}</h3>
      <div v-if="loading" class="pts-skel"><div v-for="i in 4" :key="i" class="sk-line" /></div>
      <div v-else-if="records.length">
        <div v-for="r in records.slice(0, 20)" :key="r.id" class="pts-row">
          <div class="pts-row-info">
            <div class="pts-row-desc">{{ r.remark || r.type }}</div>
            <div class="pts-row-date">{{ r.createdAt?.substring(0, 10) }}</div>
          </div>
          <span class="pts-row-amount" :class="{ plus: r.amount > 0 }">{{ r.amount > 0 ? '+' : '' }}{{ r.amount }}</span>
        </div>
      </div>
      <div v-else class="pts-empty">{{ t('points.noRecords') }}</div>
    </div>
  </div>
</template>

<style scoped>
.pts-page { max-width: 600px; margin: 0 auto; }
.pts-hero { background: linear-gradient(135deg, #ff9800, #ffc107); color: #fff; border-radius: var(--radius-xl); padding: 36px; margin-bottom: var(--space-xxl); text-align: center; box-shadow: 0 8px 32px rgba(255,152,0,.25); position: relative; overflow: hidden; }
.pts-hero-bg { position: absolute; top: -30px; right: -30px; width: 120px; height: 120px; border-radius: 50%; background: rgba(255,255,255,.1); }
.pts-hero-bg--small { top: auto; bottom: -20px; right: auto; left: -20px; width: 80px; height: 80px; background: rgba(255,255,255,.08); }
.pts-hero-content { position: relative; z-index: 1; }
.pts-label { font-size: var(--font-base); opacity: .85; margin-bottom: var(--space-xs); }
.pts-balance { font-size: 56px; font-weight: 800; margin: var(--space-sm) 0; }
.pts-stats { display: flex; justify-content: center; gap: var(--space-xxl); margin-top: var(--space-md); font-size: var(--font-base); opacity: .8; }

.pts-actions { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-md); margin-bottom: var(--space-xxl); }
.pts-action { background: var(--bg-white); border-radius: var(--radius-md); padding: var(--space-lg); text-align: center; box-shadow: var(--shadow-sm); cursor: pointer; transition: transform var(--transition); }
.pts-action:hover { transform: translateY(-2px); }
.pts-action-icon { font-size: var(--font-h1); margin-bottom: 6px; }
.pts-action-label { font-size: var(--font-base); font-weight: 600; margin-bottom: 2px; }
.pts-action-desc { font-size: var(--font-xs); color: var(--text-tertiary); }

.pts-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); box-shadow: var(--shadow-sm); }
.pts-title { font-size: var(--font-lg); font-weight: 600; margin-bottom: var(--space-lg); }
.pts-skel { display: flex; flex-direction: column; gap: var(--space-md); }
.sk-line { height: 40px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-sm); }
.pts-row { display: flex; justify-content: space-between; align-items: center; padding: var(--space-md) 0; border-bottom: 1px solid var(--border-light); }
.pts-row-info { flex: 1; }
.pts-row-desc { font-size: var(--font-md); font-weight: 500; }
.pts-row-date { color: var(--text-tertiary); font-size: var(--font-sm); }
.pts-row-amount { font-size: var(--font-lg); font-weight: 700; color: var(--text-tertiary); }
.pts-row-amount.plus { color: var(--green); }
.pts-empty { text-align: center; color: var(--text-tertiary); padding: var(--space-xl); }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }
</style>
