import { describe, it, expect, beforeEach, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import App from '../App.vue'

const routeState = vi.hoisted(() => ({
  meta: { requiresAuth: false },
}))

const authState = vi.hoisted(() => ({
  isAuthenticated: false,
  user: null as null | Record<string, unknown>,
  bootstrap: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRoute: () => routeState,
  RouterView: { template: '<div class="router-view-stub" />' },
}))

vi.mock('@/components/layout/AppNavbar.vue', () => ({
  default: { template: '<nav class="app-navbar-stub" />' },
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => authState,
}))

describe('App', () => {
  beforeEach(() => {
    routeState.meta = { requiresAuth: false }
    authState.isAuthenticated = false
    authState.user = null
    authState.bootstrap.mockReset().mockResolvedValue(undefined)
  })

  it('hides the navbar on public routes', () => {
    const wrapper = mount(App)

    expect(wrapper.find('.app-navbar-stub').exists()).toBe(false)
    expect(wrapper.find('main').classes()).not.toContain('has-navbar')
  })

  it('shows the navbar on authenticated routes', () => {
    routeState.meta = { requiresAuth: true }
    const wrapper = mount(App)

    expect(wrapper.find('.app-navbar-stub').exists()).toBe(true)
    expect(wrapper.find('main').classes()).toContain('has-navbar')
  })

  it('bootstraps auth on mount', async () => {
    routeState.meta = { requiresAuth: true }
    authState.isAuthenticated = true
    authState.user = null

    mount(App)
    await flushPromises()

    expect(authState.bootstrap).toHaveBeenCalled()
  })
})
