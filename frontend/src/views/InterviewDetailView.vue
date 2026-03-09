<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useInterviewsStore } from '@/stores/interviews'

const route = useRoute()
const router = useRouter()
const store = useInterviewsStore()
const showDeleteConfirm = ref(false)

const interviewId = Number(route.params.id)

onMounted(() => {
  store.fetchInterview(interviewId)
})

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

function formatDateTime(dateStr: string) {
  return new Date(dateStr).toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function moodLabel(value: number | null) {
  if (value === null) return 'Not recorded'
  return `${value}/10`
}

function navigateToEdit() {
  router.push({ name: 'interview-edit', params: { id: interviewId } })
}

async function confirmDelete() {
  await store.deleteInterview(interviewId)
  router.push({ name: 'interviews' })
}

function goBack() {
  router.push({ name: 'interviews' })
}
</script>

<template>
  <div class="detail-view">
    <div class="detail-header">
      <button class="btn-back" @click="goBack">← Back to interviews</button>
      <div v-if="store.currentInterview" class="header-actions">
        <button class="btn btn-secondary" @click="navigateToEdit">Edit</button>
        <button class="btn btn-danger" @click="showDeleteConfirm = true">Delete</button>
      </div>
    </div>

    <div v-if="store.loading" class="loading">
      <p>Loading interview...</p>
    </div>

    <div v-else-if="store.error" class="error-message">
      <p>{{ store.error }}</p>
      <button class="btn btn-secondary" @click="store.fetchInterview(interviewId)">Retry</button>
    </div>

    <article v-else-if="store.currentInterview" class="detail-content">
      <h1>{{ formatDate(store.currentInterview.interviewDate) }}</h1>

      <section class="mood-section">
        <h2>Mood</h2>
        <div class="mood-comparison">
          <div class="mood-block">
            <span class="mood-label">Before session</span>
            <span class="mood-number">{{ moodLabel(store.currentInterview.moodBefore) }}</span>
          </div>
          <span class="mood-separator">→</span>
          <div class="mood-block">
            <span class="mood-label">After session</span>
            <span class="mood-number">{{ moodLabel(store.currentInterview.moodAfter) }}</span>
          </div>
        </div>
      </section>

      <section v-if="store.currentInterview.topics.length" class="topics-section">
        <h2>Topics discussed</h2>
        <div class="topics-list">
          <span v-for="topic in store.currentInterview.topics" :key="topic" class="topic-chip">
            {{ topic }}
          </span>
        </div>
      </section>

      <section v-if="store.currentInterview.medicationChanges" class="text-section">
        <h2>Medication changes</h2>
        <p>{{ store.currentInterview.medicationChanges }}</p>
      </section>

      <section v-if="store.currentInterview.recommendations" class="text-section">
        <h2>Recommendations</h2>
        <p>{{ store.currentInterview.recommendations }}</p>
      </section>

      <section v-if="store.currentInterview.notes" class="text-section">
        <h2>Notes</h2>
        <p>{{ store.currentInterview.notes }}</p>
      </section>

      <footer class="detail-footer">
        <span>Created {{ formatDateTime(store.currentInterview.createdAt) }}</span>
        <span v-if="store.currentInterview.updatedAt !== store.currentInterview.createdAt">
          · Updated {{ formatDateTime(store.currentInterview.updatedAt) }}
        </span>
      </footer>
    </article>

    <div v-if="showDeleteConfirm" class="modal-overlay" @click.self="showDeleteConfirm = false">
      <dialog class="modal" open aria-labelledby="delete-title">
        <h3 id="delete-title">Delete interview?</h3>
        <p>This action cannot be undone. The interview record will be permanently removed.</p>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="showDeleteConfirm = false">Cancel</button>
          <button class="btn btn-danger" @click="confirmDelete">Delete</button>
        </div>
      </dialog>
    </div>
  </div>
</template>

<style scoped>
.detail-view {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
}

.btn-back:hover {
  color: var(--color-primary-dark);
}

.header-actions {
  display: flex;
  gap: var(--space-2);
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
  transition: all var(--transition-fast);
}

.btn-secondary {
  background: var(--color-gray-100);
  color: var(--color-gray-700);
}

.btn-secondary:hover {
  background: var(--color-gray-200);
}

.btn-danger {
  background: var(--color-error);
  color: var(--color-white);
}

.btn-danger:hover {
  opacity: 0.9;
}

.loading,
.error-message {
  text-align: center;
  padding: var(--space-16);
}

.error-message {
  color: var(--color-error);
}

.error-message .btn {
  margin-top: var(--space-4);
}

.detail-content h1 {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
  margin-bottom: var(--space-8);
}

.detail-content h2 {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-500);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: var(--space-3);
}

.mood-section,
.topics-section,
.text-section {
  margin-bottom: var(--space-8);
  padding-bottom: var(--space-6);
  border-bottom: 1px solid var(--color-gray-100);
}

.mood-comparison {
  display: flex;
  align-items: center;
  gap: var(--space-6);
}

.mood-block {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.mood-label {
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
}

.mood-number {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
}

.mood-separator {
  font-size: var(--font-size-2xl);
  color: var(--color-gray-300);
}

.topics-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.topic-chip {
  font-size: var(--font-size-sm);
  background: var(--color-primary-50);
  color: var(--color-primary-dark);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
}

.text-section p {
  color: var(--color-gray-700);
  line-height: var(--line-height-relaxed);
  white-space: pre-wrap;
}

.detail-footer {
  margin-top: var(--space-8);
  font-size: var(--font-size-xs);
  color: var(--color-gray-400);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgb(0 0 0 / 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: var(--color-white);
  border-radius: var(--border-radius-lg);
  padding: var(--space-6);
  max-width: 400px;
  width: 90%;
  box-shadow: var(--shadow-xl);
}

.modal h3 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin-bottom: var(--space-2);
}

.modal p {
  color: var(--color-gray-600);
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-6);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}
</style>
