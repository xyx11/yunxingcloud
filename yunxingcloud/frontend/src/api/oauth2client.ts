import request from '@/api/request'

export async function fetchOAuth2Clients() {
  return request.get('/api/oauth2/clients')
}

export async function createOAuth2Client(data: Record<string, unknown>) {
  return request.post('/api/oauth2/clients', data)
}

export async function updateOAuth2Client(id: string, data: Record<string, unknown>) {
  return request.put(`/api/oauth2/clients/${id}`, data)
}

export async function deleteOAuth2Client(id: number) {
  return request.delete(`/api/oauth2/clients/${id}`)
}
