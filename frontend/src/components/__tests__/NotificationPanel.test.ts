import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import NotificationPanel from '../NotificationPanel.vue'

const mockGet = vi.fn()
const mockPatch = vi.fn()
const push = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push }),
}))

vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    patch: (...args: unknown[]) => mockPatch(...args),
  },
}))

describe('NotificationPanel', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockGet.mockReset()
    mockPatch.mockReset()
    push.mockReset()
  })

  it('renders interactive notifications as buttons', async () => {
    mockGet.mockResolvedValueOnce({
      data: {
        content: [
          {
            id: 1,
            type: 'APPOINTMENT',
            title: 'Upcoming appointment',
            body: 'Tomorrow at 10:00',
            read: false,
            link: '/appointments',
            createdAt: '2026-03-21T10:00:00Z',
          },
        ],
      },
    })
    mockPatch.mockResolvedValueOnce({
      data: {
        id: 1,
        type: 'APPOINTMENT',
        title: 'Upcoming appointment',
        body: 'Tomorrow at 10:00',
        read: true,
        link: '/appointments',
        createdAt: '2026-03-21T10:00:00Z',
      },
    })

    const wrapper = mount(NotificationPanel)
    await flushPromises()

    const notificationButton = wrapper.find('.notification-item')
    expect(notificationButton.element.tagName).toBe('BUTTON')

    await notificationButton.trigger('click')

    expect(mockPatch).toHaveBeenCalledWith('/notifications/1/read')
  })
})
