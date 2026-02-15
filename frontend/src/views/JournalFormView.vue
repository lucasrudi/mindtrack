<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useJournalStore, type JournalEntryForm } from '@/stores/journal'

const route = useRoute()
const router = useRouter()
const store = useJournalStore()

const isEditing = computed(() => route.name === 'journal-edit')
const entryId = computed(() => (isEditing.value ? Number(route.params.id) : null))
const submitting = ref(false)
const tagInput = ref('')

const today = new Date().toISOString().split('T')[0]

const form = reactive<JournalEntryForm>({
  entryDate: today,
  title: '',
  content: '',
  mood: null,
  tags: [],
  sharedWithTherapist: false,
})

onMounted(async () => {
  if (isEditing.value && entryId.value) {
    await store.fetchEntries()
    const entry = store.entries.find((e) => e.id === entryId.value)
    if (entry) {
      form.entryDate = entry.entryDate
      form.title = entry.title ?? ''
      form.content = entry.content ?? ''
      form.mood = entry.mood
      form.tags = [...entry.tags]
      form.sharedWithTherapist = entry.sharedWithTherapist
    }
  }
})

function addTag() {
  const tag = tagInput.value.trim()
  if (tag && !form.tags.includes(tag)) {
    form.tags.push(tag)
  }
  tagInput.value = ''
}

function removeTag(tag: string) {
  form.tags = form.tags.filter((t) => t !== tag)
}

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEditing.value && entryId.value) {
      await store.updateEntry(entryId.value, form)
    } else {
      await store.createEntry(form)
    }
    router.push({ name: 'journal' })
  } catch {
    // error captured in store
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push({ name: 'journal' })
}
</script>

<template>
  <div class="form-view">
    <div class="form-header">
      <button class="btn-back" @click="goBack">&larr; Back</button>
      <h1>{{ isEditing ? 'Edit Entry' : 'New Journal Entry' }}</h1>
    </div>

    <div v-if="store.error" class="error-banner" role="alert">
      {{ store.error }}
      <button class="error-dismiss" @click="store.clearError()">&times;</button>
    </div>

    <form class="journal-form" @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="entry-date">Date *</label>
        <input id="entry-date" v-model="form.entryDate" type="date" class="form-input" required />
      </div>

      <div class="form-group">
        <label for="entry-title">Title</label>
        <input
          id="entry-title"
          v-model="form.title"
          type="text"
          class="form-input"
          placeholder="Give your entry a title..."
        />
      </div>

      <div class="form-group">
        <label for="entry-content">Content</label>
        <textarea
          id="entry-content"
          v-model="form.content"
          class="form-textarea"
          rows="8"
          placeholder="Write your thoughts..."
        />
      </div>

      <div class="form-group">
        <label for="entry-mood">Mood (1-10)</label>
        <input
          id="entry-mood"
          v-model.number="form.mood"
          type="number"
          class="form-input mood-input"
          min="1"
          max="10"
          placeholder="How are you feeling?"
        />
      </div>

      <div class="form-group">
        <label for="tag-input">Tags</label>
        <div class="tag-input-wrapper">
          <div v-if="form.tags.length" class="tag-list">
            <span v-for="tag in form.tags" :key="tag" class="tag">
              {{ tag }}
              <button type="button" class="tag-remove" @click="removeTag(tag)">&times;</button>
            </span>
          </div>
          <input
            id="tag-input"
            v-model="tagInput"
            type="text"
            class="form-input"
            placeholder="Add a tag and press Enter"
            @keydown.enter.prevent="addTag"
          />
        </div>
      </div>

      <div class="form-group">
        <label class="checkbox-label">
          <input v-model="form.sharedWithTherapist" type="checkbox" class="checkbox" />
          <span>Share with therapist</span>
        </label>
      </div>

      <div class="form-actions">
        <button type="button" class="btn btn-secondary" @click="goBack">Cancel</button>
        <button type="submit" class="btn btn-primary" :disabled="submitting">
          {{ submitting ? 'Saving...' : isEditing ? 'Update Entry' : 'Create Entry' }}
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
}

.journal-form {
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
  min-height: 160px;
}

.mood-input {
  max-width: 120px;
}

.tag-input-wrapper {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.tag-list {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-size-sm);
  color: var(--color-primary);
  background: var(--color-primary-50);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
}

.tag-remove {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.tag-remove:hover {
  color: var(--color-error);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
}

.checkbox {
  width: 16px;
  height: 16px;
  accent-color: var(--color-primary);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
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
</style>
