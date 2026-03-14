import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import AiConsentDialog from '../AiConsentDialog.vue'
import { useProfileStore } from '@/stores/profile'

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    fetchCurrentUser: vi.fn().mockResolvedValue(undefined),
  }),
}))

describe('AiConsentDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('renders the dialog with consent text', () => {
    const wrapper = mount(AiConsentDialog)
    expect(wrapper.text()).toContain('AI Coaching Consent')
    expect(wrapper.text()).toContain('Claude API')
  })

  it('renders Accept and Decline buttons', () => {
    const wrapper = mount(AiConsentDialog)
    const buttons = wrapper.findAll('button')
    const texts = buttons.map((b) => b.text())
    expect(texts).toContain('Accept')
    expect(texts).toContain('Decline')
  })

  it('links to Anthropic privacy policy', () => {
    const wrapper = mount(AiConsentDialog)
    const privacyLink = wrapper.find('a[href*="anthropic.com/legal/privacy"]')
    expect(privacyLink.exists()).toBe(true)
  })

  it('emits declined when Decline is clicked', async () => {
    const wrapper = mount(AiConsentDialog)
    await wrapper.find('.btn--secondary').trigger('click')
    expect(wrapper.emitted('declined')).toBeTruthy()
  })

  it('calls giveAiConsent and emits accepted when Accept is clicked', async () => {
    const api = (await import('@/services/api')).default as unknown as {
      post: ReturnType<typeof vi.fn>
    }
    api.post.mockResolvedValueOnce({})

    const wrapper = mount(AiConsentDialog)
    const profileStore = useProfileStore()
    profileStore.profile = {
      id: 1,
      userId: 10,
      displayName: null,
      avatarUrl: null,
      timezone: null,
      notificationPrefs: null,
      telegramChatId: null,
      whatsappNumber: null,
      tutorialCompleted: false,
      onboardingCompleted: true,
      surveyCompleted: false,
      isPatient: true,
      isTherapist: false,
      aiConsentGiven: false,
    }

    await wrapper.find('.btn--primary').trigger('click')
    await wrapper.vm.$nextTick()

    expect(api.post).toHaveBeenCalledWith('/ai/consent')
    expect(wrapper.emitted('accepted')).toBeTruthy()
  })
})
