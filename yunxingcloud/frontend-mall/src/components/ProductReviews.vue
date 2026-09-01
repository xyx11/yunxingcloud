<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getProductDetail } from '@/api/product'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { useI18n } from '@/locales'
import { formatRelativeTime } from '@/utils/format'
import type { Review } from '@/types'
import ReviewSummary from '@/components/ReviewSummary.vue'
import JdButton from '@/components/JdButton.vue'
import request from '@/api/request'

const props = defineProps<{
  productId: number
  reviews: Review[]
  reviewAnalytics?: { avgRating?: number; total?: number; distribution?: Record<number, number> }
}>()

const emit = defineEmits<{
  'open-preview': [img: string]
  'reviews-updated': [reviews: Review[]]
}>()

const router = useRouter()
const auth = useAuthStore()
const toast = useToast()
const { t } = useI18n()

const reviewSort = ref<'newest' | 'highest' | 'lowest'>('newest')
const reviewShow = ref(3)
const reviewForm = ref({ rating: 0, content: '' })
const reviewImages = ref<File[]>([])
const reviewPreviews = ref<string[]>([])
const reviewSubmitting = ref(false)
const MAX_REVIEW_IMAGES = 5

const sortedReviews = computed(() => {
  const arr = [...props.reviews]
  if (reviewSort.value === 'newest') arr.sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
  else if (reviewSort.value === 'highest') arr.sort((a, b) => b.rating - a.rating)
  else arr.sort((a, b) => a.rating - b.rating)
  return arr.slice(0, reviewShow.value)
})

function showMoreReviews() { reviewShow.value = Math.min(reviewShow.value + 3, props.reviews.length) }

function handleReviewImages(e: Event) {
  const files = (e.target as HTMLInputElement).files
  if (!files) return
  for (let i = 0; i < files.length && reviewImages.value.length < MAX_REVIEW_IMAGES; i++) {
    reviewImages.value.push(files[i])
    reviewPreviews.value.push(URL.createObjectURL(files[i]))
  }
  ;(e.target as HTMLInputElement).value = ''
}

function removeReviewImage(i: number) {
  URL.revokeObjectURL(reviewPreviews.value[i])
  reviewImages.value.splice(i, 1)
  reviewPreviews.value.splice(i, 1)
}

