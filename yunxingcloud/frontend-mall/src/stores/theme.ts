import { defineStore } from 'pinia'
import { ref } from 'vue'

function getInitialDark(): boolean {
  try {
    const saved = localStorage.getItem('mall_theme')
    if (saved) return saved === 'dark'
  } catch {}
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? false
}

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(getInitialDark())

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

  // 监听系统主题变化（仅在用户未手动设置时跟随）
  const mq = window.matchMedia?.('(prefers-color-scheme: dark)')
  if (mq) {
    mq.addEventListener('change', (e) => {
      try { if (!localStorage.getItem('mall_theme')) { isDark.value = e.matches; apply() } } catch {}
    })
  }

  return { isDark, toggle }
})
