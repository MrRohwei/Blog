import request from './request'

export const getAdvertisementPage = (params) =>
  request.get('/admin/v1/advertisements', { params })

export const getPublicAdvertisements = (params) =>
  request.get('/api/v1/advertisements', { params })

export const createAdvertisement = (data) => request.post('/admin/v1/advertisements', data)

export const updateAdvertisement = (id, data) =>
  request.put(`/admin/v1/advertisements/${id}`, data)

export const deleteAdvertisement = (id) => request.delete(`/admin/v1/advertisements/${id}`)

export const updateAdvertisementStatus = (id, status) =>
  request.put(`/admin/v1/advertisements/${id}/status`, { status })
