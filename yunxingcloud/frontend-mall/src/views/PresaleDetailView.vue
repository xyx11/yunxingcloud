<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPresaleById, payDeposit, payFinal } from '@/api/presale'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import CountdownTimer from '@/components/CountdownTimer.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import { formatPrice } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const presale = ref<any>(null)
const loading = ref(true)
const error = ref(false)
const paying = ref(false)
const id = Number(route.params.id)

function phaseLabel(): string {
  if (!presale.value) return ''
  const now = Date.now()
  if (presale.value.depositEndTime && now < new Date(presale.value.depositEndTime).getTime()) return 'deposit'
  if (presale.value.finalPayStartTime && now >= new Date(presale.value.finalPayStartTime).getTime()) return 'finalPay'
  return 'waiting'
}

async function load() {
  loading.value = true
  error.value = false
  try {
    const r = await getPresaleById(id)
    presale.value = r.data || null
    loading.value = false;
    } catch {
    error.value = true
    toast.error(t('presaleDetail.loadFail'))
  }
}

async function doDeposit() {
  paying.value = true
  try {
    await payDeposit(id)
    toast.success(t('presaleDetail.depositSuccess'))
    load()
  } catch {
    toast.error(t('presaleDetail.depositFail'))
  } finally {
    paying.value = false
  }
}

