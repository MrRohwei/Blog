<template>
  <section class="card sidebar-card">
    <h3 class="card-title">标签云</h3>
    <div class="cloud">
      <router-link
        v-for="item in tags"
        :key="item.id"
        :to="`/tag/${item.id}`"
        class="tag-item"
        :style="{ borderColor: item.color || '#cbd5e1' }"
      >
        {{ item.name }}
      </router-link>
      <div v-if="tags.length === 0" class="empty-hint">暂无标签</div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  tags: {
    type: Array,
    default: () => [],
  },
})
</script>

<style scoped>
.sidebar-card {
  padding: 14px;
}

.cloud {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  border: 1px solid;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  color: var(--text-soft);
  background: #fff;
}

.tag-item:hover {
  color: var(--accent);
}
</style>
