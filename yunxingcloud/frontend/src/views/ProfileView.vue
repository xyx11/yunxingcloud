<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NCard, NTag, NSpace, NDescriptions, NDescriptionsItem, NButton, NCode, NUpload, NModal, NForm, NFormItem, NInput } from 'naive-ui'

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
    await request.post('/api/password/change', { oldPassword: pwdForm.value.oldPassword, newPassword: pwdForm.value.newPassword })
    notify.success(t('pwd.success'))
    showPwdModal.value = false
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e: any) {
    notify.error(e.response?.data?.message || t('profile.changeFailed'))
  }
  pwdLoading.value = false
}

onMounted(async () => {
  const userRes = await request.get('/api/user')
  user.value = userRes.data
  avatarUrl.value = userRes.data.avatarUrl || ''
  const tok = localStorage.getItem('accessToken') || ''
  tokenPreview.value = tok ? tok.substring(0, 40) + '...' : ''
})

function copyToken() {
  const token = localStorage.getItem('accessToken') || ''
  navigator.clipboard.writeText(token).then(() => notify.success(t('profile.tokenCopied')))
}

function openHealth() { window.open('/actuator/health') }
function openInfo() { window.open('/actuator/info') }

async function handleUpload({ file }: any) {
  const form = new FormData()
  form.append('file', file.file)
  const res = await request.post('/api/files/upload', form)
  if (res.data.success) {
    avatarUrl.value = res.data.url
    notify.success(t('profile.uploadSuccess'))
  }
}
</script>

<template>
  <div style="padding:20px;max-width:800px;">
    <n-card :title="t('profile.title')">
      <n-space align="center" style="margin-bottom:16px">
        <div :style="{width:'64px',height:'64px',borderRadius:'50%',overflow:'hidden',background:'linear-gradient(135deg,#667eea,#764ba2)',display:'flex',alignItems:'center',justifyContent:'center',fontSize:'28px',color:'#fff',fontWeight:600}">
          <img v-if="avatarUrl" :src="avatarUrl" style="width:100%;height:100%;object-fit:cover">
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

    <n-card :title="t('profile.permissionList')" style="margin-top:16px">
      <n-space v-if="user?.authorities?.length">
        <n-tag
          v-for="p in user.authorities" :key="p"
          :type="p.startsWith('ROLE_') ? 'warning' : 'info'" size="small"
        >
          {{ p }}
        </n-tag>
      </n-space>
      <span v-else style="color:#999;">{{ t('profile.noPermissions') }}</span>
    </n-card>

    <n-card :title="t('profile.token')" style="margin-top:16px">
      <n-space vertical>
        <n-space align="center">
          <n-code v-if="showToken" :code="accessToken" language="text" style="max-width:600px" />
          <span v-else style="color:#999;">{{ tokenPreview }}</span>
          <n-button size="small" @click="showToken = !showToken">{{ showToken ? t('common.hide') : t('common.view') }}</n-button>
          <n-button size="small" @click="copyToken">{{ t('common.copy') }}</n-button>
        </n-space>
      </n-space>
    </n-card>

    <n-card :title="t('profile.accountSecurity')" style="margin-top:16px">
      <n-button size="small" type="warning" @click="showPwdModal = true">{{ t('pwd.change') }}</n-button>
    </n-card>

    <n-card :title="t('profile.quickActions')" style="margin-top:16px">
      <n-space>
        <n-button size="small" @click="router.push('/')">📊 {{ t('nav.dashboard') }}</n-button>
        <n-button size="small" @click="router.push('/swagger')">📄 {{ t('nav.swagger') }}</n-button>
        <n-button size="small" @click="openHealth">❤️ {{ t('profile.healthCheck') }}</n-button>
        <n-button size="small" @click="openInfo">ℹ️ {{ t('monitor.systemInfo') }}</n-button>
        <n-button size="small" @click="router.push('/operlog')">📋 {{ t('nav.operlog') }}</n-button>
      </n-space>
    </n-card>
    <n-card :title="t('profile.settings')" style="margin-top:16px">
      <n-space vertical>
        <n-space align="center">
          <span style="max-width:80px;width:95%">{{ t('profile.themeColor') }}</span>
          <n-button v-for="c in themeColors" :key="c" size="tiny" :style="{background:c,width:'28px',height:'28px',borderRadius:'50%',border:currentColor===c?'3px solid #fff':''}" @click="setThemeColor(c)" />
        </n-space>
        <n-space align="center">
          <span style="max-width:80px;width:95%">{{ t('profile.pageSize') }}</span>
          <n-select v-model:value="pageSizeSetting" :options="[10,20,50,100].map(n=>({label:n+t('common.items'),value:n}))" size="small" style="max-width:100px;width:95%" @update:value="(v:number)=>storage.setItem('pageSize',String(v))" />
        </n-space>
      </n-space>
    </n-card>

    <n-modal v-model:show="showPwdModal" :title="t('pwd.change')" style="max-width:400px;width:95%">
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
