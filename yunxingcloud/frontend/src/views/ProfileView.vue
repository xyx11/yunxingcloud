<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { fetchUserInfo, changePassword as changePasswordApi } from '@/api/auth'
import { fetchSocialAccounts, unbindSocialAccount, type SocialAccount } from '@/api/user'
import { uploadFile } from '@/api/file'
import { useNotify } from '@/composables/useNotify'
import { NCard, NTag, NSpace, NDescriptions, NDescriptionsItem, NButton, NCode, NUpload, NModal, NForm, NFormItem, NInput, NPopconfirm } from 'naive-ui'

const { t } = useI18n()

const router = useRouter()
const notify = useNotify()
const user = ref<{username:string,nickname:string,email:string,registerSource:string,authorities:string[],avatarUrl?:string,lastLoginTime?:string} | null>(null)
const showToken = ref(false)
const tokenPreview = ref('')
const avatarUrl = ref('')
const storage = globalThis.localStorage
const accessToken = computed(() => storage.getItem('accessToken') || '')
const showPwdModal = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdLoading = ref(false)
const themeColors = ['#667eea','#43e97b','#f093fb','#4facfe','#ff9a9e','#ffd700']
const currentColor = ref(localStorage.getItem('themeColor') || '#667eea')
const pageSizeSetting = ref(Number(localStorage.getItem('pageSize') || '10'))
const socialAccounts = ref<SocialAccount[]>([])
const socialLoading = ref(false)

const socialProviderColors: Record<string, string> = { wechat: '#07C160', qq: '#12B7F5', alipay: '#1677FF' }
const socialProviderNames: Record<string, string> = { wechat: '微信', qq: 'QQ', alipay: '支付宝' }
const socialProviderSvgs: Record<string, string> = {
  wechat: 'M8.5 11.5c-1.5 0-2.7-.5-3.5-1.5l.7-2.3c.5 1 1.5 1.5 2.8 1.5 1.7 0 2.8-1 2.8-2.3 0-1.2-.9-2-2.5-2-.8 0-1.5.2-2 .5l.6-5.5h5.5l-.3 1.5h-4l-.3 2.3c.5-.2 1.2-.3 2-.3 2.5 0 4 1.3 4 3.3 0 2.2-1.8 3.8-4.5 3.8z',
  qq: 'M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm1 14c-2.5 0-4.5-2-4.5-4.5S10.5 7 13 7s4.5 2 4.5 4.5S15.5 16 13 16zm0-1.5c-1.7 0-3-1.3-3-3s1.3-3 3-3 3 1.3 3 3-1.3 3-3 3z',
  alipay: 'M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm-1 6.5c-1.5 0-3 .5-4 1.5l.5-1c.5-.8 1.3-1.3 2.2-1.3 1.2 0 2 .5 2.5 1.3.3-.2.8-.5 1.3-.5 1 0 1.8.5 2.3 1.3h-2.5c-.5 0-1 .3-1.3.7.5.5 1 1 1.8 1 1.2 0 2.2-.5 3-1.3-.2 1.5-1 3-2.5 3-1 0-2-.5-2.5-1.5-.5.5-1.2.8-2 .8-1.3 0-2.3-.8-2.5-1.8.5.3 1 .5 1.5.5.8 0 1.5-.5 2-1-.3-.5-.5-1.2-.5-2 0-1 .5-2 1.5-2.5.3.5.8 1 1.5 1 .7 0 1.2-.3 1.5-.8-.3-.5-.8-1-1.5-1zM16 15.5c1 0 1.8-.5 2.3-1.3-.3 1-1 2.3-2.3 2.3-.5 0-1-.3-1.3-.7.3-.2.8-.3 1.3-.3z',
}

async function loadSocialAccounts() {
  socialLoading.value = true
  try { socialAccounts.value = await fetchSocialAccounts() } catch(e) { console.warn('加载社交账号失败:', e) }
  socialLoading.value = false
}

async function unbindSocial(id: number) {
  await unbindSocialAccount(id)
  notify.success('已解绑')
  loadSocialAccounts()
}

function bindSocial(provider: string) {
  const redirect = encodeURIComponent(window.location.origin + '/#/profile')
  window.location.href = `/oauth2/authorization/${provider}?redirect_uri=${redirect}`
}

