<template>
  <div class="detail-layout">
    <article class="card article">
      <template v-if="loading">
        <div class="skeleton-line headline" />
        <div class="skeleton-line" />
        <div class="skeleton-line short" />
        <div class="skeleton-box" />
      </template>

      <template v-else-if="loadError">
        <div class="empty-hint">{{ loadError }}</div>
      </template>

      <template v-else>
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
      </template>
    </article>

    <aside v-if="tocItems.length && !loading && !loadError" class="toc-sidebar">
      <TableOfContents :items="tocItems" v-model:active-id="activeTocId" />
    </aside>

    <button
      v-if="tocItems.length && !loading && !loadError"
      type="button"
      class="toc-fab"
      @click="isTocPanelOpen = true"
    >
      目录
    </button>

    <div v-if="isTocPanelOpen" class="toc-overlay" @click.self="isTocPanelOpen = false">
      <section class="toc-panel" @click="handleTocPanelClick">
        <header class="toc-panel-header">
          <strong>目录</strong>
          <button type="button" @click="isTocPanelOpen = false">关闭</button>
        </header>
        <TableOfContents :items="tocItems" v-model:active-id="activeTocId" />
      </section>
    </div>
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
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import 'highlight.js/styles/github-dark.css'
import CommentSection from '@/components/blog/CommentSection.vue'
import LikeButton from '@/components/blog/LikeButton.vue'
import TableOfContents from '@/components/blog/TableOfContents.vue'
import AdvertisementWidget from '@/components/common/AdvertisementWidget.vue'
import { getBlogDetail } from '@/api/blog'

const route = useRoute()
const SEO_META_TAG = 'data-voidpen-meta'

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
const loading = ref(false)
const loadError = ref('')
const isTocPanelOpen = ref(false)

const contentHtml = computed(() => buildRenderedContent(blog.value.content || ''))

async function loadBlog() {
  loading.value = true
  loadError.value = ''
  isTocPanelOpen.value = false
  try {
    blog.value = await getBlogDetail(route.params.id)
    applyBlogMeta(blog.value)
  } catch (error) {
    blog.value = {}
    tocItems.value = []
    activeTocId.value = ''
    clearBlogMeta()
    loadError.value = error?.message || '文章加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function buildRenderedContent(markdownContent) {
  const html = marked.parse(markdownContent || '')
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
    const preferredLanguage = parsePreferredLanguage(block.className)
    const canUsePreferredLanguage = preferredLanguage && hljs.getLanguage(preferredLanguage)
    const result = canUsePreferredLanguage
      ? hljs.highlight(code, { language: preferredLanguage, ignoreIllegals: true })
      : hljs.highlightAuto(code)
    const languageLabel = normalizeLanguageLabel(preferredLanguage || result.language)

    block.innerHTML = result.value
    block.classList.add('hljs')
    const pre = block.parentElement
    if (pre) {
      pre.classList.add('code-window')
      pre.dataset.lang = languageLabel
    }
  })

  tocItems.value = toc
  activeTocId.value = toc[0]?.id || ''
  return doc.body.innerHTML
}

function parsePreferredLanguage(classNames) {
  const classes = String(classNames || '').split(/\s+/).filter(Boolean)
  const languageClass = classes.find((item) => item.startsWith('language-'))
  if (!languageClass) {
    return ''
  }
  return languageClass.replace('language-', '').toLowerCase()
}

function normalizeLanguageLabel(language) {
  const normalized = String(language || '').trim()
  if (!normalized) {
    return 'TEXT'
  }
  return normalized.toUpperCase()
}

function applyBlogMeta(blogData) {
  const title = blogData?.title || '博客详情'
  const summary = blogData?.summary || 'Voidpen 博客内容详情页'
  const image = blogData?.coverImg || ''

  document.title = `${title} - Voidpen`
  upsertMeta('name', 'description', summary)
  upsertMeta('property', 'og:title', title)
  upsertMeta('property', 'og:description', summary)
  upsertMeta('property', 'og:image', image)
}

