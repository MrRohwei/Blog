<template>
  <section class="edit-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <h3>{{ isEdit ? '编辑博客' : '新建博客' }}</h3>
          <el-button link @click="router.push('/blog/list')">返回列表</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="blog-form">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="200" placeholder="请输入博客标题" />
        </el-form-item>

        <el-form-item label="摘要" prop="summary">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            maxlength="500"
            placeholder="可选：用于列表卡片的简介"
          />
        </el-form-item>

        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="标签" prop="tagIds">
              <el-select
                v-model="form.tagIds"
                multiple
                collapse-tags
                collapse-tags-tooltip
                placeholder="选择标签"
                style="width: 100%"
              >
                <el-option v-for="item in tags" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="封面" prop="coverImg">
              <div class="cover-box">
                <el-upload :show-file-list="false" :http-request="uploadCover" accept="image/*">
                  <el-button :loading="uploading">上传</el-button>
                </el-upload>
                <el-input v-model="form.coverImg" placeholder="或粘贴 URL" />
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="内容" prop="content">
          <MdEditor v-model="form.content" language="zh-CN" class="editor" />
        </el-form-item>

        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="草稿" :value="0" />
                <el-option label="发布" :value="1" />
                <el-option label="下线" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="置顶" prop="isTop">
              <el-switch v-model="topSwitch" active-text="置顶" inactive-text="普通" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="推荐" prop="isFeatured">
              <el-switch v-model="featuredSwitch" active-text="推荐" inactive-text="普通" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <div class="actions">
            <el-button :loading="saving" @click="saveAsDraft">保存草稿</el-button>
            <el-button type="primary" :loading="saving" @click="publishBlog">保存并发布</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </section>
</template>

<script setup>
import { MdEditor } from 'md-editor-v3'
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createBlog, getAdminBlogDetail, updateBlog } from '@/api/blogs'
import { getCategoryPage } from '@/api/categories'
import { getTagPage } from '@/api/tags'
import { uploadFile } from '@/api/files'
import 'md-editor-v3/lib/style.css'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const saving = ref(false)
const uploading = ref(false)
const categories = ref([])
const tags = ref([])

const form = reactive({
  title: '',
  summary: '',
  categoryId: null,
  tagIds: [],
  coverImg: '',
  content: '',
  status: 0,
  isTop: 0,
  isFeatured: 0,
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const isEdit = computed(() => Boolean(route.params.id))
const topSwitch = computed({
  get: () => form.isTop === 1,
  set: (value) => {
    form.isTop = value ? 1 : 0
  },
})

const featuredSwitch = computed({
  get: () => form.isFeatured === 1,
  set: (value) => {
    form.isFeatured = value ? 1 : 0
  },
})

async function loadOptions() {
  const [categoryData, tagData] = await Promise.all([
    getCategoryPage({ page: 1, size: 100 }),
    getTagPage({ page: 1, size: 100 }),
  ])
  categories.value = categoryData.records || []
  tags.value = tagData.records || []
}

async function loadDetail() {
  if (!isEdit.value) {
    return
  }
  const data = await getAdminBlogDetail(route.params.id)
  form.title = data.title || ''
  form.summary = data.summary || ''
  form.categoryId = data.categoryId
  form.tagIds = (data.tags || []).map((item) => item.id)
  form.coverImg = data.coverImg || ''
  form.content = data.content || ''
  form.status = data.status ?? 0
  form.isTop = data.isTop ?? 0
  form.isFeatured = data.isFeatured ?? 0
}

async function uploadCover(options) {
  uploading.value = true
  try {
    const data = await uploadFile(options.file)
    form.coverImg = data.url
    options.onSuccess?.(data)
    ElMessage.success('封面上传成功')
  } catch (error) {
    options.onError?.(error)
    ElMessage.error(error?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

async function submitWithStatus(status) {
  form.status = status
  await formRef.value.validate()
  const payload = {
    title: form.title,
    summary: form.summary,
    categoryId: form.categoryId,
    tagIds: form.tagIds,
    coverImg: form.coverImg,
    content: form.content,
    status: form.status,
    isTop: form.isTop,
    isFeatured: form.isFeatured,
  }

  saving.value = true
  try {
    if (isEdit.value) {
      await updateBlog(route.params.id, payload)
      ElMessage.success('博客已更新')
    } else {
      await createBlog(payload)
      ElMessage.success('博客已创建')
    }
    router.push('/blog/list')
  } finally {
    saving.value = false
  }
}

function saveAsDraft() {
  submitWithStatus(0).catch((error) => ElMessage.error(error?.message || '保存失败'))
}

function publishBlog() {
  submitWithStatus(1).catch((error) => ElMessage.error(error?.message || '发布失败'))
}

Promise.all([loadOptions(), loadDetail()]).catch((error) =>
  ElMessage.error(error?.message || '加载博客信息失败'),
)
</script>

<style scoped>
.edit-page {
  display: grid;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h3 {
  margin: 0;
}

.blog-form {
  max-width: 100%;
}

.cover-box {
  width: 100%;
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 10px;
}

.editor {
  width: 100%;
}

.actions {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .cover-box {
    grid-template-columns: 1fr;
  }
}
</style>
