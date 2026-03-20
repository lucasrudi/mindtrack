import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import LoginView from '../LoginView.vue'

const replaceMock = vi.fn()
const authState = vi.hoisted(() => ({
  isAuthenticated: false,
  fetchCurrentUser: vi.fn(),
  homeRouteName: 'dashboard',
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ replace: replaceMock }),
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => authState,
}))

describe('LoginView', () => {
  beforeEach(() => {
    replaceMock.mockReset()
    authState.isAuthenticated = false
    authState.homeRouteName = 'dashboard'
    authState.fetchCurrentUser.mockReset().mockResolvedValue(undefined)
  })

  it('shows the signing-in message', () => {
    const wrapper = mount(LoginView)
    expect(wrapper.text()).toContain('Signing you in...')
  })

  it('redirects authenticated users to the dashboard after fetching', async () => {
    authState.fetchCurrentUser.mockImplementation(async () => {
      authState.isAuthenticated = true
    })

    mount(LoginView)
    await flushPromises()

    expect(authState.fetchCurrentUser).toHaveBeenCalled()
    expect(replaceMock).toHaveBeenCalledWith({ name: 'dashboard' })
  })

  it('redirects unauthenticated users back to landing', async () => {
    mount(LoginView)
    await flushPromises()

    expect(replaceMock).toHaveBeenCalledWith('/')
  })
})
