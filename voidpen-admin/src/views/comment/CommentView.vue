<template>
  <section class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="query.status" clearable placeholder="评论状态" style="width: 140px">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
        <el-input
          v-model="query.keyword"
          placeholder="搜索评论内容"
          clearable
          style="width: 260px"
          @keyup.enter="loadData"
        />
        <div class="toolbar-actions">
          <el-button @click="loadData">查询</el-button>
        </div>
      </div>

      <el-table :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="blogId" label="博客ID" width="90" />
        <el-table-column prop="nickname" label="评论人" width="130" />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="handleAudit(row, 1)">通过</el-button>
            <el-button link type="warning" @click="handleAudit(row, 2)">拒绝</el-button>
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
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteComment, getCommentPage, updateCommentStatus } from '@/api/comments'

const total = ref(0)
const tableData = ref([])

const query = reactive({
  page: 1,
  size: 10,
  status: undefined,
  keyword: '',
})

function statusLabel(status) {
  return (
    {
      0: '待审核',
      1: '已通过',
      2: '已拒绝',
    }[status] || '未知'
  )
}

function statusTagType(status) {
  return (
    {
      0: 'warning',
      1: 'success',
      2: 'danger',
    }[status] || 'info'
  )
}

async function loadData() {
  const data = await getCommentPage(query)
  tableData.value = data.records || []
  total.value = data.total || 0
}

function handlePageChange(page) {
  query.page = page
  loadData()
}

async function handleAudit(row, status) {
  await updateCommentStatus(row.id, status)
  ElMessage.success('审核状态已更新')
  await loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('删除评论将级联删除子评论，是否继续？', '提示', { type: 'warning' })
  await deleteComment(row.id)
  ElMessage.success('评论已删除')
  await loadData()
}

loadData().catch((error) => ElMessage.error(error?.message || '加载评论失败'))
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
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
