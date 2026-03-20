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

      <div v-if="trend.dates.length" class="trend-list">
        <div v-for="(date, index) in trend.dates" :key="date" class="trend-row">
          <span class="trend-date">{{ date }}</span>
          <el-progress
            :percentage="calcPercent(trend.views[index])"
            :stroke-width="12"
            :show-text="false"
            color="#4f46e5"
          />
          <span class="trend-value">{{ trend.views[index] }}</span>
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

const maxView = computed(() => {
  if (!trend.value.views.length) {
    return 1
  }
  return Math.max(...trend.value.views, 1)
})

const statCards = computed(() => [
  { key: 'blogTotal', title: '博客总数', value: stats.value.blogTotal },
  { key: 'todayViews', title: '今日访问量', value: stats.value.todayViews },
  { key: 'commentTotal', title: '评论总数', value: stats.value.commentTotal },
  { key: 'userTotal', title: '用户总数', value: stats.value.userTotal },
])

function formatNumber(value) {
  return Number(value || 0).toLocaleString()
}

function calcPercent(value) {
  return Math.min(100, Math.round((value / maxView.value) * 100))
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

.trend-list {
  display: grid;
  gap: 10px;
}

.trend-row {
  display: grid;
  grid-template-columns: 110px 1fr 80px;
  gap: 12px;
  align-items: center;
}

.trend-date {
  color: #64748b;
  font-size: 13px;
}

.trend-value {
  text-align: right;
  color: #334155;
  font-weight: 500;
}

@media (max-width: 980px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .trend-row {
    grid-template-columns: 1fr;
    gap: 6px;
  }

  .trend-value {
    text-align: left;
  }
}
</style>
