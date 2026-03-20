<template>
  <section class="dashboard">
    <div class="stat-grid">
      <el-card v-for="item in statCards" :key="item.key" shadow="hover" class="stat-card">
        <div class="stat-title">{{ item.title }}</div>
        <div class="stat-value">{{ formatNumber(item.value) }}</div>
      </el-card>
    </div>

    <el-card shadow="never" class="trend-card">
      <template #header>
        <div class="trend-header">
          <h3>近 30 天访问趋势</h3>
          <el-button link @click="loadTrend">刷新</el-button>
        </div>
      </template>

      <div v-if="hasTrendData" class="trend-content">
        <div class="trend-chart-shell">
          <svg class="trend-chart" :viewBox="`0 0 ${chartSize.width} ${chartSize.height}`" preserveAspectRatio="none">
            <defs>
              <linearGradient id="trendAreaGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#3b82f6" stop-opacity="0.26" />
                <stop offset="100%" stop-color="#3b82f6" stop-opacity="0.02" />
              </linearGradient>
            </defs>

            <g v-for="guide in yGuides" :key="guide.y">
              <line
                :x1="chartSize.padding.left"
                :x2="chartSize.width - chartSize.padding.right"
                :y1="guide.y"
                :y2="guide.y"
                class="guide-line"
              />
              <text :x="chartSize.padding.left - 8" :y="guide.y + 4" class="guide-label">
                {{ guide.label }}
              </text>
            </g>

            <path :d="areaPath" fill="url(#trendAreaGradient)" />
            <polyline :points="linePoints" class="trend-line" />
            <circle
              v-for="point in chartPoints"
              :key="point.key"
              :cx="point.x"
              :cy="point.y"
              :r="4"
              class="trend-dot"
            >
              <title>{{ point.tooltip }}</title>
            </circle>
          </svg>

          <div class="x-guides">
            <span
              v-for="label in xGuides"
              :key="label.key"
              :style="{ left: `${label.percent}%` }"
            >
              {{ label.text }}
            </span>
          </div>
        </div>

        <div class="trend-summary">
          <div class="summary-item">
            <div class="summary-label">峰值访问</div>
            <div class="summary-value">{{ formatNumber(peakStat.value) }}</div>
            <div class="summary-desc">{{ peakStat.date || '--' }}</div>
          </div>

          <div class="summary-item">
            <div class="summary-label">日均访问</div>
            <div class="summary-value">{{ formatNumber(avgStat) }}</div>
            <div class="summary-desc">近 30 天平均</div>
          </div>

          <div class="summary-item">
            <div class="summary-label">最新访问</div>
            <div class="summary-value">{{ formatNumber(latestStat.value) }}</div>
            <div class="summary-desc">{{ latestStat.desc }}</div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无趋势数据" />
    </el-card>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getDashboardStats, getDashboardTrend } from '@/api/dashboard'

const chartSize = {
  width: 920,
  height: 320,
  padding: {
    top: 20,
    right: 18,
    bottom: 44,
    left: 46,
  },
}

const stats = ref({
  blogTotal: 0,
  todayViews: 0,
  commentTotal: 0,
  userTotal: 0,
})

const trend = ref({
  dates: [],
  views: [],
})

const trendPairs = computed(() => {
  const dates = Array.isArray(trend.value.dates) ? trend.value.dates : []
  const views = Array.isArray(trend.value.views) ? trend.value.views : []
  const length = Math.min(dates.length, views.length)

  return Array.from({ length }).map((_, index) => ({
    date: dates[index],
    value: Number(views[index] || 0),
  }))
})

const hasTrendData = computed(() => trendPairs.value.length > 0)

const maxView = computed(() => {
  if (!trendPairs.value.length) {
    return 1
  }
  return Math.max(...trendPairs.value.map((item) => item.value), 1)
})

const statCards = computed(() => [
  { key: 'blogTotal', title: '博客总数', value: stats.value.blogTotal },
  { key: 'todayViews', title: '今日访问量', value: stats.value.todayViews },
  { key: 'commentTotal', title: '评论总数', value: stats.value.commentTotal },
  { key: 'userTotal', title: '用户总数', value: stats.value.userTotal },
])

const chartInnerWidth = computed(() => chartSize.width - chartSize.padding.left - chartSize.padding.right)

const chartInnerHeight = computed(() => chartSize.height - chartSize.padding.top - chartSize.padding.bottom)

const chartPoints = computed(() => {
  const total = trendPairs.value.length
  if (!total) {
    return []
  }

  return trendPairs.value.map((item, index) => {
    const x =
      total === 1
        ? chartSize.padding.left + chartInnerWidth.value / 2
        : chartSize.padding.left + (index / (total - 1)) * chartInnerWidth.value
    const y = chartSize.padding.top + (1 - item.value / maxView.value) * chartInnerHeight.value

    return {
      key: `${item.date}-${index}`,
      x: Number(x.toFixed(2)),
      y: Number(y.toFixed(2)),
      tooltip: `${item.date}：${formatNumber(item.value)}`,
    }
  })
})

const linePoints = computed(() => chartPoints.value.map((point) => `${point.x},${point.y}`).join(' '))

