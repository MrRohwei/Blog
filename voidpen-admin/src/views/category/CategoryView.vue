<template>
  <section class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索分类名"
          clearable
          style="width: 220px"
          @keyup.enter="loadData"
        />
        <div class="toolbar-actions">
          <el-button @click="loadData">查询</el-button>
          <el-button type="primary" @click="openCreate">新增分类</el-button>
        </div>
      </div>

      <el-table :data="tableData" border>
        <el-table-column prop="name" label="分类名" min-width="140" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="blogCount" label="博客数" width="100" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
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

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑分类' : '新增分类'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="分类名" prop="name">
          <el-input v-model="form.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="200" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import {
  createCategory,
  deleteCategory,
  getCategoryPage,
  updateCategory,
} from '@/api/categories'

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
  description: '',
  sortOrder: 0,
  status: 1,
})

const rules = {
  name: [{ required: true, message: '请输入分类名', trigger: 'blur' }],
}

function resetForm() {
  form.name = ''
  form.description = ''
  form.sortOrder = 0
  form.status = 1
}

async function loadData() {
  const data = await getCategoryPage(query)
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
  form.description = row.description || ''
  form.sortOrder = row.sortOrder ?? 0
  form.status = row.status ?? 1
}

async function handleSubmit() {
  await formRef.value.validate()
  const payload = {
    name: form.name,
    description: form.description,
    sortOrder: form.sortOrder,
    status: form.status,
  }
  if (dialog.isEdit) {
    await updateCategory(dialog.id, payload)
    ElMessage.success('分类已更新')
  } else {
    await createCategory(payload)
    ElMessage.success('分类已创建')
  }
  dialog.visible = false
  await loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteCategory(row.id)
  ElMessage.success('分类已删除')
  await loadData()
}

loadData().catch((error) => ElMessage.error(error?.message || '加载分类失败'))
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

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
