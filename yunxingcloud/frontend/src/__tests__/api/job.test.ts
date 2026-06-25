import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('@/api/request', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
}))

import request from '@/api/request'
import { listJobs, createJob, updateJob, deleteJob, runJob, pauseJob, resumeJob, getJobLogs } from '@/api/job'

describe('job API', () => {
  beforeEach(() => vi.clearAllMocks())

  it('listJobs calls GET /api/job', async () => {
    await listJobs()
    expect(request.get).toHaveBeenCalledWith('/api/job')
  })

  it('createJob calls POST /api/job', async () => {
    const job = { jobName: 'test', jobGroup: 'DEFAULT', invokeTarget: 'x.y', cronExpression: '0 * * * * ?' }
    await createJob(job)
    expect(request.post).toHaveBeenCalledWith('/api/job', job)
  })

  it('updateJob calls PUT /api/job/:id', async () => {
    await updateJob(1, { jobName: 'updated' })
    expect(request.put).toHaveBeenCalledWith('/api/job/1', { jobName: 'updated' })
  })

  it('deleteJob calls DELETE /api/job/:id', async () => {
    await deleteJob(3)
    expect(request.delete).toHaveBeenCalledWith('/api/job/3')
  })

  it('runJob calls POST /api/job/:id/run', async () => {
    await runJob(2)
    expect(request.post).toHaveBeenCalledWith('/api/job/2/run')
  })

  it('pauseJob calls POST /api/job/:id/pause', async () => {
    await pauseJob(4)
    expect(request.post).toHaveBeenCalledWith('/api/job/4/pause')
  })

  it('resumeJob calls POST /api/job/:id/resume', async () => {
    await resumeJob(4)
    expect(request.post).toHaveBeenCalledWith('/api/job/4/resume')
  })

  it('getJobLogs calls GET /api/job/:id/logs', async () => {
    await getJobLogs(1)
    expect(request.get).toHaveBeenCalledWith('/api/job/1/logs')
  })
})
