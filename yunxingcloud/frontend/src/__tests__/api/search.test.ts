import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/api/request', () => ({
  default: {
    get: vi.fn(),
  },
}))

import request from '@/api/request'
import { globalSearch } from '@/api/search'

describe('search API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('globalSearch calls correct endpoint', async () => {
    await globalSearch('test query')
    expect(request.get).toHaveBeenCalledWith('/api/search', { params: { q: 'test query' } })
  })
})