async function submitProductReview() {
  if (!reviewForm.value.rating || !reviewForm.value.content) return
  reviewSubmitting.value = true
  try {
    let imageUrls: string[] = []
    if (reviewImages.value.length) {
      const fd = new FormData()
      reviewImages.value.forEach((f) => fd.append('files', f))
      const uploadRes = await request.post('/files/upload/review-images', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
      imageUrls = uploadRes.data || []
    }
    await request.post(`/products/${props.productId}/reviews`, { rating: reviewForm.value.rating, content: reviewForm.value.content, images: imageUrls })
    toast.success(t('toast.reviewSuccess'))
    reviewForm.value = { rating: 0, content: '' }
    reviewImages.value.forEach((_, i) => URL.revokeObjectURL(reviewPreviews.value[i]))
    reviewImages.value = []; reviewPreviews.value = []
    const res = await getProductDetail(props.productId)
    emit('reviews-updated', res.data.reviews || [])
  } catch { toast.error(t('toast.reviewFail')) } finally { reviewSubmitting.value = false }
}
</script>

<template>
  <div class="pdp-section">
    <h3 class="section-title">{{ t('product.reviews') }} ({{ reviews.length }})</h3>
    <ReviewSummary v-if="reviews.length" :reviews="reviews" :analytics="reviewAnalytics" />
    <div v-if="reviews.length" class="review-sort">
      <span class="rs-opt" :class="{ active: reviewSort === 'newest' }" @click="reviewSort = 'newest'">{{ t('product.reviewSortNewest') }}</span>
      <span class="rs-opt" :class="{ active: reviewSort === 'highest' }" @click="reviewSort = 'highest'">{{ t('product.reviewSortHighest') }}</span>
      <span class="rs-opt" :class="{ active: reviewSort === 'lowest' }" @click="reviewSort = 'lowest'">{{ t('product.reviewSortLowest') }}</span>
    </div>
    <div v-if="reviews.length">
      <div v-for="r in sortedReviews" :key="r.id" class="review-item">
        <div class="review-header">
          <div class="review-user">
            <span class="review-username">{{ r.username }}</span>
            <span class="review-stars">{{ '★'.repeat(r.rating) }}</span>
          </div>
          <span class="review-date">{{ formatRelativeTime(r.createdAt) }}</span>
        </div>
        <p class="review-content">{{ r.content }}</p>
        <div v-if="r.images?.length" class="review-images">
          <img v-for="(img, j) in r.images" :key="j" :src="img" :alt="t('product.reviewImage', { n: j + 1 })" class="review-thumb" @click.stop="emit('open-preview', img)" />
        </div>
      </div>
      <div v-if="reviewShow < reviews.length" class="review-more">
        <button class="review-more-btn" @click="showMoreReviews">{{ t('product.loadMoreReviews') }} ({{ reviews.length - reviewShow }})</button>
      </div>
    </div>
    <div v-else class="empty-text">{{ t('product.noReviews') }}</div>

    <!-- Write Review -->
    <div v-if="auth.isLoggedIn" class="write-review">
      <h4 class="write-review-title">{{ t('product.writeReview') }}</h4>
      <div class="write-review-stars">
        <span v-for="i in 5" :key="i" class="write-star" :class="{ active: i <= reviewForm.rating }" @click="reviewForm.rating = i">{{ i <= reviewForm.rating ? '★' : '☆' }}</span>
      </div>
      <textarea v-model="reviewForm.content" class="write-review-textarea" :placeholder="t('rating.placeholder')" />
      <div class="write-review-images">
        <div v-for="(preview, i) in reviewPreviews" :key="i" class="review-img-preview">
          <img :src="preview" alt="" />
          <button class="review-img-remove" @click="removeReviewImage(i)">✕</button>
        </div>
        <label v-if="reviewImages.length < MAX_REVIEW_IMAGES" class="review-img-add">
          <input type="file" accept="image/*" multiple hidden @change="handleReviewImages" />
          <span>+</span>
        </label>
      </div>
      <JdButton size="sm" :loading="reviewSubmitting" :disabled="reviewSubmitting" @click="submitProductReview">{{ t('rating.submitReview') }}</JdButton>
    </div>
    <div v-else class="write-review-login">
      <span>{{ t('login.fillRequired') }}</span>
      <JdButton size="sm" type="outline" @click="router.push('/login')">{{ t('common.login') }}</JdButton>
    </div>
  </div>
</template>

<style scoped>
.section-title { font-size: var(--font-lg); font-weight: 700; margin-bottom: var(--space-lg); }
.review-sort { display: flex; gap: var(--space-md); margin-bottom: var(--space-md); }
.rs-opt { cursor: pointer; padding: 4px 10px; border-radius: var(--radius-sm); font-size: var(--font-sm); color: var(--text-secondary); transition: all var(--transition-fast); }
.rs-opt.active { color: var(--jd-red); background: var(--jd-red-light); }
.rs-opt:hover:not(.active) { color: var(--jd-red); }
.review-more { text-align: center; padding: var(--space-lg) 0; }
.review-more-btn { padding: var(--space-md) 32px; border: 1px solid var(--border); background: var(--bg-white); border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-md); color: var(--text-secondary); transition: all var(--transition-fast); }
.review-more-btn:hover { border-color: var(--jd-red); color: var(--jd-red); }
.write-review { margin-top: var(--space-xl); padding: var(--space-lg); background: var(--bg-hover); border-radius: var(--radius-md); }
.write-review-login { margin-top: var(--space-xl); padding: var(--space-lg); background: var(--bg-hover); border-radius: var(--radius-md); display: flex; align-items: center; justify-content: space-between; gap: var(--space-md); font-size: var(--font-sm); color: var(--text-tertiary); }
.write-review-title { font-size: var(--font-md); font-weight: 600; margin-bottom: var(--space-sm); }
.write-review-stars { margin-bottom: var(--space-sm); display: flex; gap: 4px; }
.write-star { font-size: 24px; cursor: pointer; color: var(--border); transition: color .2s; }
.write-star.active { color: var(--orange); }
.write-review-textarea { width: 100%; height: 80px; padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: var(--font-base); resize: none; box-sizing: border-box; margin-bottom: var(--space-sm); }
.review-item { padding: var(--space-lg) 0; border-bottom: 1px solid var(--border-light); }
.review-header { display: flex; justify-content: space-between; margin-bottom: var(--space-sm); }
.review-user { display: flex; align-items: center; gap: var(--space-sm); }
.review-username { font-weight: 600; font-size: var(--font-md); }
.review-stars { color: var(--orange); font-size: var(--font-md); }
.review-date { color: var(--text-tertiary); font-size: var(--font-sm); }
.review-content { font-size: var(--font-md); color: var(--text-primary); line-height: 1.6; }
.review-images { display: flex; gap: 6px; margin-top: var(--space-sm); flex-wrap: wrap; }
.review-thumb { width: 80px; height: 80px; object-fit: cover; border-radius: 6px; cursor: pointer; border: 1px solid var(--border-light); transition: transform .2s; }
.review-thumb:hover { transform: scale(1.05); }
.write-review-images { display: flex; gap: 8px; margin-bottom: var(--space-sm); flex-wrap: wrap; }
.review-img-preview { position: relative; width: 80px; height: 80px; border-radius: 6px; overflow: hidden; border: 1px solid var(--border-light); }
.review-img-preview img { width: 100%; height: 100%; object-fit: cover; }
.review-img-remove { position: absolute; top: 0; right: 0; width: 20px; height: 20px; background: rgba(0,0,0,.5); color: #fff; border: none; font-size: 12px; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.review-img-add { width: 80px; height: 80px; border: 2px dashed var(--border); border-radius: 6px; display: flex; align-items: center; justify-content: center; cursor: pointer; color: var(--text-tertiary); font-size: 28px; transition: border-color .2s; }
.review-img-add:hover { border-color: var(--jd-red); color: var(--jd-red); }
.empty-text { text-align: center; padding: 30px; color: var(--text-tertiary); }
</style>