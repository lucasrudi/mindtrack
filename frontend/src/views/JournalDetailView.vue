<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useJournalStore } from '@/stores/journal'

const route = useRoute()
const router = useRouter()
const store = useJournalStore()
const showDeleteModal = ref(false)

const entryId = Number(route.params.id)

onMounted(async () => {
  await store.fetchEntry(entryId)
})

function goBack() {
  router.push({ name: 'journal' })
}

function navigateToEdit() {
  router.push({ name: 'journal-edit', params: { id: entryId } })
}

async function handleDelete() {
  await store.deleteEntry(entryId)
  router.push({ name: 'journal' })
}

async function handleToggleSharing() {
  await store.toggleSharing(entryId)
}

function moodEmoji(mood: number | null): string {
  if (mood === null) return ''
  if (mood >= 8) return '\u{1F60A}'
  if (mood >= 6) return '\u{1F642}'
  if (mood >= 4) return '\u{1F610}'
  if (mood >= 2) return '\u{1F61F}'
  return '\u{1F622}'
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr + 'T00:00:00')
  return date.toLocaleDateString('en-US', {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: 'numeric',
  })
}
</script>

<template>
  <div class="detail-view">
    <div class="detail-header">
      <button class="btn-back" @click="goBack">&larr; Back to Journal</button>
    </div>

    <div v-if="store.error" class="error-banner" role="alert">
      {{ store.error }}
      <button class="error-dismiss" @click="store.clearError()">&times;</button>
    </div>

    <div v-if="store.loading" class="loading">
      <p>Loading entry...</p>
    </div>

    <template v-else-if="store.currentEntry">
      <article class="entry-detail">
        <div class="entry-meta">
          <span class="entry-date">{{ formatDate(store.currentEntry.entryDate) }}</span>
          <span v-if="store.currentEntry.mood" class="entry-mood">
            {{ moodEmoji(store.currentEntry.mood) }} {{ store.currentEntry.mood }}/10
          </span>
        </div>

        <h1 v-if="store.currentEntry.title" class="entry-title">
          {{ store.currentEntry.title }}
        </h1>

        <div v-if="store.currentEntry.content" class="entry-content">
          {{ store.currentEntry.content }}
        </div>

        <div v-if="store.currentEntry.tags.length" class="entry-tags">
          <span v-for="tag in store.currentEntry.tags" :key="tag" class="tag">
            {{ tag }}
          </span>
        </div>

        <div class="sharing-status">
          <span
            :class="[
              'sharing-badge',
              store.currentEntry.sharedWithTherapist ? 'shared' : 'private',
            ]"
          >
            {{ store.currentEntry.sharedWithTherapist ? 'Shared with therapist' : 'Private entry' }}
          </span>
          <button class="btn-link" @click="handleToggleSharing">
            {{ store.currentEntry.sharedWithTherapist ? 'Make private' : 'Share with therapist' }}
          </button>
        </div>

        <div class="entry-actions">
          <button class="btn btn-secondary" @click="navigateToEdit">Edit</button>
          <button class="btn btn-danger" @click="showDeleteModal = true">Delete</button>
        </div>
      </article>
    </template>

    <!-- Delete confirmation modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <div class="modal" role="dialog">
        <h2>Delete Entry</h2>
        <p>Are you sure you want to delete this journal entry? This cannot be undone.</p>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="showDeleteModal = false">Cancel</button>
          <button class="btn btn-danger" @click="handleDelete">Delete</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-view {
  max-width: 700px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

.detail-header {
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
}

.loading {
  text-align: center;
  padding: var(--space-12);
}

.entry-detail {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-8);
}

.entry-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.entry-date {
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
  font-weight: var(--font-weight-medium);
}

.entry-mood {
  font-size: var(--font-size-lg);
}

.entry-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
  margin-bottom: var(--space-4);
}

.entry-content {
  font-size: var(--font-size-base);
  color: var(--color-gray-700);
  line-height: 1.8;
  white-space: pre-wrap;
  margin-bottom: var(--space-6);
}

.entry-tags {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  margin-bottom: var(--space-6);
}

.tag {
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  background: var(--color-primary-50);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
}

.sharing-status {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-6);
  padding: var(--space-3) var(--space-4);
  background: var(--color-gray-50);
  border-radius: var(--border-radius);
}

.sharing-badge {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--border-radius-full);
}

.sharing-badge.shared {
  color: var(--color-success);
  background: #f0fdf4;
}

.sharing-badge.private {
  color: var(--color-gray-600);
  background: var(--color-gray-100);
}

.btn-link {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  padding: 0;
}

.btn-link:hover {
  color: var(--color-primary-dark);
}

.entry-actions {
  display: flex;
  gap: var(--space-3);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-gray-100);
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

.btn-secondary {
  background: var(--color-gray-100);
  color: var(--color-gray-700);
}

.btn-secondary:hover {
  background: var(--color-gray-200);
}

.btn-danger {
  background: #fef2f2;
  color: var(--color-error);
}

.btn-danger:hover {
  background: #fee2e2;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.modal {
  background: var(--color-white);
  border-radius: var(--border-radius-lg);
  padding: var(--space-6);
  max-width: 400px;
  width: 90%;
}

.modal h2 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--space-3);
}

.modal p {
  color: var(--color-gray-600);
  margin-bottom: var(--space-6);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}
</style>
