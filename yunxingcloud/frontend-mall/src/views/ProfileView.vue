<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress, getFavorites, getMyCoupons } from '@/api/order'
import { changePassword } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/locales'
import type { Address, Coupon, FavoriteItem } from '@/types'
import { useToast } from '@/composables/useToast'
import request from '@/api/request'
import { provinceList, cityList, districtList } from '@/utils/regionData'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'

const router = useRouter()
const auth = useAuthStore()
const { t } = useI18n()
const toast = useToast()

type TabKey = 'addresses' | 'coupons' | 'favorites' | 'password'

const activeTab = ref<TabKey>('addresses')
const addresses = ref<Address[]>([])
const coupons = ref<Coupon[]>([])
const favorites = ref<FavoriteItem[]>([])
const shareCopied = ref(false)
const editAddr = ref<Address | null>(null)
const showAddrForm = ref(false)
const loading = ref(true)
const memberTier = ref('')
const memberPoints = ref(0)
const showFeedback = ref(false)
const feedbackForm = ref({ content: '', contact: '' })
const feedbackSent = ref(false)
const showLogout = ref(false)

async function doLogout() {
  await auth.logout()
  router.push('/login')
  toast.info(t('profile.logoutSuccess'))
}

async function submitFeedback() {
  if (!feedbackForm.value.content.trim()) return
  try { await request.post('/feedback', { type: 'suggestion', content: feedbackForm.value.content, contact: feedbackForm.value.contact }); feedbackSent.value = true } catch { toast.error(t('toast.feedbackFail')) }
}

const tierProgress = ref(0)
const nextTierName = ref('')
const memberDiscount = ref(0)

async function loadMemberTier() {
  try {
    const r = await request.get('/member/tiers'); const tiers = r.data || []
    if (tiers.length) {
      const current = tiers[0]; memberTier.value = current.name || ''
      if (tiers.length > 1) { nextTierName.value = tiers[1].name || ''; tierProgress.value = Math.min(100, Math.round((memberPoints.value || 0) / Math.max(1, tiers[1].pointsRequired || 1000) * 100)) }
    }
  } catch {}
  try { const p = await request.get('/points/account'); memberPoints.value = p.data?.balance ?? 0 } catch {}
  try { const b = await request.get('/member/benefits'); const benefits = b.data; if (benefits?.discount) memberDiscount.value = benefits.discount } catch {}
}

// Order stats
const orderStats = ref({ pending: 0, paid: 0, shipped: 0, done: 0, total: 0 })
async function loadOrderStats() {
  try { const r = await request.get('/orders'); const orders = r.data || []
    orderStats.value.total = orders.length
    orderStats.value.pending = orders.filter((o: any) => o.status === '0').length
    orderStats.value.paid = orders.filter((o: any) => o.status === '1').length
    orderStats.value.shipped = orders.filter((o: any) => o.status === '2').length
    orderStats.value.done = orders.filter((o: any) => o.status === '3').length
  } catch {}
}

const pwForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const addrForm = ref({ name: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })

const addrCityOpts = computed(() => cityList(addrForm.value.province))
const addrDistrictOpts = computed(() => districtList(addrForm.value.province, addrForm.value.city))

function onProvinceChange() { addrForm.value.city = ''; addrForm.value.district = '' }
function onCityChange() { addrForm.value.district = '' }

const quickLinks = [
  { icon: '📦', label: t('profile.menuOrders'), path: '/orders' },
  { icon: '⭐', label: t('points.title'), path: '/points' },
  { icon: '🎁', label: t('giftCard.title'), path: '/gift-card' },
  { icon: '🔔', label: t('profile.menuPriceAlerts'), path: '/price-alerts' },
  { icon: '🕐', label: t('profile.menuRecent'), path: '/recent' },
  { icon: '🏆', label: t('profile.menuRanking'), path: '/ranking' },
  { icon: '✍️', label: t('profile.menuReviews'), path: '/my-reviews' },
  { icon: '❓', label: t('profile.menuHelp'), path: '/help' },
]

