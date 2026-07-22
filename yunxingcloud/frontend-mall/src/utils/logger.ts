const LOG_LEVELS = { debug: 0, info: 1, warn: 2, error: 3 } as const
type LogLevel = keyof typeof LOG_LEVELS

const currentLevel: LogLevel = (import.meta.env.VITE_LOG_LEVEL as LogLevel) || 'info'

function log(level: LogLevel, context: string, message: string, data?: unknown) {
  if (LOG_LEVELS[level] < LOG_LEVELS[currentLevel]) return

  const entry = { level, context, message, ts: Date.now(), url: location.pathname, data }
  const consoleFn = level === 'error' ? console.error : level === 'warn' ? console.warn : level === 'debug' ? console.debug : console.log
  consoleFn(`[${context}] ${message}`, data ?? '')

  if (level === 'error' && navigator.sendBeacon) {
    navigator.sendBeacon('/api/analytics/log', new Blob([JSON.stringify(entry)], { type: 'application/json' }))
  }
}

export function createLogger(context: string) {
  return {
    debug: (msg: string, data?: unknown) => log('debug', context, msg, data),
    info: (msg: string, data?: unknown) => log('info', context, msg, data),
    warn: (msg: string, data?: unknown) => log('warn', context, msg, data),
    error: (msg: string, data?: unknown) => log('error', context, msg, data),
  }
}
