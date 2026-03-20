import request from './request'

export const getBannerPage = (params) => request.get('/admin/v1/banners', { params })

export const getPublicBanners = () => request.get('/api/v1/banners')

export const createBanner = (data) => request.post('/admin/v1/banners', data)

export const updateBanner = (id, data) => request.put(`/admin/v1/banners/${id}`, data)

export const deleteBanner = (id) => request.delete(`/admin/v1/banners/${id}`)

export const updateBannerStatus = (id, status) =>
  request.put(`/admin/v1/banners/${id}/status`, { status })
