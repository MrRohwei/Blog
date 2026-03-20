import request from './request'

export const getTagPage = (params) => request.get('/admin/v1/tags', { params })

export const getPublicTags = () => request.get('/api/v1/tags')

export const createTag = (data) => request.post('/admin/v1/tags', data)

export const updateTag = (id, data) => request.put(`/admin/v1/tags/${id}`, data)

export const deleteTag = (id) => request.delete(`/admin/v1/tags/${id}`)
