import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import AppNavbar from '../AppNavbar.vue'

// Ensure localStorage is available in test environment
const localStorageMock = {
  getItem: vi.fn(() => null),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
  length: 0,
  key: vi.fn(() => null),
}

Object.defineProperty(globalThis, 'localStorage', {
  value: localStorageMock,
  writable: true,
})

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/dashboard', component: { template: '<div>Dashboard</div>' } },
    { path: '/journal', component: { template: '<div>Journal</div>' } },
    { path: '/activities', component: { template: '<div>Activities</div>' } },
    { path: '/goals', component: { template: '<div>Goals</div>' } },
    { path: '/interviews', component: { template: '<div>Interviews</div>' } },
    { path: '/chat', component: { template: '<div>Chat</div>' } },
    { path: '/profile', component: { template: '<div>Profile</div>' } },
    { path: '/', component: { template: '<div>Home</div>' } },
  ],
})

describe('AppNavbar', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorageMock.getItem.mockReturnValue(null)
  })

  function mountNavbar() {
    return mount(AppNavbar, {
      global: {
        plugins: [router],
      },
    })
  }

  it('renders the brand name', () => {
    const wrapper = mountNavbar()
    expect(wrapper.find('.navbar-title').text()).toBe('MindTrack')
  })

  it('renders the logo', () => {
    const wrapper = mountNavbar()
    expect(wrapper.find('.navbar-logo').text()).toBe('M')
  })

  it('renders all navigation links', () => {
    const wrapper = mountNavbar()
    const links = wrapper.findAll('.nav-link')
    const linkTexts = links.map((l) => l.text())
    expect(linkTexts).toContain('Dashboard')
    expect(linkTexts).toContain('Journal')
    expect(linkTexts).toContain('Activities')
    expect(linkTexts).toContain('Goals')
    expect(linkTexts).toContain('Interviews')
    expect(linkTexts).toContain('AI Chat')
  })

  it('renders logout button', () => {
    const wrapper = mountNavbar()
    expect(wrapper.find('.btn-logout').text()).toBe('Logout')
  })

  it('shows user name when authenticated', () => {
    const auth = useAuthStore()
    auth.setUser({
      id: '1',
      email: 'test@test.com',
      name: 'John Doe',
      role: 'USER',
      isPatient: true,
      isTherapist: false,
    })

    const wrapper = mountNavbar()
    expect(wrapper.text()).toContain('John Doe')
  })

  it('shows Profile when no user set', () => {
    const wrapper = mountNavbar()
    expect(wrapper.text()).toContain('Profile')
  })

  it('calls logout and navigates on logout click', async () => {
    const auth = useAuthStore()
    auth.setUser({
      id: '1',
      email: 'test@test.com',
      name: 'Test',
      role: 'USER',
      isPatient: true,
      isTherapist: false,
    })
    const wrapper = mountNavbar()

    await wrapper.find('.btn-logout').trigger('click')
    expect(auth.isAuthenticated).toBe(false)
  })
})
