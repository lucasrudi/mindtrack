<script setup lang="ts">
import { ref } from 'vue'
import api from '@/services/api'

const selectedMood = ref<number | null>(null)
const notes = ref('')
const submitting = ref(false)
const submitted = ref(false)
const error = ref<string | null>(null)

const moodEmojis = [
  { value: 1, emoji: '😞', label: 'Very bad' },
  { value: 2, emoji: '😟', label: 'Bad' },
  { value: 3, emoji: '😕', label: 'Poor' },
  { value: 4, emoji: '😐', label: 'Below average' },
  { value: 5, emoji: '🙂', label: 'Okay' },
  { value: 6, emoji: '😊', label: 'Good' },
  { value: 7, emoji: '😄', label: 'Pretty good' },
  { value: 8, emoji: '😁', label: 'Great' },
  { value: 9, emoji: '🤩', label: 'Excellent' },
  { value: 10, emoji: '🥳', label: 'Amazing' },
]

async function submitMood() {
  if (!selectedMood.value) return
  submitting.value = true
  error.value = null
  try {
    await api.post('/mood', { moodRating: selectedMood.value, notes: notes.value || null })
    submitted.value = true
  } catch {
    error.value = 'Failed to save mood. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="mood-widget">
    <h2 class="widget-title">How are you feeling?</h2>
    <div v-if="submitted" class="submitted-state">
      <span class="submitted-emoji">{{
        moodEmojis.find((m) => m.value === selectedMood)?.emoji
      }}</span>
      <p class="submitted-text">Mood logged! Keep it up.</p>
    </div>
    <div v-else>
      <div v-if="error" class="error-msg">{{ error }}</div>
      <div class="emoji-grid">
        <button
          v-for="m in moodEmojis"
          :key="m.value"
          :class="['emoji-btn', { selected: selectedMood === m.value }]"
          :title="m.label"
          :aria-label="m.label"
          type="button"
          @click="selectedMood = m.value"
        >
          {{ m.emoji }}
        </button>
      </div>
      <textarea
        v-if="selectedMood"
        v-model="notes"
        class="notes-input"
        placeholder="Optional notes..."
        rows="2"
      />
      <button class="btn btn-primary" :disabled="!selectedMood || submitting" @click="submitMood">
        {{ submitting ? 'Saving...' : 'Log mood' }}
      </button>
    </div>
  </section>
</template>

<style scoped>
.mood-widget {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-5);
  margin-bottom: var(--space-6);
}
.widget-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0 0 var(--space-4) 0;
}
.emoji-grid {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  margin-bottom: var(--space-3);
}
.emoji-btn {
  font-size: 1.5rem;
  background: var(--color-gray-50);
  border: 2px solid transparent;
  border-radius: var(--border-radius);
  padding: var(--space-2);
  cursor: pointer;
  transition: all 0.15s;
}
.emoji-btn:hover {
  border-color: var(--color-primary-50);
  background: var(--color-gray-100);
}
.emoji-btn.selected {
  border-color: var(--color-primary);
  background: #ede9fe;
}
.notes-input {
  width: 100%;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-gray-300);
  border-radius: var(--border-radius);
  font-size: var(--font-size-sm);
  resize: vertical;
  margin-bottom: var(--space-3);
  box-sizing: border-box;
}
.btn {
  display: inline-flex;
  align-items: center;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--border-radius);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
  cursor: pointer;
  border: none;
}
.btn-primary {
  background: var(--color-primary);
  color: var(--color-white);
}
.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.submitted-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4) 0;
}
.submitted-emoji {
  font-size: 2rem;
}
.submitted-text {
  color: var(--color-gray-600);
  margin: 0;
  font-size: var(--font-size-sm);
}
.error-msg {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: var(--color-error);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--border-radius);
  margin-bottom: var(--space-3);
  font-size: var(--font-size-sm);
}
</style>
