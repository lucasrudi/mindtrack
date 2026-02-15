import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import HeroSection from '../HeroSection.vue'

describe('HeroSection', () => {
  it('renders the hero badge', () => {
    const wrapper = mount(HeroSection, {
      props: { onSignIn: vi.fn() },
    })
    expect(wrapper.find('.hero-badge').text()).toBe('Your mental health companion')
  })

  it('renders the hero title', () => {
    const wrapper = mount(HeroSection, {
      props: { onSignIn: vi.fn() },
    })
    const title = wrapper.find('.hero-title')
    expect(title.text()).toContain('Track your wellbeing.')
    expect(title.text()).toContain('Grow with AI coaching.')
  })

  it('renders the description', () => {
    const wrapper = mount(HeroSection, {
      props: { onSignIn: vi.fn() },
    })
    const description = wrapper.find('.hero-description')
    expect(description.text()).toContain('MindTrack helps you')
  })

  it('renders sign in button with Google text', () => {
    const wrapper = mount(HeroSection, {
      props: { onSignIn: vi.fn() },
    })
    const button = wrapper.find('.btn-primary')
    expect(button.text()).toContain('Sign in with Google')
  })

  it('calls onSignIn when button is clicked', async () => {
    const onSignIn = vi.fn()
    const wrapper = mount(HeroSection, {
      props: { onSignIn },
    })
    await wrapper.find('.btn-primary').trigger('click')
    expect(onSignIn).toHaveBeenCalledOnce()
  })

  it('renders Google SVG icon', () => {
    const wrapper = mount(HeroSection, {
      props: { onSignIn: vi.fn() },
    })
    expect(wrapper.find('.google-icon').exists()).toBe(true)
  })
})