function upsertMeta(key, name, content) {
  let tag = document.head.querySelector(`meta[${key}="${name}"]`)
  if (!tag) {
    tag = document.createElement('meta')
    tag.setAttribute(key, name)
    document.head.appendChild(tag)
  }
  tag.setAttribute('content', content)
  tag.setAttribute(SEO_META_TAG, 'blog-detail')
}

function clearBlogMeta() {
  document.head
    .querySelectorAll(`meta[${SEO_META_TAG}="blog-detail"]`)
    .forEach((node) => node.remove())
}

function handleTocPanelClick(event) {
  if (event.target.closest('a')) {
    isTocPanelOpen.value = false
  }
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

onBeforeUnmount(() => {
  clearBlogMeta()
})
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
  overflow: hidden;
  border-radius: 14px;
  margin: 18px 0;
}

:deep(.markdown-body pre.code-window) {
  position: relative;
  padding-top: 40px;
  background: #0f172a;
  border: 1px solid #233145;
  box-shadow: 0 14px 30px rgba(2, 6, 23, 0.24);
}

:deep(.markdown-body pre.code-window::before) {
  content: '';
  position: absolute;
  left: 14px;
  top: 14px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ff5f56;
  box-shadow:
    16px 0 0 #ffbd2e,
    32px 0 0 #27c93f;
}

:deep(.markdown-body pre.code-window::after) {
  content: attr(data-lang);
  position: absolute;
  right: 14px;
  top: 10px;
  color: #9fb5cc;
  font-size: 11px;
  letter-spacing: 0.08em;
}

:deep(.markdown-body code) {
  font-family: 'JetBrains Mono', 'Cascadia Mono', monospace;
}

:deep(.markdown-body pre code.hljs) {
  display: block;
  padding: 14px 16px 16px;
  background: transparent;
  font-size: 14px;
  line-height: 1.66;
  overflow-x: auto;
}

:deep(.markdown-body :not(pre) > code) {
  background: #e9f0f8;
  color: #16324d;
  border-radius: 6px;
  padding: 2px 6px;
  font-size: 0.92em;
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

.toc-fab {
  display: none;
}

.toc-overlay {
  display: none;
}

.skeleton-line,
.skeleton-box {
  background: linear-gradient(90deg, #eef3f8 25%, #e4ebf3 37%, #eef3f8 63%);
  background-size: 400% 100%;
  animation: skeleton 1.2s ease infinite;
}

.skeleton-line {
  height: 14px;
  border-radius: 999px;
  margin-bottom: 12px;
}

.skeleton-line.headline {
  height: 36px;
  width: 78%;
}

.skeleton-line.short {
  width: 55%;
}

.skeleton-box {
  margin-top: 16px;
  height: 280px;
  border-radius: 14px;
}

@media (max-width: 1024px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }

  .toc-sidebar {
    display: none;
  }

  .toc-fab {
    position: fixed;
    right: 14px;
    bottom: 16px;
    z-index: 40;
    border: none;
    border-radius: 999px;
    background: var(--accent);
    color: #fff;
    padding: 10px 14px;
    display: inline-flex;
    box-shadow: var(--shadow-soft);
  }

  .toc-overlay {
    display: flex;
    position: fixed;
    inset: 0;
    z-index: 45;
    background: rgba(15, 23, 42, 0.45);
    align-items: flex-end;
    justify-content: center;
  }

  .toc-panel {
    width: min(640px, 100%);
    max-height: 72vh;
    overflow: auto;
    background: var(--surface);
    border-radius: 16px 16px 0 0;
    border: 1px solid var(--border);
    padding: 12px;
  }

  .toc-panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;
  }

  .toc-panel-header button {
    border: none;
    background: transparent;
    color: var(--accent);
    cursor: pointer;
  }
}

@media (max-width: 768px) {
  .article {
    padding: 14px;
  }

  h1 {
    font-size: 28px;
  }

  .footer-actions {
    flex-direction: column;
    align-items: flex-start;
  }
}

@keyframes skeleton {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}
</style>
