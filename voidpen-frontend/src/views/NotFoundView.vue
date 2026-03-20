<template>
  <section class="card not-found">
    <h1>404</h1>
    <p>页面不存在，可能已经被移动或删除。</p>
    <router-link to="/">返回首页</router-link>
  </section>
</template>

<style scoped>
.not-found {
  margin-top: 40px;
  padding: 24px;
  text-align: center;
}

h1 {
  font-size: 56px;
  margin-bottom: 8px;
}

p {
  color: var(--text-soft);
}

a {
  color: var(--accent);
}
</style>
