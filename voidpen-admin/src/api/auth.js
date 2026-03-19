import request from './request'

export const login = (data) => request.post('/admin/v1/auth/login', data)

export const logout = () => request.post('/admin/v1/auth/logout')

export const getUserInfo = () => request.get('/admin/v1/auth/info')
