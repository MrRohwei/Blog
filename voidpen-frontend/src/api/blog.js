import request from '@/api/request'

export const getBlogPage = (params) => request.get('/api/v1/blogs', { params })

export const getBlogDetail = (id) => request.get(`/api/v1/blogs/${id}`)

export const getFeaturedBlogs = () => request.get('/api/v1/blogs/featured')

export const getArchiveBlogs = () => request.get('/api/v1/blogs/archive')

export const likeBlog = (id) => request.post(`/api/v1/blogs/${id}/like`)
