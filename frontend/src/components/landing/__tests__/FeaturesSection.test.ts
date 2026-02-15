import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import FeaturesSection from '../FeaturesSection.vue'

describe('FeaturesSection', () => {
  it('renders the section heading', () => {
    const wrapper = mount(FeaturesSection)
    expect(wrapper.find('.features-heading').text()).toBe('Everything you need to thrive')
  })

  it('renders the subheading', () => {
    const wrapper = mount(FeaturesSection)
    expect(wrapper.find('.features-subheading').text()).toContain('comprehensive toolkit')
  })

  it('renders exactly 6 feature cards', () => {
    const wrapper = mount(FeaturesSection)
    const cards = wrapper.findAll('.feature-card')
    expect(cards).toHaveLength(6)
  })

  it('renders all expected feature titles', () => {
    const wrapper = mount(FeaturesSection)
    const titles = wrapper.findAll('.feature-title').map((el) => el.text())
    expect(titles).toContain('Interview Tracking')
    expect(titles).toContain('Activity Monitoring')
    expect(titles).toContain('Journaling')
    expect(titles).toContain('Goal Setting')
    expect(titles).toContain('AI Coaching')
    expect(titles).toContain('Messaging Integration')
  })

  it('has a features-grid container', () => {
    const wrapper = mount(FeaturesSection)
    expect(wrapper.find('.features-grid').exists()).toBe(true)
  })
})
