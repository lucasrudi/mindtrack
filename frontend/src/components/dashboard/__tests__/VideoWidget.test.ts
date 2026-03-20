import { afterEach, describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import VideoWidget from '../VideoWidget.vue'
import type { ContentItem } from '@/stores/analytics'

const mockVideo: ContentItem = {
  type: 'VIDEO',
  title: 'Why We All Need to Practice Emotional First Aid',
  body: 'Guy Winch makes a compelling case for practising emotional hygiene.',
  category: 'Mental Health',
  url: 'F2hc2FLOdhI',
  sourceType: 'YOUTUBE',
  sourceLabel: 'YouTube',
}

const anotherVideo: ContentItem = {
  type: 'VIDEO',
  title: 'How to Meditate',
  body: 'Headspace guides you through a simple meditation practice.',
  category: 'Mental Health',
  url: 'wuh5B3_y8nY',
  sourceType: 'YOUTUBE',
  sourceLabel: 'YouTube',
}

describe('VideoWidget', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders the widget title', () => {
    const wrapper = mount(VideoWidget, { props: { items: [] } })
    expect(wrapper.find('.widget-title').text()).toBe('Health Videos')
  })

  it('renders iframe when items are provided', () => {
    const wrapper = mount(VideoWidget, { props: { items: [mockVideo] } })
    expect(wrapper.find('[data-testid="video-card"]').exists()).toBe(true)
    const iframe = wrapper.find('[data-testid="video-iframe"]')
    expect(iframe.exists()).toBe(true)
    expect(iframe.attributes('src')).toBe('https://www.youtube-nocookie.com/embed/F2hc2FLOdhI')
  })

  it('shows empty state when items is empty', () => {
    const wrapper = mount(VideoWidget, { props: { items: [] } })
    expect(wrapper.find('[data-testid="video-card"]').exists()).toBe(false)
    expect(wrapper.find('.video-empty').exists()).toBe(true)
  })

  it('shows title and category from selected item', () => {
    const wrapper = mount(VideoWidget, { props: { items: [mockVideo] } })
    expect(wrapper.find('.video-title').text()).toBe(
      'Why We All Need to Practice Emotional First Aid',
    )
    expect(wrapper.find('.video-category').text()).toBe('Mental Health')
  })

  it('shows nav controls and counter when multiple items are provided', () => {
    const wrapper = mount(VideoWidget, { props: { items: [mockVideo, anotherVideo] } })
    expect(wrapper.find('.video-nav').exists()).toBe(true)
    expect(wrapper.find('.video-counter').text()).toMatch(/Video \d+ of 2/)
  })

  it('does not show nav controls when only one item is provided', () => {
    const wrapper = mount(VideoWidget, { props: { items: [mockVideo] } })
    expect(wrapper.find('.video-nav').exists()).toBe(false)
  })

  it('uses the browser crypto API to choose the initial video', () => {
    const getRandomValuesSpy = vi
      .spyOn(globalThis.crypto, 'getRandomValues')
      .mockImplementation((array) => {
        array[0] = 1
        return array
      })

    const wrapper = mount(VideoWidget, { props: { items: [mockVideo, anotherVideo] } })

    expect(getRandomValuesSpy).toHaveBeenCalled()
    expect(wrapper.find('.video-title').text()).toBe('How to Meditate')
  })
})
