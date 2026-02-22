import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
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

describe('AudioSection', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  function mountComponent(props = { interviewId: 1, hasAudio: false }) {
    return mount(AudioSection, {
      props,
    })
  }

  it('shows upload area when no audio exists', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('.upload-area').exists()).toBe(true)
    expect(wrapper.find('.audio-player').exists()).toBe(false)
    expect(wrapper.text()).toContain('Click to upload audio recording')
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
    await vi.dynamicImportSettled()
    await wrapper.vm.$nextTick()
    // Wait for onMounted async
    await new Promise((resolve) => setTimeout(resolve, 0))
    await wrapper.vm.$nextTick()

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

    await vi.dynamicImportSettled()
    await new Promise((resolve) => setTimeout(resolve, 0))

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
    await vi.dynamicImportSettled()
    await new Promise((resolve) => setTimeout(resolve, 0))
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Hello world transcription')
  })

  it('displays allowed formats in upload hint', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('MP3, WAV, M4A, FLAC, OGG, WebM')
    expect(wrapper.text()).toContain('50 MB')
  })
})
