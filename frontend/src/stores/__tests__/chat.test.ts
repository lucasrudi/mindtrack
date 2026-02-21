import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useChatStore } from '../chat'

const mockConversations = [
  {
    id: 1,
    channel: 'WEB',
    startedAt: '2025-01-15T10:00:00',
    endedAt: null,
    messages: [
      { id: 1, role: 'USER', content: 'Hello', createdAt: '2025-01-15T10:00:00' },
      {
        id: 2,
        role: 'ASSISTANT',
        content: 'Hi! How are you?',
        createdAt: '2025-01-15T10:00:01',
      },
    ],
  },
  {
    id: 2,
    channel: 'WEB',
    startedAt: '2025-01-14T09:00:00',
    endedAt: null,
    messages: [{ id: 3, role: 'USER', content: 'Previous chat', createdAt: '2025-01-14T09:00:00' }],
  },
]

const mockChatResponse = {
  conversationId: 3,
  messageId: 10,
  content: 'I understand your concern.',
  cached: false,
  tokensUsed: 50,
}

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('useChatStore', () => {
  let api: {
    get: ReturnType<typeof vi.fn>
    post: ReturnType<typeof vi.fn>
  }

  beforeEach(async () => {
    setActivePinia(createPinia())
    const module = await import('@/services/api')
    api = module.default as unknown as typeof api
    vi.clearAllMocks()
  })

  describe('fetchConversations', () => {
    it('fetches conversation list', async () => {
      api.get.mockResolvedValueOnce({ data: mockConversations })
      const store = useChatStore()

      await store.fetchConversations()

      expect(api.get).toHaveBeenCalledWith('/ai/conversations')
      expect(store.conversations).toEqual(mockConversations)
      expect(store.loading).toBe(false)
    })

    it('sets loading state during fetch', async () => {
      let resolvePromise: (value: unknown) => void
      const promise = new Promise((resolve) => {
        resolvePromise = resolve
      })
      api.get.mockReturnValueOnce(promise)
      const store = useChatStore()

      const fetchPromise = store.fetchConversations()
      expect(store.loading).toBe(true)

      resolvePromise!({ data: mockConversations })
      await fetchPromise
      expect(store.loading).toBe(false)
    })

    it('sets error on failure', async () => {
      api.get.mockRejectedValueOnce(new Error('Network error'))
      const store = useChatStore()

      await expect(store.fetchConversations()).rejects.toThrow()
      expect(store.error).toBe('Failed to load conversations')
      expect(store.loading).toBe(false)
    })
  })

  describe('fetchConversation', () => {
    it('fetches a single conversation and sets messages', async () => {
      api.get.mockResolvedValueOnce({ data: mockConversations[0] })
      const store = useChatStore()

      await store.fetchConversation(1)

      expect(api.get).toHaveBeenCalledWith('/ai/conversations/1')
      expect(store.currentConversationId).toBe(1)
      expect(store.messages).toEqual(mockConversations[0].messages)
      expect(store.loading).toBe(false)
    })

    it('sets error on failure', async () => {
      api.get.mockRejectedValueOnce(new Error('Not found'))
      const store = useChatStore()

      await expect(store.fetchConversation(999)).rejects.toThrow()
      expect(store.error).toBe('Failed to load conversation')
      expect(store.loading).toBe(false)
    })
  })

  describe('sendMessage', () => {
    it('sends message and adds response', async () => {
      api.post.mockResolvedValueOnce({ data: mockChatResponse })
      api.get.mockResolvedValueOnce({ data: mockConversations })
      const store = useChatStore()

      await store.sendMessage('How can I manage anxiety?')

      expect(api.post).toHaveBeenCalledWith('/ai/chat', {
        message: 'How can I manage anxiety?',
        conversationId: null,
        type: 'COACHING',
      })
      expect(store.currentConversationId).toBe(3)
      expect(store.messages).toHaveLength(2)
      expect(store.messages[0].role).toBe('USER')
      expect(store.messages[0].content).toBe('How can I manage anxiety?')
      expect(store.messages[1].role).toBe('ASSISTANT')
      expect(store.messages[1].content).toBe('I understand your concern.')
      expect(store.sending).toBe(false)
    })

    it('uses existing conversation ID', async () => {
      api.post.mockResolvedValueOnce({ data: { ...mockChatResponse, conversationId: 1 } })
      api.get.mockResolvedValueOnce({ data: mockConversations })
      const store = useChatStore()
      store.currentConversationId = 1

      await store.sendMessage('Follow up question')

      expect(api.post).toHaveBeenCalledWith('/ai/chat', {
        message: 'Follow up question',
        conversationId: 1,
        type: 'COACHING',
      })
    })

    it('removes optimistic message on error', async () => {
      api.post.mockRejectedValueOnce(new Error('API error'))
      const store = useChatStore()

      await expect(store.sendMessage('Test')).rejects.toThrow()
      expect(store.messages).toHaveLength(0)
      expect(store.error).toBe('Failed to send message')
      expect(store.sending).toBe(false)
    })
  })

  describe('startNewConversation', () => {
    it('clears conversation state', () => {
      const store = useChatStore()
      store.currentConversationId = 1
      store.messages = [{ id: 1, role: 'USER', content: 'Test', createdAt: null }]

      store.startNewConversation()

      expect(store.currentConversationId).toBeNull()
      expect(store.messages).toHaveLength(0)
      expect(store.error).toBeNull()
    })
  })

  describe('clearError', () => {
    it('clears the error state', () => {
      const store = useChatStore()
      store.error = 'Some error'

      store.clearError()

      expect(store.error).toBeNull()
    })
  })
})
