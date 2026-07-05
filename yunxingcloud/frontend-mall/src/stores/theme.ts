import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref((() => { try { return localStorage.getItem('mall_theme') === 'dark' } catch { return false } })())

  function apply() {
    document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
  }

  function toggle() {
    isDark.value = !isDark.value
    try { localStorage.setItem('mall_theme', isDark.value ? 'dark' : 'light') } catch {}
    apply()
  }

  // 初始化时应用主题
  apply()

  return { isDark, toggle }
})
