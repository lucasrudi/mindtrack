<script setup lang="ts">
import { onMounted, ref, nextTick, watch } from 'vue'
import { useChatStore } from '@/stores/chat'

const store = useChatStore()
const messageInput = ref('')
const messagesContainer = ref<HTMLElement | null>(null)

onMounted(async () => {
  try {
    await store.fetchConversations()
    if (store.conversations.length > 0) {
      await selectConversation(store.conversations[0].id)
    }
  } catch {
    // Error handled by store
  }
})

watch(
  () => store.messages.length,
  () => scrollToBottom(),
)

async function selectConversation(id: number) {
  try {
    await store.fetchConversation(id)
  } catch {
    // Error handled by store
  }
}

function startNew() {
  store.startNewConversation()
  messageInput.value = ''
}

async function handleSend() {
  const text = messageInput.value.trim()
  if (!text || store.sending) return

  messageInput.value = ''
  try {
    await store.sendMessage(text)
  } catch {
    // Error handled by store
  }
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSend()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

function formatDate(dateStr: string | null): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
}

function formatTime(dateStr: string | null): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
}

function conversationPreview(conv: { messages: { content: string }[] }): string {
  if (!conv.messages.length) return 'Empty conversation'
  const first = conv.messages[0].content
  return first.length > 50 ? first.substring(0, 50) + '...' : first
}

function formatContent(content: string): string {
  return content.replaceAll(/\n/g, '<br>')
}
</script>

<template>
  <div class="chat-view">
    <header class="page-header">
      <div>
        <h1>AI Coach</h1>
        <p class="subtitle">Get personalized mental health coaching powered by AI</p>
      </div>
    </header>

    <div v-if="store.error" class="error-message">
      <p>{{ store.error }}</p>
      <button class="btn btn-secondary" @click="store.clearError()">Dismiss</button>
    </div>

    <div class="chat-layout">
      <!-- Sidebar: Conversation List -->
      <aside class="sidebar">
        <button class="btn btn-primary new-chat-btn" @click="startNew">+ New Chat</button>

        <div v-if="store.loading && !store.conversations.length" class="sidebar-loading">
          Loading...
        </div>

        <div class="conversation-list">
          <div
            v-for="conv in store.conversations"
            :key="conv.id"
            :class="['conversation-item', { active: conv.id === store.currentConversationId }]"
            @click="selectConversation(conv.id)"
          >
            <div class="conversation-date">{{ formatDate(conv.startedAt) }}</div>
            <div class="conversation-preview">{{ conversationPreview(conv) }}</div>
          </div>
        </div>
      </aside>

      <!-- Main: Chat Thread -->
      <main class="chat-main">
        <!-- Empty State -->
        <div v-if="!store.currentConversationId && store.messages.length === 0" class="empty-state">
          <div class="empty-icon">AI</div>
          <h2>Start a conversation</h2>
          <p>
            Ask me anything about your mental health journey. I have context about your recent
            activities, mood, goals, and interviews.
          </p>
        </div>

        <!-- Messages -->
        <div v-else ref="messagesContainer" class="messages-container">
          <div
            v-for="(msg, index) in store.messages"
            :key="msg.id ?? `temp-${index}`"
            :class="['message', `message--${msg.role.toLowerCase()}`]"
          >
            <div class="message-avatar">
              {{ msg.role === 'USER' ? 'You' : 'AI' }}
            </div>
            <div class="message-body">
              <!-- eslint-disable vue/no-v-html -->
              <div
                v-if="msg.role === 'ASSISTANT'"
                class="message-content"
                v-html="formatContent(msg.content)"
              />
              <!-- eslint-enable vue/no-v-html -->
              <div v-else class="message-content">{{ msg.content }}</div>
              <div v-if="msg.createdAt" class="message-time">
                {{ formatTime(msg.createdAt) }}
              </div>
            </div>
          </div>

          <div v-if="store.sending" class="message message--assistant">
            <div class="message-avatar">AI</div>
            <div class="message-body">
              <div class="message-content thinking">Thinking...</div>
            </div>
          </div>
        </div>

        <!-- Input Area -->
        <div class="input-area">
          <textarea
            v-model="messageInput"
            class="message-input"
            placeholder="Type your message... (Enter to send, Shift+Enter for newline)"
            rows="2"
            :disabled="store.sending"
            @keydown="handleKeydown"
          />
          <button
            class="btn btn-primary send-btn"
            :disabled="!messageInput.trim() || store.sending"
            @click="handleSend"
          >
            {{ store.sending ? 'Sending...' : 'Send' }}
          </button>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.chat-view {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--navbar-height));
}

