import request from '@/api/request'

export async function fetchIpBlacklist() {
  return request.get('/api/ip-blacklist')
}

export async function blockIp(data: { ip: string; reason: string }) {
  return request.post('/api/ip-blacklist', data)
}

export async function unblockIp(id: number) {
  return request.delete(`/api/ip-blacklist/${id}`)
}
