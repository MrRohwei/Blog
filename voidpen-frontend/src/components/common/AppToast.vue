<template>
  <teleport to="body">
    <div class="toast-wrap">
      <transition-group name="toast">
        <div
          v-for="item in toastState.items"
          :key="item.id"
          class="toast-item"
          :class="`toast-${item.type}`"
          @click="removeToast(item.id)"
        >
          {{ item.message }}
        </div>
      </transition-group>
    </div>
  </teleport>
</template>

<script setup>
import { removeToast, toastState } from '@/utils/toast'
</script>

<style scoped>
.toast-wrap {
  position: fixed;
  top: 76px;
  right: 16px;
  z-index: 60;
  display: grid;
  gap: 10px;
  max-width: min(88vw, 320px);
}

.toast-item {
  border-radius: 10px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-soft);
  background: #fff;
  color: var(--text-main);
  padding: 10px 12px;
  font-size: 13px;
  line-height: 1.5;
  cursor: pointer;
}

.toast-success {
  border-color: #97d6af;
  background: #effaf3;
}

.toast-error {
  border-color: #f2a7a7;
  background: #fff3f3;
}

.toast-info {
  border-color: #b7d7f4;
  background: #f4faff;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.2s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 768px) {
  .toast-wrap {
    top: auto;
    bottom: 16px;
    right: 10px;
    left: 10px;
    max-width: none;
  }
}
</style>
