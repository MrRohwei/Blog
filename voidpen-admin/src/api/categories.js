import request from './request'

export const getCategoryPage = (params) => request.get('/admin/v1/categories', { params })

export const getPublicCategories = () => request.get('/api/v1/categories')

export const createCategory = (data) => request.post('/admin/v1/categories', data)

export const updateCategory = (id, data) => request.put(`/admin/v1/categories/${id}`, data)

export const deleteCategory = (id) => request.delete(`/admin/v1/categories/${id}`)
