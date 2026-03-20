<template>
  <section class="page">
    <header class="card page-header">
      <h1>标签：{{ currentTag?.name || '全部' }}</h1>
      <p>共 {{ total }} 篇文章</p>
    </header>

    <div class="content-wrap">
      <main>
        <BlogList :blogs="blogs" :loading="loading.blogs" :error-message="errors.blogs" @retry="loadData" />
        <AppPagination
          v-if="!loading.blogs && total > 0"
          v-model:page="query.page"
          :size="query.size"
          :total="total"
          @update:page="loadData"
        />
      </main>
      <aside class="sidebar">
        <TagCloud :tags="tags" />
        <CategoryCard :categories="categories" />
      </aside>
    </div>
  </section>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppPagination from '@/components/common/AppPagination.vue'
import BlogList from '@/components/home/BlogList.vue'
import CategoryCard from '@/components/home/CategoryCard.vue'
import TagCloud from '@/components/home/TagCloud.vue'
import { getBlogPage } from '@/api/blog'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'

const route = useRoute()
const router = useRouter()

const categories = ref([])
const tags = ref([])
const blogs = ref([])
const total = ref(0)
const loading = reactive({
  blogs: false,
})
const errors = reactive({
  blogs: '',
})

const query = reactive({
  page: Number(route.query.page) || 1,
  size: 10,
})

const currentTag = computed(() => tags.value.find((item) => String(item.id) === String(route.params.id)))

async function loadData() {
  loading.blogs = true
  errors.blogs = ''
  try {
    const data = await getBlogPage({
      page: query.page,
      size: query.size,
      tagId: route.params.id,
    })
    blogs.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    blogs.value = []
    total.value = 0
    errors.blogs = error?.message || '文章加载失败，请稍后重试'
  } finally {
    loading.blogs = false
  }
}

async function loadSideData() {
  const [categoryData, tagData] = await Promise.all([getCategories(), getTags()])
  categories.value = categoryData || []
  tags.value = tagData || []
}

watch(
  () => query.page,
  (value) => {
    router.replace({ query: value > 1 ? { page: String(value) } : {} })
  },
)

watch(
  () => route.params.id,
  () => {
    query.page = Number(route.query.page) || 1
    loadData()
  },
  { immediate: true },
)

loadSideData()
</script>

<style scoped>
.page {
  display: grid;
  gap: 14px;
}

.page-header {
  padding: 16px;
}

.page-header h1 {
  margin-bottom: 6px;
}

.page-header p {
  margin: 0;
  color: var(--text-soft);
}

.content-wrap {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 16px;
}

.sidebar {
  display: grid;
  gap: 12px;
}

@media (max-width: 1024px) {
  .content-wrap {
    grid-template-columns: 1fr;
  }
}
</style>
