<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { useNotify } from '@/composables/useNotify'
import { NCard, NTag, NSpace, NDescriptions, NDescriptionsItem, NButton, NCode, NUpload } from 'naive-ui'

const router = useRouter()
const notify = useNotify()
const user = ref<{username:string,nickname:string,email:string,registerSource:string,authorities:string[],avatarUrl?:string} | null>(null)
const showToken = ref(false)
const tokenPreview = ref('')
const avatarUrl = ref('')

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
  <div style="padding:24px;max-width:800px;">
    <n-card title="个人中心">
      <n-space align="center" style="margin-bottom:16px">
        <div style="width:64px;height:64px;border-radius:50%;overflow:hidden;background:#f0f0f0;display:flex;align-items:center;justify-content:center;font-size:28px;">
          <img v-if="avatarUrl" :src="avatarUrl" style="width:100%;height:100%;object-fit:cover" />
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
        <n-descriptions-item label="注册来源">
          <n-tag :type="user.registerSource === 'local' ? 'info' : 'success'" size="small">
            {{ user.registerSource === 'local' ? '本地注册' : user.registerSource }}
          </n-tag>
        </n-descriptions-item>
      </n-descriptions>
    </n-card>

    <n-card title="权限列表" style="margin-top:16px">
      <n-space v-if="user?.authorities?.length">
        <n-tag v-for="p in user.authorities" :key="p"
          :type="p.startsWith('ROLE_') ? 'warning' : 'info'" size="small">{{ p }}</n-tag>
      </n-space>
      <span v-else style="color:#999;">暂无权限数据</span>
    </n-card>

    <n-card title="Token 管理" style="margin-top:16px">
      <n-space vertical>
        <n-space align="center">
          <n-code v-if="showToken" :code="localStorage.getItem('accessToken') || ''" language="text" style="max-width:600px" />
          <span v-else style="color:#999;">{{ tokenPreview }}</span>
          <n-button size="small" @click="showToken = !showToken">{{ showToken ? '隐藏' : '查看' }}</n-button>
          <n-button size="small" @click="copyToken">复制</n-button>
        </n-space>
      </n-space>
    </n-card>

    <n-card title="快捷操作" style="margin-top:16px">
      <n-space>
        <n-button size="small" @click="router.push('/')">📊 仪表盘</n-button>
        <n-button size="small" @click="router.push('/swagger')">📄 API 文档</n-button>
        <n-button size="small" @click="window.open('/actuator/health')">❤️ 健康检查</n-button>
        <n-button size="small" @click="router.push('/operlog')">📋 操作日志</n-button>
      </n-space>
    </n-card>
  </div>
</template>
