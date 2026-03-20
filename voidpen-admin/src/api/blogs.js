import request from './request'

export const getBlogPage = (params) => request.get('/admin/v1/blogs', { params })

export const getAdminBlogDetail = (id) => request.get(`/admin/v1/blogs/${id}`)

export const getPublicBlogPage = (params) => request.get('/api/v1/blogs', { params })

export const getBlogDetail = (id) => request.get(`/api/v1/blogs/${id}`)

export const createBlog = (data) => request.post('/admin/v1/blogs', data)

export const updateBlog = (id, data) => request.put(`/admin/v1/blogs/${id}`, data)

export const deleteBlog = (id) => request.delete(`/admin/v1/blogs/${id}`)

export const updateBlogStatus = (id, status) =>
  request.put(`/admin/v1/blogs/${id}/status`, { status })

export const updateBlogTop = (id, isTop) => request.put(`/admin/v1/blogs/${id}/top`, { isTop })
