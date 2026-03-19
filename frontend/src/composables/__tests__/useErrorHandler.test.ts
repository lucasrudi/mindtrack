import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { useErrorHandler } from '../useErrorHandler'

describe('useErrorHandler', () => {
  beforeEach(() => {
    const { clearAll } = useErrorHandler()
    clearAll()
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('adds an error', () => {
    const { globalErrors, addError } = useErrorHandler()
    addError('Something went wrong')
    expect(globalErrors.value).toHaveLength(1)
    expect(globalErrors.value[0].message).toBe('Something went wrong')
    expect(globalErrors.value[0].type).toBe('error')
  })

  it('auto-dismisses after 8 seconds', () => {
    const { globalErrors, addError } = useErrorHandler()
    addError('Temporary error')
    expect(globalErrors.value).toHaveLength(1)
    vi.advanceTimersByTime(8000)
    expect(globalErrors.value).toHaveLength(0)
  })

  it('dismisses a specific error by id', () => {
    const { globalErrors, addError, dismissError } = useErrorHandler()
    addError('Error 1')
    addError('Error 2')
    const id = globalErrors.value[0].id
    dismissError(id)
    expect(globalErrors.value).toHaveLength(1)
    expect(globalErrors.value[0].message).toBe('Error 2')
  })

  it('clears all errors', () => {
    const { globalErrors, addError, clearAll } = useErrorHandler()
    addError('Error 1')
    addError('Error 2')
    clearAll()
    expect(globalErrors.value).toHaveLength(0)
  })

  it('supports warning type', () => {
    const { globalErrors, addError } = useErrorHandler()
    addError('Warning', 'warning')
    expect(globalErrors.value[0].type).toBe('warning')
  })
})
