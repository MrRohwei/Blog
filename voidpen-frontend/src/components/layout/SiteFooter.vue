<template>
  <footer class="site-footer">
    <div class="inner">
      <p>© {{ year }} Voidpen · 记录技术与生活的灵感片段</p>
    </div>
  </footer>
</template>

<script setup>
const year = new Date().getFullYear()
</script>

<style scoped>
.site-footer {
  border-top: 1px solid var(--border);
  background: var(--surface);
}

.inner {
  width: min(1180px, calc(100% - 32px));
  margin: 0 auto;
  padding: 18px 0;
}

p {
  margin: 0;
  color: var(--text-soft);
  font-size: 13px;
}

@media (max-width: 768px) {
  .inner {
    width: calc(100% - 20px);
  }
}
</style>