async function copyShareLink() {
  try { await navigator.clipboard.writeText(window.location.origin + '/register?ref=' + (auth.user?.username || '')); shareCopied.value = true; toast.success(t('profile.inviteCopied')); setTimeout(() => shareCopied.value = false, 2000) } catch { toast.error('复制失败') }
}

onMounted(async () => { if (!auth.isLoggedIn) { router.push('/login'); return }; loadTab(); loadMemberTier(); loadOrderStats() })

const tabCache: Record<string, boolean> = {}

async function loadTab() {
  const key = activeTab.value
  if (tabCache[key]) { loading.value = false; return }
  loading.value = true
  if (key === 'addresses') { try { const r = await getAddresses(); addresses.value = r.data || [] } catch { toast.error(t('toast.addressLoadFail')) } }
  else if (key === 'coupons') { try { const r = await getMyCoupons(); coupons.value = r.data || [] } catch { toast.error(t('toast.couponLoadFail')) } }
  else if (key === 'favorites') { try { const r = await getFavorites(); favorites.value = r.data || [] } catch { toast.error(t('toast.favoritesLoadFail')) } }
  tabCache[key] = true
  loading.value = false
}

function refreshTab() { tabCache[activeTab.value] = false; loadTab() }

async function saveAddress() {
  try {
    if (editAddr.value) { await updateAddress(editAddr.value.id, addrForm.value) }
    else { await createAddress(addrForm.value) }
    toast.success(editAddr.value ? t('common.updated') : t('common.added'))
    showAddrForm.value = false; editAddr.value = null; refreshTab()
  } catch { toast.error(t('toast.addressSaveFail')) }
}

function editAddress(addr: Address) {
  editAddr.value = addr
  addrForm.value = { name: addr.name, phone: addr.phone, province: addr.province || '', city: addr.city || '', district: addr.district || '', detail: addr.detail || '', isDefault: addr.isDefault }
  showAddrForm.value = true
}

async function deleteAddressById(id: number) { if (!confirm(t('common.confirmDelete'))) return; await deleteAddress(id); toast.info(t('common.deleted')); refreshTab() }

