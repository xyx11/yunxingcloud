import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/api/request', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
}))

import request from '@/api/request'
import { listDictTypes, createDictType, updateDictType, deleteDictType, listDictData, deleteDictData } from '@/api/dict'

describe('dict API', () => {
  beforeEach(() => vi.clearAllMocks())

  it('listDictTypes calls /api/dict/types', async () => {
    await listDictTypes()
    expect(request.get).toHaveBeenCalledWith('/api/dict/types')
  })

  it('createDictType calls POST /api/dict/types', async () => {
    await createDictType({ dictName: 'test', dictType: 'test_type' })
    expect(request.post).toHaveBeenCalledWith('/api/dict/types', { dictName: 'test', dictType: 'test_type' })
  })

  it('updateDictType calls PUT with id', async () => {
    await updateDictType(1, { dictName: 'updated' })
    expect(request.put).toHaveBeenCalledWith('/api/dict/types/1', { dictName: 'updated' })
  })

  it('deleteDictType calls DELETE with id', async () => {
    await deleteDictType(5)
    expect(request.delete).toHaveBeenCalledWith('/api/dict/types/5')
  })

  it('listDictData calls /api/dict/data/:type', async () => {
    await listDictData('sys_user_source')
    expect(request.get).toHaveBeenCalledWith('/api/dict/data/sys_user_source')
  })

  it('deleteDictData calls DELETE with id', async () => {
    await deleteDictData(3)
    expect(request.delete).toHaveBeenCalledWith('/api/dict/data/3')
  })
})
