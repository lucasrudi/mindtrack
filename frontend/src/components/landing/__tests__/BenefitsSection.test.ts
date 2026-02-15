import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BenefitsSection from '../BenefitsSection.vue'

describe('BenefitsSection', () => {
  it('renders the heading', () => {
    const wrapper = mount(BenefitsSection)
    expect(wrapper.find('.benefits-heading').text()).toBe('Why MindTrack?')
  })

  it('renders 4 benefit items', () => {
    const wrapper = mount(BenefitsSection)
    const items = wrapper.findAll('.benefit-item')
    expect(items).toHaveLength(4)
  })

  it('renders all benefit titles', () => {
    const wrapper = mount(BenefitsSection)
    const titles = wrapper.findAll('.benefit-item strong').map((el) => el.text())
    expect(titles).toContain('Private & secure')
    expect(titles).toContain('Therapist-friendly')
    expect(titles).toContain('Evidence-based insights')
    expect(titles).toContain('Always accessible')
  })

  it('renders the decorative progress card', () => {
    const wrapper = mount(BenefitsSection)
    expect(wrapper.find('.benefits-card').exists()).toBe(true)
    expect(wrapper.find('.benefits-card-number').text()).toBe('87%')
  })

  it('renders check marks for each benefit', () => {
    const wrapper = mount(BenefitsSection)
    const checks = wrapper.findAll('.benefit-check')
    expect(checks).toHaveLength(4)
  })
})
