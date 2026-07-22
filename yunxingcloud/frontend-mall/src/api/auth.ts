import request from './request'

export interface LoginParams { username: string; password: string }
export interface RegisterParams { username: string; password: string; email?: string }

export const login = (params: LoginParams) => request.post('/login', params)
export const register = (params: RegisterParams) => request.post('/register', params)
export const changePassword = (oldPassword: string, newPassword: string) =>
  request.post('/password/change', { oldPassword, newPassword })
export const forgotPassword = (email: string) =>
  request.post('/password/forgot', { email })
export const resetPassword = (token: string, newPassword: string) =>
  request.post('/password/reset', { token, newPassword })
