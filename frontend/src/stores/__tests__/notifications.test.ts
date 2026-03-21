import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNotificationsStore } from '../notifications'

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    patch: vi.fn(),
  },
}))

const sampleNotification = {
  id: 1,
  type: 'APPOINTMENT',
  title: 'New appointment',
  body: 'You have a new appointment',
  read: false,
  link: '/appointments/1',
  createdAt: '2026-03-20T10:00:00',
}

const sampleNotification2 = {
  id: 2,
  type: 'GOAL',
  title: 'Goal progress',
  body: null,
  read: true,
  link: '/goals/2',
  createdAt: '2026-03-19T10:00:00',
}

describe('useNotificationsStore', () => {
  let api: {
    get: ReturnType<typeof vi.fn>
    patch: ReturnType<typeof vi.fn>
  }

  beforeEach(async () => {
    setActivePinia(createPinia())
    const module = await import('@/services/api')
    api = module.default as unknown as typeof api
    vi.clearAllMocks()
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('initializes with empty state', () => {
    const store = useNotificationsStore()
    expect(store.notifications).toEqual([])
    expect(store.unreadCount).toBe(0)
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
    expect(store.hasUnread).toBe(false)
  })

  it('fetches notifications', async () => {
    api.get.mockResolvedValue({
      data: {
        content: [sampleNotification, sampleNotification2],
        page: 0,
        size: 20,
        totalElements: 2,
        totalPages: 1,
      },
    })
    const store = useNotificationsStore()

    await store.fetchNotifications()

    expect(api.get).toHaveBeenCalledWith('/notifications', { params: { page: 0, size: 20 } })
    expect(store.notifications).toHaveLength(2)
    expect(store.loading).toBe(false)
  })

  it('fetches unread count', async () => {
    api.get.mockResolvedValue({ data: { count: 5 } })
    const store = useNotificationsStore()

    await store.fetchUnreadCount()

    expect(api.get).toHaveBeenCalledWith('/notifications/unread-count')
    expect(store.unreadCount).toBe(5)
    expect(store.hasUnread).toBe(true)
  })

  it('marks single notification as read', async () => {
    api.get.mockResolvedValue({
      data: {
        content: [sampleNotification],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      },
    })
    api.patch.mockResolvedValue({ data: { ...sampleNotification, read: true } })
    const store = useNotificationsStore()
    await store.fetchNotifications()
    store.unreadCount = 1

    await store.markRead(1)

    expect(api.patch).toHaveBeenCalledWith('/notifications/1/read')
    expect(store.notifications[0].read).toBe(true)
    expect(store.unreadCount).toBe(0)
  })

  it('marks all notifications as read', async () => {
    api.get.mockResolvedValue({
      data: {
        content: [sampleNotification, { ...sampleNotification2, read: false }],
        page: 0,
        size: 20,
        totalElements: 2,
        totalPages: 1,
      },
    })
    api.patch.mockResolvedValue({})
    const store = useNotificationsStore()
    await store.fetchNotifications()
    store.unreadCount = 2

    await store.markAllRead()

    expect(api.patch).toHaveBeenCalledWith('/notifications/read-all')
    expect(store.notifications.every((n) => n.read)).toBe(true)
    expect(store.unreadCount).toBe(0)
  })

  it('sets error on fetch failure', async () => {
    api.get.mockRejectedValue(new Error('Network error'))
    const store = useNotificationsStore()

    await expect(store.fetchNotifications()).rejects.toThrow('Network error')

    expect(store.error).toBe('Failed to load notifications')
  })

  it('does not set error on polling failure', async () => {
    api.get.mockRejectedValue(new Error('Network error'))
    const store = useNotificationsStore()

    await store.fetchUnreadCount()

    expect(store.error).toBeNull()
  })

  it('clears error', () => {
    const store = useNotificationsStore()
    store.error = 'Some error'
    store.clearError()
    expect(store.error).toBeNull()
  })
})
