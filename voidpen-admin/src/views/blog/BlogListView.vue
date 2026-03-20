<template>
  <section class="page">
    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="query.status" clearable placeholder="状态" style="width: 120px">
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已下线" :value="2" />
        </el-select>
        <el-select v-model="query.categoryId" clearable placeholder="分类" style="width: 150px">
          <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-select v-model="query.tagId" clearable placeholder="标签" style="width: 150px">
          <el-option v-for="item in tags" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-input
          v-model="query.keyword"
          placeholder="搜索标题 / 摘要"
          clearable
          style="width: 240px"
          @keyup.enter="loadData"
        />
        <div class="toolbar-actions">
          <el-button @click="loadData">查询</el-button>
          <el-button type="primary" @click="router.push('/blog/edit')">写博客</el-button>
        </div>
      </div>

      <el-table :data="tableData" border>
        <el-table-column prop="title" label="标题" min-width="210" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="130" />
        <el-table-column label="标签" min-width="180">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="tag in row.tags || []" :key="tag.id" size="small">{{ tag.name }}</el-tag>
              <span v-if="!row.tags?.length">-</span>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger">置顶</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="浏览" width="90" />
        <el-table-column prop="likes" label="点赞" width="90" />
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/blog/edit/${row.id}`)">编辑</el-button>
            <el-dropdown @command="(command) => handleStatusCommand(row, command)">
              <el-button link type="warning">
                状态
                <el-icon><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="0">设为草稿</el-dropdown-item>
                  <el-dropdown-item command="1">设为发布</el-dropdown-item>
                  <el-dropdown-item command="2">设为下线</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button link type="info" @click="toggleTop(row)">
              {{ row.isTop === 1 ? '取消置顶' : '置顶' }}
            </el-button>
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
import { ArrowDown } from '@element-plus/icons-vue'
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { deleteBlog, getBlogPage, updateBlogStatus, updateBlogTop } from '@/api/blogs'
import { getCategoryPage } from '@/api/categories'
import { getTagPage } from '@/api/tags'

const router = useRouter()

const total = ref(0)
const tableData = ref([])
const categories = ref([])
const tags = ref([])

const query = reactive({
  page: 1,
  size: 10,
  status: undefined,
  categoryId: undefined,
  tagId: undefined,
  keyword: '',
})

function statusLabel(status) {
  return (
    {
      0: '草稿',
      1: '发布',
      2: '下线',
    }[status] || '未知'
  )
}

function statusTagType(status) {
  return (
    {
      0: 'info',
      1: 'success',
      2: 'warning',
    }[status] || 'info'
  )
}

async function loadData() {
  const data = await getBlogPage(query)
  tableData.value = data.records || []
  total.value = data.total || 0
}

async function loadOptions() {
  const [categoryData, tagData] = await Promise.all([
    getCategoryPage({ page: 1, size: 200 }),
    getTagPage({ page: 1, size: 200 }),
  ])
  categories.value = categoryData.records || []
  tags.value = tagData.records || []
}

function handlePageChange(page) {
  query.page = page
  loadData()
}

async function handleStatusCommand(row, command) {
  const status = Number(command)
  if (row.status === status) {
    return
  }
  await updateBlogStatus(row.id, status)
  row.status = status
  ElMessage.success('状态已更新')
}

async function toggleTop(row) {
  const next = row.isTop === 1 ? 0 : 1
  await updateBlogTop(row.id, next)
  row.isTop = next
  ElMessage.success(next ? '已置顶' : '已取消置顶')
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除博客「${row.title}」吗？`, '提示', { type: 'warning' })
  await deleteBlog(row.id)
  ElMessage.success('博客已删除')
  await loadData()
}

Promise.all([loadData(), loadOptions()]).catch((error) =>
  ElMessage.error(error?.message || '加载博客列表失败'),
)
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

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
