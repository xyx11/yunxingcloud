<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyReviews, deleteReview } from '@/api/review'
import { formatRelativeTime } from '@/utils/format'
import JdButton from '@/components/JdButton.vue'
import JdEmpty from '@/components/JdEmpty.vue'
import LazyImage from '@/components/LazyImage.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'

const router = useRouter()
const toast = useToast()
const { t } = useI18n()
const reviews = ref<any[]>([])
const loading = ref(true)
const loadError = ref(false)
const deleting = ref<Set<number>>(new Set())
const confirmDelete = ref<number | null>(null)

async function load() {
  loading.value = true
  loadError.value = false
  try {
    const r = await getMyReviews()
    reviews.value = r.data?.content || r.data || []
  } catch {
    toast.error(t('myReviews.loadFail'))
    loadError.value = true
  } finally {
    loading.value = false
  }
}

async function doDelete(id: number) {
  confirmDelete.value = null
  if (deleting.value.has(id)) return
  deleting.value.add(id)
  try {
    await deleteReview(id)
    reviews.value = reviews.value.filter(r => r.id !== id)
    toast.success(t('myReviews.deleteSuccess'))
  } catch {
    toast.error(t('myReviews.deleteFail'))
  } finally {
    deleting.value.delete(id)
  }
}

function goProduct(id: number) {
  router.push(`/product/${id}`)
}

function renderStars(rating: number): string {
  const full = Math.floor(rating)
  const half = rating - full >= 0.5
  let s = '★'.repeat(full)
  if (half) s += '☆'
  return s.padEnd(5, '☆')
}

onMounted(load)
</script>

<template>
  <div class="mr-page">
    <div class="mr-header">
      <button class="back-btn" @click="router.push('/profile')">{{ t('myReviews.back') }}</button>
      <h1 class="mr-title">{{ t('myReviews.title') }}</h1>
    </div>

    <SkeletonBox v-if="loading" variant="list-item" :count="5" height="100px" gap="var(--space-md)" />

    <div v-else-if="loadError" class="mr-error">
      <JdEmpty icon="⚠️" :title="t('myReviews.loadFail')" :description="t('myReviews.loadFailDesc')">
        <JdButton @click="load">{{ t('common.retry') }}</JdButton>
      </JdEmpty>
    </div>

    <div v-else-if="reviews.length" class="mr-list">
      <div v-for="r in reviews" :key="r.id" class="mr-item">
        <LazyImage
          :src="r.productImage || ''"
          :alt="r.productName || ''"
          height="80px"
          width="80px"
          rounded="8px"
          class="mr-item-img"
          @click="goProduct(r.productId)"
        />
        <div class="mr-item-body">
          <div class="mr-item-header">
            <span
              class="mr-item-product"
              role="button"
              tabindex="0"
              @click="goProduct(r.productId)"
              @keydown.enter.prevent="goProduct(r.productId)"
              @keydown.space.prevent="goProduct(r.productId)"
            >
              {{ r.productName || (t('myReviews.productDefaultSuffix') + r.productId) }}
            </span>
            <span class="mr-item-date">{{ formatRelativeTime(r.createdAt) }}</span>
          </div>
          <div class="mr-item-rating">
            <span class="mr-stars" :aria-label="t('myReviews.ratingAria', { n: String(r.rating) })">
              {{ renderStars(r.rating) }}
            </span>
            <span class="mr-rating-num">{{ r.rating.toFixed(1) }}</span>
          </div>
          <p v-if="r.content" class="mr-item-content">{{ r.content }}</p>
          <div v-if="r.images?.length" class="mr-item-images">
            <LazyImage
              v-for="(img, i) in r.images"
              :key="i"
              :src="img"
              :alt="t('myReviews.imageAria', { n: String(i + 1) })"
              height="60px"
              width="60px"
              rounded="6px"
            />
          </div>
        </div>
        <JdButton
          size="sm"
          type="ghost"
          :loading="deleting.has(r.id)"
          @click="confirmDelete = r.id"
        >
          {{ t('common.delete') }}
        </JdButton>
      </div>
    </div>

    <JdEmpty v-else icon="✍️" :title="t('myReviews.empty')" :description="t('myReviews.emptyDesc')">
      <JdButton @click="router.push('/orders')">{{ t('myReviews.goOrders') }}</JdButton>
    </JdEmpty>

    <ConfirmDialog
      :show="confirmDelete !== null"
      :title="t('myReviews.deleteConfirm')"
      :message="t('myReviews.deleteConfirmMsg')"
      :confirm-text="t('common.delete')"
      @confirm="confirmDelete && doDelete(confirmDelete)"
      @cancel="confirmDelete = null"
    />
  </div>
</template>

<style scoped>
.mr-page { max-width: 700px; margin: 0 auto; padding: var(--space-xxl) var(--space-md); }
.mr-header { margin-bottom: var(--space-xl); }
.back-btn { background: none; border: none; color: var(--text-secondary); cursor: pointer; font-size: var(--font-md); margin-bottom: var(--space-sm); padding: 0; }
.back-btn:hover { color: var(--jd-red); }
.mr-title { font-size: var(--font-xl); font-weight: 700; }

.mr-list { display: flex; flex-direction: column; gap: var(--space-md); }
.mr-item {
  display: flex; align-items: flex-start; gap: var(--space-md);
  background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-lg);
  box-shadow: var(--shadow-sm);
}
.mr-item-img { cursor: pointer; flex-shrink: 0; }
.mr-item-body { flex: 1; min-width: 0; }
.mr-item-header { display: flex; justify-content: space-between; align-items: flex-start; gap: var(--space-sm); margin-bottom: 4px; }
.mr-item-product { font-weight: 600; font-size: var(--font-md); cursor: pointer; color: var(--text-primary); }
.mr-item-product:hover { color: var(--jd-red); }
.mr-item-date { font-size: var(--font-xs); color: var(--text-tertiary); white-space: nowrap; }
.mr-item-rating { display: flex; align-items: center; gap: var(--space-xs); margin-bottom: var(--space-xs); }
.mr-stars { color: #f5a623; font-size: 16px; letter-spacing: 1px; }
.mr-rating-num { font-size: var(--font-sm); color: var(--text-secondary); font-weight: 500; }
.mr-item-content { font-size: var(--font-sm); color: var(--text-secondary); line-height: 1.5; margin: var(--space-xs) 0 0; word-break: break-word; }
.mr-item-images { display: flex; gap: var(--space-sm); margin-top: var(--space-sm); }

.mr-error { padding: var(--space-xxxl) 0; }

@media (max-width: 768px) {
  .mr-page { padding: var(--space-lg) var(--space-md) calc(80px + env(safe-area-inset-bottom, 0px)); }
  .mr-item { flex-wrap: wrap; }
  .mr-item-body { flex-basis: calc(100% - 96px); }
}
</style>
