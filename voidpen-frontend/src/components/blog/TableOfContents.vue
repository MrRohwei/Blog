<template>
  <section class="card toc-card">
    <h3 class="card-title">目录</h3>
    <ul v-if="items.length" class="toc-list">
      <li v-for="item in items" :key="item.id" :class="[item.level === 3 ? 'level-3' : 'level-2', { active: activeId === item.id }]">
        <button type="button" @click="scrollTo(item.id)">{{ item.text }}</button>
      </li>
    </ul>
    <div v-else class="empty-hint">暂无目录</div>
  </section>
</template>

<script setup>
import { nextTick, onBeforeUnmount, watch } from 'vue'

const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
})

const activeId = defineModel('activeId', {
  type: String,
  default: '',
})

let observer = null

function scrollTo(id) {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function observeHeadings() {
  observer?.disconnect()
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          activeId.value = entry.target.id
        }
      })
    },
    { rootMargin: '-20% 0px -70% 0px' },
  )

  props.items.forEach((item) => {
    const el = document.getElementById(item.id)
    if (el) {
      observer.observe(el)
    }
  })
}

watch(
  () => props.items,
  async () => {
    await nextTick()
    observeHeadings()
  },
  { deep: true, immediate: true },
)

onBeforeUnmount(() => {
  observer?.disconnect()
})
</script>

<style scoped>
.toc-card {
  padding: 14px;
}

.toc-list {
  list-style: none;
  margin: 10px 0 0;
  padding: 0;
  display: grid;
  gap: 4px;
}

li button {
  width: 100%;
  text-align: left;
  border: none;
  background: transparent;
  color: var(--text-soft);
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 8px;
  font-size: 13px;
}

li.level-3 button {
  padding-left: 20px;
}

li.active button {
  color: var(--text-main);
  background: var(--surface-subtle);
}
</style>
