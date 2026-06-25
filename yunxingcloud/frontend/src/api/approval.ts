import request from '@/api/request'

export async function fetchMyApprovals() {
  return request.get('/api/approval')
}

export async function fetchPendingApprovals() {
  return request.get('/api/approval/pending')
}

export async function createApproval(data: { approver: string; type: string; title: string; content: string }) {
  return request.post('/api/approval', data)
}

export async function approveRequest(id: number) {
  return request.put(`/api/approval/${id}/approve`, {})
}

export async function rejectRequest(id: number) {
  return request.put(`/api/approval/${id}/reject`, {})
}
