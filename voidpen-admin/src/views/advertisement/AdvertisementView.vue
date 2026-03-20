<template>
  <section class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="query.position" clearable placeholder="广告位" style="width: 150px">
          <el-option v-for="item in positions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.status" clearable placeholder="状态" style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-input
          v-model="query.keyword"
          placeholder="搜索广告标题"
          clearable
          style="width: 230px"
          @keyup.enter="loadData"
        />
        <div class="toolbar-actions">
          <el-button @click="loadData">查询</el-button>
          <el-button type="primary" @click="openCreate">新增广告</el-button>
        </div>
      </div>

      <el-table :data="tableData" border>
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="position" label="广告位" width="130" />
        <el-table-column label="图片" width="110">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              fit="cover"
              style="width: 72px; height: 40px; border-radius: 4px"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
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
        <el-table-column prop="expiredAt" label="过期时间" min-width="170" />
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

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑广告' : '新增广告'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" />
        </el-form-item>
        <el-form-item label="广告位" prop="position">
          <el-select v-model="form.position">
            <el-option v-for="item in positions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片" prop="imageUrl">
          <div class="upload-box">
            <el-upload :show-file-list="false" :http-request="uploadImage" accept="image/*">
              <el-button :loading="uploading">上传图片</el-button>
            </el-upload>
            <el-input v-model="form.imageUrl" placeholder="可选" />
          </div>
        </el-form-item>
        <el-form-item label="跳转地址" prop="linkUrl">
          <el-input v-model="form.linkUrl" maxlength="255" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="过期时间" prop="expiredAt">
              <el-date-picker
                v-model="form.expiredAt"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                placeholder="可选"
              />
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
  createAdvertisement,
  deleteAdvertisement,
  getAdvertisementPage,
  updateAdvertisement,
  updateAdvertisementStatus,
} from '@/api/advertisements'
import { uploadFile } from '@/api/files'

const positions = [
  { label: '侧边栏', value: 'SIDEBAR' },
  { label: '顶部', value: 'HEADER' },
  { label: '底部', value: 'FOOTER' },
  { label: '详情底部', value: 'DETAIL_BOTTOM' },
]

const formRef = ref()
const total = ref(0)
const uploading = ref(false)
const tableData = ref([])

const query = reactive({
  page: 1,
  size: 10,
  position: '',
  status: undefined,
  keyword: '',
})

const dialog = reactive({
  visible: false,
  isEdit: false,
  id: null,
})

const form = reactive({
  title: '',
  position: 'SIDEBAR',
  imageUrl: '',
  linkUrl: '',
  status: 1,
  expiredAt: '',
})

const rules = {
  title: [{ required: true, message: '请输入广告标题', trigger: 'blur' }],
  position: [{ required: true, message: '请选择广告位', trigger: 'change' }],
}

function resetForm() {
  form.title = ''
  form.position = 'SIDEBAR'
  form.imageUrl = ''
  form.linkUrl = ''
  form.status = 1
  form.expiredAt = ''
}

async function loadData() {
  const data = await getAdvertisementPage(query)
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
  form.position = row.position || 'SIDEBAR'
  form.imageUrl = row.imageUrl || ''
  form.linkUrl = row.linkUrl || ''
  form.status = row.status ?? 1
  form.expiredAt = normalizeDateTime(row.expiredAt)
}

async function uploadImage(options) {
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
    position: form.position,
    imageUrl: form.imageUrl,
    linkUrl: form.linkUrl,
    status: form.status,
    expiredAt: normalizeDateTime(form.expiredAt) || null,
  }
  if (dialog.isEdit) {
    await updateAdvertisement(dialog.id, payload)
    ElMessage.success('广告已更新')
  } else {
    await createAdvertisement(payload)
    ElMessage.success('广告已创建')
  }
  dialog.visible = false
  await loadData()
}

async function handleStatusChange(row, value) {
  await updateAdvertisementStatus(row.id, value ? 1 : 0)
  row.status = value ? 1 : 0
  ElMessage.success('状态已更新')
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该广告吗？', '提示', { type: 'warning' })
  await deleteAdvertisement(row.id)
  ElMessage.success('广告已删除')
  await loadData()
}

loadData().catch((error) => ElMessage.error(error?.message || '加载广告失败'))

function normalizeDateTime(value) {
  if (!value) {
    return ''
  }
  return String(value).replace(' ', 'T')
}
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-actions {
  margin-left: auto;
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

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
