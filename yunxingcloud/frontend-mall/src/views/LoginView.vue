<script setup lang="ts">
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const toast = inject<any>('toast')
const form = ref({ username: (() => { try { return localStorage.getItem('mall_remember_user') || '' } catch { return '' } })(), password: '' })
const rememberMe = ref(!!form.value.username)
const error = ref('')
const loading = ref(false)

async function doLogin() {
  error.value = ''
  if (!form.value.username || !form.value.password) { error.value = '请填写用户名和密码'; return }
  loading.value = true
  try {
    await auth.login(form.value.username, form.value.password)
    try { if (rememberMe.value) localStorage.setItem('mall_remember_user', form.value.username); else localStorage.removeItem('mall_remember_user') } catch {}
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
      <div v-if="error" style="background:#fff0f0;color:#f10215;padding:10px 12px;border-radius:6px;font-size:13px;margin-bottom:16px">{{ error }}</div>
      <div style="margin-bottom:16px">
        <label style="display:block;font-size:13px;color:#666;margin-bottom:6px">用户名</label>
        <input v-model="form.username" placeholder="请输入用户名" style="width:100%;padding:12px;border:1px solid #ddd;border-radius:6px;font-size:14px;box-sizing:border-box;outline:none"
               @focus="(e:any) => e.target.style.borderColor='#f10215'" @blur="(e:any) => e.target.style.borderColor='#ddd'" />
      </div>
      <div style="margin-bottom:24px">
        <label style="display:block;font-size:13px;color:#666;margin-bottom:6px">密码</label>
        <input v-model="form.password" type="password" placeholder="请输入密码" @keyup.enter="doLogin" style="width:100%;padding:12px;border:1px solid #ddd;border-radius:6px;font-size:14px;box-sizing:border-box;outline:none"
               @focus="(e:any) => e.target.style.borderColor='#f10215'" @blur="(e:any) => e.target.style.borderColor='#ddd'" />
      </div>
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
        <label style="font-size:12px;color:#999;cursor:pointer;display:flex;align-items:center;gap:4px">
          <input type="checkbox" v-model="rememberMe" style="accent-color:#f10215" /> 记住用户名
        </label>
        <span @click="router.push('/forgot-password')" style="font-size:12px;color:#999;cursor:pointer">忘记密码？</span>
      </div>
      <button @click="doLogin" :disabled="loading" style="width:100%;height:44px;background:#f10215;color:#fff;border:none;border-radius:8px;font-size:16px;cursor:pointer;font-weight:600;transition:opacity .2s" :style="{opacity:loading?'.7':'1'}">
        {{ loading ? '登录中...' : '登录' }}
      </button>
      <div style="margin-top:20px">
        <div style="display:flex;align-items:center;gap:12px;margin-bottom:12px">
          <div style="flex:1;height:1px;background:#eee"></div>
          <span style="font-size:12px;color:#999;white-space:nowrap">其他方式登录</span>
          <div style="flex:1;height:1px;background:#eee"></div>
        </div>
        <div style="display:flex;gap:12px;justify-content:center">
          <a href="/oauth2/authorization/wechat" style="display:flex;align-items:center;gap:4px;padding:8px 16px;border:1px solid #07c160;border-radius:20px;cursor:pointer;text-decoration:none;color:#07c160;font-size:12px;transition:all .2s;background:#fff"
             @mouseenter="(e:any) => e.target.style.background='#f0fff4'" @mouseleave="(e:any) => e.target.style.background='#fff'">💬 微信</a>
          <a href="/oauth2/authorization/qq" style="display:flex;align-items:center;gap:4px;padding:8px 16px;border:1px solid #12b7f5;border-radius:20px;cursor:pointer;text-decoration:none;color:#12b7f5;font-size:12px;transition:all .2s;background:#fff"
             @mouseenter="(e:any) => e.target.style.background='#f0f9ff'" @mouseleave="(e:any) => e.target.style.background='#fff'">🐧 QQ</a>
          <a href="/oauth2/authorization/alipay" style="display:flex;align-items:center;gap:4px;padding:8px 16px;border:1px solid #1677ff;border-radius:20px;cursor:pointer;text-decoration:none;color:#1677ff;font-size:12px;transition:all .2s;background:#fff"
             @mouseenter="(e:any) => e.target.style.background='#f0f5ff'" @mouseleave="(e:any) => e.target.style.background='#fff'">🔵 支付宝</a>
        </div>
      </div>
      <p style="text-align:center;margin-top:16px;font-size:13px;color:#999">还没有账号？<span @click="router.push('/register')" style="color:#f10215;cursor:pointer;font-weight:600">立即注册</span></p>
      <p style="text-align:center;margin-top:8px;font-size:11px;color:#aaa">演示账号: admin / admin123</p>
    </div>
  </div>
</template>
