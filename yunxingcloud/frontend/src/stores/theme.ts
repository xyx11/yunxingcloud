import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { darkTheme, lightTheme } from 'naive-ui'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(localStorage.getItem('theme') === 'dark')

  const currentTheme = computed(() => isDark.value ? darkTheme : lightTheme)

  function toggleTheme() {
    isDark.value = !isDark.value
    localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
    document.documentElement.setAttribute('theme', isDark.value ? 'dark' : 'light')
  }

  return { isDark, currentTheme, toggleTheme }
})
