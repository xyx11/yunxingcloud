import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/api/request', () => ({
  default: {
    post: vi.fn(),
  },
}))

import request from '@/api/request'
import { uploadFile } from '@/api/file'

describe('file API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('uploadFile calls correct endpoint', async () => {
    const form = new FormData()
    form.append('file', new Blob(['test']), 'test.png')
    await uploadFile(form)
    expect(request.post).toHaveBeenCalledWith('/api/files/upload', form)
  })
})