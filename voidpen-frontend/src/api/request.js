import axios from 'axios'
import { pushToast } from '@/utils/toast'

function resolveApiBaseUrl() {
  const raw = String(import.meta.env.VITE_API_BASE_URL || '').trim()
  const normalized = raw.replace(/\/+$/, '')

  if (!normalized) {
    return ''
  }

  // Prevent production from accidentally calling localhost when env is not overridden correctly.
  if (typeof window !== 'undefined') {
    try {
      const target = new URL(normalized, window.location.origin)
      const isTargetLocal = ['localhost', '127.0.0.1'].includes(target.hostname)
      const isCurrentLocal = ['localhost', '127.0.0.1'].includes(window.location.hostname)
      if (isTargetLocal && !isCurrentLocal) {
        return ''
      }
    } catch (_error) {
      // Keep normalized value when URL parsing fails.
    }
  }

  return normalized
}

const request = axios.create({
  baseURL: resolveApiBaseUrl(),
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
