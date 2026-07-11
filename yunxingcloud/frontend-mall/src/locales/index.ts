import { ref } from 'vue'
import zh from './zh'
import en from './en'

function safeGet(k: string, d: string) { try { return localStorage.getItem(k) || d } catch { return d } }
function safeSet(k: string, v: string) { try { localStorage.setItem(k, v) } catch {} }

const messages: Record<string, Record<string, unknown>> = { zh, en }
const locale = ref(safeGet('locale', 'zh'))

function t(key: string, fallbackOrParams?: string | Record<string, string | number>): string {
  const keys = key.split('.')
  let val: unknown = messages[locale.value]
  for (const k of keys) {
    val = (val as Record<string, unknown> | undefined)?.[k]
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
