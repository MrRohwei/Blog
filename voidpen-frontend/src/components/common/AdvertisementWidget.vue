<template>
  <section class="card ad-widget">
    <h3 class="card-title">{{ title }}</h3>
    <div v-if="loading" class="empty-hint">广告加载中...</div>
    <div v-else-if="ads.length === 0" class="empty-hint">暂无广告</div>
    <div v-else class="ad-list">
      <a
        v-for="item in ads"
        :key="item.id"
        :href="item.linkUrl || '#'"
        target="_blank"
        rel="noreferrer"
        class="ad-item"
      >
        <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.title" loading="lazy" />
        <div class="ad-text">
          <h4>{{ item.title }}</h4>
        </div>
      </a>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { getAdvertisements } from '@/api/advertisement'

const props = defineProps({
  position: {
    type: String,
    default: 'SIDEBAR',
  },
})

const ads = ref([])
const loading = ref(false)

const title = computed(() =>
  ({
    SIDEBAR: '推荐广告',
    DETAIL_BOTTOM: '赞助内容',
    HEADER: '顶部广告',
    FOOTER: '底部广告',
  })[props.position] || '广告',
)

async function loadAds() {
  loading.value = true
  try {
    ads.value = await getAdvertisements({ position: props.position })
  } finally {
    loading.value = false
  }
}

watch(() => props.position, loadAds)
onMounted(loadAds)
</script>

<style scoped>
.ad-widget {
  padding: 14px;
}

.ad-list {
  margin-top: 10px;
  display: grid;
  gap: 10px;
}

.ad-item {
  display: grid;
  gap: 8px;
  padding: 8px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--surface-subtle);
}

.ad-item img {
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
  border-radius: 8px;
}

.ad-text h4 {
  margin: 0;
  font-size: 14px;
  color: var(--text-main);
}
</style>
