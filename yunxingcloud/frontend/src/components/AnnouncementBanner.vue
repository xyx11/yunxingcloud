<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { fetchLatestNotices } from '@/api/notice'
import { fetchFlags } from '@/api/system'
import { NModal, NButton, NSpace } from 'naive-ui'

const { t } = useI18n()

const announcement = ref('')
const noticeContent = ref('')
const bannerType = ref<'info' | 'warning'>('info')
const dismissed = ref(sessionStorage.getItem('announcement-dismissed'))
const showPopup = ref(false)
const popupDismissed = ref(sessionStorage.getItem('popup-dismissed'))

async function fetchAnnouncement() {
  try {
    const noticeRes = await fetchLatestNotices().catch(() => ({ data: [] }))
    if (noticeRes.data && noticeRes.data.length > 0) {
      const notice = noticeRes.data[0]
      announcement.value = notice.noticeTitle
      noticeContent.value = notice.noticeContent || ''
      bannerType.value = notice.noticeType === '1' ? 'info' : 'warning'
      if (!popupDismissed.value) showPopup.value = true
      return
    }
    const res = await fetchFlags().catch(() => ({ data: {} }))
    const text = res.data.announcement || ''
    if (text) {
      if (text.startsWith('[WARN]')) { bannerType.value = 'warning'; announcement.value = text.replace('[WARN]', '').trim() }
      else { bannerType.value = 'info'; announcement.value = text }
    } else { announcement.value = '' }
  } catch { announcement.value = '' }
}

function dismiss() { dismissed.value = 'true'; sessionStorage.setItem('announcement-dismissed', 'true') }
function closePopup() { showPopup.value = false; popupDismissed.value = 'true'; sessionStorage.setItem('popup-dismissed', 'true') }

let timer: ReturnType<typeof setInterval>
onMounted(() => { fetchAnnouncement(); timer = setInterval(fetchAnnouncement, 60000) })
onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <div v-if="announcement && !dismissed" :class="['banner', bannerType]">
    <span class="banner-text banner-clickable" @click="showPopup = true">📢 {{ announcement }}</span>
    <button @click="dismiss" class="close-btn">✕</button>
  </div>
  <n-modal v-model:show="showPopup" preset="card" :title="announcement" style="width:500px">
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div style="line-height:1.8" v-html="noticeContent || t('common.noDetail')" />
    <template #footer>
      <n-space justify="end">
        <n-button type="primary" @click="closePopup">{{ t('common.gotIt') }}</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.banner { display:flex;align-items:center;justify-content:center;gap:12px;padding:8px 16px;color:#fff;font-size:13px;font-weight:500;animation:slideDown .3s ease }
.banner.info { background:linear-gradient(135deg,#667eea,#764ba2) }
.banner.warning { background:linear-gradient(135deg,#f093fb,#f5576c) }
.banner-text { flex:1;text-align:center }
.close-btn { background:rgba(255,255,255,.2);border:none;color:#fff;cursor:pointer;border-radius:4px;padding:2px 6px;font-size:12px;flex-shrink:0 }
.close-btn:hover { background:rgba(255,255,255,.35) }
@keyframes slideDown { from { transform:translateY(-100%) } to { transform:translateY(0) } }
</style>
<style scoped>
.banner-clickable { cursor: pointer; }
.banner-text-wrap { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; flex: 1; min-width: 0; }
.banner-popup-actions { margin-top: 4px; }
</style>
