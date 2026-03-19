import axios from 'axios'

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

    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => Promise.reject(error),
)

export default request
