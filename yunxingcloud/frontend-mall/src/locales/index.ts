import { ref } from 'vue'
import zh from './zh'
import en from './en'

function safeGet(k: string, d: string) { try { return localStorage.getItem(k) || d } catch { return d } }
function safeSet(k: string, v: string) { try { localStorage.setItem(k, v) } catch {} }

const messages: Record<string, any> = { zh, en }
const locale = ref(safeGet('locale', 'zh'))

function t(key: string, fallbackOrParams?: string | Record<string, any>): string {
  const keys = key.split('.')
  let val: any = messages[locale.value]
  for (const k of keys) {
    val = val?.[k]
    if (val === undefined) {
      if (typeof fallbackOrParams === 'string') return fallbackOrParams
      return key
    }
  }
  if (typeof val === 'string' && typeof fallbackOrParams === 'object') {
    return val.replace(/\{(\w+)\}/g, (_, k) => String(fallbackOrParams[k] ?? `{${k}}`))
  }
  return val as string
}

function setLocale(lang: string) {
  locale.value = lang
  safeSet('locale', lang)
}

export function useI18n() {
  return { t, locale, setLocale }
}
