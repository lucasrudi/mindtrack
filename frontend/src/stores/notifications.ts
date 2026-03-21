import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'

export interface Notification {
  id: number
  type: string
  title: string
  body: string | null
  read: boolean
  link: string | null
  createdAt: string
}

export interface NotificationPage {
  content: Notification[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

const POLL_INTERVAL_MS = 30_000

export const useNotificationsStore = defineStore('notifications', () => {
  const notifications = ref<Notification[]>([])
  const unreadCount = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)
  let pollTimer: ReturnType<typeof setInterval> | null = null

  const hasUnread = computed(() => unreadCount.value > 0)

  async function fetchNotifications(page = 0, size = 20) {
    loading.value = true
    error.value = null
    try {
      const response = await api.get<NotificationPage>('/notifications', {
        params: { page, size },
      })
      notifications.value = response.data.content
      return response.data
    } catch (err) {
      error.value = 'Failed to load notifications'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchUnreadCount() {
    try {
      const response = await api.get<{ count: number }>('/notifications/unread-count')
      unreadCount.value = response.data.count
    } catch {
      // Silently ignore polling errors to avoid noise in the UI
    }
  }

  async function markRead(id: number) {
    try {
      const response = await api.patch<Notification>(`/notifications/${id}/read`)
      const index = notifications.value.findIndex((n) => n.id === id)
      if (index !== -1) {
        notifications.value[index] = response.data
      }
      if (unreadCount.value > 0) {
        unreadCount.value -= 1
      }
      return response.data
    } catch (err) {
      error.value = 'Failed to mark notification as read'
      throw err
    }
  }

  async function markAllRead() {
    try {
      await api.patch('/notifications/read-all')
      notifications.value = notifications.value.map((n) => ({ ...n, read: true }))
      unreadCount.value = 0
    } catch (err) {
      error.value = 'Failed to mark all notifications as read'
      throw err
    }
  }

  function startPolling() {
    if (pollTimer !== null) {
      return
    }
    fetchUnreadCount()
    pollTimer = setInterval(fetchUnreadCount, POLL_INTERVAL_MS)
  }

  function stopPolling() {
    if (pollTimer !== null) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    notifications,
    unreadCount,
    loading,
    error,
    hasUnread,
    fetchNotifications,
    fetchUnreadCount,
    markRead,
    markAllRead,
    startPolling,
    stopPolling,
    clearError,
  }
})
