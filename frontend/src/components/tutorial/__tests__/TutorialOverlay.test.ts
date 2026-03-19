import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
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

function mountOverlay() {
  return mount(TutorialOverlay, {
    global: {
      stubs: {
        Teleport: true,
      },
    },
  })
}

describe('TutorialOverlay', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    // Reset tutorial state
    const { isActive, currentStepIndex } = useTutorial()
    isActive.value = false
    currentStepIndex.value = 0
  })

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

  it('skips the tutorial when the backdrop is clicked', async () => {
    const { start, isActive } = useTutorial()
    start()
    const wrapper = mountOverlay()

    await wrapper.find('.tutorial-backdrop').trigger('click')
    await flushPromises()

    expect(isActive.value).toBe(false)
  })

  it('forwards first-click navigation to links under the backdrop', async () => {
    const { start, isActive } = useTutorial()
    start()
    const wrapper = mountOverlay()

    const link = document.createElement('a')
    link.href = '/journal'
    const clickSpy = vi.fn()
    link.click = clickSpy

    const originalElementFromPoint = document.elementFromPoint
    const elementFromPointMock = vi.fn().mockReturnValue(link)
    Object.defineProperty(document, 'elementFromPoint', {
      configurable: true,
      value: elementFromPointMock,
    })
    const rafSpy = vi
      .spyOn(window, 'requestAnimationFrame')
      .mockImplementation((callback: FrameRequestCallback) => {
        callback(0)
        return 0
      })

    await wrapper.find('.tutorial-backdrop').trigger('click', { clientX: 10, clientY: 10 })
    await flushPromises()

    expect(isActive.value).toBe(false)
    expect(clickSpy).toHaveBeenCalled()

    Object.defineProperty(document, 'elementFromPoint', {
      configurable: true,
      value: originalElementFromPoint,
    })
    rafSpy.mockRestore()
  })

  it('centers the tooltip when the target element is missing', async () => {
    const { start } = useTutorial()
    start()
    const wrapper = mountOverlay()
    await flushPromises()
    await wrapper.find('[data-testid="tutorial-next"]').trigger('click')
    await flushPromises()

    const element = wrapper.find('[data-testid="tutorial-tooltip"]').element as HTMLElement
    expect(element.style.top).toBe('50%')
    expect(element.style.left).toBe('50%')
  })
})
