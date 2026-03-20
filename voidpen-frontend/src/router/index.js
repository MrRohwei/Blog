import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/views/layout/FrontLayout.vue'),
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'blog/:id',
        name: 'blog-detail',
        component: () => import('@/views/BlogDetailView.vue'),
        meta: { title: '博客详情' },
      },
      {
        path: 'category/:id',
        name: 'category',
        component: () => import('@/views/CategoryView.vue'),
        meta: { title: '分类' },
      },
      {
        path: 'tag/:id',
        name: 'tag',
        component: () => import('@/views/TagView.vue'),
        meta: { title: '标签' },
      },
      {
        path: 'archive',
        name: 'archive',
        component: () => import('@/views/ArchiveView.vue'),
        meta: { title: '归档' },
      },
      {
        path: 'about',
        name: 'about',
        component: () => import('@/views/AboutView.vue'),
        meta: { title: '关于' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.afterEach((to) => {
  const title = to.meta?.title ? `${to.meta.title} - Voidpen` : 'Voidpen'
  document.title = title
})

export default router
