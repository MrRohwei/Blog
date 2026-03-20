import request from '@/api/request'

export const getCategories = () => request.get('/api/v1/categories')
