import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import DailyTipWidget from '../DailyTipWidget.vue'
import type { ContentItem } from '@/stores/analytics'

const mockTip: ContentItem = {
  type: 'TIP',
  title: 'Practice Mindfulness',
  body: 'Spend 5 minutes focusing on your breath.',
  category: 'Mental Health',
  url: null,
  sourceType: null,
  sourceLabel: null,
}

describe('DailyTipWidget', () => {
  it('renders the widget title', () => {
    const wrapper = mount(DailyTipWidget, { props: { tip: null } })
    expect(wrapper.find('.widget-title').text()).toBe('Daily Tip')
  })

  it('renders the tip when provided', () => {
    const wrapper = mount(DailyTipWidget, { props: { tip: mockTip } })
    expect(wrapper.find('[data-testid="tip-card"]').exists()).toBe(true)
    expect(wrapper.find('.tip-heading').text()).toBe('Practice Mindfulness')
    expect(wrapper.find('.tip-body').text()).toBe('Spend 5 minutes focusing on your breath.')
    expect(wrapper.find('.tip-category').text()).toBe('Mental Health')
  })

  it('renders empty message when tip is null', () => {
    const wrapper = mount(DailyTipWidget, { props: { tip: null } })
    expect(wrapper.find('[data-testid="tip-card"]').exists()).toBe(false)
    expect(wrapper.find('.tip-empty').exists()).toBe(true)
  })
})
