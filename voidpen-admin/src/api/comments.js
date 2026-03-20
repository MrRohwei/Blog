import request from './request'

export const getCommentPage = (params) => request.get('/admin/v1/comments', { params })

export const updateCommentStatus = (id, status) =>
  request.put(`/admin/v1/comments/${id}/status`, { status })

export const deleteComment = (id) => request.delete(`/admin/v1/comments/${id}`)