const changingPwd = ref(false)
async function changePwd() {
  if (!pwForm.value.oldPassword || !pwForm.value.newPassword) { toast.error(t('register.fillRequired')); return }
  if (pwForm.value.newPassword.length < 8) { toast.error(t('register.pwdLen')); return }
  if (pwForm.value.newPassword !== pwForm.value.confirmPassword) { toast.error(t('register.passwordMismatch')); return }
  changingPwd.value = true
  try { await changePassword(pwForm.value.oldPassword, pwForm.value.newPassword); toast.success(t('toast.passwordChanged')); pwForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' } }
  catch (e: unknown) { toast.error((e as { response?: { data?: { message?: string } } }).response?.data?.message || t('common.updateFailed')) }
  finally { changingPwd.value = false }
}

async function toggleDefault(addr: Address) { await setDefaultAddress(addr.id, !addr.isDefault); refreshTab() }
</script>

<template>
  <div v-if="auth.isLoggedIn" class="profile-page">
    <!-- Avatar -->
    <div class="profile-header">
      <div class="avatar">{{ auth.user?.username?.charAt(0)?.toUpperCase() }}</div>
      <div>
        <h2 class="username">{{ auth.user?.username }}</h2>
        <p class="member-tag">{{ memberTier || t('profile.memberLabel') }}{{ memberPoints ? ' · ' + memberPoints + t('profile.pointsUnit') : '' }}</p>
        <p v-if="memberDiscount" class="member-benefit">🎉 {{ t('profile.memberDiscount', { n: memberDiscount }) }}</p>
        <div v-if="nextTierName" class="tier-progress">
          <div class="tier-progress-bar"><div class="tier-progress-fill" :style="{ width: tierProgress + '%' }" /></div>
          <span class="tier-progress-label">{{ t('profile.tierProgressLabel', { name: nextTierName, pct: tierProgress }) }}</span>
        </div>
      </div>
    </div>

    <!-- Order Stats -->
    <div class="order-stats" v-if="orderStats.total > 0">
      <div class="stats-title" @click="router.push('/orders')">{{ t('common.orders') }} ({{ orderStats.total }}) <span class="stats-arrow">&gt;</span></div>
      <div class="stats-grid">
        <div class="stat-item" @click="router.push('/orders?tab=0')">
          <span class="stat-num">{{ orderStats.pending }}</span>
          <span class="stat-label">{{ t('profile.pendingPay') }}</span>
        </div>
        <div class="stat-item" @click="router.push('/orders?tab=1')">
          <span class="stat-num">{{ orderStats.paid }}</span>
          <span class="stat-label">{{ t('profile.pendingShip') }}</span>
        </div>
        <div class="stat-item" @click="router.push('/orders?tab=2')">
          <span class="stat-num">{{ orderStats.shipped }}</span>
          <span class="stat-label">{{ t('profile.pendingReceive') }}</span>
        </div>
        <div class="stat-item" @click="router.push('/orders?tab=3')">
          <span class="stat-num">{{ orderStats.done }}</span>
          <span class="stat-label">{{ t('profile.done') }}</span>
        </div>
      </div>
    </div>

    <!-- Invite -->
    <div class="invite-bar" @click="copyShareLink">
      <div>
        <div class="invite-title">🎁 {{ t('profile.inviteFriend') }}</div>
        <div class="invite-desc">{{ t('profile.inviteDesc') }}</div>
      </div>
      <button class="invite-btn" :class="{ copied: shareCopied }" @click.stop="copyShareLink">
        {{ shareCopied ? t('profile.inviteCopied') : t('profile.inviteBtn') }}
      </button>
    </div>

    <!-- Feedback -->
    <div class="invite-bar" @click="showFeedback = !showFeedback">
      <div>
        <div class="invite-title">💬 {{ t('profile.feedback') }}</div>
        <div class="invite-desc">{{ t('profile.feedbackPlaceholder') }}</div>
      </div>
      <button class="invite-btn">{{ showFeedback ? t('profile.collapseLabel') : t('profile.feedbackLabel') }}</button>
    </div>
    <div v-if="showFeedback" class="feedback-form">
      <div v-if="feedbackSent" class="feedback-success">✅ {{ t('profile.feedbackSuccess') }}</div>
      <template v-else>
        <textarea v-model="feedbackForm.content" :placeholder="t('profile.feedbackPlaceholder')" class="feedback-textarea" />
        <input v-model="feedbackForm.contact" placeholder="联系方式（选填）" class="field-input field-full" />
        <JdButton size="sm" @click="submitFeedback">{{ t('common.submit') }}</JdButton>
      </template>
    </div>

    <!-- Quick Links -->
    <div class="quick-links">
      <div v-for="l in quickLinks" :key="l.path" class="quick-item" role="button" tabindex="0" @click="router.push(l.path)" @keydown.enter.prevent="router.push(l.path)" @keydown.space.prevent="router.push(l.path)">
        <div class="quick-icon">{{ l.icon }}</div>
        <div class="quick-label">{{ l.label }}</div>
      </div>
    </div>

    <!-- Tabs -->
    <div class="tab-bar">
      <span v-for="tab in [{key:'addresses',label:t('profile.addresses')},{key:'favorites',label:t('profile.favorites')},{key:'coupons',label:t('profile.coupons')},{key:'password',label:t('profile.changePassword')}]"
            :key="tab.key" class="tab" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key as TabKey; loadTab()">
        {{ tab.label }}
      </span>
    </div>

    <!-- Tab Content -->
    <div class="tab-content">
      <!-- Addresses -->
      <div v-if="activeTab==='addresses'">
        <div class="tab-header">
          <h3 class="tab-title">{{ t('profile.addresses') }}</h3>
          <JdButton size="sm" @click="editAddr=null;addrForm={name:'',phone:'',province:'',city:'',district:'',detail:'',isDefault:false};showAddrForm=true">+ {{ t('profile.addAddress') }}</JdButton>
        </div>

        <div v-if="showAddrForm" class="addr-form">
          <div class="addr-form-grid">
            <input v-model="addrForm.name" :placeholder="t('profile.receiverName')" class="field-input" />
            <input v-model="addrForm.phone" :placeholder="t('profile.receiverPhone')" class="field-input" />
            <select v-model="addrForm.province" class="field-input field-select" @change="onProvinceChange">
              <option value="">{{ t('profile.province') }}</option>
              <option v-for="p in provinceList" :key="p" :value="p">{{ p }}</option>
            </select>
            <select v-model="addrForm.city" class="field-input field-select" :disabled="!addrForm.province" @change="onCityChange">
              <option value="">{{ t('profile.city') }}</option>
              <option v-for="c in addrCityOpts" :key="c" :value="c">{{ c }}</option>
            </select>
            <select v-model="addrForm.district" class="field-input field-select" :disabled="!addrForm.city">
              <option value="">{{ t('profile.district') }}</option>
              <option v-for="d in addrDistrictOpts" :key="d" :value="d">{{ d }}</option>
            </select>
          </div>
          <input v-model="addrForm.detail" :placeholder="t('profile.detail')" class="field-input field-full" />
          <div class="addr-form-footer">
            <label class="checkbox-sm"><input type="checkbox" v-model="addrForm.isDefault" /> {{ t('profile.setDefault') }}</label>
            <div class="flex gap-sm">
              <JdButton type="ghost" size="sm" @click="showAddrForm=false;editAddr=null">{{ t('profile.cancel') }}</JdButton>
              <JdButton size="sm" @click="saveAddress">{{ t('profile.saveAddress') }}</JdButton>
            </div>
          </div>
        </div>

        <div v-if="addresses.length">
          <div v-for="addr in addresses" :key="addr.id" class="addr-card">
            <div v-if="addr.isDefault" class="addr-default-tag">{{ t('profile.default') }}</div>
            <div class="addr-name">{{ addr.name }} <span class="addr-phone">{{ addr.phone }}</span></div>
            <div class="addr-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</div>
            <div class="addr-actions">
              <button class="action-btn" @click="editAddress(addr)">{{ t('profile.edit') }}</button>
              <button class="action-btn action-btn--danger" @click="deleteAddressById(addr.id)">{{ t('profile.delete') }}</button>
              <button v-if="!addr.isDefault" class="action-btn" @click="toggleDefault(addr)">{{ t('profile.setDefault') }}</button>
            </div>
          </div>
        </div>
        <div v-if="loading" class="sk-box"><div class="sk-line" /><div class="sk-line w60" /><div class="sk-line w40" /></div>
        <div v-else-if="!addresses.length" class="empty-text">{{ t('profile.noAddresses') }}</div>
      </div>

      <!-- Favorites -->
      <div v-if="activeTab==='favorites'">
        <h3 class="tab-title">{{ t('profile.favorites') }}</h3>
        <div v-if="favorites.length" class="fav-grid">
          <div v-for="f in favorites" :key="f.id" class="fav-card" role="button" tabindex="0" @click="router.push(`/product/${f.productId}`)" @keydown.enter.prevent="router.push(`/product/${f.productId}`)" @keydown.space.prevent="router.push(`/product/${f.productId}`)">
            <LazyImage :src="f.imageUrl || ''" :alt="f.name || f.productName || ''" height="100px" rounded="6px" />
            <div class="fav-name">{{ f.productName || (t('profile.productDefaultSuffix') + f.productId) }}</div>
          </div>
        </div>
        <div v-if="loading" class="sk-box"><div class="sk-line" /><div class="sk-line w60" /></div>
        <div v-else-if="!loading" class="empty-text">{{ t('profile.noFavorites') }}</div>
      </div>

      <!-- Coupons -->
      <div v-if="activeTab==='coupons'">
        <h3 class="tab-title">{{ t('profile.myCoupons') }}</h3>
        <div v-if="coupons.length">
          <div v-for="c in coupons" :key="c.id" class="coupon-card">
            <div class="coupon-icon"><span class="fs-12">{{ t('profile.coupons') }}</span><span class="fs-18">#{{ c.couponId }}</span></div>
            <div><div class="coupon-name">{{ t('profile.coupons') }} #{{ c.couponId }}</div><div class="coupon-status">{{ c.status === '0' ? t('profile.unused') : t('profile.used') }}</div></div>
          </div>
        </div>
        <div v-if="loading" class="sk-box"><div class="sk-line" /><div class="sk-line w60" /></div>
        <div v-else-if="!loading" class="empty-text">{{ t('profile.noCoupons') }}</div>
      </div>

      <!-- Password -->
      <div v-if="activeTab==='password'">
        <h3 class="tab-title">{{ t('profile.changePassword') }}</h3>
        <div class="pw-form">
          <div class="pw-field">
            <label class="pw-label">{{ t('profile.currentPassword') }}</label>
            <input v-model="pwForm.oldPassword" type="password" class="field-input" />
          </div>
          <div class="pw-field">
            <label class="pw-label">{{ t('profile.newPassword') }}</label>
            <input v-model="pwForm.newPassword" type="password" class="field-input" />
          </div>
          <div class="pw-field">
            <label class="pw-label">{{ t('profile.confirmPassword') }}</label>
            <input v-model="pwForm.confirmPassword" type="password" class="field-input" />
          </div>
          <JdButton @click="changePwd" :loading="changingPwd" :disabled="changingPwd">{{ t('profile.confirmChange') }}</JdButton>
        </div>
      </div>

      <!-- Logout -->
      <div class="logout-section">
        <JdButton type="ghost" block @click="showLogout = true">{{ t('profile.logoutTitle') }}</JdButton>
      </div>

      <!-- Logout confirm -->
      <div v-if="showLogout" class="confirm-overlay" @click.self="showLogout = false">
        <div class="confirm-dialog">
          <p class="confirm-msg">{{ t('profile.logoutConfirm') }}</p>
          <div class="confirm-btns">
            <JdButton type="outline" size="sm" @click="showLogout = false">{{ t('common.cancel') }}</JdButton>
            <JdButton size="sm" @click="doLogout()">{{ t('profile.logoutConfirmBtn') }}</JdButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page { max-width: 900px; margin: 0 auto; }

.profile-header { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); box-shadow: var(--shadow-sm); margin-bottom: var(--space-lg); display: flex; align-items: center; gap: var(--space-lg); }
.avatar { width: 64px; height: 64px; border-radius: 50%; background: var(--jd-red); color: #fff; display: flex; align-items: center; justify-content: center; font-size: var(--font-h1); font-weight: 700; }
.username { font-size: var(--font-xl); }
.member-tag { color: var(--text-tertiary); font-size: var(--font-base); }
.member-benefit { color: var(--jd-red); font-size: var(--font-sm); font-weight: 600; margin-top: 2px; }

/* Order Stats */
.order-stats { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-lg) var(--space-xl); box-shadow: var(--shadow-sm); margin-bottom: var(--space-lg); }
.stats-title { font-size: var(--font-md); font-weight: 700; margin-bottom: var(--space-md); display: flex; justify-content: space-between; align-items: center; cursor: pointer; }
.stats-arrow { color: var(--text-tertiary); font-size: var(--font-sm); }
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-sm); }
.stat-item { text-align: center; cursor: pointer; padding: var(--space-sm); border-radius: var(--radius-md); transition: background var(--transition-fast); }
.stat-item:hover { background: var(--bg-hover); }
.stat-num { display: block; font-size: var(--font-xxl); font-weight: 700; color: var(--jd-red); }
.stat-label { display: block; font-size: var(--font-xs); color: var(--text-tertiary); margin-top: 2px; }

