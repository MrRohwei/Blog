import request from '@/api/request'

export const getBanners = () => request.get('/api/v1/banners')
