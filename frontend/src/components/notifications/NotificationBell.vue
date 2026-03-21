<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useNotificationsStore } from '@/stores/notifications'
import NotificationPanel from './NotificationPanel.vue'

const notifications = useNotificationsStore()
const panelOpen = ref(false)
const bellRef = ref<HTMLElement | null>(null)

function togglePanel() {
  panelOpen.value = !panelOpen.value
}

function closePanel() {
  panelOpen.value = false
}

function handleDocumentClick(event: MouseEvent) {
  if (bellRef.value && !bellRef.value.contains(event.target as Node)) {
    closePanel()
  }
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    closePanel()
  }
}

onMounted(() => {
  notifications.startPolling()
  document.addEventListener('click', handleDocumentClick)
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  notifications.stopPolling()
  document.removeEventListener('click', handleDocumentClick)
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <div ref="bellRef" class="notification-bell">
    <button
      class="notification-bell__btn"
      type="button"
      aria-label="Notifications"
      :aria-expanded="panelOpen"
      @click.stop="togglePanel"
    >
      <svg
        class="notification-bell__icon"
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
        aria-hidden="true"
      >
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
        <path d="M13.73 21a2 2 0 0 1-3.46 0" />
      </svg>
      <span v-if="notifications.unreadCount > 0" class="notification-bell__badge">
        {{ notifications.unreadCount > 99 ? '99+' : notifications.unreadCount }}
      </span>
    </button>

    <div v-if="panelOpen" class="notification-bell__panel">
      <NotificationPanel />
    </div>
  </div>
</template>

<style scoped>
.notification-bell {
  position: relative;
  display: inline-flex;
}

.notification-bell__btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: none;
  border: none;
  border-radius: var(--border-radius-sm);
  color: var(--color-gray-600);
  cursor: pointer;
  transition:
    color var(--transition-fast),
    background-color var(--transition-fast);
}

.notification-bell__btn:hover {
  color: var(--color-primary);
  background-color: var(--color-primary-50);
}

.notification-bell__icon {
  width: 20px;
  height: 20px;
}

.notification-bell__badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 16px;
  height: 16px;
  padding: 0 3px;
  font-size: 10px;
  font-weight: var(--font-weight-bold);
  line-height: 16px;
  text-align: center;
  color: var(--color-white);
  background-color: var(--color-error, #ef4444);
  border-radius: 999px;
}

.notification-bell__panel {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  z-index: 200;
}
</style>