.invite-bar { background: linear-gradient(135deg, var(--jd-red-light), var(--jd-red-bg)); border: 1px solid #ffcccc; border-radius: var(--radius-lg); padding: var(--space-lg) var(--space-xl); margin-bottom: var(--space-lg); display: flex; align-items: center; justify-content: space-between; cursor: pointer; box-shadow: var(--shadow-sm); transition: border-color var(--transition-fast); }
.invite-bar:hover { border-color: var(--jd-red); }
.invite-title { font-weight: 700; font-size: var(--font-md); margin-bottom: 2px; }
.invite-desc { font-size: var(--font-xs); color: var(--text-tertiary); }
.invite-btn { padding: 6px var(--space-lg); background: var(--jd-red); color: #fff; border: none; border-radius: var(--radius-round); cursor: pointer; font-size: var(--font-sm); font-weight: 600; white-space: nowrap; }
.invite-btn.copied { background: var(--green); }

.quick-links { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-sm); margin-bottom: var(--space-lg); }
.quick-item { background: var(--bg-white); border-radius: var(--radius-md); padding: var(--space-md) var(--space-sm); text-align: center; cursor: pointer; box-shadow: var(--shadow-sm); transition: transform var(--transition); }
.quick-item:hover { transform: translateY(-2px); }
.quick-icon { font-size: var(--font-xxl); margin-bottom: var(--space-xs); }
.quick-label { font-size: var(--font-xs); color: var(--text-primary); font-weight: 500; }

.tab-bar { display: flex; margin-bottom: var(--space-lg); background: var(--bg-white); border-radius: var(--radius-md); overflow: hidden; box-shadow: var(--shadow-sm); }
.tab { flex: 1; text-align: center; padding: var(--space-md); cursor: pointer; font-size: var(--font-md); transition: all var(--transition-fast); background: var(--bg-white); color: var(--text-secondary); }
.tab.active { background: var(--jd-red); color: #fff; }

.tab-content { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); box-shadow: var(--shadow-sm); min-height: 300px; }
.tab-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.tab-title { font-size: var(--font-lg); font-weight: 600; }

.field-input { padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-md); font-size: var(--font-base); background: var(--bg-white); color: var(--text-primary); }
.field-input:focus { border-color: var(--jd-red); outline: none; }
.field-full { width: 100%; box-sizing: border-box; }
.field-select { cursor: pointer; appearance: auto; }
.field-select:disabled { background: var(--bg-hover); color: var(--text-tertiary); cursor: not-allowed; }

