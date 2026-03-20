<template>
  <div class="front-layout">
    <SiteHeader />

    <div class="ad-slot ad-slot-top">
      <AdvertisementWidget position="HEADER" display-mode="ticker" :hide-when-empty="true" />
    </div>

    <main class="layout-main">
      <router-view />
    </main>

    <div class="ad-slot ad-slot-bottom">
      <AdvertisementWidget position="FOOTER" display-mode="ticker" :hide-when-empty="true" />
    </div>

    <SiteFooter />
    <BackTopButton />
  </div>
</template>

<script setup>
import AdvertisementWidget from '@/components/common/AdvertisementWidget.vue'
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

.ad-slot {
  width: min(1180px, calc(100% - 32px));
  margin: 12px auto 0;
}

.ad-slot-bottom {
  margin: 0 auto 14px;
}

@media (max-width: 768px) {
  .layout-main {
    width: calc(100% - 20px);
    margin-top: 16px;
  }

  .ad-slot {
    width: calc(100% - 20px);
  }
}
</style>