function setThemeColor(color: string) {
  currentColor.value = color
  localStorage.setItem('themeColor', color)
  document.documentElement.style.setProperty('--primary', color)
}

async function changePassword() {
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    notify.error(t('validate.passwordMismatch'))
    return
  }
  if (pwdForm.value.newPassword.length < 8) {
    notify.error(t('validate.passwordMinLen'))
    return
  }
  pwdLoading.value = true
  try {
    await changePasswordApi(pwdForm.value.oldPassword, pwdForm.value.newPassword)
    notify.success(t('pwd.success'))
    showPwdModal.value = false
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    notify.error(err.response?.data?.message || t('profile.changeFailed'))
  }
  pwdLoading.value = false
}

onMounted(async () => {
  const userRes = await fetchUserInfo()
  user.value = userRes as { username: string; nickname: string; email: string; registerSource: string; authorities: string[]; avatarUrl?: string; lastLoginTime?: string }
  avatarUrl.value = (userRes as { avatarUrl?: string }).avatarUrl || ''
  const tok = localStorage.getItem('accessToken') || ''
  tokenPreview.value = tok ? tok.substring(0, 40) + '...' : ''
  loadSocialAccounts()
})

function copyToken() {
  const token = localStorage.getItem('accessToken') || ''
  navigator.clipboard.writeText(token).then(() => notify.success(t('profile.tokenCopied')))
}

function openHealth() { window.open('/actuator/health') }
function openInfo() { window.open('/actuator/info') }

async function handleUpload({ file }: { file: { file: File } }) {
  const form = new FormData()
  form.append('file', file.file)
  const res = await uploadFile(form)
  if (res.data.success) {
    avatarUrl.value = res.data.url
    notify.success(t('profile.uploadSuccess'))
  }
}
</script>

