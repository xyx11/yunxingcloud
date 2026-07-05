<script setup lang="ts">
import { ref, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { getAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress, getFavorites, getMyCoupons } from '@/api/order'
import { changePassword } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { useI18n } from '@/locales'
import type { Address } from '@/types'
import { ToastInjectionKey } from '@/composables/useToast'
import LazyImage from '@/components/LazyImage.vue'
import JdButton from '@/components/JdButton.vue'

const router = useRouter()
const auth = useAuthStore()
const { t } = useI18n()
const toast = inject(ToastInjectionKey)!

type TabKey = 'addresses' | 'coupons' | 'favorites' | 'password'

const activeTab = ref<TabKey>('addresses')
const addresses = ref<any[]>([])
const coupons = ref<any[]>([])
const favorites = ref<any[]>([])
const shareCopied = ref(false)
const editAddr = ref<Address | null>(null)
const showAddrForm = ref(false)
const loading = ref(true)

const pwForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const addrForm = ref({ name: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })

const quickLinks = [
  { icon: '⭐', label: t('points.title'), path: '/points' },
  { icon: '🎁', label: t('giftCard.title'), path: '/gift-card' },
  { icon: '🏆', label: '排行榜', path: '/ranking' },
  { icon: '❓', label: '帮助', path: '/help' },
]

async function copyShareLink() {
  try { await navigator.clipboard.writeText(window.location.origin + '/register?ref=' + (auth.user?.username || '')); shareCopied.value = true; toast.success(t('profile.inviteCopied')); setTimeout(() => shareCopied.value = false, 2000) } catch {}
}

onMounted(async () => { if (!auth.isLoggedIn) { router.push('/login'); return }; loadTab() })

const tabCache: Record<string, boolean> = {}

async function loadTab() {
  const key = activeTab.value
  if (tabCache[key]) { loading.value = false; return }
  loading.value = true
  if (key === 'addresses') { try { const r = await getAddresses(); addresses.value = r.data || [] } catch {} }
  else if (key === 'coupons') { try { const r = await getMyCoupons(); coupons.value = r.data || [] } catch {} }
  else if (key === 'favorites') { try { const r = await getFavorites(); favorites.value = r.data || [] } catch {} }
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
  } catch {}
}

function editAddress(addr: any) {
  editAddr.value = addr
  addrForm.value = { name: addr.name, phone: addr.phone, province: addr.province || '', city: addr.city || '', district: addr.district || '', detail: addr.detail || '', isDefault: addr.isDefault }
  showAddrForm.value = true
}

async function deleteAddressById(id: number) { if (!confirm(t('common.confirmDelete'))) return; await deleteAddress(id); toast.info(t('common.deleted')); refreshTab() }

async function changePwd() {
  if (!pwForm.value.oldPassword || !pwForm.value.newPassword) { toast.error(t('register.fillRequired')); return }
  if (pwForm.value.newPassword !== pwForm.value.confirmPassword) { toast.error(t('register.passwordMismatch')); return }
  try { await changePassword(pwForm.value.oldPassword, pwForm.value.newPassword); toast.success(t('toast.passwordChanged')); pwForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' } }
  catch (e: any) { toast.error(e.response?.data?.message || t('common.updateFailed')) }
}

async function toggleDefault(addr: any) { await setDefaultAddress(addr.id, !addr.isDefault); refreshTab() }
</script>

<template>
  <div v-if="auth.isLoggedIn" class="profile-page">
    <!-- Avatar -->
    <div class="profile-header">
      <div class="avatar">{{ auth.user?.username?.charAt(0)?.toUpperCase() }}</div>
      <div>
        <h2 class="username">{{ auth.user?.username }}</h2>
        <p class="member-tag">{{ t('profile.member') }}</p>
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

    <!-- Quick Links -->
    <div class="quick-links">
      <div v-for="l in quickLinks" :key="l.path" class="quick-item" @click="router.push(l.path)">
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
            <input v-model="addrForm.province" :placeholder="t('profile.province')" class="field-input" />
            <input v-model="addrForm.city" :placeholder="t('profile.city')" class="field-input" />
            <input v-model="addrForm.district" :placeholder="t('profile.district')" class="field-input" />
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
        <div v-else-if="!loading" class="empty-text">{{ t('profile.noAddresses') }}</div>
      </div>

      <!-- Favorites -->
      <div v-if="activeTab==='favorites'">
        <h3 class="tab-title">{{ t('profile.favorites') }}</h3>
        <div v-if="favorites.length" class="fav-grid">
          <div v-for="f in favorites" :key="f.id" class="fav-card" @click="router.push(`/product/${f.productId}`)">
            <LazyImage :src="f.imageUrl || ''" alt="" height="100px" rounded="6px" />
            <div class="fav-name">{{ f.productName || '商品 #' + f.productId }}</div>
          </div>
        </div>
        <div v-else-if="!loading" class="empty-text">{{ t('profile.noFavorites') }}</div>
      </div>

      <!-- Coupons -->
      <div v-if="activeTab==='coupons'">
        <h3 class="tab-title">我的优惠券</h3>
        <div v-if="coupons.length">
          <div v-for="c in coupons" :key="c.id" class="coupon-card">
            <div class="coupon-icon"><span style="font-size:12px">{{ t('profile.coupons') }}</span><span style="font-size:18px">#{{ c.couponId }}</span></div>
            <div><div class="coupon-name">优惠券 #{{ c.couponId }}</div><div class="coupon-status">{{ c.status === '0' ? t('profile.unused') : t('profile.used') }}</div></div>
          </div>
        </div>
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
          <JdButton @click="changePwd">{{ t('profile.confirmChange') }}</JdButton>
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
</style>
