import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/auth'

const TOKEN_KEY = 'voidpen_token'
const USER_KEY = 'voidpen_user'

function parseUser(rawValue) {
  if (!rawValue) {
    return null
  }
  try {
    return JSON.parse(rawValue)
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref(parseUser(localStorage.getItem(USER_KEY)))

  function setAuth(nextToken, nextUserInfo) {
    token.value = nextToken
    userInfo.value = nextUserInfo
    localStorage.setItem(TOKEN_KEY, nextToken)
    localStorage.setItem(USER_KEY, JSON.stringify(nextUserInfo))
  }

  function clearAuth() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  async function loginAction(username, password) {
    const data = await loginApi({ username, password })
    setAuth(data.token, data.userInfo)
  }

  async function logout() {
    try {
      if (token.value) {
        await logoutApi()
      }
    } catch {
      // 忽略登出接口异常，优先清理本地状态
    } finally {
      clearAuth()
    }
  }

  return {
    token,
    userInfo,
    loginAction,
    logout,
    clearAuth,
  }
})
