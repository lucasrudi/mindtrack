import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'

export interface JournalEntry {
  id: number
  entryDate: string
  title: string | null
  content: string | null
  mood: number | null
  tags: string[]
  sharedWithTherapist: boolean
  createdAt: string
  updatedAt: string
}

export interface JournalEntryForm {
  entryDate: string
  title: string
  content: string
  mood: number | null
  tags: string[]
  sharedWithTherapist: boolean
}

export const useJournalStore = defineStore('journal', () => {
  const entries = ref<JournalEntry[]>([])
  const currentEntry = ref<JournalEntry | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const sortedEntries = computed(() =>
    [...entries.value].sort(
      (a, b) => new Date(b.entryDate).getTime() - new Date(a.entryDate).getTime(),
    ),
  )

  async function fetchEntries() {
    loading.value = true
    error.value = null
    try {
      const response = await api.get('/journal')
      entries.value = response.data
    } catch (err) {
      error.value = 'Failed to load journal entries'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchEntry(id: number) {
    loading.value = true
    error.value = null
    try {
      const response = await api.get(`/journal/${id}`)
      currentEntry.value = response.data
      return response.data
    } catch (err) {
      error.value = 'Failed to load journal entry'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function createEntry(form: JournalEntryForm) {
    loading.value = true
    error.value = null
    try {
      const response = await api.post('/journal', form)
      entries.value.unshift(response.data)
      return response.data
    } catch (err) {
      error.value = 'Failed to create journal entry'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateEntry(id: number, form: JournalEntryForm) {
    loading.value = true
    error.value = null
    try {
      const response = await api.put(`/journal/${id}`, form)
      const index = entries.value.findIndex((e) => e.id === id)
      if (index !== -1) {
        entries.value[index] = response.data
      }
      currentEntry.value = response.data
      return response.data
    } catch (err) {
      error.value = 'Failed to update journal entry'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function deleteEntry(id: number) {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/journal/${id}`)
      entries.value = entries.value.filter((e) => e.id !== id)
      if (currentEntry.value?.id === id) {
        currentEntry.value = null
      }
    } catch (err) {
      error.value = 'Failed to delete journal entry'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function toggleSharing(id: number) {
    error.value = null
    try {
      const response = await api.patch(`/journal/${id}/share`)
      const index = entries.value.findIndex((e) => e.id === id)
      if (index !== -1) {
        entries.value[index] = response.data
      }
      if (currentEntry.value?.id === id) {
        currentEntry.value = response.data
      }
      return response.data
    } catch (err) {
      error.value = 'Failed to update sharing status'
      throw err
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    entries,
    currentEntry,
    loading,
    error,
    sortedEntries,
    fetchEntries,
    fetchEntry,
    createEntry,
    updateEntry,
    deleteEntry,
    toggleSharing,
    clearError,
  }
})