const areaPath = computed(() => {
  const points = chartPoints.value
  if (!points.length) {
    return ''
  }

  const bottom = chartSize.padding.top + chartInnerHeight.value
  const first = points[0]
  const last = points[points.length - 1]
  const linePath = points.map((point) => `${point.x} ${point.y}`).join(' L ')

  return `M ${first.x} ${bottom} L ${linePath} L ${last.x} ${bottom} Z`
})

const yGuides = computed(() => {
  const guideCount = 4
  return Array.from({ length: guideCount + 1 }).map((_, index) => {
    const ratio = index / guideCount
    const y = chartSize.padding.top + ratio * chartInnerHeight.value
    const value = Math.round((1 - ratio) * maxView.value)

    return {
      y: Number(y.toFixed(2)),
      label: formatNumber(value),
    }
  })
})

const xGuides = computed(() => {
  const total = trendPairs.value.length
  if (!total) {
    return []
  }

  const sampleSize = Math.min(6, total)
  const indexSet = new Set()

  for (let i = 0; i < sampleSize; i += 1) {
    const index = total === 1 ? 0 : Math.round((i * (total - 1)) / (sampleSize - 1))
    indexSet.add(index)
  }

  return [...indexSet].map((index) => {
    const percent = total === 1 ? 50 : (index / (total - 1)) * 100
    return {
      key: `${trendPairs.value[index].date}-${index}`,
      text: formatDateLabel(trendPairs.value[index].date),
      percent: Number(percent.toFixed(2)),
    }
  })
})

const peakStat = computed(() => {
  if (!trendPairs.value.length) {
    return { value: 0, date: '' }
  }

  let peak = trendPairs.value[0]
  trendPairs.value.forEach((item) => {
    if (item.value > peak.value) {
      peak = item
    }
  })

  return {
    value: peak.value,
    date: peak.date,
  }
})

const avgStat = computed(() => {
  if (!trendPairs.value.length) {
    return 0
  }
  const total = trendPairs.value.reduce((sum, item) => sum + item.value, 0)
  return Math.round(total / trendPairs.value.length)
})

const latestStat = computed(() => {
  if (!trendPairs.value.length) {
    return {
      value: 0,
      desc: '--',
    }
  }

  const length = trendPairs.value.length
  const current = trendPairs.value[length - 1].value
  const prev = length > 1 ? trendPairs.value[length - 2].value : current

  if (length === 1 || prev === current) {
    return {
      value: current,
      desc: '较前一日持平',
    }
  }

  if (prev <= 0) {
    return {
      value: current,
      desc: '前一日为 0',
    }
  }

  const change = ((current - prev) / prev) * 100
  const prefix = change > 0 ? '+' : ''

  return {
    value: current,
    desc: `较前一日 ${prefix}${change.toFixed(1)}%`,
  }
})

function formatNumber(value) {
  return Number(value || 0).toLocaleString()
}

function formatDateLabel(dateText) {
  if (!dateText) {
    return ''
  }

  if (dateText.length >= 10) {
    return dateText.slice(5, 10)
  }

  return dateText
}

async function loadStats() {
  stats.value = await getDashboardStats()
}

async function loadTrend() {
  trend.value = await getDashboardTrend()
}

async function init() {
  try {
    await Promise.all([loadStats(), loadTrend()])
  } catch (error) {
    ElMessage.error(error?.message || '加载看板失败')
  }
}

onMounted(init)
</script>

<style scoped>
.dashboard {
  display: grid;
  gap: 16px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  border: none;
}

.stat-title {
  font-size: 13px;
  color: #64748b;
}

.stat-value {
  margin-top: 12px;
  font-size: 28px;
  font-weight: 600;
  color: #1e293b;
}

.trend-card {
  border-radius: 12px;
}

.trend-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.trend-header h3 {
  margin: 0;
  font-size: 16px;
}

.trend-content {
  display: grid;
  gap: 16px;
}

.trend-chart-shell {
  position: relative;
  height: 320px;
  border: 1px solid #e5edf6;
  border-radius: 12px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
  padding: 10px 10px 24px;
}

.trend-chart {
  width: 100%;
  height: 100%;
}

.guide-line {
  stroke: #dbe5f0;
  stroke-width: 1;
  stroke-dasharray: 3 4;
}

.guide-label {
  fill: #94a3b8;
  font-size: 12px;
  text-anchor: end;
}

.trend-line {
  fill: none;
  stroke: #2563eb;
  stroke-width: 3;
  stroke-linejoin: round;
  stroke-linecap: round;
}

.trend-dot {
  fill: #ffffff;
  stroke: #2563eb;
  stroke-width: 2;
}

.x-guides {
  position: absolute;
  left: 46px;
  right: 18px;
  bottom: 2px;
  height: 20px;
}

.x-guides span {
  position: absolute;
  transform: translateX(-50%);
  color: #94a3b8;
  font-size: 12px;
  white-space: nowrap;
}

.trend-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.summary-item {
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 10px;
  padding: 14px;
}

.summary-label {
  font-size: 12px;
  color: #64748b;
}

.summary-value {
  margin-top: 8px;
  font-size: 24px;
  line-height: 1.1;
  font-weight: 600;
  color: #1d4ed8;
}

.summary-desc {
  margin-top: 8px;
  font-size: 12px;
  color: #475569;
}

@media (max-width: 980px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .trend-summary {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .trend-chart-shell {
    height: 280px;
  }

  .x-guides span {
    font-size: 11px;
  }
}
</style>
