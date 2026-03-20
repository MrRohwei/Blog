import request from './request'

export const getDashboardStats = () => request.get('/admin/v1/dashboard/stats')

export const getDashboardTrend = () => request.get('/admin/v1/dashboard/trend')
