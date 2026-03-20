<template>
  <div class="pagination">
    <button :disabled="page <= 1" @click="$emit('update:page', page - 1)">上一页</button>
    <span>第 {{ page }} 页</span>
    <button :disabled="page >= pageCount" @click="$emit('update:page', page + 1)">下一页</button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  total: {
    type: Number,
    default: 0,
  },
  page: {
    type: Number,
    default: 1,
  },
  size: {
    type: Number,
    default: 10,
  },
})

defineEmits(['update:page'])

const pageCount = computed(() => {
  const count = Math.ceil((props.total || 0) / (props.size || 10))
  return count > 0 ? count : 1
})
</script>

<style scoped>
.pagination {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

button {
  border: 1px solid var(--border);
  background: var(--surface);
  border-radius: 10px;
  padding: 8px 12px;
  color: var(--text-main);
  cursor: pointer;
}

button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

span {
  color: var(--text-soft);
  font-size: 14px;
}
</style>
