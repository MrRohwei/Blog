<template>
  <section class="card about">
    <h1>关于 Voidpen</h1>
    <p>
      Voidpen（虚笔）是一个轻量的技术博客系统，目标是把「写作、发布、阅读、互动」做得足够直接。
      在这里，你可以记录编程笔记、产品想法，也可以写生活随笔。
    </p>
    <p>
      项目采用 Vue 3 + Spring Boot，支持后台管理、评论审核、广告位配置、Markdown 内容高亮和缓存优化。
    </p>
    <p>如果你也在构建自己的内容系统，欢迎一起交流改进。</p>
  </section>
</template>

<style scoped>
.about {
  padding: 20px;
}

h1 {
  margin-bottom: 12px;
}

p {
  margin: 0 0 10px;
  color: var(--text-soft);
}
</style>
