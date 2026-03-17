import { beforeEach, describe, expect, it, vi, afterEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import OnboardingView from '../OnboardingView.vue'
import { useProfileStore } from '@/stores/profile'

const mockPush = vi.fn()

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>()
  return {
    ...actual,
    useRouter: () => ({ push: mockPush }),
  }
})

describe('OnboardingView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockReset()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('requires at least one role before continuing', async () => {
    const wrapper = mount(OnboardingView)
    const checkboxes = wrapper.findAll('.role-checkbox')

    await checkboxes[0].setValue(false)
    await checkboxes[1].setValue(false)
    await wrapper.find('.submit-btn').trigger('click')

    expect(wrapper.find('.error-msg').text()).toContain('Please select at least one role')
  })

  it('moves to survey mode after roles are saved', async () => {
    const profileStore = useProfileStore()
    const updateRolesSpy = vi.spyOn(profileStore, 'updateRoles').mockResolvedValue(undefined)

    const wrapper = mount(OnboardingView)
    await wrapper.find('.submit-btn').trigger('click')
    await flushPromises()

    expect(updateRolesSpy).toHaveBeenCalledWith(true, false)
    expect(wrapper.text()).toContain('Tell us about yourself')
  })

  it('shows a role setup error when role save fails', async () => {
    const profileStore = useProfileStore()
    vi.spyOn(profileStore, 'updateRoles').mockRejectedValue(new Error('boom'))

    const wrapper = mount(OnboardingView)
    await wrapper.find('.submit-btn').trigger('click')
    await flushPromises()

    expect(wrapper.find('.error-msg').text()).toContain('Could not set roles')
  })

  it('submits the survey and redirects to the dashboard', async () => {
    vi.useFakeTimers()
    const profileStore = useProfileStore()
    vi.spyOn(profileStore, 'updateRoles').mockResolvedValue(undefined)
    const submitSurveySpy = vi.spyOn(profileStore, 'submitSurvey').mockResolvedValue(undefined)

    const wrapper = mount(OnboardingView)
    await wrapper.find('.submit-btn').trigger('click')
    await flushPromises()

    const chips = wrapper.findAll('.chip')
    await chips[0].trigger('click')
    await wrapper.find('.survey-actions .submit-btn').trigger('click')
    await flushPromises()

    expect(submitSurveySpy).toHaveBeenCalledWith(
      expect.objectContaining({ lifeAreas: ['Work'], moodBaseline: 5, anxietyLevel: 5 }),
    )
    expect(wrapper.text()).toContain('Your goals are ready!')

    await vi.advanceTimersByTimeAsync(1500)
    expect(mockPush).toHaveBeenCalledWith({ name: 'dashboard' })
  })

  it('skips the survey and redirects to the dashboard', async () => {
    const profileStore = useProfileStore()
    vi.spyOn(profileStore, 'updateRoles').mockResolvedValue(undefined)
    const skipSurveySpy = vi.spyOn(profileStore, 'skipSurvey').mockResolvedValue(undefined)

    const wrapper = mount(OnboardingView)
    await wrapper.find('.submit-btn').trigger('click')
    await flushPromises()

    await wrapper.find('.skip-btn').trigger('click')
    await flushPromises()

    expect(skipSurveySpy).toHaveBeenCalled()
    expect(mockPush).toHaveBeenCalledWith({ name: 'dashboard' })
  })

  it('shows a survey submission error when submit fails', async () => {
    const profileStore = useProfileStore()
    vi.spyOn(profileStore, 'updateRoles').mockResolvedValue(undefined)
    vi.spyOn(profileStore, 'submitSurvey').mockRejectedValue(new Error('boom'))

    const wrapper = mount(OnboardingView)
    await wrapper.find('.submit-btn').trigger('click')
    await flushPromises()

    await wrapper.find('.survey-actions .submit-btn').trigger('click')
    await flushPromises()

    expect(wrapper.find('.error-msg').text()).toContain('Something went wrong')
  })
})
