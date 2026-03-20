<template>
  <section class="card sidebar-card">
    <h3 class="card-title">分类</h3>
    <div class="list">
      <router-link v-for="item in categories" :key="item.id" :to="`/category/${item.id}`" class="row">
        <span>{{ item.name }}</span>
        <em>{{ item.blogCount || 0 }}</em>
      </router-link>
      <div v-if="categories.length === 0" class="empty-hint">暂无分类</div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  categories: {
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
  margin-top: 8px;
  display: grid;
  gap: 8px;
}

.row {
  display: flex;
  justify-content: space-between;
  color: var(--text-soft);
  padding: 6px 8px;
  border-radius: 8px;
}

.row:hover {
  background: var(--surface-subtle);
}

em {
  font-style: normal;
  color: var(--accent);
}
</style>
