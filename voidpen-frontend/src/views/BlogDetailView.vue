<template>
  <div class="detail-layout">
    <article class="card article">
      <h1>{{ blog.title }}</h1>

      <div class="meta">
        <span>{{ formatDate(blog.createdAt) }}</span>
        <span>{{ blog.views || 0 }} 次阅读</span>
        <router-link :to="`/category/${blog.categoryId}`">{{ blog.categoryName || '未分类' }}</router-link>
      </div>

      <div class="markdown-body" v-html="contentHtml" />

      <div class="footer-actions">
        <div class="tags">
          <router-link
            v-for="tag in blog.tags || []"
            :key="tag.id"
            :to="`/tag/${tag.id}`"
            class="tag"
            :style="{ borderColor: tag.color || '#cbd5e1' }"
          >
            {{ tag.name }}
          </router-link>
        </div>
        <LikeButton v-if="blog.id" :blog-id="blog.id" :count="blog.likes || 0" />
      </div>

      <AdvertisementWidget position="DETAIL_BOTTOM" />
      <CommentSection v-if="blog.id" :blog-id="blog.id" />
    </article>

    <aside class="toc-sidebar">
      <TableOfContents :items="tocItems" v-model:active-id="activeTocId" />
    </aside>
  </div>
</template>

<script setup>
import DOMPurify from 'dompurify'
import hljs from 'highlight.js/lib/core'
import bash from 'highlight.js/lib/languages/bash'
import css from 'highlight.js/lib/languages/css'
import java from 'highlight.js/lib/languages/java'
import javascript from 'highlight.js/lib/languages/javascript'
import json from 'highlight.js/lib/languages/json'
import markdown from 'highlight.js/lib/languages/markdown'
import sql from 'highlight.js/lib/languages/sql'
import typescript from 'highlight.js/lib/languages/typescript'
import xml from 'highlight.js/lib/languages/xml'
import yaml from 'highlight.js/lib/languages/yaml'
import { marked } from 'marked'
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import 'highlight.js/styles/github.css'
import CommentSection from '@/components/blog/CommentSection.vue'
import LikeButton from '@/components/blog/LikeButton.vue'
import TableOfContents from '@/components/blog/TableOfContents.vue'
import AdvertisementWidget from '@/components/common/AdvertisementWidget.vue'
import { getBlogDetail } from '@/api/blog'

const route = useRoute()

hljs.registerLanguage('bash', bash)
hljs.registerLanguage('css', css)
hljs.registerLanguage('java', java)
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('json', json)
hljs.registerLanguage('markdown', markdown)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('yaml', yaml)

const blog = ref({})
const tocItems = ref([])
const activeTocId = ref('')

const contentHtml = computed(() => buildRenderedContent(blog.value.content || ''))

async function loadBlog() {
  blog.value = await getBlogDetail(route.params.id)
  if (blog.value?.title) {
    document.title = `${blog.value.title} - Voidpen`
  }
}

function buildRenderedContent(markdown) {
  const html = marked.parse(markdown || '')
  const sanitized = DOMPurify.sanitize(html)
  const parser = new DOMParser()
  const doc = parser.parseFromString(sanitized, 'text/html')
  const headings = doc.querySelectorAll('h2, h3')
  const toc = []

  headings.forEach((heading, index) => {
    const id = `heading-${index}`
    heading.id = id
    toc.push({
      id,
      text: heading.textContent || `章节 ${index + 1}`,
      level: Number(heading.tagName.replace('H', '')),
    })
  })

  doc.querySelectorAll('pre code').forEach((block) => {
    const code = block.textContent || ''
    const result = hljs.highlightAuto(code)
    block.innerHTML = result.value
    block.classList.add('hljs')
  })

  tocItems.value = toc
  activeTocId.value = toc[0]?.id || ''
  return doc.body.innerHTML
}

function formatDate(value) {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

watch(
  () => route.params.id,
  () => {
    loadBlog()
  },
  { immediate: true },
)
</script>

<style scoped>
.detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 16px;
}

.article {
  padding: 18px 22px;
}

h1 {
  margin-bottom: 10px;
  font-size: 34px;
  line-height: 1.2;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: var(--text-soft);
  font-size: 14px;
  margin-bottom: 18px;
}

.meta a {
  color: var(--accent);
}

.markdown-body {
  font-size: 16px;
  color: #243648;
}

:deep(.markdown-body pre) {
  overflow: auto;
  border-radius: 10px;
  padding: 12px;
  background: #0f172a;
}

:deep(.markdown-body code) {
  font-family: 'JetBrains Mono', 'Cascadia Mono', monospace;
}

:deep(.markdown-body img) {
  border-radius: 12px;
}

.footer-actions {
  margin: 18px 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  border: 1px solid;
  border-radius: 999px;
  padding: 3px 10px;
  color: var(--text-soft);
  font-size: 12px;
}

.toc-sidebar {
  position: sticky;
  top: 80px;
  align-self: start;
}

@media (max-width: 1024px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .toc-sidebar {
    position: static;
  }
}

@media (max-width: 768px) {
  .article {
    padding: 14px;
  }

  h1 {
    font-size: 28px;
  }
}
</style>
