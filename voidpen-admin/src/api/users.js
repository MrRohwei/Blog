import request from './request'

export const getUserPage = (params) => request.get('/admin/v1/users', { params })

export const createUser = (data) => request.post('/admin/v1/users', data)

export const updateUser = (id, data) => request.put(`/admin/v1/users/${id}`, data)

export const deleteUser = (id) => request.delete(`/admin/v1/users/${id}`)

export const updateUserStatus = (id, status) =>
  request.put(`/admin/v1/users/${id}/status`, { status })
