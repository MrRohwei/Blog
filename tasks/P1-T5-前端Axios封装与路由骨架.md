# P1-T5 · 前端 Axios 封装 & 路由骨架

| 字段 | 内容 |
|------|------|
| 阶段 | 阶段一 · 项目基础搭建 |
| 周期 | Week 2 |
| 预估工时 | 2 天 |
| 负责范围 | 前端 |
| 前置依赖 | P1-T1、P1-T4（后端登录接口就绪） |

---

## 任务目标

完成两个前端项目的 HTTP 请求层封装与路由框架，包括：Token 自动注入、401 自动跳转登录、后台管理的登录页面与布局框架。

---

## 详细步骤

### 1. voidpen-admin：request.js 封装

```javascript
// src/api/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
})

// 请求拦截器：注入 Token
request.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    }
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  error => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
```

```javascript
// src/api/auth.js
import request from './request'

export const login = (data) => request.post('/admin/v1/auth/login', data)
export const logout = () => request.post('/admin/v1/auth/logout')
export const getUserInfo = () => request.get('/admin/v1/auth/info')
```

### 2. voidpen-admin：Pinia 用户 Store

```javascript
// src/stores/user.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('voidpen_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('voidpen_user') || 'null'))

  async function loginAction(username, password) {
    const data = await loginApi({ username, password })
    token.value = data.token
    userInfo.value = data.userInfo
    localStorage.setItem('voidpen_token', data.token)
    localStorage.setItem('voidpen_user', JSON.stringify(data.userInfo))
  }

  async function logout() {
    try { await logoutApi() } catch {}
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('voidpen_token')
    localStorage.removeItem('voidpen_user')
  }

  return { token, userInfo, loginAction, logout }
})
```

### 3. voidpen-admin：路由配置 & 登录守卫

```javascript
// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/AdminLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/dashboard/DashboardView.vue'), meta: { title: '数据看板' } },
      { path: 'blog/list',  component: () => import('@/views/blog/BlogListView.vue'),  meta: { title: '博客管理' } },
      { path: 'blog/edit',  component: () => import('@/views/blog/BlogEditView.vue'),  meta: { title: '写博客' } },
      { path: 'blog/edit/:id', component: () => import('@/views/blog/BlogEditView.vue'), meta: { title: '编辑博客' } },
      { path: 'category',   component: () => import('@/views/category/CategoryView.vue'),  meta: { title: '分类管理' } },
      { path: 'tag',        component: () => import('@/views/tag/TagView.vue'),         meta: { title: '标签管理' } },
      { path: 'comment',    component: () => import('@/views/comment/CommentView.vue'), meta: { title: '评论管理' } },
      { path: 'user',       component: () => import('@/views/user/UserView.vue'),       meta: { title: '用户管理' } },
      { path: 'banner',     component: () => import('@/views/banner/BannerView.vue'),   meta: { title: '轮播图' } },
      { path: 'advertisement', component: () => import('@/views/advertisement/AdvertisementView.vue'), meta: { title: '广告管理' } },
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫
router.beforeEach((to) => {
  if (to.meta.requiresAuth !== false) {
    const userStore = useUserStore()
    if (!userStore.token) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }
})

export default router
```

### 4. voidpen-admin：登录页面

```vue
<!-- src/views/login/LoginView.vue -->
<template>
  <div class="login-wrap">
    <div class="login-card">
      <div class="login-logo">
        <h1>Voidpen</h1>
        <p>虚笔 · 后台管理</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码"
                    size="large" prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="handleLogin">
          登 录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.loginAction(form.username, form.password)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } finally {
    loading.value = false
  }
}
</script>
```

### 5. voidpen-frontend：request.js 封装

```javascript
// src/api/request.js（前台，无 Token 注入逻辑）
import axios from 'axios'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
})

request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code === 200) return data
    return Promise.reject(new Error(message || '请求失败'))
  },
  error => Promise.reject(error)
)

export default request
```

### 6. AdminLayout 框架组件

完成侧边栏导航 + 顶部 Header 的基础布局框架（内容页面留空，后续阶段填充）。

侧边栏菜单项对应路由：数据看板、博客管理、分类管理、标签管理、评论管理、用户管理、轮播图、广告管理。

---

## 验收标准

- [ ] `voidpen-admin` 访问 `http://localhost:5174` 自动跳转至 `/login`
- [ ] 输入正确的用户名密码后成功跳转至 `/dashboard`，顶部显示用户昵称
- [ ] 刷新页面后仍保持登录状态（Token 持久化至 localStorage）
- [ ] Token 失效或手动清除 localStorage 后刷新页面跳回登录页
- [ ] 点击登出后清除 Token，跳回登录页
- [ ] `voidpen-frontend` `request.js` 封装完毕，可在控制台发起一次 GET 请求不报错
- [ ] `.env.development` 中配置 `VITE_API_BASE_URL=http://localhost:8080`
