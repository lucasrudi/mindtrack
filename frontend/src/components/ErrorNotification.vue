<script setup lang="ts">
import { useErrorHandler } from '@/composables/useErrorHandler'

const { globalErrors, dismissError } = useErrorHandler()
</script>

<template>
  <div class="error-notification-container">
    <transition-group name="toast">
      <div
        v-for="error in globalErrors"
        :key="error.id"
        class="toast"
        :class="`toast--${error.type}`"
      >
        <span class="toast__message">{{ error.message }}</span>
        <button class="toast__dismiss" aria-label="Dismiss" @click="dismissError(error.id)">
          ×
        </button>
      </div>
    </transition-group>
  </div>
</template>

<style scoped>
.error-notification-container {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 380px;
}

.toast {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  font-size: 0.875rem;
  line-height: 1.4;
}

.toast--error {
  background: #fef2f2;
  border-left: 4px solid #ef4444;
  color: #991b1b;
}

.toast--warning {
  background: #fffbeb;
  border-left: 4px solid #f59e0b;
  color: #92400e;
}

.toast--info {
  background: #eff6ff;
  border-left: 4px solid #3b82f6;
  color: #1e40af;
}

.toast__message {
  flex: 1;
}

.toast__dismiss {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.25rem;
  line-height: 1;
  opacity: 0.6;
  padding: 0;
  color: inherit;
  flex-shrink: 0;
}

.toast__dismiss:hover {
  opacity: 1;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style>
