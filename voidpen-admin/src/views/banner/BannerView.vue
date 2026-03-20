<template>
  <section class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索标题"
          clearable
          style="width: 220px"
          @keyup.enter="loadData"
        />
        <div class="toolbar-actions">
          <el-button @click="loadData">查询</el-button>
          <el-button type="primary" @click="openCreate">新增轮播图</el-button>
        </div>
      </div>

      <el-table :data="tableData" border>
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column label="图片" width="110">
          <template #default="{ row }">
            <el-image :src="row.imageUrl" fit="cover" style="width: 72px; height: 40px; border-radius: 4px" />
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              active-text="启用"
              inactive-text="禁用"
              @change="(value) => handleStatusChange(row, value)"
            />
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑轮播图' : '新增轮播图'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" />
        </el-form-item>
        <el-form-item label="图片" prop="imageUrl">
          <div class="upload-box">
            <el-upload :show-file-list="false" :http-request="uploadBannerImage" accept="image/*">
              <el-button :loading="uploading">上传图片</el-button>
            </el-upload>
            <el-input v-model="form.imageUrl" placeholder="或手动输入图片 URL" />
          </div>
          <el-image v-if="form.imageUrl" :src="form.imageUrl" fit="cover" class="preview" />
        </el-form-item>
        <el-form-item label="跳转地址" prop="linkUrl">
          <el-input v-model="form.linkUrl" maxlength="255" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
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
  createBanner,
  deleteBanner,
  getBannerPage,
  updateBanner,
  updateBannerStatus,
} from '@/api/banners'
import { uploadFile } from '@/api/files'

const formRef = ref()
const total = ref(0)
const uploading = ref(false)
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
  title: '',
  imageUrl: '',
  linkUrl: '',
  sortOrder: 0,
  status: 1,
})

const rules = {
  imageUrl: [{ required: true, message: '请上传或填写图片地址', trigger: 'blur' }],
}

function resetForm() {
  form.title = ''
  form.imageUrl = ''
  form.linkUrl = ''
  form.sortOrder = 0
  form.status = 1
}

async function loadData() {
  const data = await getBannerPage(query)
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
  form.title = row.title || ''
  form.imageUrl = row.imageUrl || ''
  form.linkUrl = row.linkUrl || ''
  form.sortOrder = row.sortOrder ?? 0
  form.status = row.status ?? 1
}

async function uploadBannerImage(options) {
  uploading.value = true
  try {
    const data = await uploadFile(options.file)
    form.imageUrl = data.url
    options.onSuccess?.(data)
    ElMessage.success('图片上传成功')
  } catch (error) {
    options.onError?.(error)
    ElMessage.error(error?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  const payload = {
    title: form.title,
    imageUrl: form.imageUrl,
    linkUrl: form.linkUrl,
    sortOrder: form.sortOrder,
    status: form.status,
  }
  if (dialog.isEdit) {
    await updateBanner(dialog.id, payload)
    ElMessage.success('轮播图已更新')
  } else {
    await createBanner(payload)
    ElMessage.success('轮播图已创建')
  }
  dialog.visible = false
  await loadData()
}

async function handleStatusChange(row, value) {
  await updateBannerStatus(row.id, value ? 1 : 0)
  row.status = value ? 1 : 0
  ElMessage.success('状态已更新')
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该轮播图吗？', '提示', { type: 'warning' })
  await deleteBanner(row.id)
  ElMessage.success('轮播图已删除')
  await loadData()
}

loadData().catch((error) => ElMessage.error(error?.message || '加载轮播图失败'))
</script>

<style scoped>
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

.upload-box {
  width: 100%;
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 12px;
  align-items: center;
}

.preview {
  margin-top: 12px;
  width: 160px;
  height: 90px;
  border-radius: 8px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
