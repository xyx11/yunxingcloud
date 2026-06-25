import { computed } from 'vue'
import { darkTheme, lightTheme } from 'naive-ui'

export function useThemeProvider() {
  const currentTheme = computed(() =>
    localStorage.getItem('theme') === 'dark' ? darkTheme : lightTheme
  )
  return { currentTheme }
}
