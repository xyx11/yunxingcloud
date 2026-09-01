import { describe, it, expect } from 'vitest'
import { useNetworkStatus } from '@/composables/useNetworkStatus'
import { useCartFly } from '@/composables/useCartFly'

describe('useNetworkStatus', () => {
  it('should return initial online status', () => {
    const { isOnline, wasOffline } = useNetworkStatus()
    // navigator.onLine is true in jsdom by default
    expect(typeof isOnline.value).toBe('boolean')
    expect(typeof wasOffline.value).toBe('boolean')
  })
})

describe('useCartFly', () => {
  it('should return flyToCart function', () => {
    const { flyToCart } = useCartFly()
    expect(typeof flyToCart).toBe('function')
  })

  it('should not throw when called without cart target', () => {
    const { flyToCart } = useCartFly()
    const e = new MouseEvent('click', { clientX: 100, clientY: 200 })
    expect(() => flyToCart(e)).not.toThrow()
  })
})