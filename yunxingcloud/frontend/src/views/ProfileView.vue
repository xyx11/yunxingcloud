<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NCard, NTag, NSpace, NDescriptions, NDescriptionsItem, NButton, NCode, NUpload, NModal, NForm, NFormItem, NInput } from 'naive-ui'

const router = useRouter()
const notify = useNotify()
const user = ref<{username:string,nickname:string,email:string,registerSource:string,authorities:string[],avatarUrl?:string,lastLoginTime?:string} | null>(null)
const showToken = ref(false)
const tokenPreview = ref('')
const avatarUrl = ref('')
const accessToken = computed(() => localStorage.getItem('accessToken') || '')
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
    notify.error('两次密码输入不一致')
    return
  }
  if (pwdForm.value.newPassword.length < 8) {
    notify.error('新密码至少 8 位')
    return
  }
  pwdLoading.value = true
  try {
    await request.post('/api/password/change', { oldPassword: pwdForm.value.oldPassword, newPassword: pwdForm.value.newPassword })
    notify.success('密码修改成功')
    showPwdModal.value = false
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e: any) {
    notify.error(e.response?.data?.message || '修改失败')
  }
  pwdLoading.value = false
}

onMounted(async () => {
  const userRes = await request.get('/api/user')
  user.value = userRes.data
  avatarUrl.value = userRes.data.avatarUrl || ''
  const t = localStorage.getItem('accessToken') || ''
  tokenPreview.value = t ? t.substring(0, 40) + '...' : ''
})

function copyToken() {
  const t = localStorage.getItem('accessToken') || ''
  navigator.clipboard.writeText(t).then(() => notify.success('Token 已复制到剪贴板'))
}

function openHealth() { window.open('/actuator/health') }
function openInfo() { window.open('/actuator/info') }

async function handleUpload({ file }: any) {
  const form = new FormData()
  form.append('file', file.file)
  const res = await request.post('/api/files/upload', form)
  if (res.data.success) {
    avatarUrl.value = res.data.url
    notify.success('头像上传成功')
  }
}
</script>

<template>
  <div style="padding:20px;max-width:800px;">
    <n-card title="个人中心">
      <n-space align="center" style="margin-bottom:16px">
        <div :style="{width:'64px',height:'64px',borderRadius:'50%',overflow:'hidden',background:'linear-gradient(135deg,#667eea,#764ba2)',display:'flex',alignItems:'center',justifyContent:'center',fontSize:'28px',color:'#fff',fontWeight:600}">
          <img v-if="avatarUrl" :src="avatarUrl" style="width:100%;height:100%;object-fit:cover">
          <span v-else>{{ user?.username?.charAt(0)?.toUpperCase() }}</span>
        </div>
        <n-upload :show-file-list="false" accept="image/*" :custom-request="handleUpload">
          <n-button size="small">更换头像</n-button>
        </n-upload>
      </n-space>
      <n-descriptions v-if="user" bordered :column="2" label-placement="left">
        <n-descriptions-item label="用户名">{{ user.username }}</n-descriptions-item>
        <n-descriptions-item label="昵称">{{ user.nickname || '-' }}</n-descriptions-item>
        <n-descriptions-item label="邮箱">{{ user.email || '-' }}</n-descriptions-item>
        <n-descriptions-item label="最后登录">{{ user.lastLoginTime || "-" }}</n-descriptions-item>
        <n-descriptions-item label="注册来源">
          <n-tag :type="user.registerSource === 'local' ? 'info' : 'success'" size="small">
            {{ user.registerSource === 'local' ? '本地注册' : user.registerSource }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="角色数">{{ user.authorities?.filter((a: string) => a.startsWith('ROLE_')).length || 0 }}</n-descriptions-item>
        <n-descriptions-item label="权限数">{{ user.authorities?.filter((a: string) => !a.startsWith('ROLE_')).length || 0 }}</n-descriptions-item>
      </n-descriptions>
    </n-card>

    <n-card title="权限列表" style="margin-top:16px">
      <n-space v-if="user?.authorities?.length">
        <n-tag
          v-for="p in user.authorities" :key="p"
          :type="p.startsWith('ROLE_') ? 'warning' : 'info'" size="small"
        >
          {{ p }}
        </n-tag>
      </n-space>
      <span v-else style="color:#999;">暂无权限数据</span>
    </n-card>

    <n-card title="Token 管理" style="margin-top:16px">
      <n-space vertical>
        <n-space align="center">
          <n-code v-if="showToken" :code="accessToken" language="text" style="max-width:600px" />
          <span v-else style="color:#999;">{{ tokenPreview }}</span>
          <n-button size="small" @click="showToken = !showToken">{{ showToken ? '隐藏' : '查看' }}</n-button>
          <n-button size="small" @click="copyToken">复制</n-button>
        </n-space>
      </n-space>
    </n-card>

    <n-card title="账号安全" style="margin-top:16px">
      <n-button size="small" type="warning" @click="showPwdModal = true">修改密码</n-button>
    </n-card>

    <n-card title="快捷操作" style="margin-top:16px">
      <n-space>
        <n-button size="small" @click="router.push('/')">📊 仪表盘</n-button>
        <n-button size="small" @click="router.push('/swagger')">📄 API 文档</n-button>
        <n-button size="small" @click="openHealth">❤️ 健康检查</n-button>
        <n-button size="small" @click="openInfo">ℹ️ 系统信息</n-button>
        <n-button size="small" @click="router.push('/operlog')">📋 操作日志</n-button>
      </n-space>
    </n-card>
    <n-card title="个人设置" style="margin-top:16px">
      <n-space vertical>
        <n-space align="center"><span style="width:80px">主题色</span>
          <n-button v-for="c in themeColors" :key="c" size="tiny" :style="{background:c,width:'28px',height:'28px',borderRadius:'50%',border:currentColor===c?'3px solid #fff':''}" @click="setThemeColor(c)" />
        </n-space>
        <n-space align="center"><span style="width:80px">每页条数</span>
          <n-select v-model:value="pageSizeSetting" :options="[10,20,50,100].map(n=>({label:n+'条',value:n}))" size="small" style="width:100px" @update:value="(v:number)=>localStorage.setItem('pageSize',String(v))" />
        </n-space>
      </n-space>
    </n-card>

    <n-modal v-model:show="showPwdModal" title="修改密码" style="width:400px">
      <n-form label-placement="left" label-width="80">
        <n-form-item label="当前密码">
          <n-input v-model:value="pwdForm.oldPassword" type="password" />
        </n-form-item>
        <n-form-item label="新密码">
          <n-input v-model:value="pwdForm.newPassword" type="password" placeholder="至少8位" />
        </n-form-item>
        <n-form-item label="确认密码">
          <n-input v-model:value="pwdForm.confirmPassword" type="password" placeholder="再次输入新密码" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showPwdModal = false">取消</n-button>
          <n-button type="primary" :loading="pwdLoading" @click="changePassword">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
