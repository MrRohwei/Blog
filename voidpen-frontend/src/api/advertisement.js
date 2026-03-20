import request from '@/api/request'

export const getAdvertisements = (params) => request.get('/api/v1/advertisements', { params })
