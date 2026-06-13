import { createI18n } from 'vue-i18n'
import zh from './zh'
import en from './en'

const saved = localStorage.getItem('locale') || 'zh'

export const i18n = createI18n({
  legacy: false,
  locale: saved,
  fallbackLocale: 'zh',
  messages: { zh, en },
})

export function switchLocale(locale: 'zh' | 'en') {
  i18n.global.locale.value = locale
  localStorage.setItem('locale', locale as string)
}
