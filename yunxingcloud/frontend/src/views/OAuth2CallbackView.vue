<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NConfigProvider, NResult, NButton, NSpin } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const status = ref<'loading' | 'success' | 'error'>('loading')
const message = ref('')

onMounted(() => {
  const token = route.query.token as string
  const username = route.query.username as string
  const nickname = route.query.nickname as string

  if (token && username) {
    localStorage.setItem('accessToken', token)
    if (nickname) localStorage.setItem('nickname', nickname)
    status.value = 'success'
    message.value = nickname || username
    setTimeout(() => router.push('/'), 1500)
  } else {
    status.value = 'error'
    message.value = 'OAuth2 登录失败：缺少 token'
  }
})
</script>

<template>
  <n-config-provider>
    <div style="min-height:100vh;display:flex;align-items:center;justify-content:center;background:linear-gradient(135deg,#667eea,#764ba2)">
      <n-result
        v-if="status === 'loading'"
        title="正在登录..."
        description="正在通过第三方平台验证您的身份"
      >
        <template #icon>
          <n-spin size="large" />
        </template>
      </n-result>
      <n-result
        v-else-if="status === 'success'"
        status="success"
        :title="`欢迎，${message}`"
        description="登录成功，正在跳转..."
      />
      <n-result
        v-else
        status="error"
        title="登录失败"
        :description="message"
      >
        <template #footer>
          <n-button type="primary" @click="router.push('/login')">返回登录</n-button>
        </template>
      </n-result>
    </div>
  </n-config-provider>
</template>
