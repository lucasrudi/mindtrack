import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import TutorialOverlay from '../TutorialOverlay.vue'
import { useTutorial } from '@/composables/useTutorial'

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn().mockResolvedValue({ data: {} }),
    delete: vi.fn(),
  },
}))

describe('TutorialOverlay', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    // Reset tutorial state
    const { isActive, currentStepIndex } = useTutorial()
    isActive.value = false
    currentStepIndex.value = 0
  })

  function mountOverlay() {
    return mount(TutorialOverlay, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })
  }

  it('does not render when tutorial is inactive', () => {
    const wrapper = mountOverlay()
    expect(wrapper.find('[data-testid="tutorial-overlay"]').exists()).toBe(false)
  })

  it('renders when tutorial is active', () => {
    const { start } = useTutorial()
    start()
    const wrapper = mountOverlay()
    expect(wrapper.find('[data-testid="tutorial-overlay"]').exists()).toBe(true)
  })

  it('shows the first step title', () => {
    const { start } = useTutorial()
    start()
    const wrapper = mountOverlay()
    expect(wrapper.text()).toContain('Welcome to MindTrack!')
  })

  it('shows step counter', () => {
    const { start } = useTutorial()
    start()
    const wrapper = mountOverlay()
    expect(wrapper.text()).toContain('1 / 6')
  })

  it('advances to next step on Next click', async () => {
    const { start } = useTutorial()
    start()
    const wrapper = mountOverlay()

    await wrapper.find('[data-testid="tutorial-next"]').trigger('click')

    expect(wrapper.text()).toContain('2 / 6')
    expect(wrapper.text()).toContain('Journal')
  })

  it('shows Back button after first step', async () => {
    const { start } = useTutorial()
    start()
    const wrapper = mountOverlay()

    expect(wrapper.find('[data-testid="tutorial-prev"]').exists()).toBe(false)

    await wrapper.find('[data-testid="tutorial-next"]').trigger('click')

    expect(wrapper.find('[data-testid="tutorial-prev"]').exists()).toBe(true)
  })

  it('shows Get Started on last step', async () => {
    const { start, currentStepIndex, steps } = useTutorial()
    start()
    currentStepIndex.value = steps.length - 1
    const wrapper = mountOverlay()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('[data-testid="tutorial-next"]').text()).toBe('Get Started')
  })
})
