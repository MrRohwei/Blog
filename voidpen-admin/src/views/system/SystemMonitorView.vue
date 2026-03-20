<template>
  <section class="monitor-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <h3>运行监控</h3>
          <el-button link @click="refreshAll">刷新</el-button>
        </div>
      </template>

      <div class="overview-meta">
        <span>服务：{{ overview.applicationName || '-' }}</span>
        <span>环境：{{ overview.activeProfile || '-' }}</span>
        <span>运行时长：{{ formatUptime(overview.uptimeSeconds) }}</span>
        <span>采样时间：{{ overview.serverTime || '-' }}</span>
      </div>

      <div class="resource-grid">
        <el-card shadow="never" class="resource-card">
          <div class="resource-title">CPU</div>
          <el-progress :percentage="safePercent(overview.resource?.cpuUsage)" />
        </el-card>
        <el-card shadow="never" class="resource-card">
          <div class="resource-title">内存</div>
          <el-progress :percentage="safePercent(overview.resource?.memoryUsage)" color="#10b981" />
        </el-card>
        <el-card shadow="never" class="resource-card">
          <div class="resource-title">磁盘</div>
          <el-progress :percentage="safePercent(overview.resource?.diskUsage)" color="#f59e0b" />
        </el-card>
      </div>

      <div class="jvm-grid">
        <el-card shadow="never" class="jvm-card">
          <div class="label">Heap</div>
          <div class="value">
            {{ overview.jvm?.heapUsedMb || 0 }} MB / {{ overview.jvm?.heapMaxMb || 0 }} MB
          </div>
        </el-card>
        <el-card shadow="never" class="jvm-card">
          <div class="label">线程数</div>
          <div class="value">{{ overview.jvm?.threadCount || 0 }}</div>
        </el-card>
        <el-card shadow="never" class="jvm-card">
          <div class="label">GC 次数</div>
          <div class="value">{{ overview.jvm?.gcCount || 0 }}</div>
        </el-card>
        <el-card shadow="never" class="jvm-card">
          <div class="label">GC 耗时</div>
          <div class="value">{{ overview.jvm?.gcTimeMs || 0 }} ms</div>
        </el-card>
      </div>

      <el-table v-loading="overviewLoading" :data="overview.services || []" border>
        <el-table-column prop="name" label="服务" width="160" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="serviceStatusType(row.status)">
              {{ row.status || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="说明" min-width="220" />
        <el-table-column prop="lastCheckedAt" label="探测时间" width="200" />
      </el-table>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <h3>缓存监控</h3>
          <el-button link @click="loadCacheStats">刷新</el-button>
        </div>
      </template>

      <div class="cache-meta">
        <span>Redis 版本：{{ cacheStats.redisVersion || '-' }}</span>
        <span>连接数：{{ cacheStats.connectedClients || 0 }}</span>
        <span>内存：{{ cacheStats.usedMemoryHuman || '-' }}</span>
        <span>命中率：{{ safePercent(cacheStats.hitRate) }}%</span>
      </div>

      <div class="cache-actions">
        <el-input
          v-model="customPrefix"
          placeholder="输入缓存前缀，如 voidpen:blog:detail:"
          clearable
        />
        <el-button :loading="evicting" @click="handleEvict(customPrefix)">按前缀清理</el-button>
        <el-button type="danger" :loading="clearingAll" @click="handleClearAll">清理所有业务缓存</el-button>
      </div>

      <el-table v-loading="cacheLoading" :data="cacheStats.prefixes || []" border>
        <el-table-column prop="prefix" label="前缀" min-width="280" />
        <el-table-column prop="keyCount" label="Key 数量" width="120" />
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button link type="warning" :loading="evicting" @click="handleEvict(row.prefix)">清理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { clearAllCache, evictCachePrefix, getCacheStats, getMonitorOverview } from '@/api/system'

const overviewLoading = ref(false)
const cacheLoading = ref(false)
const evicting = ref(false)
const clearingAll = ref(false)
const customPrefix = ref('')

const overview = ref({
  applicationName: '',
  activeProfile: '',
  uptimeSeconds: 0,
  serverTime: '',
  resource: {},
  jvm: {},
  services: [],
})

const cacheStats = ref({
  redisVersion: '',
  connectedClients: 0,
  usedMemoryHuman: '',
  hitRate: 0,
  prefixes: [],
})

function safePercent(value) {
  const number = Number(value || 0)
  if (!Number.isFinite(number)) {
    return 0
  }
  return Math.max(0, Math.min(100, Number(number.toFixed(2))))
}

function serviceStatusType(status) {
  return status === 'UP' ? 'success' : 'danger'
}

function formatUptime(value) {
  const seconds = Number(value || 0)
  if (!seconds) {
    return '0s'
  }
  const day = Math.floor(seconds / 86400)
  const hour = Math.floor((seconds % 86400) / 3600)
  const minute = Math.floor((seconds % 3600) / 60)
  const second = seconds % 60
  if (day > 0) {
    return `${day}d ${hour}h ${minute}m`
  }
  if (hour > 0) {
    return `${hour}h ${minute}m ${second}s`
  }
  if (minute > 0) {
    return `${minute}m ${second}s`
  }
  return `${second}s`
}

async function loadOverview() {
  overviewLoading.value = true
  try {
    overview.value = await getMonitorOverview()
  } finally {
    overviewLoading.value = false
  }
}

async function loadCacheStats() {
  cacheLoading.value = true
  try {
    cacheStats.value = await getCacheStats()
  } finally {
    cacheLoading.value = false
  }
}

async function refreshAll() {
  try {
    await Promise.all([loadOverview(), loadCacheStats()])
    ElMessage.success('监控数据已刷新')
  } catch (error) {
    ElMessage.error(error?.message || '刷新失败')
  }
}

async function handleEvict(prefix) {
  const normalized = String(prefix || '').trim()
  if (!normalized) {
    ElMessage.warning('请输入缓存前缀')
    return
  }
  evicting.value = true
  try {
    const data = await evictCachePrefix(normalized)
    ElMessage.success(`已清理 ${data.deletedCount || 0} 个缓存`)
    customPrefix.value = ''
    await loadCacheStats()
  } catch (error) {
    ElMessage.error(error?.message || '清理失败')
  } finally {
    evicting.value = false
  }
}

async function handleClearAll() {
  await ElMessageBox.confirm(
    '该操作会清理所有 voidpen:* 业务缓存，是否继续？',
    '高危操作确认',
    {
      type: 'warning',
      confirmButtonText: '继续清理',
      cancelButtonText: '取消',
    },
  )

  clearingAll.value = true
  try {
    const data = await clearAllCache()
    ElMessage.success(`已清理 ${data.deletedCount || 0} 个缓存`)
    await Promise.all([loadOverview(), loadCacheStats()])
  } catch (error) {
    ElMessage.error(error?.message || '清理失败')
  } finally {
    clearingAll.value = false
  }
}

Promise.all([loadOverview(), loadCacheStats()]).catch((error) =>
  ElMessage.error(error?.message || '加载监控信息失败'),
)
</script>

<style scoped>
.monitor-page {
  display: grid;
  gap: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h3 {
  margin: 0;
}

.overview-meta,
.cache-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #475569;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.resource-card {
  border: 1px solid #e2e8f0;
}

.resource-title {
  margin-bottom: 10px;
  font-size: 13px;
  color: #64748b;
}

.jvm-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.jvm-card {
  border: 1px solid #e2e8f0;
}

.jvm-card .label {
  color: #64748b;
  font-size: 13px;
}

.jvm-card .value {
  margin-top: 8px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 600;
}

.cache-actions {
  margin-bottom: 12px;
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 10px;
}

@media (max-width: 980px) {
  .resource-grid {
    grid-template-columns: 1fr;
  }

  .jvm-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .cache-actions {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .jvm-grid {
    grid-template-columns: 1fr;
  }
}
</style>
