import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useTutorial } from '../useTutorial'

const mockPut = vi.fn()

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: (...args: unknown[]) => mockPut(...args),
    delete: vi.fn(),
  },
}))

describe('useTutorial', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    // Reset shared state
    const { isActive, currentStepIndex } = useTutorial()
    isActive.value = false
    currentStepIndex.value = 0
  })

  it('initializes as inactive', () => {
    const { isActive, currentStep } = useTutorial()
    expect(isActive.value).toBe(false)
    expect(currentStep.value).toBeNull()
  })

  it('starts the tutorial', () => {
    const { isActive, currentStep, start } = useTutorial()
    start()
    expect(isActive.value).toBe(true)
    expect(currentStep.value).toBeDefined()
    expect(currentStep.value?.title).toBe('Welcome to MindTrack!')
  })

  it('advances to next step', () => {
    const { currentStepIndex, start, next } = useTutorial()
    start()
    next()
    expect(currentStepIndex.value).toBe(1)
  })

  it('goes back to previous step', () => {
    const { currentStepIndex, start, next, previous } = useTutorial()
    start()
    next()
    next()
    previous()
    expect(currentStepIndex.value).toBe(1)
  })

  it('does not go below step 0', () => {
    const { currentStepIndex, start, previous } = useTutorial()
    start()
    previous()
    expect(currentStepIndex.value).toBe(0)
  })

  it('has 6 tutorial steps', () => {
    const { steps } = useTutorial()
    expect(steps).toHaveLength(6)
  })

  it('detects last step', () => {
    const { isLastStep, currentStepIndex, start } = useTutorial()
    start()
    expect(isLastStep.value).toBe(false)
    currentStepIndex.value = 5
    expect(isLastStep.value).toBe(true)
  })

  it('completes tutorial and updates profile', async () => {
    mockPut.mockResolvedValue({ data: { tutorialCompleted: true } })
    const { isActive, start, complete } = useTutorial()
    start()

    await complete()

    expect(isActive.value).toBe(false)
    expect(mockPut).toHaveBeenCalledWith(
      '/profile',
      expect.objectContaining({ tutorialCompleted: true }),
    )
  })

  it('skips tutorial and updates profile', async () => {
    mockPut.mockResolvedValue({ data: { tutorialCompleted: true } })
    const { isActive, start, skip } = useTutorial()
    start()

    await skip()

    expect(isActive.value).toBe(false)
    expect(mockPut).toHaveBeenCalledWith(
      '/profile',
      expect.objectContaining({ tutorialCompleted: true }),
    )
  })
})
