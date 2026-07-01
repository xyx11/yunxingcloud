import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useToast, useGlobalToast } from '@/composables/useToast'

describe('useToast', () => {
  let toast: ReturnType<typeof useToast>

  beforeEach(() => {
    vi.useFakeTimers()
    toast = useToast()
  })

  afterEach(() => {
    vi.advanceTimersByTime(3000)
    vi.useRealTimers()
  })

  it('should add toast via helpers', () => {
    toast.info('hello')
    expect(toast.toasts.value).toHaveLength(1)
    expect(toast.toasts.value[0].message).toBe('hello')
    expect(toast.toasts.value[0].type).toBe('info')
  })

  it('should auto-remove toast after 3 seconds', () => {
    toast.success('ok')
    expect(toast.toasts.value).toHaveLength(1)
    vi.advanceTimersByTime(3000)
    expect(toast.toasts.value).toHaveLength(0)
  })

  it('success helper should set type success', () => {
    toast.success('ok')
    expect(toast.toasts.value[0].type).toBe('success')
  })

  it('error helper should set type error', () => {
    toast.error('fail')
    expect(toast.toasts.value[0].type).toBe('error')
  })
})

describe('useGlobalToast', () => {
  it('should return singleton instance', () => {
    const a = useGlobalToast()
    const b = useGlobalToast()
    expect(a).toBe(b)
  })
})