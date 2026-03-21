import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { reactive } from 'vue'
import NotificationBell from '../NotificationBell.vue'

const notificationsStore = reactive({
  unreadCount: 0,
  startPolling: vi.fn(),
  stopPolling: vi.fn(),
})

vi.mock('@/stores/notifications', () => ({
  useNotificationsStore: () => notificationsStore,
}))

describe('NotificationBell', () => {
  beforeEach(() => {
    notificationsStore.unreadCount = 0
    notificationsStore.startPolling.mockReset()
    notificationsStore.stopPolling.mockReset()
  })

  function mountBell() {
    return mount(NotificationBell, {
      attachTo: document.body,
      global: {
        stubs: {
          NotificationPanel: {
            template: '<div class="notification-panel-stub">Panel</div>',
          },
        },
      },
    })
  }

  it('starts polling on mount and stops on unmount', () => {
    const wrapper = mountBell()

    expect(notificationsStore.startPolling).toHaveBeenCalledTimes(1)

    wrapper.unmount()

    expect(notificationsStore.stopPolling).toHaveBeenCalledTimes(1)
  })

  it('toggles the notification panel from the bell button', async () => {
    const wrapper = mountBell()

    expect(wrapper.find('.notification-panel-stub').exists()).toBe(false)

    await wrapper.find('.notification-bell__btn').trigger('click')

    expect(wrapper.find('.notification-panel-stub').exists()).toBe(true)
  })

  it('closes the panel on outside click and escape', async () => {
    const wrapper = mountBell()

    await wrapper.find('.notification-bell__btn').trigger('click')
    expect(wrapper.find('.notification-panel-stub').exists()).toBe(true)

    document.body.dispatchEvent(new MouseEvent('click', { bubbles: true }))
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.notification-panel-stub').exists()).toBe(false)

    await wrapper.find('.notification-bell__btn').trigger('click')
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }))
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.notification-panel-stub').exists()).toBe(false)
  })

  it('caps the badge text at 99+', () => {
    notificationsStore.unreadCount = 120

    const wrapper = mountBell()

    expect(wrapper.find('.notification-bell__badge').text()).toBe('99+')
  })
})
