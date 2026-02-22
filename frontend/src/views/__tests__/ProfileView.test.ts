import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ProfileView from '../ProfileView.vue'

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
}))

const mockProfile = {
  id: 1,
  userId: 10,
  displayName: 'John Doe',
  avatarUrl: 'https://example.com/avatar.jpg',
  timezone: 'America/New_York',
  notificationPrefs: { emailNotifications: true, pushNotifications: false, reminderTime: '09:00' },
  telegramChatId: '123456',
  whatsappNumber: '+1234567890',
  tutorialCompleted: true,
}

const mockGet = vi.fn().mockResolvedValue({ data: mockProfile })
const mockPut = vi.fn().mockResolvedValue({ data: mockProfile })
vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: vi.fn(),
    put: (...args: unknown[]) => mockPut(...args),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('ProfileView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockGet.mockReset().mockResolvedValue({ data: mockProfile })
    mockPut.mockReset().mockResolvedValue({ data: mockProfile })
  })

  it('renders page header', () => {
    const wrapper = mount(ProfileView)
    expect(wrapper.find('h1').text()).toBe('Profile Settings')
    expect(wrapper.find('.subtitle').text()).toContain('account preferences')
  })

  it('shows loading state while fetching', async () => {
    let resolveGet: (value: unknown) => void
    mockGet.mockReturnValueOnce(
      new Promise((resolve) => {
        resolveGet = resolve
      }),
    )
    const wrapper = mount(ProfileView)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.loading').exists()).toBe(true)

    resolveGet!({ data: mockProfile })
    await flushPromises()

    expect(wrapper.find('.loading').exists()).toBe(false)
  })

  it('renders form sections after profile loads', async () => {
    const wrapper = mount(ProfileView)
    await flushPromises()

    expect(wrapper.find('.profile-form').exists()).toBe(true)
    const sections = wrapper.findAll('.form-section')
    expect(sections).toHaveLength(5)
  })

  it('populates form with profile data', async () => {
    const wrapper = mount(ProfileView)
    await flushPromises()

    const displayNameInput = wrapper.find('#displayName')
    expect((displayNameInput.element as HTMLInputElement).value).toBe('John Doe')

    const timezoneSelect = wrapper.find('#timezone')
    expect((timezoneSelect.element as HTMLSelectElement).value).toBe('America/New_York')
  })

  it('renders messaging fields with unlink buttons', async () => {
    const wrapper = mount(ProfileView)
    await flushPromises()

    const telegramInput = wrapper.find('#telegramChatId')
    expect((telegramInput.element as HTMLInputElement).value).toBe('123456')

    const whatsappInput = wrapper.find('#whatsappNumber')
    expect((whatsappInput.element as HTMLInputElement).value).toBe('+1234567890')

    const unlinkButtons = wrapper.findAll('.btn-danger')
    expect(unlinkButtons).toHaveLength(2)
  })

  it('submits profile form', async () => {
    const wrapper = mount(ProfileView)
    await flushPromises()

    await wrapper.find('.profile-form').trigger('submit')
    await flushPromises()

    expect(mockPut).toHaveBeenCalledWith('/profile', expect.any(Object))
  })

  it('shows success message after save', async () => {
    const wrapper = mount(ProfileView)
    await flushPromises()

    await wrapper.find('.profile-form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('.success-message').exists()).toBe(true)
    expect(wrapper.find('.success-message').text()).toContain('saved successfully')
  })

  it('shows error message on fetch failure', async () => {
    mockGet.mockRejectedValueOnce(new Error('Network error'))
    const wrapper = mount(ProfileView)
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.error-message').exists()).toBe(true)
    expect(wrapper.find('.error-message').text()).toContain('Failed to load profile')
  })
})
