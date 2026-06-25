import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/api/request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

import request from '@/api/request'
import { fetchGeneratorTables, fetchGeneratorTableColumns, generateCode } from '@/api/generator'

describe('generator API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('fetchGeneratorTables calls correct endpoint', async () => {
    await fetchGeneratorTables()
    expect(request.get).toHaveBeenCalledWith('/api/generator/tables')
  })

  it('fetchGeneratorTableColumns calls correct endpoint', async () => {
    await fetchGeneratorTableColumns('sys_user')
    expect(request.get).toHaveBeenCalledWith('/api/generator/table/sys_user')
  })

  it('generateCode calls correct endpoint', async () => {
    await generateCode('sys_user', 'com.example')
    expect(request.post).toHaveBeenCalledWith('/api/generator/generate/sys_user', { packageName: 'com.example' })
  })
})