import request from '@/api/request'

export async function uploadFile(form: FormData) {
  return request.post('/api/files/upload', form)
}