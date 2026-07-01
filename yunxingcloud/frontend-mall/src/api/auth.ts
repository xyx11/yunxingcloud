import request from './request'

export interface LoginParams { username: string; password: string }
export interface RegisterParams { username: string; password: string; email?: string }

export const login = (params: LoginParams) => request.post('/login', params)
export const register = (params: RegisterParams) => request.post('/register', params)
export const changePassword = (oldPassword: string, newPassword: string) =>
  request.put('/password', { oldPassword, newPassword })
