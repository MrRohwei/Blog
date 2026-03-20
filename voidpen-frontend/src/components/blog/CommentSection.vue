<template>
  <section class="card comment-section">
    <h3 class="card-title">评论区（{{ comments.length }}）</h3>

    <form class="comment-form" @submit.prevent="submitComment">
      <div v-if="replyTarget" class="reply-hint">
        回复：{{ replyTarget.nickname || '匿名' }}
        <button type="button" @click="clearReply">取消</button>
      </div>

      <div class="guest-fields">
        <input v-model.trim="form.nickname" placeholder="昵称（必填）" maxlength="50" />
        <input v-model.trim="form.email" placeholder="邮箱（必填）" type="email" maxlength="100" />
      </div>

      <textarea v-model.trim="form.content" rows="4" placeholder="说点什么..." maxlength="500" />
      <div class="form-actions">
        <button type="submit" :disabled="submitting">发表评论</button>
      </div>
    </form>

    <div v-if="comments.length" class="comment-list">
      <CommentItem v-for="item in comments" :key="item.id" :comment="item" @reply="setReply" />
    </div>
    <div v-else class="empty-hint">暂无评论，欢迎抢沙发</div>
  </section>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import DOMPurify from 'dompurify'
import CommentItem from '@/components/blog/CommentItem.vue'
import { getComments, postComment } from '@/api/comment'

const props = defineProps({
  blogId: {
    type: Number,
    required: true,
  },
})

const comments = ref([])
const replyTarget = ref(null)
const submitting = ref(false)
const form = reactive({
  nickname: '',
  email: '',
  content: '',
})

function clearReply() {
  replyTarget.value = null
}

function setReply(comment) {
  replyTarget.value = comment
}

async function loadComments() {
  comments.value = await getComments(props.blogId)
}

async function submitComment() {
  if (!form.nickname) {
    window.alert('请填写昵称')
    return
  }
  if (!form.content) {
    window.alert('请输入评论内容')
    return
  }
  if (!form.email) {
    window.alert('请填写邮箱')
    return
  }
  const safeContent = DOMPurify.sanitize(form.content, { ALLOWED_TAGS: [] })
  if (!safeContent) {
    window.alert('评论内容无效')
    return
  }

  submitting.value = true
  try {
    await postComment({
      blogId: props.blogId,
      parentId: replyTarget.value?.id || null,
      content: safeContent,
      nickname: form.nickname,
      email: form.email || undefined,
    })
    form.content = ''
    clearReply()
    window.alert('评论已提交，审核通过后展示')
  } finally {
    submitting.value = false
  }
}

watch(
  () => props.blogId,
  () => {
    loadComments()
  },
  { immediate: true },
)
</script>

<style scoped>
.comment-section {
  padding: 16px;
}

.comment-form {
  margin-top: 12px;
  display: grid;
  gap: 10px;
}

.reply-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid var(--border);
  color: var(--text-soft);
}

.reply-hint button {
  border: none;
  background: transparent;
  color: var(--accent);
  cursor: pointer;
}

.guest-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

input,
textarea {
  width: 100%;
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 10px 12px;
  font: inherit;
  color: inherit;
  background: #fff;
}

textarea {
  resize: vertical;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.form-actions button {
  border: none;
  border-radius: 10px;
  padding: 10px 14px;
  background: var(--accent);
  color: #fff;
  cursor: pointer;
}

.form-actions button:disabled {
  opacity: 0.55;
}

.comment-list {
  margin-top: 14px;
  display: grid;
  gap: 8px;
}

@media (max-width: 640px) {
  .guest-fields {
    grid-template-columns: 1fr;
  }
}
</style>
