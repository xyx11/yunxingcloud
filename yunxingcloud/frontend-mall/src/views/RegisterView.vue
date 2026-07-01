<script setup lang="ts">
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const toast = inject<any>('toast')
const form = ref({ username: '', password: '', confirmPassword: '' })
const error = ref('')
const loading = ref(false)

async function doRegister() {
  error.value = ''
  if (!form.value.username || !form.value.password) { error.value = '请填写完整信息'; return }
  if (form.value.password !== form.value.confirmPassword) { error.value = '两次密码不一致'; return }
  if (form.value.password.length < 6) { error.value = '密码至少6位'; return }
  loading.value = true
  try {
    await auth.register(form.value.username, form.value.password)
    toast.success('注册成功，请登录')
    router.push('/login')
  } catch (e: any) { error.value = e.response?.data?.message || '注册失败' }
  finally { loading.value = false }
}
</script>

<template>
  <div style="max-width:420px;margin:40px auto">
    <div style="background:#fff;border-radius:12px;padding:40px;box-shadow:0 2px 8px rgba(0,0,0,.06)">
      <div style="text-align:center;margin-bottom:24px">
        <div style="font-size:48px;margin-bottom:8px">🎉</div>
        <h2 style="font-size:22px;margin-bottom:4px">用户注册</h2>
        <p style="color:#999;font-size:13px">注册 YXCLOUD 商城账号</p>
      </div>
      <div v-if="error" style="background:#fff0f0;color:#e4393c;padding:10px 12px;border-radius:6px;font-size:13px;margin-bottom:16px">{{ error }}</div>
      <div style="margin-bottom:16px">
        <label style="display:block;font-size:13px;color:#666;margin-bottom:6px">用户名</label>
        <input v-model="form.username" placeholder="请输入用户名" style="width:100%;padding:12px;border:1px solid #ddd;border-radius:6px;font-size:14px;box-sizing:border-box;outline:none"
               @focus="(e:any) => e.target.style.borderColor='#e4393c'" @blur="(e:any) => e.target.style.borderColor='#ddd'" />
      </div>
      <div style="margin-bottom:16px">
        <label style="display:block;font-size:13px;color:#666;margin-bottom:6px">密码</label>
        <input v-model="form.password" type="password" placeholder="至少6位" style="width:100%;padding:12px;border:1px solid #ddd;border-radius:6px;font-size:14px;box-sizing:border-box;outline:none"
               @focus="(e:any) => e.target.style.borderColor='#e4393c'" @blur="(e:any) => e.target.style.borderColor='#ddd'" />
      </div>
      <div style="margin-bottom:24px">
        <label style="display:block;font-size:13px;color:#666;margin-bottom:6px">确认密码</label>
        <input v-model="form.confirmPassword" type="password" placeholder="再次输入确认" @keyup.enter="doRegister" style="width:100%;padding:12px;border:1px solid #ddd;border-radius:6px;font-size:14px;box-sizing:border-box;outline:none"
               @focus="(e:any) => e.target.style.borderColor='#e4393c'" @blur="(e:any) => e.target.style.borderColor='#ddd'" />
      </div>
      <button @click="doRegister" :disabled="loading" style="width:100%;height:44px;background:#e4393c;color:#fff;border:none;border-radius:8px;font-size:16px;cursor:pointer;font-weight:600;transition:opacity .2s" :style="{opacity:loading?'.7':'1'}">
        {{ loading ? '注册中...' : '注册' }}
      </button>
      <p style="text-align:center;margin-top:16px;font-size:13px;color:#999">已有账号？<span @click="router.push('/login')" style="color:#e4393c;cursor:pointer">立即登录</span></p>
    </div>
  </div>
</template>
