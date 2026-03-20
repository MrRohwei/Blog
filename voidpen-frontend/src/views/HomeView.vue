<template>
  <section class="home-layout">
    <BannerSwiper :banners="banners" />

    <div class="content-wrap">
      <main class="main-content">
        <BlogList :blogs="blogs" :loading="loading.blogs" :error-message="errors.blogs" @retry="loadBlogs" />
        <AppPagination
          v-if="!loading.blogs && total > 0"
          v-model:page="query.page"
          :size="query.size"
          :total="total"
          @update:page="loadBlogs"
        />
      </main>

      <aside class="sidebar">
        <CategoryCard :categories="categories" />
        <TagCloud :tags="tags" />
        <FeaturedCard :blogs="featuredBlogs" />
        <AdvertisementWidget position="SIDEBAR" />
      </aside>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdvertisementWidget from '@/components/common/AdvertisementWidget.vue'
import AppPagination from '@/components/common/AppPagination.vue'
import BannerSwiper from '@/components/home/BannerSwiper.vue'
import BlogList from '@/components/home/BlogList.vue'
import CategoryCard from '@/components/home/CategoryCard.vue'
import FeaturedCard from '@/components/home/FeaturedCard.vue'
import TagCloud from '@/components/home/TagCloud.vue'
import { getBanners } from '@/api/banner'
import { getBlogPage, getFeaturedBlogs } from '@/api/blog'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'

const route = useRoute()
const router = useRouter()

const query = reactive({
  page: Number(route.query.page) || 1,
  size: 10,
})

const total = ref(0)
const blogs = ref([])
const banners = ref([])
const categories = ref([])
const tags = ref([])
const featuredBlogs = ref([])
const loading = reactive({
  blogs: false,
})
const errors = reactive({
  blogs: '',
})

async function loadBlogs() {
  loading.blogs = true
  errors.blogs = ''
  try {
    const data = await getBlogPage(query)
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

async function loadSidebarData() {
  const [bannerData, categoryData, tagData, featuredData] = await Promise.all([
    getBanners(),
    getCategories(),
    getTags(),
    getFeaturedBlogs(),
  ])
  banners.value = bannerData || []
  categories.value = categoryData || []
  tags.value = tagData || []
  featuredBlogs.value = featuredData || []
}

watch(
  () => query.page,
  (value) => {
    router.replace({ query: value > 1 ? { page: String(value) } : {} })
  },
)

loadBlogs()
loadSidebarData()
</script>

<style scoped>
.home-layout {
  display: grid;
  gap: 18px;
}

.content-wrap {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 16px;
  align-items: start;
}

.main-content {
  min-width: 0;
}

.sidebar {
  display: grid;
  gap: 12px;
  position: sticky;
  top: 78px;
}

@media (max-width: 1024px) {
  .content-wrap {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .sidebar {
    grid-template-columns: 1fr;
  }
}
</style>