async function doFinalPay() {
  paying.value = true
  try {
    await payFinal(id, 0)
    toast.success(t('presaleDetail.finalSuccess'))
    load()
  } catch {
    toast.error(t('presaleDetail.finalFail'))
  } finally {
    paying.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="pd-page">
    <div class="pd-back">
      <button class="back-btn" @click="router.back()">{{ t('presaleDetail.back') }}</button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="pd-skel">
      <SkeletonBox variant="banner" height="400px" />
      <SkeletonBox variant="text" :count="3" />
    </div>

    <!-- Error -->
    <JdEmpty v-else-if="error" icon="⚠️" :title="t('presaleDetail.loadFail')" :description="t('presaleDetail.loadFail')">
      <JdButton @click="load">{{ t('common.retry') }}</JdButton>
    </JdEmpty>

    <!-- Not found -->
    <JdEmpty v-else-if="!presale" icon="🎯" :title="t('presaleDetail.notFound')" :description="t('presaleDetail.notFoundDesc')" />

    <!-- Content -->
    <template v-else>
      <div class="pd-main">
        <div class="pd-image">
          <LazyImage
            :src="presale.imageUrl || presale.productImage || ''"
            :alt="presale.productName || presale.name"
            height="400px"
          />
        </div>

        <div class="pd-info">
          <span class="pd-badge">{{ t('presaleDetail.badge') }}</span>
          <h1 class="pd-name">{{ presale.productName || presale.name }}</h1>

          <div class="pd-prices">
            <div class="pd-price-row">
              <span class="pd-label">{{ t('presaleDetail.deposit') }}</span>
              <span class="pd-deposit-amount">{{ formatPrice((presale.depositAmount || 0) / 100, 2) }}</span>
            </div>
            <div class="pd-price-row">
              <span class="pd-label">{{ t('presaleDetail.finalPayment') }}</span>
              <span class="pd-final-amount">{{ formatPrice(((presale.fullAmount || presale.price || 0) - (presale.depositAmount || 0)) / 100, 2) }}</span>
            </div>
            <div class="pd-price-divider" />
            <div class="pd-price-row">
              <span class="pd-label">{{ t('presaleDetail.fullPrice') }}</span>
              <span class="pd-full-amount">{{ formatPrice((presale.fullAmount || presale.price || 0) / 100, 2) }}</span>
            </div>
          </div>

          <!-- Timeline -->
          <div class="pd-timeline">
            <div class="pd-phase" :class="{ active: phaseLabel() === 'deposit' }">
              <div class="pd-phase-dot" />
              <div>
                <div class="pd-phase-title">{{ t('presaleDetail.depositPhase') }}</div>
                <div class="pd-phase-time" v-if="presale.depositEndTime">
                  {{ t('presaleDetail.deadline') }} {{ new Date(presale.depositEndTime).toLocaleDateString() }}
                </div>
                <CountdownTimer v-if="presale.depositEndTime && phaseLabel() === 'deposit'" :end-time="presale.depositEndTime" />
              </div>
            </div>
            <div class="pd-phase-line" :class="{ done: phaseLabel() === 'finalPay' || phaseLabel() === 'waiting' }" />
            <div class="pd-phase" :class="{ active: phaseLabel() === 'finalPay' }">
              <div class="pd-phase-dot" />
              <div>
                <div class="pd-phase-title">{{ t('presaleDetail.finalPhase') }}</div>
                <div class="pd-phase-time" v-if="presale.finalPayStartTime">
                  {{ t('presaleDetail.starts') }} {{ new Date(presale.finalPayStartTime).toLocaleDateString() }}
                </div>
              </div>
            </div>
          </div>

          <!-- Progress -->
          <div class="pd-progress">
            <div class="pd-progress-bar">
              <div class="pd-progress-fill" :style="{ width: Math.min(100, ((presale.depositCount || 0) / Math.max(1, presale.stock || 1)) * 100) + '%' }" />
            </div>
            <span class="pd-progress-text">{{ presale.depositCount || 0 }}/{{ presale.stock || 0 }} {{ t('presaleDetail.booked') }}</span>
          </div>

          <!-- Actions -->
          <div class="pd-actions">
            <JdButton v-if="phaseLabel() === 'deposit'" size="lg" class="pd-btn" :loading="paying" @click="doDeposit">
              {{ t('presaleDetail.payDeposit') }} {{ formatPrice((presale.depositAmount || 0) / 100) }}
            </JdButton>
            <JdButton v-else-if="phaseLabel() === 'finalPay'" size="lg" class="pd-btn" :loading="paying" @click="doFinalPay">
              {{ t('presaleDetail.payFinal') }}
            </JdButton>
            <JdButton v-else size="lg" disabled class="pd-btn">{{ t('presaleDetail.waitingPhase') }}</JdButton>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.pd-page { max-width: 1000px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }
.pd-back { margin-bottom: var(--space-lg); }
.back-btn { background: none; border: none; color: var(--text-secondary); cursor: pointer; font-size: var(--font-md); }

.pd-main { display: grid; grid-template-columns: 1fr 420px; gap: var(--space-xxl); }
.pd-image { border-radius: var(--radius-lg); overflow: hidden; background: var(--bg-white); }

.pd-info { display: flex; flex-direction: column; gap: var(--space-xl); }
.pd-badge {
  display: inline-block; padding: 2px 10px; background: var(--jd-red); color: #fff;
  border-radius: var(--radius-round); font-size: 12px; font-weight: 600; width: fit-content;
}
.pd-name { font-size: var(--font-xl); font-weight: 700; }

.pd-prices {
  background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl);
  display: flex; flex-direction: column; gap: var(--space-sm);
}
.pd-price-row { display: flex; justify-content: space-between; align-items: center; }
.pd-label { font-size: var(--font-sm); color: var(--text-secondary); }
.pd-deposit-amount { color: var(--jd-red); font-weight: 700; font-size: var(--font-lg); }
.pd-final-amount { color: var(--text-primary); font-weight: 600; font-size: var(--font-lg); }
.pd-full-amount { color: var(--jd-red); font-weight: 800; font-size: var(--font-xxl); }
.pd-price-divider { border-top: 1px dashed var(--border); }

.pd-timeline { display: flex; align-items: flex-start; gap: 0; }
.pd-phase { display: flex; gap: var(--space-md); align-items: flex-start; flex: 1; }
.pd-phase-dot {
  width: 12px; height: 12px; border-radius: 50%; background: var(--border);
  flex-shrink: 0; margin-top: 4px; transition: background var(--transition);
}
.pd-phase.active .pd-phase-dot { background: var(--jd-red); box-shadow: 0 0 0 4px var(--jd-red-light); }
.pd-phase-title { font-weight: 600; font-size: var(--font-sm); }
.pd-phase-time { font-size: 12px; color: var(--text-tertiary); margin-top: 2px; }
.pd-phase-line {
  width: 40px; height: 2px; background: var(--border); flex-shrink: 0; margin-top: 9px;
  transition: background var(--transition);
}
.pd-phase-line.done { background: var(--jd-red); }

.pd-progress { margin-top: var(--space-xs); }
.pd-progress-bar { height: 6px; background: var(--bg-hover); border-radius: var(--radius-sm); overflow: hidden; margin-bottom: 4px; }
.pd-progress-fill { height: 100%; background: var(--jd-red); border-radius: var(--radius-sm); transition: width .6s; }
.pd-progress-text { font-size: 12px; color: var(--text-tertiary); }

.pd-actions { margin-top: var(--space-md); }
.pd-btn { width: 100%; }

/* Skeleton */
.pd-skel { display: flex; flex-direction: column; gap: var(--space-lg); }

@media (max-width: 768px) {
  .pd-page { padding: var(--space-lg) var(--space-md) 80px; }
  .pd-main { grid-template-columns: 1fr; }
}
</style>
