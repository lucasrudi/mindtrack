<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useInterviewsStore } from '@/stores/interviews'

const router = useRouter()
const store = useInterviewsStore()

onMounted(() => {
  store.fetchInterviews()
})

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

function moodLabel(value: number | null) {
  if (value === null) return '-'
  return `${value}/10`
}

function navigateToNew() {
  router.push({ name: 'interview-new' })
}

function navigateToDetail(id: number) {
  router.push({ name: 'interview-detail', params: { id } })
}
</script>

<template>
  <div class="interviews-view">
    <header class="page-header">
      <div>
        <h1>Interviews</h1>
        <p class="subtitle">Track your psychiatrist sessions and progress</p>
      </div>
      <button class="btn btn-primary" @click="navigateToNew">+ New Interview</button>
    </header>

    <div v-if="store.loading && !store.interviews.length" class="loading">
      <p>Loading interviews...</p>
    </div>

    <div v-else-if="store.error" class="error-message">
      <p>{{ store.error }}</p>
      <button class="btn btn-secondary" @click="store.fetchInterviews()">Retry</button>
    </div>

    <div v-else-if="!store.sortedInterviews.length" class="empty-state">
      <div class="empty-icon">📋</div>
      <h2>No interviews yet</h2>
      <p>Start tracking your psychiatrist sessions to monitor your progress over time.</p>
      <button class="btn btn-primary" @click="navigateToNew">Log your first interview</button>
    </div>

    <div v-else class="interview-list">
      <div
        v-for="interview in store.sortedInterviews"
        :key="interview.id"
        class="interview-card"
        role="button"
        tabindex="0"
        @click="navigateToDetail(interview.id)"
        @keydown.enter="navigateToDetail(interview.id)"
      >
        <div class="card-header">
          <time class="card-date">{{ formatDate(interview.interviewDate) }}</time>
          <span v-if="interview.hasAudio" class="audio-badge">Audio</span>
        </div>

        <div class="mood-row">
          <div class="mood-item">
            <span class="mood-label">Before</span>
            <span class="mood-value">{{ moodLabel(interview.moodBefore) }}</span>
          </div>
          <span class="mood-arrow">→</span>
          <div class="mood-item">
            <span class="mood-label">After</span>
            <span class="mood-value">{{ moodLabel(interview.moodAfter) }}</span>
          </div>
        </div>

        <div v-if="interview.topics.length" class="topics-row">
          <span v-for="topic in interview.topics" :key="topic" class="topic-chip">
            {{ topic }}
          </span>
        </div>

        <p v-if="interview.notes" class="card-notes">{{ interview.notes }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.interviews-view {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-8);
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
  font-size: var(--font-size-base);
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
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

.btn-secondary {
  background: var(--color-gray-100);
  color: var(--color-gray-700);
}

.btn-secondary:hover {
  background: var(--color-gray-200);
}

.loading,
.error-message,
.empty-state {
  text-align: center;
  padding: var(--space-16) var(--space-6);
}

.error-message {
  color: var(--color-error);
}

.error-message .btn {
  margin-top: var(--space-4);
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: var(--space-4);
}

.empty-state h2 {
  font-size: var(--font-size-xl);
  color: var(--color-gray-700);
  margin-bottom: var(--space-2);
}

.empty-state p {
  color: var(--color-gray-500);
  margin-bottom: var(--space-6);
}

.interview-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.interview-card {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-5);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.interview-card:hover {
  border-color: var(--color-primary-light);
  box-shadow: var(--shadow-md);
}

.interview-card:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-3);
}

.card-date {
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  font-size: var(--font-size-base);
}

.audio-badge {
  font-size: var(--font-size-xs);
  background: var(--color-primary-50);
  color: var(--color-primary);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
}

.mood-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

.mood-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.mood-label {
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.mood-value {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-800);
}

.mood-arrow {
  color: var(--color-gray-400);
  font-size: var(--font-size-lg);
}

.topics-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.topic-chip {
  font-size: var(--font-size-xs);
  background: var(--color-gray-100);
  color: var(--color-gray-700);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--border-radius-full);
}

.card-notes {
  color: var(--color-gray-600);
  font-size: var(--font-size-sm);
  line-height: var(--line-height-normal);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
