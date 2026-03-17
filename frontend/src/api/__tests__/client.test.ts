import { beforeEach, describe, expect, it, vi } from 'vitest'

let successHandler: (response: unknown) => unknown
let errorHandler: (error: { response?: { status?: number } }) => unknown

vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      interceptors: {
        response: {
          use: (success: typeof successHandler, error: typeof errorHandler) => {
            successHandler = success
            errorHandler = error
          },
        },
      },
    })),
  },
}))

describe('api client', () => {
  beforeEach(() => {
    vi.resetModules()
    Object.defineProperty(globalThis, 'location', {
      value: { href: 'http://localhost/' },
      configurable: true,
    })
  })

  it('returns successful responses unchanged', async () => {
    await import('../client')
    const response = { data: { ok: true } }

    expect(successHandler(response)).toBe(response)
  })

  it('redirects to login on unauthorized errors', async () => {
    await import('../client')
    const error = { response: { status: 401 } }

    expect(() => errorHandler(error)).toThrow()
    expect(globalThis.location.href).toBe('/login')
  })

  it('does not redirect on non-401 errors', async () => {
    await import('../client')
    const error = { response: { status: 500 } }

    expect(() => errorHandler(error)).toThrow()
    expect(globalThis.location.href).toBe('http://localhost/')
  })
})
