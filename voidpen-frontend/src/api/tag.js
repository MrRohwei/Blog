import request from '@/api/request'

export const getTags = () => request.get('/api/v1/tags')
