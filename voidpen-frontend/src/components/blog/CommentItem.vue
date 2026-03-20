<template>
  <article class="comment-item">
    <header class="head">
      <strong>{{ comment.nickname || '匿名' }}</strong>
      <span>{{ formatDate(comment.createdAt) }}</span>
      <button type="button" @click="$emit('reply', comment)">回复</button>
    </header>
    <p>{{ comment.content }}</p>

    <div v-if="comment.children?.length" class="children">
      <CommentItem
        v-for="child in comment.children"
        :key="child.id"
        :comment="child"
        @reply="$emit('reply', $event)"
      />
    </div>
  </article>
</template>

<script setup>
defineProps({
  comment: {
    type: Object,
    required: true,
  },
})

defineEmits(['reply'])

function formatDate(value) {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 16)
}
</script>

<style scoped>
.comment-item {
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 12px;
  background: #fff;
}

.head {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}

.head span {
  color: var(--text-soft);
}

.head button {
  margin-left: auto;
  border: none;
  background: transparent;
  color: var(--accent);
  cursor: pointer;
}

p {
  margin: 8px 0 0;
}

.children {
  margin-top: 10px;
  margin-left: 12px;
  display: grid;
  gap: 8px;
}
</style>
