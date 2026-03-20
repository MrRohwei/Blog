<template>
  <section class="archive-page">
    <header class="card page-header">
      <h1>文章归档</h1>
      <p>按年月整理所有已发布文章</p>
    </header>

    <div class="timeline card">
      <template v-if="loading">
        <div v-for="item in [1, 2, 3]" :key="item" class="month-block skeleton-block">
          <div class="skeleton-line title-line" />
          <div class="skeleton-line" />
          <div class="skeleton-line short" />
        </div>
      </template>
      <div v-else-if="errorMessage" class="empty-hint">{{ errorMessage }}</div>
      <template v-else>
        <div v-if="archives.length === 0" class="empty-hint">暂无归档数据</div>
        <div v-for="item in archives" :key="`${item.year}-${item.month}`" class="month-block">
          <div class="month-title">{{ item.year }} 年 {{ item.month }} 月（{{ item.count }}）</div>
          <ul>
            <li v-for="blog in item.blogs || []" :key="blog.id">
              <span>{{ formatDate(blog.createdAt) }}</span>
              <router-link :to="`/blog/${blog.id}`">{{ blog.title }}</router-link>
            </li>
          </ul>
        </div>
      </template>
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { getArchiveBlogs } from '@/api/blog'

const archives = ref([])
const loading = ref(true)
const errorMessage = ref('')

function formatDate(value) {
  if (!value) {
    return '--'
  }
  return String(value).replace('T', ' ').slice(5, 16)
}

getArchiveBlogs()
  .then((data) => {
    archives.value = data || []
  })
  .catch((error) => {
    errorMessage.value = error?.message || '归档加载失败，请稍后重试'
    archives.value = []
  })
  .finally(() => {
    loading.value = false
  })
</script>

<style scoped>
.archive-page {
  display: grid;
  gap: 14px;
}

.page-header {
  padding: 16px;
}

.page-header p {
  margin: 0;
  color: var(--text-soft);
}

.timeline {
  padding: 18px;
  display: grid;
  gap: 12px;
}

.month-block {
  border-left: 2px solid var(--border);
  padding-left: 14px;
}

.skeleton-block {
  border-left: none;
  padding-left: 0;
}

.skeleton-line {
  height: 14px;
  border-radius: 999px;
  margin-bottom: 10px;
  background: linear-gradient(90deg, #eef3f8 25%, #e4ebf3 37%, #eef3f8 63%);
  background-size: 400% 100%;
  animation: skeleton 1.2s ease infinite;
}

.skeleton-line.title-line {
  height: 18px;
  width: 44%;
}

.skeleton-line.short {
  width: 52%;
}

.month-title {
  color: var(--text-main);
  font-weight: 600;
}

ul {
  margin: 8px 0 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 6px;
}

li {
  display: flex;
  gap: 12px;
  color: var(--text-soft);
  font-size: 14px;
}

li span {
  width: 88px;
}

li a {
  color: var(--accent);
}

@media (max-width: 640px) {
  li {
    flex-direction: column;
    gap: 2px;
  }

  li span {
    width: auto;
  }
}

@keyframes skeleton {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}
</style>
