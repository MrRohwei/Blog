<template>
  <section class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索标签名"
          clearable
          style="width: 220px"
          @keyup.enter="loadData"
        />
        <div class="toolbar-actions">
          <el-button @click="loadData">查询</el-button>
          <el-button type="primary" @click="openCreate">新增标签</el-button>
        </div>
      </div>

      <el-table :data="tableData" border>
        <el-table-column prop="name" label="标签名" min-width="140" />
        <el-table-column label="颜色" width="120">
          <template #default="{ row }">
            <div class="tag-color-cell">
              <span class="color-dot" :style="{ backgroundColor: row.color || '#64748b' }" />
              <span>{{ row.color || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="blogCount" label="博客数" width="100" />
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="query.page"
          :page-size="query.size"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑标签' : '新增标签'" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标签名" prop="name">
          <el-input v-model="form.name" maxlength="30" />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <div class="color-editor">
            <el-color-picker v-model="form.color" />
            <el-input v-model="form.color" placeholder="#409eff" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createTag, deleteTag, getTagPage, updateTag } from '@/api/tags'

const formRef = ref()
const total = ref(0)
const tableData = ref([])

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
})

const dialog = reactive({
  visible: false,
  isEdit: false,
  id: null,
})

const form = reactive({
  name: '',
  color: '#409eff',
})

const rules = {
  name: [{ required: true, message: '请输入标签名', trigger: 'blur' }],
}

function resetForm() {
  form.name = ''
  form.color = '#409eff'
}

async function loadData() {
  const data = await getTagPage(query)
  tableData.value = data.records || []
  total.value = data.total || 0
}

function handlePageChange(page) {
  query.page = page
  loadData()
}

function openCreate() {
  dialog.visible = true
  dialog.isEdit = false
  dialog.id = null
  resetForm()
}

function openEdit(row) {
  dialog.visible = true
  dialog.isEdit = true
  dialog.id = row.id
  form.name = row.name
  form.color = row.color || '#409eff'
}

async function handleSubmit() {
  await formRef.value.validate()
  const payload = { name: form.name, color: form.color }
  if (dialog.isEdit) {
    await updateTag(dialog.id, payload)
    ElMessage.success('标签已更新')
  } else {
    await createTag(payload)
    ElMessage.success('标签已创建')
  }
  dialog.visible = false
  await loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除标签「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteTag(row.id)
  ElMessage.success('标签已删除')
  await loadData()
}

loadData().catch((error) => ElMessage.error(error?.message || '加载标签失败'))
</script>

<style scoped>
.page {
  display: grid;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

.tag-color-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.color-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 1px solid #cbd5e1;
}

.color-editor {
  width: 100%;
  display: flex;
  gap: 12px;
  align-items: center;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
