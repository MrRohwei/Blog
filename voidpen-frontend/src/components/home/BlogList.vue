<template>
  <section class="blog-list">
    <template v-if="loading">
      <article v-for="item in skeletonList" :key="item" class="blog-item card skeleton-item">
        <div class="cover-skeleton skeleton-block" />
        <div class="content">
          <div class="skeleton-line title-line" />
          <div class="skeleton-line" />
          <div class="skeleton-line" />
          <div class="skeleton-line short" />
        </div>
      </article>
    </template>

    <div v-else-if="errorMessage" class="card empty-hint">
      {{ errorMessage }}
      <button type="button" class="retry-btn" @click="emit('retry')">重新加载</button>
    </div>

    <template v-else>
      <article v-for="item in blogs" :key="item.id" class="blog-item card">
        <router-link :to="`/blog/${item.id}`" class="cover-link">
          <img v-if="item.coverImg" :src="item.coverImg" :alt="item.title" loading="lazy" />
          <div v-else class="cover-placeholder">{{ item.title }}</div>
        </router-link>

        <div class="content">
          <router-link :to="`/blog/${item.id}`" class="title">{{ item.title }}</router-link>
          <p class="summary">{{ item.summary || '暂无摘要，点击阅读全文。' }}</p>

          <div class="meta">
            <router-link :to="`/category/${item.categoryId}`">{{ item.categoryName || '未分类' }}</router-link>
            <span>{{ item.views || 0 }} 阅读</span>
            <span>{{ item.likes || 0 }} 点赞</span>
            <span>{{ formatDate(item.createdAt) }}</span>
          </div>

          <div class="tags">
            <router-link
              v-for="tag in item.tags || []"
              :key="tag.id"
              :to="`/tag/${tag.id}`"
              class="tag-item"
              :style="{ borderColor: tag.color || '#cbd5e1' }"
            >
              {{ tag.name }}
            </router-link>
          </div>
        </div>
      </article>

      <div v-if="blogs.length === 0" class="card empty-hint">暂无文章</div>
    </template>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  blogs: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  errorMessage: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['retry'])

const skeletonList = computed(() => [1, 2, 3])

function formatDate(value) {
  if (!value) {
    return '-'
  }
  const text = String(value)
  return text.includes('T') ? text.slice(0, 10) : text.slice(0, 10)
}
</script>

<style scoped>
.blog-list {
  display: grid;
  gap: 14px;
}

.blog-item {
  display: grid;
  grid-template-columns: 240px 1fr;
  overflow: hidden;
}

.skeleton-item {
  min-height: 180px;
}

.skeleton-block,
.skeleton-line {
  background: linear-gradient(90deg, #eef3f8 25%, #e4ebf3 37%, #eef3f8 63%);
  background-size: 400% 100%;
  animation: skeleton 1.2s ease infinite;
}

.cover-skeleton {
  min-height: 170px;
}

.skeleton-line {
  height: 14px;
  border-radius: 999px;
  margin-bottom: 10px;
}

.title-line {
  height: 18px;
  width: 68%;
}

.skeleton-line.short {
  width: 45%;
}

.cover-link {
  display: block;
  min-height: 170px;
}

.cover-link img,
.cover-placeholder {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  background: linear-gradient(120deg, #d7e7f7, #e7f4f2);
  color: #1f3b57;
  text-align: center;
  font-size: 14px;
}

.content {
  padding: 14px 16px;
}

.title {
  display: inline-block;
  font-size: 22px;
  line-height: 1.3;
  color: var(--text-main);
  margin-bottom: 6px;
}

.summary {
  margin: 4px 0 10px;
  color: var(--text-soft);
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 13px;
  color: var(--text-soft);
}

.meta a {
  color: var(--accent);
}

.tags {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  border: 1px solid;
  border-radius: 999px;
  padding: 3px 9px;
  font-size: 12px;
  color: var(--text-soft);
}

.retry-btn {
  margin-top: 10px;
  border: none;
  border-radius: 999px;
  padding: 6px 14px;
  background: var(--accent);
  color: #fff;
  cursor: pointer;
}

@keyframes skeleton {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}

@media (max-width: 900px) {
  .blog-item {
    grid-template-columns: 1fr;
  }

  .cover-link {
    min-height: 190px;
  }
}
</style>
