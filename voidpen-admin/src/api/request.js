import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import pinia from '@/stores'
import { useUserStore } from '@/stores/user'

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

request.interceptors.request.use((config) => {
  const userStore = useUserStore(pinia)
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
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

    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      const userStore = useUserStore(pinia)
      userStore.clearAuth()
      router.replace('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误，请稍后重试')
    }
    return Promise.reject(error)
  },
)

export default request
