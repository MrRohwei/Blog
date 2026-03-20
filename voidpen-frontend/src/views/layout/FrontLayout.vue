<template>
  <div class="front-layout">
    <SiteHeader />
    <main class="layout-main">
      <router-view />
    </main>
    <SiteFooter />
    <BackTopButton />
  </div>
</template>

<script setup>
import BackTopButton from '@/components/common/BackTopButton.vue'
import SiteFooter from '@/components/layout/SiteFooter.vue'
import SiteHeader from '@/components/layout/SiteHeader.vue'
</script>

<style scoped>
.front-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-main {
  flex: 1;
  width: min(1180px, calc(100% - 32px));
  margin: 24px auto 36px;
}

@media (max-width: 768px) {
  .layout-main {
    width: calc(100% - 20px);
    margin-top: 16px;
  }
}
</style>
