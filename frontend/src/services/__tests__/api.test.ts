import { beforeEach, describe, expect, it, vi } from 'vitest'

const postMock = vi.fn()
const apiCallMock = vi.fn()
const pushMock = vi.fn()
const logoutMock = vi.fn()
const authStore = { logout: logoutMock }
let successHandler: (response: unknown) => unknown
let errorHandler: (error: {
  config: { _retry?: boolean; headers: Record<string, string> }
  response?: { status?: number }
}) => Promise<unknown>

vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => {
      const instance = Object.assign(apiCallMock, {
        post: postMock,
        interceptors: {
          response: {
            use: (success: typeof successHandler, error: typeof errorHandler) => {
              successHandler = success
              errorHandler = error
            },
          },
        },
      })
      return instance
    }),
  },
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => authStore,
}))

vi.mock('@/router', () => ({
  default: {
    push: pushMock,
  },
}))

describe('api service', () => {
  beforeEach(() => {
    vi.resetModules()
    vi.clearAllMocks()
    Object.defineProperty(globalThis, 'localStorage', {
      value: {
        getItem: vi.fn(() => null),
        setItem: vi.fn(),
        removeItem: vi.fn(),
      },
      configurable: true,
    })
  })

  it('returns successful responses unchanged', async () => {
    const { default: api } = await import('../api')
    const response = { data: { ok: true } }

    expect(successHandler(response)).toBe(response)
    expect(api).toBeDefined()
  })

  it('refreshes tokens and retries the original request on 401', async () => {
    postMock.mockResolvedValueOnce({ data: { token: 'jwt-new', refreshToken: 'refresh-new' } })
    apiCallMock.mockResolvedValueOnce({ data: { retried: true } })
    const storage = globalThis.localStorage as unknown as {
      getItem: ReturnType<typeof vi.fn>
      setItem: ReturnType<typeof vi.fn>
    }
    storage.getItem.mockReturnValue('refresh-old')

    await import('../api')

    const originalRequest = { _retry: false, headers: {} as Record<string, string> }
    const result = await errorHandler({
      config: originalRequest,
      response: { status: 401 },
    })

    expect(postMock).toHaveBeenCalledWith('/auth/refresh', { refreshToken: 'refresh-old' })
    expect(storage.setItem).toHaveBeenCalledWith('refreshToken', 'refresh-new')
    expect(originalRequest.headers.Authorization).toBe('Bearer jwt-new')
    expect(result).toEqual({ data: { retried: true } })
  })

  it('logs out and redirects when refresh fails', async () => {
    postMock.mockRejectedValueOnce(new Error('refresh failed'))
    const storage = globalThis.localStorage as unknown as {
      getItem: ReturnType<typeof vi.fn>
      removeItem: ReturnType<typeof vi.fn>
    }
    storage.getItem.mockReturnValue('refresh-old')

    await import('../api')

    let thrown: unknown
    try {
      await errorHandler({
        config: { _retry: false, headers: {} },
        response: { status: 401 },
      })
    } catch (error) {
      thrown = error
    }

    expect(thrown).toEqual(
      expect.objectContaining({
        response: { status: 401 },
      }),
    )
    expect(storage.removeItem).toHaveBeenCalledWith('refreshToken')
    expect(logoutMock).toHaveBeenCalled()
    expect(pushMock).toHaveBeenCalledWith('/')
  })
})
