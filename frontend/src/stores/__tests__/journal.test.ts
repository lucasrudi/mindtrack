import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useJournalStore } from '../journal'

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

const sampleEntry = {
  id: 1,
  entryDate: '2025-01-15',
  title: 'Good day',
  content: 'Today was productive',
  mood: 7,
  tags: ['gratitude', 'work'],
  sharedWithTherapist: false,
  createdAt: '2025-01-15T10:00:00',
  updatedAt: '2025-01-15T10:00:00',
}

const sampleEntry2 = {
  id: 2,
  entryDate: '2025-01-14',
  title: 'Tough day',
  content: 'Had some challenges',
  mood: 4,
  tags: ['anxiety'],
  sharedWithTherapist: true,
  createdAt: '2025-01-14T10:00:00',
  updatedAt: '2025-01-14T10:00:00',
}

describe('useJournalStore', () => {
  let api: {
    get: ReturnType<typeof vi.fn>
    post: ReturnType<typeof vi.fn>
    put: ReturnType<typeof vi.fn>
    patch: ReturnType<typeof vi.fn>
    delete: ReturnType<typeof vi.fn>
  }

  beforeEach(async () => {
    setActivePinia(createPinia())
    const module = await import('@/services/api')
    api = module.default as unknown as typeof api
    vi.clearAllMocks()
  })

  it('initializes with empty state', () => {
    const store = useJournalStore()
    expect(store.entries).toEqual([])
    expect(store.currentEntry).toBeNull()
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('fetches entries', async () => {
    api.get.mockResolvedValue({ data: [sampleEntry, sampleEntry2] })
    const store = useJournalStore()

    await store.fetchEntries()

    expect(api.get).toHaveBeenCalledWith('/journal')
    expect(store.entries).toHaveLength(2)
  })

  it('sorts entries by date descending', async () => {
    api.get.mockResolvedValue({ data: [sampleEntry2, sampleEntry] })
    const store = useJournalStore()
    await store.fetchEntries()

    expect(store.sortedEntries[0].entryDate).toBe('2025-01-15')
    expect(store.sortedEntries[1].entryDate).toBe('2025-01-14')
  })

  it('fetches single entry', async () => {
    api.get.mockResolvedValue({ data: sampleEntry })
    const store = useJournalStore()

    const result = await store.fetchEntry(1)

    expect(api.get).toHaveBeenCalledWith('/journal/1')
    expect(result).toEqual(sampleEntry)
    expect(store.currentEntry).toEqual(sampleEntry)
  })

  it('creates entry', async () => {
    const newEntry = { ...sampleEntry, id: 3 }
    api.post.mockResolvedValue({ data: newEntry })
    const store = useJournalStore()

    const result = await store.createEntry({
      entryDate: '2025-01-15',
      title: 'Good day',
      content: 'Today was productive',
      mood: 7,
      tags: ['gratitude', 'work'],
      sharedWithTherapist: false,
    })

    expect(api.post).toHaveBeenCalledWith('/journal', expect.any(Object))
    expect(result).toEqual(newEntry)
    expect(store.entries).toHaveLength(1)
  })

  it('updates entry', async () => {
    api.get.mockResolvedValue({ data: [sampleEntry] })
    const updated = { ...sampleEntry, title: 'Updated title' }
    api.put.mockResolvedValue({ data: updated })
    const store = useJournalStore()
    await store.fetchEntries()

    await store.updateEntry(1, {
      entryDate: '2025-01-15',
      title: 'Updated title',
      content: 'Today was productive',
      mood: 7,
      tags: ['gratitude', 'work'],
      sharedWithTherapist: false,
    })

    expect(api.put).toHaveBeenCalledWith('/journal/1', expect.any(Object))
    expect(store.entries[0].title).toBe('Updated title')
  })

  it('deletes entry', async () => {
    api.get.mockResolvedValue({ data: [sampleEntry, sampleEntry2] })
    api.delete.mockResolvedValue({})
    const store = useJournalStore()
    await store.fetchEntries()

    await store.deleteEntry(1)

    expect(api.delete).toHaveBeenCalledWith('/journal/1')
    expect(store.entries).toHaveLength(1)
    expect(store.entries[0].id).toBe(2)
  })

  it('toggles sharing', async () => {
    api.get.mockResolvedValue({ data: [sampleEntry] })
    const shared = { ...sampleEntry, sharedWithTherapist: true }
    api.patch.mockResolvedValue({ data: shared })
    const store = useJournalStore()
    await store.fetchEntries()

    await store.toggleSharing(1)

    expect(api.patch).toHaveBeenCalledWith('/journal/1/share')
    expect(store.entries[0].sharedWithTherapist).toBe(true)
  })

  it('sets error on fetch failure', async () => {
    api.get.mockRejectedValue(new Error('Network error'))
    const store = useJournalStore()

    await expect(store.fetchEntries()).rejects.toThrow('Network error')

    expect(store.error).toBe('Failed to load journal entries')
  })

  it('clears error', () => {
    const store = useJournalStore()
    store.error = 'Some error'
    store.clearError()
    expect(store.error).toBeNull()
  })
})
