<template>
  <dialog
    class="consent-dialog"
    open
    aria-labelledby="consent-title"
    aria-describedby="consent-description"
    @cancel.prevent="onDecline"
  >
    <div class="consent-dialog__content">
      <h2 id="consent-title">AI Coaching Consent</h2>
      <p id="consent-description">
        MindTrack uses the
        <a href="https://www.anthropic.com/claude" target="_blank" rel="noopener noreferrer"
          >Claude API</a
        >
        by Anthropic to power AI coaching. Before sending any messages, please review what data is
        shared.
      </p>
      <h3>Data shared with the Claude API</h3>
      <ul>
        <li>Your journal entries and mood logs</li>
        <li>Interview notes and session summaries</li>
        <li>Goals and milestone progress</li>
        <li>Activity and habit records</li>
      </ul>
      <p>
        Anthropic processes this data to generate coaching responses. By clicking
        <strong>Accept</strong>, you consent to this data being sent to the Claude API. You can
        review
        <a href="https://www.anthropic.com/legal/privacy" target="_blank" rel="noopener noreferrer"
          >Anthropic's Privacy Policy</a
        >
        for details on how your data is handled.
      </p>
      <div class="consent-dialog__actions">
        <button type="button" class="btn btn--secondary" @click="onDecline">Decline</button>
        <button type="button" class="btn btn--primary" :disabled="saving" @click="onAccept">
          {{ saving ? 'Saving...' : 'Accept' }}
        </button>
      </div>
    </div>
  </dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useProfileStore } from '@/stores/profile'

const emit = defineEmits<{
  (e: 'accepted'): void
  (e: 'declined'): void
}>()

const profileStore = useProfileStore()
const saving = ref(false)

async function onAccept() {
  saving.value = true
  try {
    await profileStore.giveAiConsent()
    emit('accepted')
  } finally {
    saving.value = false
  }
}

function onDecline() {
  emit('declined')
}
</script>

<style scoped>
.consent-dialog {
  border: none;
  background: white;
  border-radius: 8px;
  max-width: 540px;
  width: min(90vw, 540px);
  padding: 0;
  z-index: 1000;
}

.consent-dialog::backdrop {
  background: rgba(0, 0, 0, 0.5);
}

.consent-dialog__content {
  padding: 2rem;
}

.consent-dialog h2 {
  margin-top: 0;
}

.consent-dialog__actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
}

.btn {
  padding: 0.5rem 1.25rem;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 1rem;
}

.btn--primary {
  background: #4f46e5;
  color: white;
}

.btn--primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn--secondary {
  background: #e5e7eb;
  color: #374151;
}
</style>
