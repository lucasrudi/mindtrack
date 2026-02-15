import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import JournalView from '../JournalView.vue'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
}))

const mockGet = vi.fn().mockResolvedValue({ data: [] })
vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
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
  content: 'Today was productive and I felt great about completing my tasks.',
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

describe('JournalView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockClear()
    mockGet.mockReset().mockResolvedValue({ data: [] })
  })

  it('renders page header', () => {
    const wrapper = mount(JournalView)
    expect(wrapper.find('h1').text()).toBe('Journal')
    expect(wrapper.find('.subtitle').text()).toContain('Reflect')
  })

  it('renders new entry button', () => {
    const wrapper = mount(JournalView)
    const btn = wrapper.find('.btn-primary')
    expect(btn.exists()).toBe(true)
    expect(btn.text()).toContain('New Entry')
  })

  it('shows empty state when no entries', async () => {
    const wrapper = mount(JournalView)
    await flushPromises()
    expect(wrapper.find('.empty-state').exists()).toBe(true)
    expect(wrapper.text()).toContain('No journal entries yet')
  })

  it('navigates to new entry form', async () => {
    const wrapper = mount(JournalView)
    await wrapper.find('.page-header .btn-primary').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'journal-new' })
  })

  it('renders entry cards when data loaded', async () => {
    mockGet.mockResolvedValue({ data: [sampleEntry, sampleEntry2] })

    const wrapper = mount(JournalView)
    await flushPromises()

    const cards = wrapper.findAll('.entry-card')
    expect(cards).toHaveLength(2)
    expect(wrapper.text()).toContain('Good day')
    expect(wrapper.text()).toContain('Tough day')
  })

  it('displays mood on entry cards', async () => {
    mockGet.mockResolvedValue({ data: [sampleEntry] })

    const wrapper = mount(JournalView)
    await flushPromises()

    expect(wrapper.find('.entry-mood').text()).toContain('7/10')
  })

  it('displays tags on entry cards', async () => {
    mockGet.mockResolvedValue({ data: [sampleEntry] })

    const wrapper = mount(JournalView)
    await flushPromises()

    const tags = wrapper.findAll('.tag')
    expect(tags).toHaveLength(2)
    expect(tags[0].text()).toBe('gratitude')
    expect(tags[1].text()).toBe('work')
  })

  it('shows shared badge for shared entries', async () => {
    mockGet.mockResolvedValue({ data: [sampleEntry2] })

    const wrapper = mount(JournalView)
    await flushPromises()

    expect(wrapper.find('.shared-badge').exists()).toBe(true)
    expect(wrapper.find('.shared-badge').text()).toBe('Shared')
  })

  it('navigates to entry detail on card click', async () => {
    mockGet.mockResolvedValue({ data: [sampleEntry] })

    const wrapper = mount(JournalView)
    await flushPromises()

    await wrapper.find('.entry-card').trigger('click')
    expect(mockPush).toHaveBeenCalledWith({ name: 'journal-detail', params: { id: 1 } })
  })
})
