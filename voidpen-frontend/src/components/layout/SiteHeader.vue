<template>
  <header class="site-header">
    <div class="inner">
      <router-link to="/" class="brand">
        <span class="brand-main">Voidpen</span>
        <span class="brand-sub">虚笔</span>
      </router-link>

      <button class="menu-btn" @click="menuOpen = !menuOpen">菜单</button>

      <nav class="nav" :class="{ open: menuOpen }">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path" @click="menuOpen = false">
          {{ item.label }}
        </router-link>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue'

const menuOpen = ref(false)

const navItems = [
  { path: '/', label: '首页' },
  { path: '/archive', label: '归档' },
  { path: '/about', label: '关于' },
]
</script>

<style scoped>
.site-header {
  position: sticky;
  top: 0;
  z-index: 20;
  backdrop-filter: blur(8px);
  background: color-mix(in srgb, var(--surface), transparent 12%);
  border-bottom: 1px solid var(--border);
}

.inner {
  width: min(1180px, calc(100% - 32px));
  margin: 0 auto;
  min-height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.brand {
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
  color: var(--text-main);
}

.brand-main {
  font-family: var(--font-display);
  font-size: 24px;
  letter-spacing: 0.4px;
}

.brand-sub {
  font-size: 13px;
  color: var(--text-soft);
}

.nav {
  display: inline-flex;
  align-items: center;
  gap: 20px;
}

.nav a {
  color: var(--text-soft);
  padding: 6px 4px;
  border-bottom: 2px solid transparent;
}

.nav a.router-link-active {
  color: var(--text-main);
  border-color: var(--accent);
}

.menu-btn {
  display: none;
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text-main);
  border-radius: 8px;
  padding: 6px 10px;
}

@media (max-width: 768px) {
  .inner {
    width: calc(100% - 20px);
  }

  .menu-btn {
    display: inline-flex;
  }

  .nav {
    display: none;
    position: absolute;
    top: 64px;
    right: 12px;
    padding: 12px;
    border-radius: 10px;
    background: var(--surface);
    border: 1px solid var(--border);
    box-shadow: var(--shadow-soft);
    flex-direction: column;
    align-items: flex-start;
    min-width: 120px;
  }

  .nav.open {
    display: flex;
  }
}
</style>
