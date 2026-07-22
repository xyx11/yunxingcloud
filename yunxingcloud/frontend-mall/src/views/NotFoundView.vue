<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from '@/locales'
import JdButton from '@/components/JdButton.vue'
import LazyImage from '@/components/LazyImage.vue'
import request from '@/api/request'
import { formatPrice } from '@/utils/format'

const router = useRouter()
const { t } = useI18n()
const searchText = ref('')
const recProducts = ref<any[]>([])

function doSearch() {
  if (searchText.value.trim()) {
    router.push('/search?q=' + encodeURIComponent(searchText.value.trim()))
  }
}

const suggestions = [
  { label: 'nav.home', path: '/' },
  { label: 'nav.products', path: '/products' },
  { label: 'common.cart', path: '/cart' },
  { label: 'brands.title', path: '/brands' },
]

onMounted(async () => {
  try { const r = await request.get('/recommend/hot'); recProducts.value = (r.data || []).slice(0, 4) } catch {}
})
</script>

<template>
  <div class="nf-page">
    <div class="nf-animation">
      <div class="nf-cart">🛒</div>
      <div class="nf-trail" v-for="i in 5" :key="i" :style="{ animationDelay: i * 0.15 + 's' }">·</div>
    </div>
    <div class="nf-code">404</div>
    <h2 class="nf-title">{{ t('notFound.title') }}</h2>
    <p class="nf-desc">{{ t('notFound.desc') }}</p>

    <div class="nf-search">
      <input v-model="searchText" :placeholder="t('search.placeholder')" class="nf-input" @keyup.enter="doSearch" />
      <JdButton @click="doSearch">{{ t('common.search') }}</JdButton>
    </div>

    <div class="nf-links">
      <span>{{ t('notFound.tryPages') }}:</span>
      <button v-for="s in suggestions" :key="s.path" class="nf-link" @click="router.push(s.path)">{{ t(s.label) }}</button>
    </div>

    <JdButton size="lg" @click="router.push('/')">{{ t('notFound.backHome') }}</JdButton>

    <!-- Recommendations -->
    <div v-if="recProducts.length" class="nf-recs">
      <h4 class="nf-recs-title">🔥 为您推荐</h4>
      <div class="nf-recs-grid">
        <div v-for="p in recProducts" :key="p.id" class="nf-rec-item" @click="router.push('/product/' + p.id)">
          <LazyImage :src="p.imageUrl || ''" :alt="p.name" height="120px" />
          <div class="nf-rec-name">{{ p.name }}</div>
          <div class="nf-rec-price">{{ formatPrice(p.price / 100, 2) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.nf-page { text-align: center; padding: 80px 20px; max-width: 480px; margin: 0 auto; }
.nf-animation { position: relative; height: 60px; margin-bottom: 8px; }
.nf-cart { font-size: 36px; animation: float-cart 2s ease-in-out infinite; }
@keyframes float-cart { 0%,100% { transform: translateY(0) rotate(0deg); } 25% { transform: translateY(-10px) rotate(-5deg); } 75% { transform: translateY(5px) rotate(5deg); } }
.nf-trail { position: absolute; font-size: 20px; color: var(--jd-red); opacity: 0; animation: trail-fade 1.5s ease-in-out infinite; }
.nf-trail:nth-child(2) { top: 10px; left: 50%; }
.nf-trail:nth-child(3) { top: 30px; left: 40%; }
.nf-trail:nth-child(4) { top: 20px; left: 60%; }
.nf-trail:nth-child(5) { top: 40px; left: 55%; }
.nf-trail:nth-child(6) { top: 15px; left: 45%; }
@keyframes trail-fade { 0%,100% { opacity: 0; transform: scale(0); } 50% { opacity: 1; transform: scale(1.5); } }
.nf-code { font-size: 80px; font-weight: 800; color: var(--jd-red); margin-bottom: 16px; line-height: 1; animation: pulse-code 2s ease-in-out infinite; }
@keyframes pulse-code { 0%,100% { opacity: 1; } 50% { opacity: .7; } }
.nf-title { font-size: var(--font-xl); color: var(--text-primary); margin-bottom: 8px; }
.nf-desc { color: var(--text-tertiary); font-size: var(--font-md); margin-bottom: 28px; }

.nf-search { display: flex; gap: var(--space-sm); max-width: 360px; margin: 0 auto var(--space-lg); }
.nf-input { flex: 1; padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-md); font-size: var(--font-md); background: var(--bg-white); color: var(--text-primary); }
.nf-input:focus { border-color: var(--jd-red); outline: none; }

.nf-links { margin-bottom: var(--space-xl); font-size: var(--font-sm); color: var(--text-secondary); }
.nf-link { background: none; border: none; color: var(--jd-red); cursor: pointer; font-size: var(--font-sm); margin-left: var(--space-sm); text-decoration: underline; }

.nf-recs { margin-top: var(--space-xxxl); }
.nf-recs-title { font-size: var(--font-md); font-weight: 700; margin-bottom: var(--space-md); color: var(--text-primary); }
.nf-recs-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-md); }
.nf-rec-item { background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition-fast); text-align: center; }
.nf-rec-item:hover { transform: translateY(-4px); }
.nf-rec-name { font-size: 12px; padding: 4px 8px 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.nf-rec-price { font-size: var(--font-sm); color: var(--jd-red); font-weight: 700; padding: 2px 8px 8px; }

@media (max-width: 768px) {
  .nf-page { padding: 60px 20px; }
  .nf-code { font-size: 60px; }
  .nf-search { flex-direction: column; }
}
</style>
