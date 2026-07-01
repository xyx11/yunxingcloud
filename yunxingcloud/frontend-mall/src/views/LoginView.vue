<script setup lang="ts">
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const toast = inject<any>('toast')
const form = ref({ username: '', password: '' })
const error = ref('')
const loading = ref(false)

async function doLogin() {
  error.value = ''
  if (!form.value.username || !form.value.password) { error.value = '请填写用户名和密码'; return }
  loading.value = true
  try {
    await auth.login(form.value.username, form.value.password)
    toast.success('登录成功')
    const redirect = router.currentRoute.value.query.redirect as string
    router.push(redirect || '/')
  } catch { error.value = '用户名或密码错误' }
  finally { loading.value = false }
}
</script>

<template>
  <div style="max-width:420px;margin:40px auto">
    <div style="background:#fff;border-radius:12px;padding:40px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <div style="text-align:center;margin-bottom:24px">
        <div style="font-size:48px;margin-bottom:8px">🛒</div>
        <h2 style="font-size:22px;margin-bottom:4px">欢迎登录</h2>
        <p style="color:#999;font-size:13px">YXCLOUD 品质商城</p>
      </div>
      <div v-if="error" style="background:#fff0f0;color:#e4393c;padding:10px 12px;border-radius:6px;font-size:13px;margin-bottom:16px">{{ error }}</div>
      <div style="margin-bottom:16px">
        <label style="display:block;font-size:13px;color:#666;margin-bottom:6px">用户名</label>
        <input v-model="form.username" placeholder="请输入用户名" style="width:100%;padding:12px;border:1px solid #ddd;border-radius:6px;font-size:14px;box-sizing:border-box;outline:none"
               @focus="(e:any) => e.target.style.borderColor='#e4393c'" @blur="(e:any) => e.target.style.borderColor='#ddd'" />
      </div>
      <div style="margin-bottom:24px">
        <label style="display:block;font-size:13px;color:#666;margin-bottom:6px">密码</label>
        <input v-model="form.password" type="password" placeholder="请输入密码" @keyup.enter="doLogin" style="width:100%;padding:12px;border:1px solid #ddd;border-radius:6px;font-size:14px;box-sizing:border-box;outline:none"
               @focus="(e:any) => e.target.style.borderColor='#e4393c'" @blur="(e:any) => e.target.style.borderColor='#ddd'" />
      </div>
      <button @click="doLogin" :disabled="loading" style="width:100%;height:44px;background:#e4393c;color:#fff;border:none;border-radius:8px;font-size:16px;cursor:pointer;font-weight:600;transition:opacity .2s" :style="{opacity:loading?'.7':'1'}">
        {{ loading ? '登录中...' : '登录' }}
      </button>
      <p style="text-align:center;margin-top:16px;font-size:13px;color:#999">还没有账号？<span @click="router.push('/register')" style="color:#e4393c;cursor:pointer;font-weight:600">立即注册</span></p>
      <p style="text-align:center;margin-top:8px;font-size:11px;color:#aaa">演示账号: admin / admin123</p>
    </div>
  </div>
</template>
