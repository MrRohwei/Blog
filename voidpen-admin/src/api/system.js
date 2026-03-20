import request from './request'

export const getConfigGroups = () => request.get('/admin/v1/system/config/groups')

export const getConfigGroupItems = (group) => request.get(`/admin/v1/system/config/group/${group}`)

export const updateConfigItem = (payload) => request.put('/admin/v1/system/config/item', payload)

export const getMonitorOverview = () => request.get('/admin/v1/system/monitor/overview')

export const getCacheStats = () => request.get('/admin/v1/system/cache/stats')

export const evictCachePrefix = (prefix) => request.post('/admin/v1/system/cache/evict-prefix', { prefix })

export const clearAllCache = () => request.post('/admin/v1/system/cache/clear-all')
