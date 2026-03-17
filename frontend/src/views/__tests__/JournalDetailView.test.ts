import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'
import JournalDetailView from '../JournalDetailView.vue'
import { useJournalStore } from '@/stores/journal'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: '3' } }),
  useRouter: () => ({ push: mockPush }),
}))

const mockGet = vi.fn()
const mockPatch = vi.fn()
const mockDelete = vi.fn()

vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: vi.fn(),
    put: vi.fn(),
    patch: (...args: unknown[]) => mockPatch(...args),
    delete: (...args: unknown[]) => mockDelete(...args),
  },
}))

const sampleEntry = {
  id: 3,
  entryDate: '2025-04-05',
  title: 'Morning reflection',
  content: 'Today I felt much calmer after meditating.',
  mood: 8,
  tags: ['meditation', 'calm'],
  sharedWithTherapist: false,
  createdAt: '2025-04-05T08:00:00',
  updatedAt: '2025-04-05T08:00:00',
}

describe('JournalDetailView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockClear()
    mockGet.mockReset().mockResolvedValue({ data: sampleEntry })
    mockPatch.mockReset().mockResolvedValue({ data: { ...sampleEntry, sharedWithTherapist: true } })
    mockDelete.mockReset().mockResolvedValue({})
  })

  it('shows loading state before data arrives', async () => {
    mockGet.mockReturnValue(new Promise(() => {}))
    const wrapper = mount(JournalDetailView)
    await nextTick()
    expect(wrapper.find('.loading').exists()).toBe(true)
    expect(wrapper.text()).toContain('Loading entry...')
  })

  it('renders journal entry details after loading', async () => {
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    expect(wrapper.find('.entry-title').text()).toBe('Morning reflection')
    expect(wrapper.find('.entry-content').text()).toContain('Today I felt much calmer')
  })

  it('renders entry date', async () => {
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    expect(wrapper.find('.entry-date').text()).toContain('April 5, 2025')
  })

  it('renders mood with emoji', async () => {
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    // mood 8 → 😊 emoji
    const mood = wrapper.find('.entry-mood')
    expect(mood.exists()).toBe(true)
    expect(mood.text()).toContain('8/10')
  })

  it('renders tags', async () => {
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    const tags = wrapper.findAll('.tag')
    expect(tags).toHaveLength(2)
    expect(tags[0].text()).toBe('meditation')
    expect(tags[1].text()).toBe('calm')
  })

  it('shows private sharing badge when not shared', async () => {
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    const badge = wrapper.find('.sharing-badge')
    expect(badge.classes()).toContain('private')
    expect(badge.text()).toBe('Private entry')
  })

  it('shows shared sharing badge when shared', async () => {
    mockGet.mockResolvedValue({ data: { ...sampleEntry, sharedWithTherapist: true } })
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    const badge = wrapper.find('.sharing-badge')
    expect(badge.classes()).toContain('shared')
    expect(badge.text()).toBe('Shared with therapist')
  })

  it('shows correct toggle sharing button text when private', async () => {
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    expect(wrapper.find('.btn-link').text()).toBe('Share with therapist')
  })

  it('shows correct toggle sharing button text when shared', async () => {
    mockGet.mockResolvedValue({ data: { ...sampleEntry, sharedWithTherapist: true } })
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    expect(wrapper.find('.btn-link').text()).toBe('Make private')
  })

  it('calls toggleSharing when the sharing link button is clicked', async () => {
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    await wrapper.find('.btn-link').trigger('click')
    await flushPromises()

    expect(mockPatch).toHaveBeenCalledWith('/journal/3/share')
  })

  it('shows error banner when fetch fails', async () => {
    // Spy on fetchEntry to set error state without re-throwing
    const store = useJournalStore()
    const spy = vi.spyOn(store, 'fetchEntry').mockImplementation(async () => {
      store.error = 'Failed to load journal entry'
      return undefined as never
    })
    const wrapper = mount(JournalDetailView)
    await flushPromises()
    spy.mockRestore()

    expect(wrapper.find('.error-banner').exists()).toBe(true)
    expect(wrapper.text()).toContain('Failed to load journal entry')
  })

  it('clears error when dismiss button is clicked', async () => {
    // Spy on fetchEntry to set error state without re-throwing
    const store = useJournalStore()
    const spy = vi.spyOn(store, 'fetchEntry').mockImplementation(async () => {
      store.error = 'Failed to load journal entry'
      return undefined as never
    })
    const wrapper = mount(JournalDetailView)
    await flushPromises()
    spy.mockRestore()

    expect(wrapper.find('.error-banner').exists()).toBe(true)
    await wrapper.find('.error-dismiss').trigger('click')
    expect(wrapper.find('.error-banner').exists()).toBe(false)
  })

  it('navigates back to journal list when back button is clicked', async () => {
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    await wrapper.find('.btn-back').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'journal' })
  })

  it('navigates to edit page when Edit button is clicked', async () => {
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    await wrapper.find('.entry-actions .btn-secondary').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'journal-edit', params: { id: 3 } })
  })

  it('opens delete confirmation modal when Delete is clicked', async () => {
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    expect(wrapper.find('.modal-overlay').exists()).toBe(false)
    await wrapper.find('.entry-actions .btn-danger').trigger('click')
    expect(wrapper.find('.modal-overlay').exists()).toBe(true)
    expect(wrapper.text()).toContain('Delete Entry')
  })

  it('closes delete modal when Cancel is clicked', async () => {
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    await wrapper.find('.entry-actions .btn-danger').trigger('click')
    expect(wrapper.find('.modal-overlay').exists()).toBe(true)

    await wrapper.find('.modal-actions .btn-secondary').trigger('click')
    expect(wrapper.find('.modal-overlay').exists()).toBe(false)
  })

  it('deletes entry and navigates to journal list', async () => {
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    await wrapper.find('.entry-actions .btn-danger').trigger('click')
    await wrapper.find('.modal-actions .btn-danger').trigger('click')
    await flushPromises()

    expect(mockDelete).toHaveBeenCalledWith('/journal/3')
    expect(mockPush).toHaveBeenCalledWith({ name: 'journal' })
  })

  it('hides title when entry has no title', async () => {
    mockGet.mockResolvedValue({ data: { ...sampleEntry, title: null } })
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    expect(wrapper.find('.entry-title').exists()).toBe(false)
  })

  it('hides content section when entry has no content', async () => {
    mockGet.mockResolvedValue({ data: { ...sampleEntry, content: null } })
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    expect(wrapper.find('.entry-content').exists()).toBe(false)
  })

  it('hides tags section when entry has no tags', async () => {
    mockGet.mockResolvedValue({ data: { ...sampleEntry, tags: [] } })
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    expect(wrapper.find('.entry-tags').exists()).toBe(false)
  })

  it('hides mood when entry has no mood', async () => {
    mockGet.mockResolvedValue({ data: { ...sampleEntry, mood: null } })
    const wrapper = mount(JournalDetailView)
    await flushPromises()

    expect(wrapper.find('.entry-mood').exists()).toBe(false)
  })
})
