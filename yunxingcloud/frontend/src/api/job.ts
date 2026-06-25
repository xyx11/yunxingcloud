import request from '@/api/request'

export interface SysJob {
  id: number
  jobName: string
  jobGroup: string
  invokeTarget: string
  cronExpression: string
  misfirePolicy: string
  concurrent: string
  status: string
  remark?: string
}

export interface SysJobLog {
  id: number
  jobName: string
  jobGroup: string
  invokeTarget: string
  jobMessage: string
  status: string
  exceptionInfo?: string
  startTime?: string
  endTime?: string
}

export async function listJobs() {
  return request.get('/api/job')
}

export async function createJob(data: Partial<SysJob>) {
  return request.post('/api/job', data)
}

export async function updateJob(id: number, data: Partial<SysJob>) {
  return request.put(`/api/job/${id}`, data)
}

export async function deleteJob(id: number) {
  return request.delete(`/api/job/${id}`)
}

export async function runJob(id: number) {
  return request.post(`/api/job/${id}/run`)
}

export async function pauseJob(id: number) {
  return request.post(`/api/job/${id}/pause`)
}

export async function resumeJob(id: number) {
  return request.post(`/api/job/${id}/resume`)
}

export async function getJobLogs(id: number) {
  return request.get(`/api/job/${id}/logs`)
}
