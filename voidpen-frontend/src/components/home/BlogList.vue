<template>
  <section class="blog-list">
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
  </section>
</template>

<script setup>
defineProps({
  blogs: {
    type: Array,
    default: () => [],
  },
})

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

@media (max-width: 900px) {
  .blog-item {
    grid-template-columns: 1fr;
  }

  .cover-link {
    min-height: 190px;
  }
}
</style>
