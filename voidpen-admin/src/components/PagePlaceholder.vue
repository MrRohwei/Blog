<template>
  <section class="placeholder">
    <h2>{{ title }}</h2>
    <p>{{ description }}</p>
  </section>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: '页面骨架已就绪，后续任务将补充完整功能。',
  },
})
</script>

<style scoped>
.placeholder {
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
  background: #f8fafc;
  padding: 24px;
}

.placeholder h2 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #0f172a;
}

.placeholder p {
  margin: 0;
  color: #475569;
}
</style>
