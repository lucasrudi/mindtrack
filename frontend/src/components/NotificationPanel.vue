<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationsStore } from '@/stores/notifications'

const notifications = useNotificationsStore()
const router = useRouter()

onMounted(() => {
  notifications.fetchNotifications()
})

async function handleClick(id: number, link: string | null) {
  await notifications.markRead(id)
  if (link) {
    router.push(link)
  }
}
</script>

<template>
  <div class="notification-panel">
    <div class="notification-panel__header">
      <span class="notification-panel__title">Notifications</span>
      <button
        v-if="notifications.hasUnread"
        class="notification-panel__mark-all"
        type="button"
        @click="notifications.markAllRead()"
      >
        Mark all read
      </button>
    </div>

    <div v-if="notifications.loading" class="notification-panel__empty">Loading…</div>

    <div v-else-if="notifications.notifications.length === 0" class="notification-panel__empty">
      No notifications yet.
    </div>

    <ul v-else class="notification-panel__list">
      <li
        v-for="notification in notifications.notifications"
        :key="notification.id"
        :class="['notification-item', { 'notification-item--unread': !notification.read }]"
        role="button"
        tabindex="0"
        @click="handleClick(notification.id, notification.link)"
        @keydown.enter="handleClick(notification.id, notification.link)"
      >
        <div class="notification-item__title">{{ notification.title }}</div>
        <div v-if="notification.body" class="notification-item__body">{{ notification.body }}</div>
        <div class="notification-item__time">
          {{ new Date(notification.createdAt).toLocaleString() }}
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.notification-panel {
  width: 360px;
  max-height: 480px;
  overflow-y: auto;
  background-color: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-sm);
}

.notification-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-gray-200);
}

.notification-panel__title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
}

.notification-panel__mark-all {
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.notification-panel__mark-all:hover {
  text-decoration: underline;
}

.notification-panel__empty {
  padding: var(--space-6) var(--space-4);
  text-align: center;
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
}

.notification-panel__list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.notification-item {
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-gray-100);
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item:hover {
  background-color: var(--color-gray-50);
}

.notification-item--unread {
  background-color: var(--color-primary-50, #eff6ff);
}

.notification-item--unread:hover {
  background-color: var(--color-primary-100, #dbeafe);
}

.notification-item__title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-900);
  margin-bottom: var(--space-1);
}

.notification-item__body {
  font-size: var(--font-size-xs);
  color: var(--color-gray-600);
  margin-bottom: var(--space-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notification-item__time {
  font-size: var(--font-size-xs);
  color: var(--color-gray-400);
}
</style>
