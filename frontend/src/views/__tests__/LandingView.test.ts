import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import LandingView from '../LandingView.vue'

const pushMock = vi.fn()
const authState = vi.hoisted(() => ({
  isAuthenticated: false,
  homeRouteName: 'dashboard',
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock }),
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => authState,
}))

vi.mock('@/components/landing/HeroSection.vue', () => ({
  default: {
    props: ['onSignIn'],
    template: '<button class="hero-signin" @click="onSignIn()">Sign in</button>',
  },
}))

vi.mock('@/components/landing/FeaturesSection.vue', () => ({
  default: { template: '<section class="features-stub" />' },
}))

vi.mock('@/components/landing/BenefitsSection.vue', () => ({
  default: { template: '<section class="benefits-stub" />' },
}))

vi.mock('@/components/landing/FooterSection.vue', () => ({
  default: { template: '<footer class="footer-stub" />' },
}))

describe('LandingView', () => {
  beforeEach(() => {
    pushMock.mockReset()
    authState.isAuthenticated = false
    authState.homeRouteName = 'dashboard'
    Object.defineProperty(globalThis, 'location', {
      value: { href: 'http://localhost/' },
      configurable: true,
    })
  })

  it('renders the landing sections', () => {
    const wrapper = mount(LandingView)

    expect(wrapper.find('.hero-signin').exists()).toBe(true)
    expect(wrapper.find('.features-stub').exists()).toBe(true)
    expect(wrapper.find('.benefits-stub').exists()).toBe(true)
    expect(wrapper.find('.footer-stub').exists()).toBe(true)
  })

  it('routes authenticated users to the dashboard on sign in', async () => {
    authState.isAuthenticated = true
    const wrapper = mount(LandingView)

    await wrapper.find('.hero-signin').trigger('click')

    expect(pushMock).toHaveBeenCalledWith({ name: 'dashboard' })
  })

  it('redirects unauthenticated users to Google OAuth', async () => {
    const wrapper = mount(LandingView)

    await wrapper.find('.hero-signin').trigger('click')

    expect(globalThis.location.href).toBe('/api/oauth2/authorization/google')
  })
})
