<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from '@/locales'
import { useToast } from '@/composables/useToast'
const { t } = useI18n()
const toast = useToast()
import request from '@/api/request'
import { formatPrice } from '@/utils/format'
import CountdownTimer from '@/components/CountdownTimer.vue'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'
import JdBadge from '@/components/JdBadge.vue'
import JdEmpty from '@/components/JdEmpty.vue'

interface GroupBuyItem {
  id: number
  productId: number
  productName?: string
  imageUrl?: string
  groupPrice: number
  originalPrice: number
  minMembers: number
  currentMembers?: number
  endTime: string
  stock?: number
}

const router = useRouter()
const groups = ref<GroupBuyItem[]>([])
const loading = ref(true)

onMounted(async () => {
  try { const r = await request.get('/group-buy'); groups.value = r.data || [] } catch { toast.error('拼团列表加载失败') }
  finally { loading.value = false }
})

function goProduct(id: number) { router.push(`/product/${id}`) }
const progress = (g: GroupBuyItem) => Math.min(100, Math.round(((g.currentMembers || 0) / (g.minMembers || 1)) * 100))

// Share poster
const showShare = ref(false)
const sharePoster = ref('')
const shareGroupName = ref('')
function share(g: GroupBuyItem) {
  shareGroupName.value = g.productName || ''
  drawPoster(g)
  showShare.value = true
}
function drawPoster(g: GroupBuyItem) {
  const canvas = document.createElement('canvas')
  canvas.width = 400; canvas.height = 600
  const ctx = canvas.getContext('2d')!
  // bg
  ctx.fillStyle = '#fff'; ctx.fillRect(0, 0, 400, 600)
  // header gradient
  const grad = ctx.createLinearGradient(0, 0, 400, 0)
  grad.addColorStop(0, '#f10215'); grad.addColorStop(1, '#ff6b6b')
  ctx.fillStyle = grad; ctx.fillRect(0, 0, 400, 180)
  // title
  ctx.fillStyle = '#fff'; ctx.font = 'bold 28px system-ui'; ctx.textAlign = 'center'
  ctx.fillText('拼团邀请', 200, 60)
  ctx.font = '16px system-ui'; ctx.fillText('邀请好友一起拼，享超低价', 200, 90)
  // product name
  ctx.fillStyle = '#333'; ctx.font = 'bold 20px system-ui'
  ctx.fillText(g.productName || '拼团商品', 200, 230)
  // prices
  ctx.font = 'bold 36px system-ui'; ctx.fillStyle = '#f10215'
  ctx.fillText('¥' + (g.groupPrice / 100).toFixed(2), 200, 290)
  ctx.font = '18px system-ui'; ctx.fillStyle = '#999'
  ctx.fillText('原价 ¥' + (g.originalPrice / 100).toFixed(2), 200, 320)
  const lw = ctx.measureText('原价 ¥' + (g.originalPrice / 100).toFixed(2))
  ctx.strokeStyle = '#999'; ctx.lineWidth = 1
  ctx.beginPath(); ctx.moveTo(200 - lw.width/2, 316); ctx.lineTo(200 + lw.width/2, 316); ctx.stroke()
  // progress
  const pct = Math.min(100, Math.round(((g.currentMembers || 0) / (g.minMembers || 1)) * 100))
  ctx.fillStyle = '#666'; ctx.font = '16px system-ui'
  ctx.fillText(`已参团 ${g.currentMembers || 0}/${g.minMembers} (${pct}%)`, 200, 370)
  ctx.fillStyle = '#eee'; ctx.fillRect(50, 385, 300, 12)
  ctx.fillStyle = '#f10215'; ctx.fillRect(50, 385, 300 * pct / 100, 12)
  // QR placeholder
  ctx.fillStyle = '#f5f5f5'; ctx.fillRect(150, 430, 100, 100)
  ctx.fillStyle = '#999'; ctx.font = '12px system-ui'; ctx.textAlign = 'center'
  ctx.fillText('扫码参团', 200, 485)
  // footer
  ctx.fillStyle = '#999'; ctx.font = '12px system-ui'
  ctx.fillText('长按识别二维码 · 邀请好友参团', 200, 570)
  sharePoster.value = canvas.toDataURL()
}
function downloadPoster() {
  const a = document.createElement('a'); a.href = sharePoster.value; a.download = '拼团海报.png'; a.click()
}
async function copyShareLink() {
  try { await navigator.clipboard.writeText(window.location.origin + '/group-buy'); toast.success(t('toast.copied')) } catch {}
}
</script>

