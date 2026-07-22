import { useThemeStore } from '@/stores/theme'

export function useThemeProvider() {
  const store = useThemeStore()
  return { currentTheme: store.currentTheme }
}
