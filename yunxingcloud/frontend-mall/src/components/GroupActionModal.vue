<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { addToCart } from '@/api/cart'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import { formatPrice } from '@/utils/format'
import JdButton from '@/components/JdButton.vue'
import LazyImage from '@/components/LazyImage.vue'
import CountdownTimer from '@/components/CountdownTimer.vue'

const props = defineProps<{
  group: { id: number; productId: number; productName?: string; imageUrl?: string; groupPrice: number; originalPrice: number; minMembers: number; currentMembers?: number; endTime: string; groups?: { id: number; memberCount: number; remaining: number }[] }
}>()

const emit = defineEmits<{ close: [] }>()
const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const opening = ref(false)
const joining = ref<number | null>(null)

const progress = () => Math.min(100, Math.round(((props.group.currentMembers || 0) / (props.group.minMembers || 1)) * 100))

async function openGroup() {
  opening.value = true
  try {
    await addToCart(props.group.productId, 1)
    const orderRes = await request.post('/orders', {
      receiverName: '', receiverPhone: '', receiverAddress: '',
      productId: props.group.productId, quantity: 1
    })
    const orderId = orderRes.data?.id || orderRes.data?.data?.id
    if (orderId) {
      await request.post(`/group-buy/${props.group.id}/open?orderId=${orderId}`)
      toast.success(t('toast.groupOpenSuccess'))
      router.push(`/order/${orderId}`)
    }
    emit('close')
  } catch {
    toast.error(t('toast.groupOpenFail'))
  } finally {
    opening.value = false
  }
}

async function joinGroup(groupId: number) {
  joining.value = groupId
  try {
    await addToCart(props.group.productId, 1)
    const orderRes = await request.post('/orders', {
      receiverName: '', receiverPhone: '', receiverAddress: '',
      productId: props.group.productId, quantity: 1
    })
    const orderId = orderRes.data?.id || orderRes.data?.data?.id
    if (orderId) {
      await request.post(`/group-buy/${groupId}/join?orderId=${orderId}`)
      toast.success(t('toast.groupJoinSuccess'))
      router.push(`/order/${orderId}`)
    }
    emit('close')
  } catch {
    toast.error(t('toast.groupJoinFail'))
  } finally {
    joining.value = null
  }
}
</script>

<template>
  <div class="gam-overlay" @click.self="emit('close')">
    <div class="gam-modal">
      <button class="gam-close" @click="emit('close')">&times;</button>

      <div class="gam-header">
        <LazyImage :src="group.imageUrl || ''" :alt="group.productName" height="160px" />
        <div class="gam-header-info">
          <h3 class="gam-name">{{ group.productName || t('groupAction.productName') }}</h3>
          <div class="gam-prices">
            <span class="gam-price">{{ formatPrice(group.groupPrice / 100, 2) }}</span>
            <span class="gam-original">{{ formatPrice(group.originalPrice / 100, 2) }}</span>
          </div>
          <CountdownTimer :end-time="group.endTime" compact />
        </div>
      </div>

      <div class="gam-progress-section">
        <div class="gam-progress-label">{{ t('groupAction.joinedCount', { n: group.currentMembers || 0, m: group.minMembers }) }} ({{ progress() }}%)</div>
        <div class="gam-progress-bar">
          <div class="gam-progress-fill" :style="{ width: progress() + '%' }" />
        </div>
      </div>

      <div class="gam-actions">
        <JdButton size="lg" class="gam-btn" :loading="opening" @click="openGroup">
          {{ t('groupAction.openGroup') }}
        </JdButton>
      </div>

      <!-- Existing groups to join -->
      <div v-if="group.groups && group.groups.length" class="gam-existing">
        <h4 class="gam-section-title">{{ t('groupAction.availableGroups') }}</h4>
        <div v-for="g in group.groups" :key="g.id" class="gam-existing-item">
          <div class="gam-existing-info">
            <span>{{ t('groupAction.memberCount', { n: g.memberCount, m: group.minMembers }) }}</span>
            <span class="gam-remaining">{{ t('groupAction.remaining', { n: g.remaining }) }}</span>
          </div>
          <JdButton size="sm" :loading="joining === g.id" @click="joinGroup(g.id)">{{ t('groupAction.joinGroup') }}</JdButton>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.gam-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,.5); z-index: 500;
  display: flex; align-items: center; justify-content: center;
}
.gam-modal {
  background: var(--bg-white); border-radius: var(--radius-xl);
  padding: var(--space-xxl); max-width: 480px; width: 90%;
  position: relative; max-height: 90vh; overflow-y: auto;
}
.gam-close {
  position: absolute; top: var(--space-md); right: var(--space-md);
  background: none; border: none; font-size: 24px; cursor: pointer;
  color: var(--text-tertiary); line-height: 1;
}
.gam-header { display: flex; gap: var(--space-lg); margin-bottom: var(--space-xl); }
.gam-header-info { flex: 1; display: flex; flex-direction: column; gap: var(--space-sm); }
.gam-name { font-size: var(--font-lg); font-weight: 700; }
.gam-prices { display: flex; gap: var(--space-sm); align-items: baseline; }
.gam-price { color: var(--jd-red); font-size: var(--font-xxl); font-weight: 700; }
.gam-original { color: var(--text-tertiary); font-size: var(--font-sm); text-decoration: line-through; }

.gam-progress-section { margin-bottom: var(--space-xl); }
.gam-progress-label { font-size: var(--font-sm); color: var(--text-secondary); margin-bottom: 4px; }
.gam-progress-bar { height: 8px; background: var(--bg-hover); border-radius: var(--radius-sm); overflow: hidden; }
.gam-progress-fill { height: 100%; background: var(--jd-red); border-radius: var(--radius-sm); transition: width .6s; }

.gam-actions { margin-bottom: var(--space-xl); }
.gam-btn { width: 100%; }

.gam-existing { border-top: 1px solid var(--border-light); padding-top: var(--space-lg); }
.gam-section-title { font-size: var(--font-md); font-weight: 600; margin-bottom: var(--space-md); }
.gam-existing-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--space-sm) var(--space-md); background: var(--bg-hover);
  border-radius: var(--radius-md); margin-bottom: var(--space-sm);
}
.gam-existing-info { display: flex; gap: var(--space-md); font-size: var(--font-sm); }
.gam-remaining { color: var(--jd-red); font-weight: 600; }
</style>
