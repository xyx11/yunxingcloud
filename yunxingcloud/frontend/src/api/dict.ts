import request from '@/api/request'

export interface DictType {
  id: number
  dictName: string
  dictType: string
  status: string
  remark?: string
}

export interface DictData {
  id: number
  dictType: string
  dictLabel: string
  dictValue: string
  cssClass: string
  listClass: string
  isDefault: string
  sortOrder: number
  status: string
  remark: string
  createdAt: string
}

// Dict Type APIs
export async function listDictTypes() {
  return request.get('/api/dict/types')
}

export async function getDictType(id: number) {
  return request.get(`/api/dict/types/${id}`)
}

export async function createDictType(data: Partial<DictType>) {
  return request.post('/api/dict/types', data)
}

export async function updateDictType(id: number, data: Partial<DictType>) {
  return request.put(`/api/dict/types/${id}`, data)
}

export async function deleteDictType(id: number) {
  return request.delete(`/api/dict/types/${id}`)
}

// Dict Data APIs
export async function listDictData(dictType: string) {
  return request.get(`/api/dict/data/${dictType}`)
}

export async function createDictData(data: Partial<DictData>) {
  return request.post('/api/dict/data', data)
}

export async function updateDictData(id: number, data: Partial<DictData>) {
  return request.put(`/api/dict/data/${id}`, data)
}

export async function deleteDictData(id: number) {
  return request.delete(`/api/dict/data/${id}`)
}