.addr-form { background: var(--bg-hover); padding: var(--space-xl); border-radius: var(--radius-md); margin-bottom: var(--space-lg); }
.addr-form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-md); margin-bottom: var(--space-md); }
.addr-form-footer { display: flex; justify-content: space-between; align-items: center; }
.checkbox-sm { font-size: var(--font-base); cursor: pointer; }
.checkbox-sm input { accent-color: var(--jd-red); }

.addr-card { padding: var(--space-lg); border: 1px solid var(--border-light); border-radius: var(--radius-md); margin-bottom: var(--space-md); position: relative; }
.addr-default-tag { position: absolute; top: 0; right: 0; background: var(--jd-red); color: #fff; font-size: var(--font-xs); padding: 2px 8px; border-radius: 0 var(--radius-md) 0 var(--radius-md); }
.addr-name { font-weight: 600; margin-bottom: var(--space-xs); }
.addr-phone { color: var(--text-tertiary); font-weight: 400; margin-left: var(--space-sm); }
.addr-detail { color: var(--text-secondary); font-size: var(--font-base); }
.addr-actions { margin-top: var(--space-sm); display: flex; gap: var(--space-sm); }
.action-btn { padding: 2px 10px; border: 1px solid var(--border); background: var(--bg-white); border-radius: var(--radius-sm); cursor: pointer; font-size: var(--font-sm); color: var(--text-secondary); }
.action-btn:hover { border-color: var(--jd-red); color: var(--jd-red); }
.action-btn--danger { color: var(--jd-red); }
.action-btn--danger:hover { background: var(--jd-red-light); }

.fav-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--space-md); }
.fav-card { border: 1px solid var(--border-light); border-radius: var(--radius-md); padding: var(--space-md); cursor: pointer; transition: box-shadow var(--transition); }
.fav-card:hover { box-shadow: var(--shadow-md); }
.fav-name { font-size: var(--font-base); text-align: center; color: var(--text-secondary); margin-top: var(--space-sm); }