<template>
  <div class="gb-page">
    <div class="gb-hero">
      <h1 class="gb-hero-title">👥 拼团专区</h1>
      <p class="gb-hero-sub">{{ t('groupBuy.subtitle') }}</p>
    </div>

    <div v-if="loading" class="gb-grid">
      <div v-for="i in 6" :key="i" class="gb-card"><div class="sk-img" /><div class="sk-body"><div class="sk-line" /></div></div>
    </div>

    <div v-else-if="groups.length" class="gb-grid">
      <div v-for="g in groups" :key="g.id" class="gb-card" role="button" tabindex="0" @click="goProduct(g.productId)" @keydown.enter.prevent="goProduct(g.productId)" @keydown.space.prevent="goProduct(g.productId)">
        <div class="gb-img">
          <LazyImage :src="g.imageUrl || ''" :alt="g.productName" height="200px" bg="linear-gradient(135deg,#f0f0ff,#e8e8ff)" />
          <JdBadge class="gb-badge-tl">{{ g.minMembers }}人团</JdBadge>
          <span class="gb-countdown"><CountdownTimer :end-time="g.endTime" compact /></span>
        </div>
        <div class="gb-info">
          <h3 class="gb-name">{{ g.productName || t('groupBuy.defaultName', '拼团商品') }}</h3>
          <div class="gb-prices">
            <span class="gb-price">{{ formatPrice(g.groupPrice / 100, 2) }}</span>
            <span class="gb-original">{{ formatPrice(g.originalPrice / 100, 2) }}</span>
            <span class="gb-members">{{ g.currentMembers || 0 }}/{{ g.minMembers }}{{ t('groupBuy.people', '人') }}</span>
          </div>
          <div class="gb-bar"><div class="gb-bar-fill" :style="{ width: progress(g) + '%' }" /></div>
          <div class="gb-actions">
            <JdButton size="sm" class="flex-1" @click.stop="goProduct(g.productId)">{{ t('groupBuy.joinGroup') }}</JdButton>
            <JdButton size="sm" type="outline" @click.stop="share(g)">📤 邀请</JdButton>
          </div>
        </div>
      </div>
    </div>

    <JdEmpty v-else icon="👥" :title="t('groupBuy.empty', '暂无拼团活动')" />

    <!-- Share Modal -->
    <div v-if="showShare" class="share-overlay" @click.self="showShare = false">
      <div class="share-modal">
        <h3 class="share-title">分享拼团 - {{ shareGroupName }}</h3>
        <img :src="sharePoster" class="share-poster" />
        <div class="share-btns">
          <JdButton @click="downloadPoster">下载海报</JdButton>
          <JdButton type="outline" @click="copyShareLink">复制链接</JdButton>
          <JdButton type="ghost" @click="showShare = false">关闭</JdButton>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.gb-page { max-width: 900px; margin: 0 auto; }
.gb-hero { background: linear-gradient(135deg, var(--jd-red), #ff6b6b); color: #fff; border-radius: var(--radius-xl); padding: var(--space-xxxl); margin-bottom: var(--space-xxl); text-align: center; box-shadow: 0 4px 20px rgba(228,57,60,.25); }
.gb-hero-title { font-size: var(--font-h1); font-weight: 800; margin-bottom: var(--space-sm); }
.gb-hero-sub { font-size: 15px; opacity: .9; }

.gb-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-lg); }
.gb-card { background: var(--bg-white); border-radius: var(--radius-lg); overflow: hidden; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition); }
.gb-card:hover { transform: translateY(-6px); }

.gb-img { height: 200px; position: relative; }
.gb-badge-tl { position: absolute; top: 10px; left: 10px; z-index: 1; }
.gb-countdown { position: absolute; top: 10px; right: 10px; color: var(--jd-red); font-size: var(--font-sm); font-weight: 700; background: var(--bg-white); padding: 2px 8px; border-radius: var(--radius-sm); }

.gb-info { padding: var(--space-lg); }
.gb-name { font-size: 15px; font-weight: 600; margin-bottom: var(--space-sm); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.gb-prices { display: flex; align-items: baseline; gap: var(--space-sm); margin-bottom: var(--space-md); }
.gb-price { color: var(--jd-red); font-size: var(--font-title); font-weight: 700; }
.gb-original { color: var(--text-tertiary); font-size: var(--font-sm); text-decoration: line-through; }
.gb-members { color: var(--jd-red); font-size: var(--font-xs); margin-left: auto; }

.gb-bar { background: var(--bg-hover); border-radius: var(--radius-sm); height: 6px; overflow: hidden; margin-bottom: var(--space-md); }
.gb-bar-fill { height: 100%; background: linear-gradient(90deg, var(--jd-red), #ff6b6b); border-radius: var(--radius-sm); transition: width .6s; }

.sk-img { height: 200px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; }
.sk-body { padding: var(--space-lg); }
.sk-line { height: 16px; width: 60%; background: var(--border-light); border-radius: var(--radius-sm); }

@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

.gb-actions { display: flex; gap: var(--space-sm); }
.flex-1 { flex: 1; }
.share-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,.6); z-index: 500; display: flex; align-items: center; justify-content: center; }
.share-modal { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xl); max-width: 440px; width: 90%; text-align: center; }
.share-title { font-size: var(--font-lg); font-weight: 700; margin-bottom: var(--space-lg); }
.share-poster { width: 100%; max-width: 360px; border-radius: var(--radius-md); box-shadow: var(--shadow-md); }
.share-btns { display: flex; gap: var(--space-sm); margin-top: var(--space-lg); justify-content: center; }

@media (max-width: 768px) {
  .gb-page { padding: 0 var(--space-md); }
  .gb-hero { padding: var(--space-xl); }
  .gb-hero-title { font-size: var(--font-xl); }
  .gb-grid { grid-template-columns: repeat(2, 1fr); gap: var(--space-md); }
  .gb-img { height: 140px; }
  .gb-info { padding: var(--space-md); }
  .gb-name { font-size: 14px; }
  .gb-price { font-size: var(--font-lg); }
}
</style>
