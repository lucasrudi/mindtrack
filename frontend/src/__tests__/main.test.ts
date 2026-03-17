import { describe, it, expect, beforeEach, vi } from 'vitest'

const appUse = vi.fn()
const appMount = vi.fn()
const createAppMock = vi.fn(() => ({
  use: appUse,
  mount: appMount,
}))
const createPiniaMock = vi.fn(() => ({ pinia: true }))
const sentryInit = vi.fn()
const browserTracingIntegration = vi.fn(() => ({ name: 'browserTracing' }))
const createGtagMock = vi.fn((config) => ({ plugin: 'gtag', config }))
const routerMock = { name: 'router' }

vi.mock('vue', () => ({
  createApp: createAppMock,
}))

vi.mock('pinia', () => ({
  createPinia: createPiniaMock,
}))

vi.mock('@sentry/vue', () => ({
  init: sentryInit,
  browserTracingIntegration,
}))

vi.mock('vue-gtag', () => ({
  createGtag: createGtagMock,
}))

vi.mock('../App.vue', () => ({
  default: { name: 'AppStub' },
}))

vi.mock('../router', () => ({
  default: routerMock,
}))

describe('main bootstrap', () => {
  beforeEach(() => {
    vi.resetModules()
    vi.unstubAllEnvs()
    createAppMock.mockClear()
    createPiniaMock.mockClear()
    appUse.mockClear()
    appMount.mockClear()
    sentryInit.mockClear()
    browserTracingIntegration.mockClear()
    createGtagMock.mockClear()
  })

  it('boots the app without Sentry or Google Analytics by default', async () => {
    await import('../main')

    expect(createAppMock).toHaveBeenCalled()
    expect(appUse).toHaveBeenCalledWith({ pinia: true })
    expect(appUse).toHaveBeenCalledWith(routerMock)
    expect(appMount).toHaveBeenCalledWith('#app')
    expect(sentryInit).not.toHaveBeenCalled()
    expect(createGtagMock).not.toHaveBeenCalled()
  })

  it('initializes Sentry and Google Analytics when env vars are set', async () => {
    vi.stubEnv('VITE_SENTRY_DSN', 'https://dsn.example')
    vi.stubEnv('VITE_SENTRY_RELEASE', 'frontend@1.2.3')
    vi.stubEnv('VITE_SENTRY_TRACES_SAMPLE_RATE', '0.25')
    vi.stubEnv('VITE_APP_ENV', 'production')
    vi.stubEnv('VITE_GA_MEASUREMENT_ID', 'G-12345')

    await import('../main')

    expect(browserTracingIntegration).toHaveBeenCalledWith({ router: routerMock })
    expect(sentryInit).toHaveBeenCalledTimes(1)
    const sentryConfig = sentryInit.mock.calls[0][0] as {
      beforeBreadcrumb: (breadcrumb: {
        category: string
        data?: { to?: string; from?: string }
      }) => { data?: { to?: string; from?: string } }
    }
    const breadcrumb = sentryConfig.beforeBreadcrumb({
      category: 'navigation',
      data: { to: '/goals/12', from: '/journal/9' },
    })
    expect(breadcrumb.data?.to).toBe('/goals/[id]')
    expect(breadcrumb.data?.from).toBe('/journal/[id]')

    expect(createGtagMock).toHaveBeenCalledTimes(1)
    const gtagConfig = createGtagMock.mock.calls[0][0] as {
      pageTracker: {
        template: (to: { name?: string; path: string; matched: Array<{ path: string }> }) => unknown
      }
    }
    expect(
      gtagConfig.pageTracker.template({
        name: 'goal-detail',
        path: '/goals/42',
        matched: [{ path: '/goals/:id' }],
      }),
    ).toEqual({
      page_title: 'goal-detail',
      page_path: '/goals/:id',
    })
  })
})
