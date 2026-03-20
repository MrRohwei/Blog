<template>
  <section class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索用户名"
          clearable
          style="width: 220px"
          @keyup.enter="loadData"
        />
        <div class="toolbar-actions">
          <el-button @click="loadData">查询</el-button>
          <el-button type="primary" @click="openCreate">新增用户</el-button>
        </div>
      </div>

      <el-table :data="tableData" border>
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="170" />
        <el-table-column prop="role" label="角色" width="120" />
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
        <el-table-column prop="lastLoginTime" label="最后登录" min-width="170" />
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

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑用户' : '新增用户'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" maxlength="100" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="dialog.isEdit ? '不填则不修改' : '请输入登录密码'"
          />
        </el-form-item>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select v-model="form.role">
                <el-option label="管理员" value="ROLE_ADMIN" />
                <el-option label="普通用户" value="ROLE_USER" />
              </el-select>
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
  createUser,
  deleteUser,
  getUserPage,
  updateUser,
  updateUserStatus,
} from '@/api/users'

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
  username: '',
  nickname: '',
  email: '',
  password: '',
  role: 'ROLE_USER',
  status: 1,
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

function resetForm() {
  form.username = ''
  form.nickname = ''
  form.email = ''
  form.password = ''
  form.role = 'ROLE_USER'
  form.status = 1
}

async function loadData() {
  const data = await getUserPage(query)
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
  form.username = row.username
  form.nickname = row.nickname || ''
  form.email = row.email || ''
  form.password = ''
  form.role = row.role || 'ROLE_USER'
  form.status = row.status ?? 1
}

async function handleSubmit() {
  await formRef.value.validate()
  const payload = {
    username: form.username,
    nickname: form.nickname,
    email: form.email,
    role: form.role,
    status: form.status,
  }
  if (form.password) {
    payload.password = form.password
  }

  if (dialog.isEdit) {
    await updateUser(dialog.id, payload)
    ElMessage.success('用户已更新')
  } else {
    if (!payload.password) {
      ElMessage.warning('新增用户必须设置密码')
      return
    }
    await createUser(payload)
    ElMessage.success('用户已创建')
  }

  dialog.visible = false
  await loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('用户已删除')
  await loadData()
}

async function handleStatusChange(row, value) {
  await updateUserStatus(row.id, value ? 1 : 0)
  ElMessage.success('状态已更新')
  row.status = value ? 1 : 0
}

loadData().catch((error) => ElMessage.error(error?.message || '加载用户失败'))
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

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
