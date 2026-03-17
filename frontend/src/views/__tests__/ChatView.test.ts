import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ChatView from '../ChatView.vue'
import { useProfileStore } from '@/stores/profile'

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
}))

const mockConversations = [
  {
    id: 1,
    channel: 'WEB',
    startedAt: '2025-01-15T10:00:00',
    endedAt: null,
    messages: [
      { id: 1, role: 'USER', content: 'Hello AI coach', createdAt: '2025-01-15T10:00:00' },
      {
        id: 2,
        role: 'ASSISTANT',
        content: 'Hello! How can I help you today?',
        createdAt: '2025-01-15T10:00:01',
      },
    ],
  },
  {
    id: 2,
    channel: 'WEB',
    startedAt: '2025-01-14T09:00:00',
    endedAt: null,
    messages: [{ id: 3, role: 'USER', content: 'Earlier chat', createdAt: '2025-01-14T09:00:00' }],
  },
]

const mockConversationDetail = mockConversations[0]

const mockChatResponse = {
  conversationId: 1,
  messageId: 10,
  content: 'Here is my response.',
  cached: false,
  tokensUsed: 50,
}

const mockGet = vi.fn().mockResolvedValue({ data: [] })
const mockPost = vi.fn().mockResolvedValue({ data: mockChatResponse })
vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('ChatView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockGet.mockReset().mockResolvedValue({ data: [] })
    mockPost.mockReset().mockResolvedValue({ data: mockChatResponse })
    const profileStore = useProfileStore()
    profileStore.profile = {
      id: 1,
      userId: 1,
      displayName: null,
      avatarUrl: null,
      timezone: null,
      notificationPrefs: null,
      telegramChatId: null,
      whatsappNumber: null,
      tutorialCompleted: false,
      onboardingCompleted: true,
      surveyCompleted: true,
      isPatient: true,
      isTherapist: false,
      aiConsentGiven: true,
    }
  })

  it('renders page header', () => {
    const wrapper = mount(ChatView)
    expect(wrapper.find('h1').text()).toBe('AI Coach')
    expect(wrapper.find('.subtitle').text()).toContain('coaching')
  })

  it('shows empty state when no conversations', async () => {
    mockGet.mockResolvedValueOnce({ data: [] })
    const wrapper = mount(ChatView)
    await flushPromises()

    expect(wrapper.find('.empty-state').exists()).toBe(true)
    expect(wrapper.find('.empty-state h2').text()).toContain('Start a conversation')
  })

  it('renders conversation list in sidebar', async () => {
    mockGet
      .mockResolvedValueOnce({ data: mockConversations })
      .mockResolvedValueOnce({ data: mockConversationDetail })
    const wrapper = mount(ChatView)
    await flushPromises()

    const items = wrapper.findAll('.conversation-item')
    expect(items).toHaveLength(2)
    expect(items[0].find('.conversation-preview').text()).toContain('Hello AI coach')
  })

  it('renders messages when conversation is loaded', async () => {
    mockGet
      .mockResolvedValueOnce({ data: mockConversations })
      .mockResolvedValueOnce({ data: mockConversationDetail })
    const wrapper = mount(ChatView)
    await flushPromises()

    const messages = wrapper.findAll('.message')
    expect(messages).toHaveLength(2)
    expect(messages[0].classes()).toContain('message--user')
    expect(messages[1].classes()).toContain('message--assistant')
  })

  it('shows New Chat button in sidebar', () => {
    const wrapper = mount(ChatView)
    expect(wrapper.find('.new-chat-btn').exists()).toBe(true)
    expect(wrapper.find('.new-chat-btn').text()).toContain('New Chat')
  })

  it('clears conversation on New Chat click', async () => {
    mockGet
      .mockResolvedValueOnce({ data: mockConversations })
      .mockResolvedValueOnce({ data: mockConversationDetail })
    const wrapper = mount(ChatView)
    await flushPromises()

    expect(wrapper.findAll('.message')).toHaveLength(2)

    await wrapper.find('.new-chat-btn').trigger('click')
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.empty-state').exists()).toBe(true)
  })

  it('has input area with textarea and send button', () => {
    const wrapper = mount(ChatView)
    expect(wrapper.find('.message-input').exists()).toBe(true)
    expect(wrapper.find('.send-btn').exists()).toBe(true)
  })

  it('disables send button when input is empty', () => {
    const wrapper = mount(ChatView)
    const sendBtn = wrapper.find('.send-btn')
    expect((sendBtn.element as HTMLButtonElement).disabled).toBe(true)
  })

  it('shows error message on fetch failure', async () => {
    mockGet.mockRejectedValueOnce(new Error('Network error'))
    const wrapper = mount(ChatView)
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.error-message').exists()).toBe(true)
    expect(wrapper.find('.error-message').text()).toContain('Failed to load conversations')
  })

  it('sends a message from the textarea', async () => {
    mockGet.mockResolvedValueOnce({ data: [] }).mockResolvedValueOnce({ data: [] })
    const wrapper = mount(ChatView)
    await flushPromises()

    await wrapper.find('.message-input').setValue('How am I doing?')
    await wrapper.find('.send-btn').trigger('click')
    await flushPromises()

    expect(mockPost).toHaveBeenCalledWith('/ai/chat', {
      message: 'How am I doing?',
      conversationId: null,
      type: 'COACHING',
    })
    expect(wrapper.text()).toContain('Here is my response.')
  })

  it('sends on Enter but not on Shift+Enter', async () => {
    mockGet.mockResolvedValueOnce({ data: [] }).mockResolvedValueOnce({ data: [] })
    const wrapper = mount(ChatView)
    await flushPromises()

    const input = wrapper.find('.message-input')
    await input.setValue('Check in')
    await input.trigger('keydown', {
      key: 'Enter',
      shiftKey: true,
      preventDefault: vi.fn(),
    })
    expect(mockPost).not.toHaveBeenCalled()

    await input.trigger('keydown', {
      key: 'Enter',
      shiftKey: false,
      preventDefault: vi.fn(),
    })
    await flushPromises()

    expect(mockPost).toHaveBeenCalledTimes(1)
  })

  it('loads another conversation when a sidebar item is clicked', async () => {
    mockGet
      .mockResolvedValueOnce({ data: mockConversations })
      .mockResolvedValueOnce({ data: mockConversationDetail })
      .mockResolvedValueOnce({ data: mockConversations[1] })
    const wrapper = mount(ChatView)
    await flushPromises()

    await wrapper.findAll('.conversation-item')[1].trigger('click')
    await flushPromises()

    const messages = wrapper.findAll('.message')
    expect(messages).toHaveLength(1)
    expect(messages[0].text()).toContain('Earlier chat')
  })

  it('shows the consent dialog when AI consent is missing', async () => {
    const profileStore = useProfileStore()
    profileStore.profile = { ...profileStore.profile!, aiConsentGiven: false }
    mockGet.mockResolvedValueOnce({ data: [] })
    const wrapper = mount(ChatView)
    await flushPromises()

    await wrapper.find('.message-input').setValue('Need support')
    await wrapper.find('.send-btn').trigger('click')
    await flushPromises()

    expect(wrapper.find('dialog').exists()).toBe(true)
  })

  it('retries the pending message after consent is accepted', async () => {
    const profileStore = useProfileStore()
    profileStore.profile = { ...profileStore.profile!, aiConsentGiven: false }
    mockGet.mockResolvedValueOnce({ data: [] }).mockResolvedValueOnce({ data: [] })
    const wrapper = mount(ChatView, {
      global: {
        stubs: {
          AiConsentDialog: {
            template: '<button class="accept-consent" @click="$emit(\'accepted\')">Accept</button>',
          },
        },
      },
    })
    await flushPromises()

    await wrapper.find('.message-input').setValue('Need support')
    await wrapper.find('.send-btn').trigger('click')
    await flushPromises()

    profileStore.profile = { ...profileStore.profile!, aiConsentGiven: true }
    await wrapper.find('.accept-consent').trigger('click')
    await flushPromises()

    expect(mockPost).toHaveBeenCalledWith('/ai/chat', {
      message: 'Need support',
      conversationId: null,
      type: 'COACHING',
    })
  })

  it('clears the consent error when consent is declined', async () => {
    const profileStore = useProfileStore()
    profileStore.profile = { ...profileStore.profile!, aiConsentGiven: false }
    mockGet.mockResolvedValueOnce({ data: [] })
    const wrapper = mount(ChatView, {
      global: {
        stubs: {
          AiConsentDialog: {
            template:
              '<button class="decline-consent" @click="$emit(\'declined\')">Decline</button>',
          },
        },
      },
    })
    await flushPromises()

    await wrapper.find('.message-input').setValue('Need support')
    await wrapper.find('.send-btn').trigger('click')
    await flushPromises()

    await wrapper.find('.decline-consent').trigger('click')
    await flushPromises()

    expect(wrapper.find('.decline-consent').exists()).toBe(false)
  })

  it('dismisses non-consent errors', async () => {
    mockGet.mockRejectedValueOnce(new Error('Network error'))
    const wrapper = mount(ChatView)
    await flushPromises()

    await wrapper.find('.error-message .btn').trigger('click')

    expect(wrapper.find('.error-message').exists()).toBe(false)
  })
})
