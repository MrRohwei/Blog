<template>
  <section class="banner card" @mouseenter="pause" @mouseleave="resume">
    <div v-if="banners.length === 0" class="empty-hint">暂无轮播图</div>
    <template v-else>
      <a
        v-for="(item, index) in banners"
        :key="item.id"
        :href="item.linkUrl || '#'"
        target="_blank"
        rel="noreferrer"
        class="slide"
        :class="{ active: index === currentIndex }"
      >
        <img :src="item.imageUrl" :alt="item.title || 'banner'" />
        <div class="caption">{{ item.title || 'Voidpen' }}</div>
      </a>

      <div class="dots">
        <button
          v-for="(item, index) in banners"
          :key="item.id"
          :class="{ active: currentIndex === index }"
          @click="currentIndex = index"
        />
      </div>
    </template>
  </section>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'

const props = defineProps({
  banners: {
    type: Array,
    default: () => [],
  },
})

const currentIndex = ref(0)
let timer = null

function start() {
  stop()
  if (props.banners.length <= 1) {
    return
  }
  timer = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % props.banners.length
  }, 4000)
}

function stop() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

function pause() {
  stop()
}

function resume() {
  start()
}

watch(
  () => props.banners.length,
  () => {
    currentIndex.value = 0
    start()
  },
)

onMounted(start)
onUnmounted(stop)
</script>

<style scoped>
.banner {
  position: relative;
  overflow: hidden;
  min-height: 260px;
}

.slide {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity 0.4s ease;
}

.slide.active {
  opacity: 1;
}

.slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.caption {
  position: absolute;
  left: 18px;
  bottom: 14px;
  background: rgba(20, 36, 54, 0.6);
  color: #fff;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 13px;
}

.dots {
  position: absolute;
  right: 14px;
  bottom: 14px;
  display: inline-flex;
  gap: 6px;
}

.dots button {
  width: 8px;
  height: 8px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
}

.dots button.active {
  width: 22px;
  background: #fff;
}
</style>