.coupon-card { display: flex; align-items: center; padding: var(--space-lg); border: 1px solid var(--border-light); border-radius: var(--radius-md); margin-bottom: var(--space-md); }
.coupon-icon { width: 80px; height: 80px; background: linear-gradient(135deg, var(--jd-red), #ff6b6b); color: #fff; display: flex; flex-direction: column; align-items: center; justify-content: center; border-radius: var(--radius-md); margin-right: var(--space-lg); flex-shrink: 0; }
.coupon-name { font-weight: 600; margin-bottom: var(--space-xs); }
.coupon-status { color: var(--text-tertiary); font-size: var(--font-sm); }

.pw-form { max-width: 400px; }
.pw-field { margin-bottom: var(--space-md); }
.pw-label { display: block; font-size: var(--font-base); color: var(--text-secondary); margin-bottom: var(--space-xs); }
.pw-field .field-input { width: 100%; box-sizing: border-box; }

.empty-text { text-align: center; padding: 40px; color: var(--text-tertiary); }

.flex { display: flex; }
.gap-sm { gap: var(--space-sm); }
.fs-12 { font-size: 12px; }
.fs-18 { font-size: 18px; }

@media (max-width: 768px) {
  .profile-page { padding: 0 var(--space-md) calc(80px + env(safe-area-inset-bottom, 0px)); }
  .profile-header { flex-direction: column; text-align: center; padding: var(--space-lg); }
  .quick-links { grid-template-columns: repeat(4, 1fr); gap: var(--space-xs); }
  .quick-label { font-size: 11px; }
  .tab-bar { overflow-x: auto; -webkit-overflow-scrolling: touch; }
  .tab { flex-shrink: 0; min-width: fit-content; padding: var(--space-sm) var(--space-md); font-size: var(--font-sm); }
  .tab-content { padding: var(--space-lg); }
  .tab-header { flex-direction: column; gap: var(--space-sm); }
  .addr-form-grid { grid-template-columns: 1fr; }
  .addr-form-footer { flex-direction: column; gap: var(--space-sm); }
  .fav-grid { grid-template-columns: repeat(2, 1fr); }
  .coupon-card { flex-direction: column; align-items: flex-start; gap: var(--space-sm); }
  .addr-actions { flex-wrap: wrap; }
  .action-btn { padding: 4px 14px; font-size: var(--font-md); }
  .invite-bar { flex-direction: column; gap: var(--space-sm); text-align: center; }
}

.sk-box { padding: var(--space-lg) 0; display: flex; flex-direction: column; gap: var(--space-md); }
.sk-line { height: 16px; background: linear-gradient(90deg, var(--border-light), var(--border), var(--border-light)); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-sm); width: 100%; }
.sk-line.w60 { width: 60%; }
.sk-line.w40 { width: 40%; }
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

.feedback-form { background: var(--bg-white); border-radius: var(--radius-md); padding: var(--space-lg); margin-bottom: var(--space-lg); box-shadow: var(--shadow-sm); }
.feedback-textarea { width: 100%; height: 80px; padding: var(--space-md); border: 1px solid var(--border); border-radius: var(--radius-sm); font-size: var(--font-base); resize: none; box-sizing: border-box; margin-bottom: var(--space-sm); }
.feedback-success { text-align: center; color: var(--green); font-size: var(--font-md); padding: var(--space-md); }

.logout-section { margin-top: var(--space-xxl); }
.confirm-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: var(--bg-overlay); z-index: 400; display: flex; align-items: center; justify-content: center; }
.confirm-dialog { background: var(--bg-white); border-radius: var(--radius-lg); padding: var(--space-xxl); max-width: 320px; width: 90%; text-align: center; box-shadow: var(--shadow-xl); }
.confirm-msg { font-size: var(--font-md); margin-bottom: var(--space-xl); color: var(--text-primary); }
.confirm-btns { display: flex; gap: var(--space-md); justify-content: center; }
.tier-progress { margin-top: var(--space-xs); }
.tier-progress-bar { height: 4px; background: var(--border-light); border-radius: 2px; overflow: hidden; margin-bottom: 2px; max-width: 200px; }
.tier-progress-fill { height: 100%; background: linear-gradient(90deg, var(--jd-red), #ff6b6b); border-radius: 2px; transition: width .6s; }
.tier-progress-label { font-size: 11px; color: var(--text-tertiary); }
</style>
