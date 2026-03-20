<template>
  <section v-if="!props.hideWhenEmpty || loading || ads.length > 0" class="card ad-widget" :class="widgetClass">
    <template v-if="isTickerMode">
      <div v-if="loading" class="empty-hint">广告加载中...</div>
      <div v-else-if="ads.length === 0" class="empty-hint">暂无广告</div>
      <a
        v-else
        :key="currentAd.id"
        :href="currentAd.linkUrl || '#'"
        target="_blank"
        rel="noreferrer"
        class="ticker-link"
      >
        <span class="ticker-badge">广播📢</span>
        <span class="ticker-title-viewport">
          <span class="ticker-title-track">
            <span class="ticker-title-item">{{ currentAd.title }}</span>
            <span class="ticker-title-item">{{ currentAd.title }}</span>
          </span>
        </span>
        <span class="ticker-index">{{ activeIndex + 1 }}/{{ ads.length }}</span>
      </a>
    </template>

    <template v-else>
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
    </template>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { getAdvertisements } from '@/api/advertisement'

const TICKER_INTERVAL_MS = 6000

const props = defineProps({
  position: {
    type: String,
    default: 'SIDEBAR',
  },
  hideWhenEmpty: {
    type: Boolean,
    default: false,
  },
  displayMode: {
    type: String,
    default: 'card',
  },
})

const ads = ref([])
const loading = ref(false)
const activeIndex = ref(0)
let tickerTimer = null

const title = computed(() =>
  ({
    SIDEBAR: '推荐广告',
    DETAIL_BOTTOM: '赞助内容',
    HEADER: '顶部广告',
    FOOTER: '底部广告',
  })[props.position] || '广告',
)

const isTickerMode = computed(() => props.displayMode === 'ticker')

const widgetClass = computed(() => (isTickerMode.value ? 'ticker-widget' : ''))

const currentAd = computed(() => {
  if (!ads.value.length) {
    return { id: 'none', title: '' }
  }
  return ads.value[activeIndex.value] || ads.value[0]
})

function stopTicker() {
  if (tickerTimer) {
    clearInterval(tickerTimer)
    tickerTimer = null
  }
}

function startTicker() {
  stopTicker()
  if (!isTickerMode.value || ads.value.length <= 1) {
    return
  }
  tickerTimer = setInterval(() => {
    activeIndex.value = (activeIndex.value + 1) % ads.value.length
  }, TICKER_INTERVAL_MS)
}

async function loadAds() {
  loading.value = true
  try {
    ads.value = await getAdvertisements({ position: props.position })
    activeIndex.value = 0
  } finally {
    loading.value = false
    startTicker()
  }
}

watch(() => props.position, loadAds)
watch(() => props.displayMode, startTicker)
watch(() => ads.value.length, startTicker)

onMounted(loadAds)
onBeforeUnmount(stopTicker)
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

.ticker-widget {
  padding: 8px 12px;
  background: linear-gradient(90deg, #e7f1ff 0%, #eef8f4 100%);
}

.ticker-link {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 10px;
  color: #0f172a;
  min-height: 30px;
}

.ticker-badge {
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  padding: 2px 8px;
  animation: ticker-breath 1.8s ease-in-out infinite;
}

.ticker-title-viewport {
  overflow: hidden;
  position: relative;
  min-width: 0;
}

.ticker-title-track {
  display: inline-flex;
  min-width: max-content;
  animation: ticker-scroll 18s linear infinite;
}

.ticker-title-item {
  font-size: 14px;
  font-weight: 600;
  color: #1d4ed8;
  white-space: nowrap;
  padding-right: 60px;
}

.ticker-index {
  color: #64748b;
  font-size: 12px;
}

@keyframes ticker-scroll {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}

@keyframes ticker-breath {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}
</style>
