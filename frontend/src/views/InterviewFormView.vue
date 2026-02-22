<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useInterviewsStore, type InterviewForm } from '@/stores/interviews'
import AudioSection from '@/components/interview/AudioSection.vue'

const route = useRoute()
const router = useRouter()
const store = useInterviewsStore()

const isEditing = computed(() => route.name === 'interview-edit')
const interviewId = computed(() => (isEditing.value ? Number(route.params.id) : null))
const submitting = ref(false)
const topicInput = ref('')

const form = reactive<InterviewForm>({
  interviewDate: new Date().toISOString().split('T')[0],
  moodBefore: null,
  moodAfter: null,
  topics: [],
  medicationChanges: '',
  recommendations: '',
  notes: '',
})

onMounted(async () => {
  if (isEditing.value && interviewId.value) {
    const interview = await store.fetchInterview(interviewId.value)
    if (interview) {
      form.interviewDate = interview.interviewDate
      form.moodBefore = interview.moodBefore
      form.moodAfter = interview.moodAfter
      form.topics = [...interview.topics]
      form.medicationChanges = interview.medicationChanges ?? ''
      form.recommendations = interview.recommendations ?? ''
      form.notes = interview.notes ?? ''
    }
  }
})

function addTopic() {
  const topic = topicInput.value.trim()
  if (topic && !form.topics.includes(topic)) {
    form.topics.push(topic)
  }
  topicInput.value = ''
}

function removeTopic(index: number) {
  form.topics.splice(index, 1)
}

function handleTopicKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter') {
    event.preventDefault()
    addTopic()
  }
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEditing.value && interviewId.value) {
      await store.updateInterview(interviewId.value, form)
      router.push({ name: 'interview-detail', params: { id: interviewId.value } })
    } else {
      const created = await store.createInterview(form)
      router.push({ name: 'interview-detail', params: { id: created.id } })
    }
  } catch {
    // error is captured in the store
  } finally {
    submitting.value = false
  }
}

function goBack() {
  if (isEditing.value && interviewId.value) {
    router.push({ name: 'interview-detail', params: { id: interviewId.value } })
  } else {
    router.push({ name: 'interviews' })
  }
}
</script>

<template>
  <div class="form-view">
    <div class="form-header">
      <button class="btn-back" @click="goBack">← Back</button>
      <h1>{{ isEditing ? 'Edit Interview' : 'New Interview' }}</h1>
    </div>

    <div v-if="store.error" class="error-banner" role="alert">
      {{ store.error }}
      <button class="error-dismiss" @click="store.clearError()">×</button>
    </div>

    <form class="interview-form" @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="interview-date">Interview date *</label>
        <input
          id="interview-date"
          v-model="form.interviewDate"
          type="date"
          required
          class="form-input"
        />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label for="mood-before">Mood before (1-10)</label>
          <input
            id="mood-before"
            v-model.number="form.moodBefore"
            type="number"
            min="1"
            max="10"
            class="form-input"
            placeholder="1-10"
          />
        </div>
        <div class="form-group">
          <label for="mood-after">Mood after (1-10)</label>
          <input
            id="mood-after"
            v-model.number="form.moodAfter"
            type="number"
            min="1"
            max="10"
            class="form-input"
            placeholder="1-10"
          />
        </div>
      </div>

      <div class="form-group">
        <label for="topic-input">Topics</label>
        <div class="topic-input-row">
          <input
            id="topic-input"
            v-model="topicInput"
            type="text"
            class="form-input"
            placeholder="Add a topic and press Enter"
            @keydown="handleTopicKeydown"
          />
          <button type="button" class="btn btn-secondary" @click="addTopic">Add</button>
        </div>
        <div v-if="form.topics.length" class="topic-chips">
          <span v-for="(topic, index) in form.topics" :key="topic" class="topic-chip">
            {{ topic }}
            <button
              type="button"
              class="chip-remove"
              :aria-label="`Remove ${topic}`"
              @click="removeTopic(index)"
            >
              ×
            </button>
          </span>
        </div>
      </div>

      <div class="form-group">
        <label for="medication-changes">Medication changes</label>
        <textarea
          id="medication-changes"
          v-model="form.medicationChanges"
          class="form-textarea"
          rows="3"
          placeholder="Any changes to medications discussed..."
        />
      </div>

      <div class="form-group">
        <label for="recommendations">Recommendations</label>
        <textarea
          id="recommendations"
          v-model="form.recommendations"
          class="form-textarea"
          rows="3"
          placeholder="Recommendations from your session..."
        />
      </div>

      <div class="form-group">
        <label for="notes">Notes</label>
        <textarea
          id="notes"
          v-model="form.notes"
          class="form-textarea"
          rows="5"
          placeholder="Additional notes about the session..."
        />
      </div>

      <!-- Audio section — only shown when editing an existing interview -->
      <AudioSection
        v-if="isEditing && interviewId && store.currentInterview"
        :interview-id="interviewId"
        :has-audio="store.currentInterview.hasAudio"
        @audio-changed="store.fetchInterview(interviewId!)"
      />

      <div class="form-actions">
        <button type="button" class="btn btn-secondary" @click="goBack">Cancel</button>
        <button type="submit" class="btn btn-primary" :disabled="submitting">
          {{ submitting ? 'Saving...' : isEditing ? 'Update Interview' : 'Save Interview' }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.form-view {
  max-width: 700px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

.form-header {
  margin-bottom: var(--space-6);
}

.btn-back {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  padding: var(--space-1) 0;
  margin-bottom: var(--space-3);
  display: inline-block;
}

.btn-back:hover {
  color: var(--color-primary-dark);
}

.form-header h1 {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
}

.error-banner {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: var(--color-error);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--border-radius);
  margin-bottom: var(--space-6);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--font-size-sm);
}

.error-dismiss {
  background: none;
  border: none;
  color: var(--color-error);
  font-size: var(--font-size-lg);
  cursor: pointer;
  padding: 0 var(--space-1);
}

.interview-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-group label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-700);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}

.form-input,
.form-textarea {
  padding: var(--space-3);
  border: 1px solid var(--color-gray-300);
  border-radius: var(--border-radius);
  font-size: var(--font-size-base);
  font-family: var(--font-sans);
  color: var(--color-gray-900);
  background: var(--color-white);
  transition: border-color var(--transition-fast);
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-50);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.topic-input-row {
  display: flex;
  gap: var(--space-2);
}

.topic-input-row .form-input {
  flex: 1;
}

.topic-chips {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-top: var(--space-1);
}

.topic-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-size-sm);
  background: var(--color-primary-50);
  color: var(--color-primary-dark);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
}

.chip-remove {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: var(--font-size-base);
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.chip-remove:hover {
  color: var(--color-error);
}

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

.btn-primary:hover:not(:disabled) {
  background: var(--color-primary-dark);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: var(--color-gray-100);
  color: var(--color-gray-700);
}

.btn-secondary:hover {
  background: var(--color-gray-200);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-gray-100);
}

@media (max-width: 640px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
