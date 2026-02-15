import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import FooterSection from '../FooterSection.vue'

describe('FooterSection', () => {
  it('renders the brand name', () => {
    const wrapper = mount(FooterSection)
    expect(wrapper.find('.footer-name').text()).toBe('MindTrack')
  })

  it('renders the logo letter', () => {
    const wrapper = mount(FooterSection)
    expect(wrapper.find('.footer-logo').text()).toBe('M')
  })

  it('renders the tagline', () => {
    const wrapper = mount(FooterSection)
    expect(wrapper.find('.footer-text').text()).toContain('mental health companion')
  })

  it('renders copyright with current year', () => {
    const wrapper = mount(FooterSection)
    const year = new Date().getFullYear().toString()
    expect(wrapper.find('.footer-copyright').text()).toContain(year)
    expect(wrapper.find('.footer-copyright').text()).toContain('MindTrack')
  })
})
