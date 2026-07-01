import request from '@/api/request'

export interface SysTicket {
  id: number
  ticketNo: string
  title: string
  content: string
  type: string
  priority: string
  status: string
  applicant: string
  assignee: string
  createdAt: string
  updatedAt: string
}

export function fetchTickets() {
  return request.get('/api/tickets')
}

export function getTicket(id: number) {
  return request.get(`/api/tickets/${id}`)
}

export function createTicket(data: Partial<SysTicket>) {
  return request.post('/api/tickets', data)
}

export function updateTicket(id: number, data: Partial<SysTicket>) {
  return request.put(`/api/tickets/${id}`, data)
}

export function deleteTicket(id: number) {
  return request.delete(`/api/tickets/${id}`)
}

export function assignTicket(id: number, assignee: string) {
  return request.put(`/api/tickets/${id}/assign`, { assignee })
}

export function updateTicketStatus(id: number, status: string) {
  return request.put(`/api/tickets/${id}/status`, { status })
}
