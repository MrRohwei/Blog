<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-aside">
      <div class="brand">
        <h1>Voidpen</h1>
        <p>虚笔后台管理</p>
      </div>
      <el-menu class="menu" :default-active="activeMenu" router>
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">{{ item.label }}</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item v-for="item in breadcrumbItems" :key="item.path">{{ item.meta.title }}</el-breadcrumb-item>
        </el-breadcrumb>

        <div class="user-area">
          <el-link :href="frontendHomeUrl" target="_blank" type="primary" class="front-link">
            访问前台
          </el-link>
          <span class="nickname">{{ displayName }}</span>
          <el-button link type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { resolveFrontendUrl } from '@/utils/site'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menuItems = [
  { path: '/dashboard', label: '数据看板' },
  { path: '/blog/list', label: '博客管理' },
  { path: '/category', label: '分类管理' },
  { path: '/tag', label: '标签管理' },
  { path: '/comment', label: '评论管理' },
  { path: '/user', label: '用户管理' },
  { path: '/banner', label: '轮播图管理' },
  { path: '/advertisement', label: '广告管理' },
]

const activeMenu = computed(() => {
  if (route.path.startsWith('/blog/edit')) {
    return '/blog/list'
  }
  return route.path
})

const breadcrumbItems = computed(() => route.matched.filter((item) => item.meta?.title))

const displayName = computed(() => {
  return userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员'
})

const frontendHomeUrl = computed(() => resolveFrontendUrl('/'))

async function handleLogout() {
  await userStore.logout()
  ElMessage.success('已退出登录')
  router.replace('/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.admin-aside {
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  color: #dbeafe;
}

.brand {
  padding: 24px 20px;
}

.brand h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #f8fafc;
}

.brand p {
  margin: 8px 0 0;
  font-size: 13px;
  color: #93c5fd;
}

.menu {
  border-right: none;
  background: transparent;
}

:deep(.menu .el-menu-item) {
  color: #cbd5e1;
}

:deep(.menu .el-menu-item.is-active) {
  color: #0f172a;
  background: #e2e8f0;
}

:deep(.menu .el-menu-item:hover) {
  color: #f8fafc;
  background: rgba(148, 163, 184, 0.2);
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e2e8f0;
  background: #fff;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.front-link {
  font-size: 13px;
}

.nickname {
  font-size: 14px;
  color: #334155;
}

.admin-main {
  background: #f8fafc;
}
</style>
