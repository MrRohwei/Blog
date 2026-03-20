<template>
  <button class="like-btn" :disabled="liked || loading" @click="handleLike">
    <span>{{ liked ? '已点赞' : '点赞' }}</span>
    <strong>{{ countValue }}</strong>
  </button>
</template>

<script setup>
import { ref, watch } from 'vue'
import { likeBlog } from '@/api/blog'

const props = defineProps({
  blogId: {
    type: Number,
    required: true,
  },
  count: {
    type: Number,
    default: 0,
  },
})

const emit = defineEmits(['liked'])

const loading = ref(false)
const liked = ref(localStorage.getItem(`voidpen_liked_${props.blogId}`) === '1')
const countValue = ref(props.count)

watch(
  () => props.count,
  (value) => {
    countValue.value = value || 0
  },
)

async function handleLike() {
  if (liked.value || loading.value) {
    return
  }
  loading.value = true
  try {
    await likeBlog(props.blogId)
    liked.value = true
    countValue.value += 1
    localStorage.setItem(`voidpen_liked_${props.blogId}`, '1')
    emit('liked')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.like-btn {
  border: none;
  border-radius: 999px;
  background: linear-gradient(120deg, var(--accent), var(--accent-2));
  color: #fff;
  padding: 8px 16px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.like-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

strong {
  font-size: 14px;
}
</style>
