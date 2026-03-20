<template>
  <section class="page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <h3>系统配置</h3>
          <el-button link @click="loadGroupItems">刷新</el-button>
        </div>
      </template>

      <el-tabs v-model="activeGroup" class="group-tabs" @tab-change="handleGroupChange">
        <el-tab-pane
          v-for="group in groups"
          :key="group"
          :label="group"
          :name="group"
        />
      </el-tabs>

      <el-alert
        type="warning"
        show-icon
        :closable="false"
        class="tips"
        title="修改系统配置会即时生效，请谨慎操作。敏感配置仅显示脱敏值。"
      />

      <el-table v-loading="loading" :data="items" border>
        <el-table-column prop="configKey" label="配置键" min-width="180" />
        <el-table-column prop="description" label="说明" min-width="180" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="valueTypeTagType(row.valueType)">
              {{ row.valueType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="可编辑" width="90">
          <template #default="{ row }">
            <el-tag :type="row.editable === 1 ? 'success' : 'info'" size="small">
              {{ row.editable === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="配置值" min-width="320">
          <template #default="{ row }">
            <template v-if="row.editable === 1">
              <el-select
                v-if="row.valueType === 'boolean'"
                v-model="draftValues[row.id]"
                style="width: 100%"
              >
                <el-option label="true" value="true" />
                <el-option label="false" value="false" />
              </el-select>

              <el-input
                v-else-if="row.valueType === 'json'"
                v-model="draftValues[row.id]"
                type="textarea"
                :rows="3"
                placeholder="请输入合法 JSON"
              />

              <el-input
                v-else
                v-model="draftValues[row.id]"
                :placeholder="row.sensitive ? '敏感字段，请输入新值后保存' : '请输入配置值'"
              />
            </template>
            <template v-else>
              <span>{{ row.configValue }}</span>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.editable === 1"
              link
              type="primary"
              :loading="savingId === row.id"
              @click="handleSave(row)"
            >
              保存
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigGroupItems, getConfigGroups, updateConfigItem } from '@/api/system'

const groups = ref([])
const activeGroup = ref('')
const items = ref([])
const loading = ref(false)
const savingId = ref(null)
const draftValues = reactive({})

function valueTypeTagType(valueType) {
  return (
    {
      string: 'info',
      number: 'success',
      boolean: 'warning',
      json: 'primary',
    }[valueType] || 'info'
  )
}

async function loadGroups() {
  const data = await getConfigGroups()
  groups.value = data || []
  if (!groups.value.length) {
    activeGroup.value = ''
    items.value = []
    return
  }
  if (!activeGroup.value || !groups.value.includes(activeGroup.value)) {
    activeGroup.value = groups.value[0]
  }
}

function fillDraftValues() {
  Object.keys(draftValues).forEach((key) => delete draftValues[key])
  items.value.forEach((item) => {
    draftValues[item.id] = item.sensitive ? '' : String(item.configValue ?? '')
  })
}

async function loadGroupItems() {
  if (!activeGroup.value) {
    items.value = []
    return
  }
  loading.value = true
  try {
    items.value = await getConfigGroupItems(activeGroup.value)
    fillDraftValues()
  } finally {
    loading.value = false
  }
}

async function handleGroupChange() {
  loadGroupItems().catch((error) => ElMessage.error(error?.message || '加载配置失败'))
}

async function handleSave(row) {
  const nextValue = String(draftValues[row.id] ?? '')
  if (!nextValue.trim()) {
    ElMessage.warning('配置值不能为空')
    return
  }

  savingId.value = row.id
  try {
    await updateConfigItem({
      configGroup: row.configGroup,
      configKey: row.configKey,
      configValue: nextValue,
    })
    ElMessage.success('配置已更新')
    await loadGroupItems()
  } catch (error) {
    ElMessage.error(error?.message || '更新配置失败')
  } finally {
    savingId.value = null
  }
}

Promise.resolve()
  .then(loadGroups)
  .then(loadGroupItems)
  .catch((error) => ElMessage.error(error?.message || '加载系统配置失败'))
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h3 {
  margin: 0;
}

.group-tabs {
  margin-top: -8px;
}

.tips {
  margin-bottom: 12px;
}
</style>
