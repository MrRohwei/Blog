<template>
  <section class="card sidebar-card">
    <h3 class="card-title">推荐阅读</h3>
    <ul class="list">
      <li v-for="item in blogs" :key="item.id">
        <router-link :to="`/blog/${item.id}`">{{ item.title }}</router-link>
      </li>
      <li v-if="blogs.length === 0" class="empty-hint">暂无推荐</li>
    </ul>
  </section>
</template>

<script setup>
defineProps({
  blogs: {
    type: Array,
    default: () => [],
  },
})
</script>

<style scoped>
.sidebar-card {
  padding: 14px;
}

.list {
  margin: 10px 0 0;
  padding: 0 0 0 16px;
  display: grid;
  gap: 8px;
}

li {
  color: var(--text-soft);
}

a {
  color: inherit;
}

a:hover {
  color: var(--accent);
}
</style>
