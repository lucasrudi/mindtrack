<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useJournalStore } from '@/stores/journal'

const router = useRouter()
const store = useJournalStore()

onMounted(() => {
  store.fetchEntries()
})

function navigateToNew() {
  router.push({ name: 'journal-new' })
}

function navigateToDetail(id: number) {
  router.push({ name: 'journal-detail', params: { id } })
}

function moodEmoji(mood: number | null): string {
  if (mood === null) return ''
  if (mood >= 8) return '😊'
  if (mood >= 6) return '🙂'
  if (mood >= 4) return '😐'
  if (mood >= 2) return '😟'
  return '😢'
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr + 'T00:00:00')
  return date.toLocaleDateString('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}

function truncate(text: string | null, maxLength: number): string {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}
</script>

<template>
  <div class="journal-view">
    <header class="page-header">
      <div>
        <h1>Journal</h1>
        <p class="subtitle">Reflect on your thoughts and track your mood</p>
      </div>
      <button class="btn btn-primary" @click="navigateToNew">+ New Entry</button>
    </header>

    <div v-if="store.error" class="error-message">
      <p>{{ store.error }}</p>
      <button class="btn btn-secondary" @click="store.clearError()">Dismiss</button>
    </div>

    <div v-if="store.loading && !store.entries.length" class="loading">
      <p>Loading entries...</p>
    </div>

    <div v-else-if="!store.entries.length" class="empty-state">
      <h2>No journal entries yet</h2>
      <p>Start writing to track your thoughts and mood over time.</p>
      <button class="btn btn-primary" @click="navigateToNew">Write your first entry</button>
    </div>

    <div v-else class="entries-list">
      <div
        v-for="entry in store.sortedEntries"
        :key="entry.id"
        class="entry-card"
        @click="navigateToDetail(entry.id)"
      >
        <div class="entry-header">
          <span class="entry-date">{{ formatDate(entry.entryDate) }}</span>
          <span v-if="entry.mood" class="entry-mood">
            {{ moodEmoji(entry.mood) }} {{ entry.mood }}/10
          </span>
        </div>
        <h3 v-if="entry.title" class="entry-title">{{ entry.title }}</h3>
        <p v-if="entry.content" class="entry-preview">
          {{ truncate(entry.content, 150) }}
        </p>
        <div class="entry-footer">
          <div v-if="entry.tags.length" class="entry-tags">
            <span v-for="tag in entry.tags.slice(0, 3)" :key="tag" class="tag">
              {{ tag }}
            </span>
            <span v-if="entry.tags.length > 3" class="tag-more">
              +{{ entry.tags.length - 3 }}
            </span>
          </div>
          <span v-if="entry.sharedWithTherapist" class="shared-badge">Shared</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.journal-view {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-6);
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

.btn {
  display: inline-flex;
  align-items: center;
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

.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: var(--color-error);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--border-radius);
  margin-bottom: var(--space-6);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading,
.empty-state {
  text-align: center;
  padding: var(--space-12) var(--space-6);
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

.entries-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.entry-card {
  padding: var(--space-5);
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.entry-card:hover {
  border-color: var(--color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.entry-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}

.entry-date {
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
  font-weight: var(--font-weight-medium);
}

.entry-mood {
  font-size: var(--font-size-sm);
  color: var(--color-gray-600);
}

.entry-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin-bottom: var(--space-2);
}

.entry-preview {
  font-size: var(--font-size-sm);
  color: var(--color-gray-600);
  line-height: 1.5;
  margin-bottom: var(--space-3);
}

.entry-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.entry-tags {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.tag {
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  background: var(--color-primary-50);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
}

.tag-more {
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
  padding: var(--space-1) var(--space-2);
}

.shared-badge {
  font-size: var(--font-size-xs);
  color: var(--color-success);
  background: #f0fdf4;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
}
</style>
