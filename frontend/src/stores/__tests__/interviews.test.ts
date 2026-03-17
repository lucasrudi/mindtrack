import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useInterviewsStore, type Interview } from '../interviews'

const mockInterviews: Interview[] = [
  {
    id: 1,
    interviewDate: '2025-01-15',
    moodBefore: 5,
    moodAfter: 7,
    topics: ['anxiety', 'sleep'],
    medicationChanges: 'Increased dosage',
    recommendations: 'Try meditation',
    notes: 'Good session',
    hasAudio: false,
    transcriptionText: null,
    audioExpiresAt: null,
    createdAt: '2025-01-15T10:00:00',
    updatedAt: '2025-01-15T10:00:00',
  },
  {
    id: 2,
    interviewDate: '2025-02-20',
    moodBefore: 6,
    moodAfter: 8,
    topics: ['depression'],
    medicationChanges: null,
    recommendations: null,
    notes: null,
    hasAudio: true,
    transcriptionText: 'Existing transcript',
    audioExpiresAt: '2025-02-21T10:00:00',
    createdAt: '2025-02-20T10:00:00',
    updatedAt: '2025-02-20T10:00:00',
  },
]

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('useInterviewsStore', () => {
  let api: {
    get: ReturnType<typeof vi.fn>
    post: ReturnType<typeof vi.fn>
    put: ReturnType<typeof vi.fn>
    delete: ReturnType<typeof vi.fn>
  }

  beforeEach(async () => {
    setActivePinia(createPinia())
    const module = await import('@/services/api')
    api = module.default as unknown as typeof api
    vi.clearAllMocks()
  })

  it('initializes with empty state', () => {
    const store = useInterviewsStore()
    expect(store.interviews).toEqual([])
    expect(store.currentInterview).toBeNull()
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('fetches interviews', async () => {
    api.get.mockResolvedValue({ data: mockInterviews })
    const store = useInterviewsStore()

    await store.fetchInterviews()

    expect(api.get).toHaveBeenCalledWith('/interviews')
    expect(store.interviews).toEqual(mockInterviews)
    expect(store.loading).toBe(false)
  })

  it('sorts interviews by date descending', async () => {
    api.get.mockResolvedValue({ data: mockInterviews })
    const store = useInterviewsStore()

    await store.fetchInterviews()

    expect(store.sortedInterviews[0].id).toBe(2)
    expect(store.sortedInterviews[1].id).toBe(1)
  })

  it('fetches single interview', async () => {
    api.get.mockResolvedValue({ data: mockInterviews[0] })
    const store = useInterviewsStore()

    const result = await store.fetchInterview(1)

    expect(api.get).toHaveBeenCalledWith('/interviews/1')
    expect(store.currentInterview).toEqual(mockInterviews[0])
    expect(result).toEqual(mockInterviews[0])
  })

  it('creates interview', async () => {
    const newInterview = { ...mockInterviews[0], id: 3 }
    api.post.mockResolvedValue({ data: newInterview })
    const store = useInterviewsStore()

    const result = await store.createInterview({
      interviewDate: '2025-01-15',
      moodBefore: 5,
      moodAfter: 7,
      topics: ['anxiety'],
      medicationChanges: '',
      recommendations: '',
      notes: '',
    })

    expect(api.post).toHaveBeenCalledWith('/interviews', expect.any(Object))
    expect(result).toEqual(newInterview)
    expect(store.interviews).toHaveLength(1)
  })

  it('updates interview', async () => {
    api.get.mockResolvedValue({ data: mockInterviews })
    const store = useInterviewsStore()
    await store.fetchInterviews()

    const updated = { ...mockInterviews[0], moodAfter: 9 }
    api.put.mockResolvedValue({ data: updated })

    await store.updateInterview(1, {
      interviewDate: '2025-01-15',
      moodBefore: 5,
      moodAfter: 9,
      topics: [],
      medicationChanges: '',
      recommendations: '',
      notes: '',
    })

    expect(api.put).toHaveBeenCalledWith('/interviews/1', expect.any(Object))
    expect(store.interviews.find((i) => i.id === 1)?.moodAfter).toBe(9)
    expect(store.currentInterview?.moodAfter).toBe(9)
  })

  it('deletes interview', async () => {
    api.get.mockResolvedValue({ data: mockInterviews })
    api.delete.mockResolvedValue({})
    const store = useInterviewsStore()
    await store.fetchInterviews()

    await store.deleteInterview(1)

    expect(api.delete).toHaveBeenCalledWith('/interviews/1')
    expect(store.interviews).toHaveLength(1)
    expect(store.interviews[0].id).toBe(2)
  })

  it('sets error on fetch failure', async () => {
    api.get.mockRejectedValue(new Error('Network error'))
    const store = useInterviewsStore()

    await expect(store.fetchInterviews()).rejects.toThrow('Network error')

    expect(store.error).toBe('Failed to load interviews')
    expect(store.loading).toBe(false)
  })

  it('clears error', () => {
    const store = useInterviewsStore()
    store.error = 'Some error'

    store.clearError()

    expect(store.error).toBeNull()
  })

  it('uploads audio and updates the current interview', async () => {
    api.post.mockResolvedValue({
      data: {
        audioUrl: 'https://example.com/audio.mp3',
        transcriptionText: 'Transcript',
        audioExpiresAt: '2025-02-22T10:00:00',
      },
    })
    const store = useInterviewsStore()
    store.currentInterview = {
      ...mockInterviews[1],
      id: 2,
      hasAudio: false,
      transcriptionText: null,
    }

    await store.uploadAudio(2, new File(['audio'], 'clip.mp3', { type: 'audio/mpeg' }))

    expect(api.post).toHaveBeenCalledWith(
      '/interviews/2/audio',
      expect.any(FormData),
      expect.objectContaining({
        headers: { 'Content-Type': 'multipart/form-data' },
      }),
    )
    expect(store.currentInterview?.hasAudio).toBe(true)
    expect(store.currentInterview?.transcriptionText).toBe('Transcript')
  })

  it('returns audio metadata', async () => {
    api.get.mockResolvedValueOnce({
      data: {
        audioUrl: 'https://example.com/audio.mp3',
        transcriptionText: null,
        audioExpiresAt: '2025-02-22T10:00:00',
      },
    })
    const store = useInterviewsStore()

    await expect(store.getAudioUrl(2)).resolves.toEqual({
      audioUrl: 'https://example.com/audio.mp3',
      transcriptionText: null,
      audioExpiresAt: '2025-02-22T10:00:00',
    })
  })

  it('deletes audio and clears interview flags', async () => {
    api.delete.mockResolvedValue({})
    const store = useInterviewsStore()
    store.currentInterview = {
      ...mockInterviews[1],
      id: 2,
      hasAudio: true,
      transcriptionText: 'Transcript',
      audioExpiresAt: '2025-02-22T10:00:00',
    }

    await store.deleteAudio(2)

    expect(api.delete).toHaveBeenCalledWith('/interviews/2/audio')
    expect(store.currentInterview?.hasAudio).toBe(false)
    expect(store.currentInterview?.transcriptionText).toBeNull()
    expect(store.currentInterview?.audioExpiresAt).toBeNull()
  })

  it('sets errors for single interview and audio failures', async () => {
    const store = useInterviewsStore()
    api.get.mockRejectedValueOnce(new Error('Interview error'))
    await expect(store.fetchInterview(9)).rejects.toThrow('Interview error')
    expect(store.error).toBe('Failed to load interview')

    api.get.mockRejectedValueOnce(new Error('Audio error'))
    await expect(store.getAudioUrl(9)).rejects.toThrow('Audio error')
    expect(store.error).toBe('Failed to load audio')
  })
})
