import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import FeatureCard from '../FeatureCard.vue'

describe('FeatureCard', () => {
  const defaultProps = {
    icon: '📋',
    title: 'Test Feature',
    description: 'A test feature description',
  }

  it('renders the icon', () => {
    const wrapper = mount(FeatureCard, { props: defaultProps })
    const icon = wrapper.find('.feature-icon')
    expect(icon.text()).toBe('📋')
  })

  it('renders the title', () => {
    const wrapper = mount(FeatureCard, { props: defaultProps })
    const title = wrapper.find('.feature-title')
    expect(title.text()).toBe('Test Feature')
  })

  it('renders the description', () => {
    const wrapper = mount(FeatureCard, { props: defaultProps })
    const description = wrapper.find('.feature-description')
    expect(description.text()).toBe('A test feature description')
  })

  it('sets aria-label on the icon span', () => {
    const wrapper = mount(FeatureCard, { props: defaultProps })
    const icon = wrapper.find('.feature-icon')
    expect(icon.attributes('aria-label')).toBe('Test Feature')
  })

  it('has the feature-card class on root element', () => {
    const wrapper = mount(FeatureCard, { props: defaultProps })
    expect(wrapper.find('.feature-card').exists()).toBe(true)
  })
})
