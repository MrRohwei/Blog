import axios from 'axios'
import { pushToast } from '@/utils/toast'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data
    const { code, message, data } = payload || {}

    if (typeof code !== 'number') {
      return payload
    }

    if (code === 200) {
      return data
    }

    pushToast(message || '请求失败', 'error')
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    pushToast(error.response?.data?.message || error.message || '网络错误，请稍后重试', 'error')
    return Promise.reject(error)
  },
)

export default request
