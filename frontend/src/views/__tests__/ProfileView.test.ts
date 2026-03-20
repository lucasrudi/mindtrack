import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ProfileView from '../ProfileView.vue'
import { useAuthStore } from '@/stores/auth'
import { useProfileStore } from '@/stores/profile'
import { clearDashboardSessionCache } from '@/stores/dashboardSessionCache'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
}))

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
const mockPatch = vi.fn().mockResolvedValue({})
const mockDelete = vi.fn().mockResolvedValue({})
vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: vi.fn(),
    put: (...args: unknown[]) => mockPut(...args),
    patch: (...args: unknown[]) => mockPatch(...args),
    delete: (...args: unknown[]) => mockDelete(...args),
  },
}))

describe('ProfileView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    clearDashboardSessionCache()
    mockPush.mockReset()
    localStorageMock.getItem.mockReturnValue(null)
    mockGet.mockReset().mockResolvedValue({ data: mockProfile })
    mockPut.mockReset().mockResolvedValue({ data: mockProfile })
    mockPatch.mockReset().mockResolvedValue({})
    mockDelete.mockReset().mockResolvedValue({})
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
    expect(sections).toHaveLength(8)
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

  it('unlinks telegram and WhatsApp identifiers', async () => {
    const wrapper = mount(ProfileView)
    await flushPromises()

    const unlinkButtons = wrapper.findAll('.input-with-action .btn-danger')
    await unlinkButtons[0].trigger('click')
    await unlinkButtons[1].trigger('click')

    expect((wrapper.find('#telegramChatId').element as HTMLInputElement).value).toBe('')
    expect((wrapper.find('#whatsappNumber').element as HTMLInputElement).value).toBe('')
  })

  it('shows a validation error when all roles are deselected', async () => {
    const wrapper = mount(ProfileView)
    await flushPromises()

    const roleCheckboxes = wrapper.findAll('.role-toggle-group input[type="checkbox"]')
    await roleCheckboxes[0].setValue(false)
    await roleCheckboxes[1].setValue(false)
    await wrapper.findAll('.form-section')[1].find('.btn.btn-secondary').trigger('click')

    expect(wrapper.text()).toContain('At least one role must be selected.')
  })

  it('saves roles successfully', async () => {
    const store = useProfileStore()
    const updateRolesSpy = vi.spyOn(store, 'updateRoles').mockResolvedValue(undefined)
    const wrapper = mount(ProfileView)
    await flushPromises()

    const roleCheckboxes = wrapper.findAll('.role-toggle-group input[type="checkbox"]')
    await roleCheckboxes[1].setValue(true)
    await wrapper.findAll('.form-section')[1].find('.btn.btn-secondary').trigger('click')
    await flushPromises()

    expect(updateRolesSpy).toHaveBeenCalledWith(true, true)
    expect(wrapper.text()).toContain('Roles updated successfully.')
  })

  it('shows a role error when saving roles fails', async () => {
    const store = useProfileStore()
    vi.spyOn(store, 'updateRoles').mockRejectedValue(new Error('boom'))
    const wrapper = mount(ProfileView)
    await flushPromises()

    await wrapper.findAll('.form-section')[1].find('.btn.btn-secondary').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Could not update roles')
  })

  it('submits the inline survey baseline', async () => {
    const store = useProfileStore()
    const submitSurveySpy = vi.spyOn(store, 'submitSurvey').mockResolvedValue(undefined)
    const wrapper = mount(ProfileView)
    await flushPromises()

    const toggleSurveyButton = wrapper
      .findAll('button')
      .find((button) => button.text().includes('Complete Survey'))
    await toggleSurveyButton!.trigger('click')
    await wrapper.find('.chip').trigger('click')
    await wrapper.find('#wellness-baseline .btn.btn-primary').trigger('click')
    await flushPromises()

    expect(submitSurveySpy).toHaveBeenCalledWith(
      expect.objectContaining({ lifeAreas: ['Work'], moodBaseline: 5, anxietyLevel: 5 }),
    )
  })

  it('replays the tutorial and redirects to dashboard', async () => {
    const store = useProfileStore()
    const updateProfileSpy = vi.spyOn(store, 'updateProfile').mockResolvedValue({
      ...mockProfile,
      tutorialCompleted: false,
    })
    const wrapper = mount(ProfileView)
    await flushPromises()

    const replayButton = wrapper
      .findAll('button')
      .find((button) => button.text() === 'Replay Tutorial')
    await replayButton!.trigger('click')
    await flushPromises()

    expect(updateProfileSpy).toHaveBeenCalledWith(
      expect.objectContaining({ tutorialCompleted: false }),
    )
    expect(mockPush).toHaveBeenCalledWith({ name: 'dashboard' })
  })

  it('deletes the account after confirmation', async () => {
    const authStore = useAuthStore()
    const deleteAccountSpy = vi.spyOn(authStore, 'deleteAccount').mockResolvedValue(undefined)
    const wrapper = mount(ProfileView)
    await flushPromises()

    const openDeleteButton = wrapper
      .findAll('button')
      .find((button) => button.text() === 'Delete Account')
    await openDeleteButton!.trigger('click')
    await wrapper.find('.delete-confirm-input').setValue('DELETE')
    await wrapper.find('.delete-confirm-actions .btn-danger').trigger('click')
    await flushPromises()

    expect(deleteAccountSpy).toHaveBeenCalled()
    expect(mockPush).toHaveBeenCalledWith('/login')
  })

  it('shows an error when account deletion fails', async () => {
    const authStore = useAuthStore()
    vi.spyOn(authStore, 'deleteAccount').mockRejectedValue(new Error('boom'))
    const wrapper = mount(ProfileView)
    await flushPromises()

    const openDeleteButton = wrapper
      .findAll('button')
      .find((button) => button.text() === 'Delete Account')
    await openDeleteButton!.trigger('click')
    await wrapper.find('.delete-confirm-input').setValue('DELETE')
    await wrapper.find('.delete-confirm-actions .btn-danger').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Could not delete account')
  })
})
