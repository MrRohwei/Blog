import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import pinia from '@/stores'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '数据看板' },
      },
      {
        path: 'blog/list',
        name: 'blog-list',
        component: () => import('@/views/blog/BlogListView.vue'),
        meta: { title: '博客管理' },
      },
      {
        path: 'blog/edit',
        name: 'blog-edit',
        component: () => import('@/views/blog/BlogEditView.vue'),
        meta: { title: '写博客' },
      },
      {
        path: 'blog/edit/:id',
        name: 'blog-edit-with-id',
        component: () => import('@/views/blog/BlogEditView.vue'),
        meta: { title: '编辑博客' },
      },
      {
        path: 'category',
        name: 'category',
        component: () => import('@/views/category/CategoryView.vue'),
        meta: { title: '分类管理' },
      },
      {
        path: 'tag',
        name: 'tag',
        component: () => import('@/views/tag/TagView.vue'),
        meta: { title: '标签管理' },
      },
      {
        path: 'comment',
        name: 'comment',
        component: () => import('@/views/comment/CommentView.vue'),
        meta: { title: '评论管理' },
      },
      {
        path: 'user',
        name: 'user',
        component: () => import('@/views/user/UserView.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'banner',
        name: 'banner',
        component: () => import('@/views/banner/BannerView.vue'),
        meta: { title: '轮播图管理' },
      },
      {
        path: 'advertisement',
        name: 'advertisement',
        component: () => import('@/views/advertisement/AdvertisementView.vue'),
        meta: { title: '广告管理' },
      },
      {
        path: 'system/config',
        name: 'system-config',
        component: () => import('@/views/system/SystemConfigView.vue'),
        meta: { title: '系统配置' },
      },
      {
        path: 'system/monitor',
        name: 'system-monitor',
        component: () => import('@/views/system/SystemMonitorView.vue'),
        meta: { title: '运行监控' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const userStore = useUserStore(pinia)

  if (to.meta.requiresAuth !== false && !userStore.token) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.path === '/login' && userStore.token) {
    return { path: '/dashboard' }
  }

  return true
})

router.afterEach((to) => {
  const title = to.meta?.title ? `${to.meta.title} - Voidpen Admin` : 'Voidpen Admin'
  document.title = title
})

export default router
