import request from '@/api/request'

export const getComments = (blogId) => request.get(`/api/v1/comments/${blogId}`)

export const postComment = (data) => request.post('/api/v1/comments', data)