<template>
  <div class="view-pad view-max-w">
    <n-card :title="t('profile.title')">
      <n-space align="center" class="mb-16">
        <div :style="{width:'64px',height:'64px',borderRadius:'50%',overflow:'hidden',background:'linear-gradient(135deg,#667eea,#764ba2)',display:'flex',alignItems:'center',justifyContent:'center',fontSize:'28px',color:'#fff',fontWeight:600}">
          <img v-if="avatarUrl" :src="avatarUrl" class="avatar-image">
          <span v-else>{{ user?.username?.charAt(0)?.toUpperCase() }}</span>
        </div>
        <n-upload :show-file-list="false" accept="image/*" :custom-request="handleUpload">
          <n-button size="small">{{ t('profile.avatar') }}</n-button>
        </n-upload>
      </n-space>
      <n-descriptions v-if="user" bordered :column="2" label-placement="left">
        <n-descriptions-item :label="t('user.username')">{{ user.username }}</n-descriptions-item>
        <n-descriptions-item :label="t('user.nickname')">{{ user.nickname || '-' }}</n-descriptions-item>
        <n-descriptions-item :label="t('user.email')">{{ user.email || '-' }}</n-descriptions-item>
        <n-descriptions-item :label="t('user.lastLogin')">{{ user.lastLoginTime || "-" }}</n-descriptions-item>
        <n-descriptions-item :label="t('user.source')">
          <n-tag :type="user.registerSource === 'local' ? 'info' : 'success'" size="small">
            {{ user.registerSource === 'local' ? t('user.localRegister') : user.registerSource }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item :label="t('user.roleCount')">{{ user.authorities?.filter((a: string) => a.startsWith('ROLE_')).length || 0 }}</n-descriptions-item>
        <n-descriptions-item :label="t('user.permCount')">{{ user.authorities?.filter((a: string) => !a.startsWith('ROLE_')).length || 0 }}</n-descriptions-item>
      </n-descriptions>
    </n-card>

    <n-card :title="t('profile.permissionList')" class="mt-16">
      <n-space v-if="user?.authorities?.length">
        <n-tag
          v-for="p in user.authorities" :key="p"
          :type="p.startsWith('ROLE_') ? 'warning' : 'info'" size="small"
        >
          {{ p }}
        </n-tag>
      </n-space>
      <span v-else class="text-muted">{{ t('profile.noPermissions') }}</span>
    </n-card>

    <n-card :title="t('profile.token')" class="mt-16">
      <n-space vertical>
        <n-space align="center">
          <n-code v-if="showToken" :code="accessToken" language="text" class="max-w-code" />
          <span v-else class="text-muted">{{ tokenPreview }}</span>
          <n-button size="small" @click="showToken = !showToken">{{ showToken ? t('common.hide') : t('common.view') }}</n-button>
          <n-button size="small" @click="copyToken">{{ t('common.copy') }}</n-button>
        </n-space>
      </n-space>
    </n-card>

    <n-card :title="t('profile.accountSecurity')" class="mt-16">
      <n-button size="small" type="warning" @click="showPwdModal = true">{{ t('pwd.change') }}</n-button>
    </n-card>

    <n-card title="第三方账号绑定" class="mt-16">
      <n-space vertical>
        <n-space v-if="socialAccounts.length">
          <n-tag
            v-for="sa in socialAccounts" :key="sa.id"
            :color="{ color: socialProviderColors[sa.provider] || '#999', textColor: '#fff' }"
            size="medium" round closable @close="unbindSocial(sa.id)"
          >
            {{ socialProviderNames[sa.provider] || sa.provider }}: {{ sa.nickname }}
          </n-tag>
        </n-space>
        <span v-else class="text-muted-sm">暂未绑定第三方账号</span>
        <n-space class="mt-8">
          <n-button v-for="p in ['wechat','qq','alipay']" :key="p" size="tiny" @click="bindSocial(p)" :style="{borderColor:socialProviderColors[p],color:socialProviderColors[p]}">
            <svg viewBox="0 0 24 24" width="14" height="14" :fill="socialProviderColors[p]" class="social-icon-sm"><path :d="socialProviderSvgs[p]" /></svg>
            绑定{{ socialProviderNames[p] }}
          </n-button>
        </n-space>
      </n-space>
    </n-card>

    <n-card :title="t('profile.quickActions')" class="mt-16">
      <n-space>
        <n-button size="small" @click="router.push('/')">📊 {{ t('nav.dashboard') }}</n-button>
        <n-button size="small" @click="router.push('/swagger')">📄 {{ t('nav.swagger') }}</n-button>
        <n-button size="small" @click="openHealth">❤️ {{ t('profile.healthCheck') }}</n-button>
        <n-button size="small" @click="openInfo">ℹ️ {{ t('monitor.systemInfo') }}</n-button>
        <n-button size="small" @click="router.push('/operlog')">📋 {{ t('nav.operlog') }}</n-button>
      </n-space>
    </n-card>
    <n-card :title="t('profile.settings')" class="mt-16">
      <n-space vertical>
        <n-space align="center">
          <span class="setting-label">{{ t('profile.themeColor') }}</span>
          <n-button v-for="c in themeColors" :key="c" size="tiny" :style="{background:c,width:'28px',height:'28px',borderRadius:'50%',border:currentColor===c?'3px solid #fff':''}" @click="setThemeColor(c)" />
        </n-space>
        <n-space align="center">
          <span class="setting-label">{{ t('profile.pageSize') }}</span>
          <n-select v-model:value="pageSizeSetting" :options="[10,20,50,100].map(n=>({label:n+t('common.items'),value:n}))" size="small" class="filter-select-xs" @update:value="(v:number)=>storage.setItem('pageSize',String(v))" />
        </n-space>
      </n-space>
    </n-card>

    <n-modal v-model:show="showPwdModal" :title="t('pwd.change')" preset="card" display-directive="show" class="modal-md">
      <n-form label-placement="left" label-width="80">
        <n-form-item :label="t('pwd.old')">
          <n-input v-model:value="pwdForm.oldPassword" type="password" />
        </n-form-item>
        <n-form-item :label="t('pwd.new')">
          <n-input v-model:value="pwdForm.newPassword" type="password" :placeholder="t('validate.passwordMinLen')" />
        </n-form-item>
        <n-form-item :label="t('pwd.confirm')">
          <n-input v-model:value="pwdForm.confirmPassword" type="password" :placeholder="t('pwd.confirmPlaceholder')" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showPwdModal = false">{{ t('common.cancel') }}</n-button>
          <n-button type="primary" :loading="pwdLoading" @click="changePassword">{{ t('common.ok') }}</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
.avatar-image { width: 100%; height: 100%; object-fit: cover; }
.text-muted-sm { color: #999; font-size: 13px; }
.setting-label { max-width: 80px; width: 95%; }
.modal-md { max-width: 400px; width: 95%; }
.social-icon-sm { margin-right: 4px; vertical-align: middle; }
</style>
