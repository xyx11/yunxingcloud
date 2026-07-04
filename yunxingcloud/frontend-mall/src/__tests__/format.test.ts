import { describe, it, expect } from 'vitest'
import { formatPrice, formatCount, formatRating, formatPercent, formatFileSize, formatDate, formatDateTime } from '@/utils/format'

describe('formatPrice', () => {
  it('formats integer price', () => {
    expect(formatPrice(1299)).toBe('¥1,299')
  })
  it('formats zero', () => {
    expect(formatPrice(0)).toBe('¥0')
  })
  it('formats large price', () => {
    expect(formatPrice(1000000)).toBe('¥1,000,000')
  })
})

describe('formatCount', () => {
  it('formats numbers < 1000', () => {
    expect(formatCount(999)).toBe('999')
  })
  it('formats numbers >= 10000 as wan', () => {
    expect(formatCount(12345)).toBe('1.2万')
  })
  it('formats numbers 1000-9999 with comma', () => {
    expect(formatCount(5000)).toBe('5,000')
  })
})

describe('formatRating', () => {
  it('formats rating to 1 decimal', () => {
    expect(formatRating(4.567)).toBe('4.6')
  })
})

describe('formatPercent', () => {
  it('calculates percentage', () => {
    expect(formatPercent(3, 10)).toBe('30%')
  })
  it('handles zero total', () => {
    expect(formatPercent(1, 0)).toBe('0%')
  })
})

describe('formatFileSize', () => {
  it('formats bytes', () => { expect(formatFileSize(500)).toBe('500B') })
  it('formats KB', () => { expect(formatFileSize(2048)).toBe('2.0KB') })
  it('formats MB', () => { expect(formatFileSize(2 * 1024 * 1024)).toBe('2.0MB') })
})

describe('formatDate', () => {
  it('formats date string', () => {
    const result = formatDate('2026-01-15T10:30:00Z')
    expect(result).toContain('2026')
  })
})

describe('formatDateTime', () => {
  it('formats date time string', () => {
    const result = formatDateTime('2026-01-15T10:30:00Z')
    expect(result).toContain('2026')
    expect(result).toContain('01')
  })
})
