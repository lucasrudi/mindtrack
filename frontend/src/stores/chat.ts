import { ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'
import { useProfileStore } from '@/stores/profile'

export interface ChatMessage {
  id: number | null
  role: 'USER' | 'ASSISTANT' | 'SYSTEM'
  content: string
  createdAt: string | null
}

export interface ConversationSummary {
  id: number
  channel: string
  startedAt: string
  endedAt: string | null
  messages: ChatMessage[]
}

export const useChatStore = defineStore('chat', () => {
  const conversations = ref<ConversationSummary[]>([])
  const currentConversationId = ref<number | null>(null)
  const messages = ref<ChatMessage[]>([])
  const loading = ref(false)
  const sending = ref(false)
  const error = ref<string | null>(null)
  const pendingMessage = ref<string | null>(null)

  async function fetchConversations() {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get('/ai/conversations')
      conversations.value = data
    } catch {
      error.value = 'Failed to load conversations'
      throw new Error('Failed to load conversations')
    } finally {
      loading.value = false
    }
  }

  async function fetchConversation(id: number) {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get(`/ai/conversations/${id}`)
      currentConversationId.value = data.id
      messages.value = data.messages
    } catch {
      error.value = 'Failed to load conversation'
      throw new Error('Failed to load conversation')
    } finally {
      loading.value = false
    }
  }

  async function sendMessage(message: string, conversationId?: number | null) {
    const profileStore = useProfileStore()
    if (!profileStore.profile?.aiConsentGiven) {
      pendingMessage.value = message
      error.value = 'CONSENT_REQUIRED'
      return
    }

    sending.value = true
    error.value = null

    // Optimistically add user message
    const userMessage: ChatMessage = {
      id: null,
      role: 'USER',
      content: message,
      createdAt: new Date().toISOString(),
    }
    messages.value.push(userMessage)

    try {
      const { data } = await api.post('/ai/chat', {
        message,
        conversationId: conversationId ?? currentConversationId.value,
        type: 'COACHING',
      })

      // Update conversation ID if this was a new conversation
      if (data.conversationId) {
        currentConversationId.value = data.conversationId
      }

      // Add assistant response
      const assistantMessage: ChatMessage = {
        id: data.messageId,
        role: 'ASSISTANT',
        content: data.content,
        createdAt: new Date().toISOString(),
      }
      messages.value.push(assistantMessage)

      // Refresh conversations list to show new/updated conversation
      await fetchConversations()
    } catch {
      // Remove optimistic message on error
      messages.value.pop()
      error.value = 'Failed to send message'
      throw new Error('Failed to send message')
    } finally {
      sending.value = false
    }
  }

  function startNewConversation() {
    currentConversationId.value = null
    messages.value = []
    error.value = null
  }

  function clearError() {
    error.value = null
  }

  return {
    conversations,
    currentConversationId,
    messages,
    loading,
    sending,
    error,
    pendingMessage,
    fetchConversations,
    fetchConversation,
    sendMessage,
    startNewConversation,
    clearError,
  }
})
