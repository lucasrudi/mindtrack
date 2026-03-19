import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ResourcesWidget from '../ResourcesWidget.vue'
import type { ContentItem } from '@/stores/analytics'

const mockItems: ContentItem[] = [
  {
    type: 'RESOURCE',
    title: 'Understanding CBT',
    body: 'A gentle introduction to CBT techniques.',
    category: 'Mental Health',
    url: 'https://example.com/cbt',
    sourceType: 'YOUTUBE',
    sourceLabel: 'YouTube',
  },
  {
    type: 'THERAPIST_TIP',
    title: 'Journaling Prompt',
    body: 'Write about a recent calm moment.',
    category: 'Mental Health',
    url: null,
    sourceType: null,
    sourceLabel: null,
  },
]

describe('ResourcesWidget', () => {
  it('renders the widget title', () => {
    const wrapper = mount(ResourcesWidget, { props: { items: [] } })
    expect(wrapper.find('.widget-title').text()).toBe('Resources')
  })

  it('renders resource cards when items are provided', () => {
    const wrapper = mount(ResourcesWidget, { props: { items: mockItems } })
    const cards = wrapper.findAll('[data-testid="resource-card"]')
    expect(cards).toHaveLength(2)
  })

  it('shows a YouTube icon for YOUTUBE source type', () => {
    const wrapper = mount(ResourcesWidget, { props: { items: [mockItems[0]] } })
    expect(wrapper.find('.resource-icon').text()).toBe('📺')
  })

  it('renders a link when url is provided', () => {
    const wrapper = mount(ResourcesWidget, { props: { items: [mockItems[0]] } })
    const link = wrapper.find('.resource-link')
    expect(link.exists()).toBe(true)
    expect(link.attributes('href')).toBe('https://example.com/cbt')
  })

  it('renders empty message when no items', () => {
    const wrapper = mount(ResourcesWidget, { props: { items: [] } })
    expect(wrapper.find('.resources-empty').exists()).toBe(true)
    expect(wrapper.find('[data-testid="resource-card"]').exists()).toBe(false)
  })
})
