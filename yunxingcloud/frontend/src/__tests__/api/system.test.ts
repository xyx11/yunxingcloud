import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/api/request', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
}))

import request from '@/api/request'
import { fetchSystemInfo, fetchSessions, revokeSession, clearCache, fetchFlags, saveAnnouncement } from '@/api/system'

describe('system API', () => {
  beforeEach(() => vi.clearAllMocks())

  it('fetchSystemInfo calls GET /api/system/info', async () => {
    await fetchSystemInfo()
    expect(request.get).toHaveBeenCalledWith('/api/system/info')
  })

  it('fetchSessions calls GET /api/system/sessions', async () => {
    await fetchSessions()
    expect(request.get).toHaveBeenCalledWith('/api/system/sessions')
  })

  it('revokeSession calls POST with username', async () => {
    await revokeSession('testuser')
    expect(request.post).toHaveBeenCalledWith('/api/system/sessions/revoke', { username: 'testuser' })
  })

  it('clearCache calls POST /api/system/cache/clear', async () => {
    await clearCache()
    expect(request.post).toHaveBeenCalledWith('/api/system/cache/clear')
  })

  it('fetchFlags calls GET /api/system/flags', async () => {
    await fetchFlags()
    expect(request.get).toHaveBeenCalledWith('/api/system/flags')
  })

  it('saveAnnouncement calls POST with announcement', async () => {
    await saveAnnouncement('hello world')
    expect(request.post).toHaveBeenCalledWith('/api/system/flags', { announcement: 'hello world' })
  })
})
