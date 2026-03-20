<template>
  <button v-show="visible" class="back-top" @click="scrollTop">↑</button>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'

const visible = ref(false)

function handleScroll() {
  visible.value = window.scrollY > 300
}

function scrollTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.back-top {
  position: fixed;
  right: 20px;
  bottom: 20px;
  width: 38px;
  height: 38px;
  border: none;
  border-radius: 999px;
  background: var(--accent);
  color: #fff;
  cursor: pointer;
  box-shadow: var(--shadow-soft);
}
</style>