.page-header {
  margin-bottom: var(--space-4);
  flex-shrink: 0;
}

.page-header h1 {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
  margin: 0;
}

.subtitle {
  color: var(--color-gray-500);
  margin-top: var(--space-1);
}

.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: var(--color-error);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--border-radius);
  margin-bottom: var(--space-4);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

/* Layout */
.chat-layout {
  display: flex;
  gap: var(--space-4);
  flex: 1;
  min-height: 0;
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  background: var(--color-white);
}

/* Sidebar */
.sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--color-gray-200);
  background: var(--color-gray-50);
}

.new-chat-btn {
  margin: var(--space-3);
  flex-shrink: 0;
}

.sidebar-loading {
  padding: var(--space-4);
  text-align: center;
  color: var(--color-gray-500);
  font-size: var(--font-size-sm);
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  padding: var(--space-3) var(--space-4);
  cursor: pointer;
  border-bottom: 1px solid var(--color-gray-100);
  transition: background-color var(--transition-fast);
}

.conversation-item:hover {
  background-color: var(--color-gray-100);
}

.conversation-item.active {
  background-color: var(--color-primary-50);
  border-left: 3px solid var(--color-primary);
}

.conversation-date {
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
  margin-bottom: var(--space-1);
}

.conversation-preview {
  font-size: var(--font-size-sm);
  color: var(--color-gray-700);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Chat Main */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* Empty State */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: var(--space-8);
  color: var(--color-gray-500);
}

.empty-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
  background: var(--color-primary-50);
  border-radius: var(--border-radius-full);
  margin-bottom: var(--space-4);
}

.empty-state h2 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0 0 var(--space-2);
}

.empty-state p {
  max-width: 400px;
  font-size: var(--font-size-sm);
  line-height: 1.6;
}

/* Messages */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4) var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.message {
  display: flex;
  gap: var(--space-3);
  max-width: 80%;
}

.message--user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message--assistant {
  align-self: flex-start;
}

.message-avatar {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  border-radius: var(--border-radius-full);
  flex-shrink: 0;
}

.message--user .message-avatar {
  background: var(--color-primary);
  color: var(--color-white);
}

.message--assistant .message-avatar {
  background: var(--color-gray-200);
  color: var(--color-gray-700);
}

.message-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.message-content {
  padding: var(--space-3) var(--space-4);
  border-radius: var(--border-radius-lg);
  font-size: var(--font-size-sm);
  line-height: 1.6;
  word-wrap: break-word;
}

.message--user .message-content {
  background: var(--color-primary);
  color: var(--color-white);
  border-bottom-right-radius: var(--border-radius-sm);
}

.message--assistant .message-content {
  background: var(--color-gray-100);
  color: var(--color-gray-900);
  border-bottom-left-radius: var(--border-radius-sm);
}

.message-time {
  font-size: var(--font-size-xs);
  color: var(--color-gray-400);
}

.message--user .message-time {
  text-align: right;
}

.thinking {
  color: var(--color-gray-500);
  font-style: italic;
}

/* Input Area */
.input-area {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--color-gray-200);
  background: var(--color-white);
  flex-shrink: 0;
}

.message-input {
  flex: 1;
  padding: var(--space-3);
  border: 1px solid var(--color-gray-300);
  border-radius: var(--border-radius);
  font-size: var(--font-size-sm);
  font-family: inherit;
  resize: none;
  color: var(--color-gray-900);
  transition: border-color var(--transition-fast);
}

.message-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-50);
}

.message-input::placeholder {
  color: var(--color-gray-400);
}

.message-input:disabled {
  background: var(--color-gray-50);
  cursor: not-allowed;
}

.send-btn {
  align-self: flex-end;
  flex-shrink: 0;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-3) var(--space-5);
  border-radius: var(--border-radius);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
  cursor: pointer;
  border: none;
  transition: all var(--transition-fast);
}

.btn-primary {
  background: var(--color-primary);
  color: var(--color-white);
}

.btn-primary:hover {
  background: var(--color-primary-dark);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: var(--color-gray-100);
  color: var(--color-gray-700);
}

.btn-secondary:hover {
  background: var(--color-gray-200);
}
</style>
