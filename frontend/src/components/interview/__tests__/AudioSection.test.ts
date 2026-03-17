import { describe, it, expect, beforeEach, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import AudioSection from '../AudioSection.vue'

const mockGet = vi.fn()
const mockPost = vi.fn()
const mockDelete = vi.fn()

vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
    put: vi.fn(),
    delete: (...args: unknown[]) => mockDelete(...args),
  },
}))

const defaultProps = { interviewId: 1, hasAudio: false }

function mountComponent(props = defaultProps) {
  return mount(AudioSection, {
    props,
  })
}

async function settle() {
  await vi.dynamicImportSettled()
  await flushPromises()
  await new Promise((resolve) => setTimeout(resolve, 0))
}

describe('AudioSection', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.useRealTimers()
  })

  it('shows upload area when no audio exists', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('.upload-area').exists()).toBe(true)
    expect(wrapper.find('.audio-player').exists()).toBe(false)
    expect(wrapper.text()).toContain('Or click to upload a file')
  })

  it('shows audio player when audio exists', async () => {
    mockGet.mockResolvedValue({
      data: {
        audioUrl: 'http://example.com/audio.mp3',
        transcriptionText: null,
        audioExpiresAt: '2025-02-01T00:00:00',
      },
    })

    const wrapper = mountComponent({ interviewId: 1, hasAudio: true })
    await settle()

    expect(mockGet).toHaveBeenCalledWith('/interviews/1/audio')
    expect(wrapper.find('audio').exists()).toBe(true)
  })

  it('validates unsupported file format', async () => {
    const wrapper = mountComponent()
    const fileInput = wrapper.find('[data-testid="audio-file-input"]')

    const file = new File(['content'], 'document.pdf', { type: 'application/pdf' })
    Object.defineProperty(fileInput.element, 'files', { value: [file] })
    await fileInput.trigger('change')

    expect(wrapper.find('.audio-error').exists()).toBe(true)
    expect(wrapper.text()).toContain('Unsupported format')
  })

  it('validates file size limit', async () => {
    const wrapper = mountComponent()
    const fileInput = wrapper.find('[data-testid="audio-file-input"]')

    const largeContent = new ArrayBuffer(51 * 1024 * 1024)
    const file = new File([largeContent], 'huge.mp3', { type: 'audio/mpeg' })
    Object.defineProperty(fileInput.element, 'files', { value: [file] })
    await fileInput.trigger('change')

    expect(wrapper.find('.audio-error').exists()).toBe(true)
    expect(wrapper.text()).toContain('File too large')
  })

  it('uploads a valid audio file', async () => {
    mockPost.mockResolvedValue({
      data: {
        audioUrl: 'http://example.com/new.mp3',
        transcriptionText: null,
        audioExpiresAt: '2025-02-01T00:00:00',
      },
    })

    const wrapper = mountComponent()
    const fileInput = wrapper.find('[data-testid="audio-file-input"]')

    const file = new File(['audio-content'], 'recording.mp3', { type: 'audio/mpeg' })
    Object.defineProperty(fileInput.element, 'files', { value: [file] })
    await fileInput.trigger('change')

    await settle()

    expect(mockPost).toHaveBeenCalledWith(
      '/interviews/1/audio',
      expect.any(FormData),
      expect.objectContaining({
        headers: { 'Content-Type': 'multipart/form-data' },
      }),
    )
  })

  it('shows transcription when available', async () => {
    mockGet.mockResolvedValue({
      data: {
        audioUrl: 'http://example.com/audio.mp3',
        transcriptionText: 'Hello world transcription',
        audioExpiresAt: '2025-02-01T00:00:00',
      },
    })

    const wrapper = mountComponent({ interviewId: 1, hasAudio: true })
    await settle()

    expect(wrapper.text()).toContain('Hello world transcription')
  })

  it('displays allowed formats in upload hint', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('MP3, WAV, M4A, FLAC, OGG, WebM')
    expect(wrapper.text()).toContain('50 MB')
  })

  it('shows pending transcription when audio is still processing', async () => {
    mockGet.mockResolvedValue({
      data: {
        audioUrl: 'http://example.com/audio.mp3',
        transcriptionText: null,
        audioExpiresAt: '2025-02-01T00:00:00',
        transcriptionStatus: 'IN_PROGRESS',
      },
    })

    const wrapper = mountComponent({ interviewId: 1, hasAudio: true })
    await settle()

    expect(wrapper.text()).toContain('Transcribing audio...')
  })

  it('deletes existing audio', async () => {
    mockGet.mockResolvedValue({
      data: {
        audioUrl: 'http://example.com/audio.mp3',
        transcriptionText: null,
        audioExpiresAt: '2025-02-01T00:00:00',
        transcriptionStatus: 'COMPLETED',
      },
    })

    const wrapper = mountComponent({ interviewId: 1, hasAudio: true })
    await settle()

    await wrapper.find('.btn-delete').trigger('click')
    await settle()

    expect(mockDelete).toHaveBeenCalledWith('/interviews/1/audio')
  })

  it('shows an upload error when upload fails', async () => {
    mockPost.mockRejectedValue(new Error('upload failed'))

    const wrapper = mountComponent()
    const fileInput = wrapper.find('[data-testid="audio-file-input"]')
    const file = new File(['audio-content'], 'recording.mp3', { type: 'audio/mpeg' })

    Object.defineProperty(fileInput.element, 'files', { value: [file] })
    await fileInput.trigger('change')
    await settle()

    expect(wrapper.text()).toContain('Upload failed. Please try again.')
  })

  it('dismisses validation errors', async () => {
    const wrapper = mountComponent()
    const fileInput = wrapper.find('[data-testid="audio-file-input"]')
    const file = new File(['content'], 'document.pdf', { type: 'application/pdf' })

    Object.defineProperty(fileInput.element, 'files', { value: [file] })
    await fileInput.trigger('change')
    expect(wrapper.find('.audio-error').exists()).toBe(true)

    await wrapper.find('.error-dismiss').trigger('click')
    expect(wrapper.find('.audio-error').exists()).toBe(false)
  })

  it('shows an error when microphone access is denied', async () => {
    Object.defineProperty(globalThis.navigator, 'mediaDevices', {
      value: {
        getUserMedia: vi.fn().mockRejectedValue(new Error('denied')),
      },
      configurable: true,
    })

    const wrapper = mountComponent()
    await wrapper.find('.btn-record').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('Microphone access denied.')
  })

  it('records audio, uploads it, and shows transcription progress', async () => {
    vi.useFakeTimers()

    const stopTrack = vi.fn()
    Object.defineProperty(globalThis.navigator, 'mediaDevices', {
      value: {
        getUserMedia: vi.fn().mockResolvedValue({
          getTracks: () => [{ stop: stopTrack }],
        }),
      },
      configurable: true,
    })

    class MediaRecorderMock {
      ondataavailable: ((event: { data: Blob }) => void) | null = null
      onstop: (() => void) | null = null

      constructor() {}

      start() {}

      stop() {
        this.ondataavailable?.({ data: new Blob(['chunk'], { type: 'audio/webm' }) })
        this.onstop?.()
      }
    }

    Object.defineProperty(globalThis, 'MediaRecorder', {
      value: MediaRecorderMock,
      configurable: true,
    })

    mockPost.mockResolvedValue({
      data: {
        audioUrl: 'http://example.com/recording.webm',
        transcriptionText: null,
        audioExpiresAt: '2025-02-01T00:00:00',
      },
    })

    const wrapper = mountComponent()
    await wrapper.find('.btn-record').trigger('click')
    await flushPromises()
    await vi.advanceTimersByTimeAsync(2000)
    await flushPromises()

    expect(wrapper.text()).toContain('00:02')

    await wrapper.find('.btn-stop').trigger('click')
    await flushPromises()

    expect(stopTrack).toHaveBeenCalled()
    expect(mockPost).toHaveBeenCalledWith(
      '/interviews/1/audio',
      expect.any(FormData),
      expect.objectContaining({
        headers: { 'Content-Type': 'multipart/form-data' },
      }),
    )
    expect(wrapper.text()).toContain('Transcribing')
  })

  it('shows a slow-transcription warning after repeated pending polls', async () => {
    vi.useFakeTimers()
    mockGet
      .mockResolvedValueOnce({
        data: {
          audioUrl: 'http://example.com/audio.mp3',
          transcriptionText: null,
          audioExpiresAt: '2025-02-01T00:00:00',
          transcriptionStatus: 'IN_PROGRESS',
        },
      })
      .mockResolvedValue({
        data: {
          audioUrl: 'http://example.com/audio.mp3',
          transcriptionText: null,
          audioExpiresAt: '2025-02-01T00:00:00',
          transcriptionStatus: 'IN_PROGRESS',
        },
      })

    const wrapper = mountComponent({ interviewId: 1, hasAudio: true })
    await flushPromises()

    for (let i = 0; i < 4; i++) {
      await vi.advanceTimersByTimeAsync(10000)
      await flushPromises()
    }

    expect(wrapper.text()).toContain('Transcription taking longer than expected')
  })
})
