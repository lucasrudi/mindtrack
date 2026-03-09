import { createApp } from 'vue'
import { createPinia } from 'pinia'
import * as Sentry from '@sentry/vue'
import { createGtag } from 'vue-gtag'
import type { RouteLocationNormalizedGeneric } from 'vue-router'

import App from './App.vue'
import router from './router'
import './assets/styles/global.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

const sentryDsn = import.meta.env.VITE_SENTRY_DSN

if (sentryDsn) {
  Sentry.init({
    app,
    dsn: sentryDsn,
    environment: import.meta.env.VITE_APP_ENV ?? 'local',
    integrations: [Sentry.browserTracingIntegration({ router })],
    tracesSampleRate: Number(import.meta.env.VITE_SENTRY_TRACES_SAMPLE_RATE ?? '0.1'),
    sendDefaultPii: false,
    // Redact numeric IDs from navigation breadcrumbs
    beforeBreadcrumb(breadcrumb) {
      if (breadcrumb.category === 'navigation') {
        if (breadcrumb.data?.to) {
          breadcrumb.data.to = redactPath(breadcrumb.data.to as string)
        }
        if (breadcrumb.data?.from) {
          breadcrumb.data.from = redactPath(breadcrumb.data.from as string)
        }
      }
      return breadcrumb
    },
  })
}

const gaMeasurementId = import.meta.env.VITE_GA_MEASUREMENT_ID

if (gaMeasurementId) {
  app.use(
    createGtag({
      tagId: gaMeasurementId,
      pageTracker: {
        router,
        // Send route pattern + name, never full URL with resource IDs
        template(to: RouteLocationNormalizedGeneric) {
          return {
            page_title: String(to.name ?? to.path),
            page_path: to.matched[0]?.path ?? to.path,
          }
        },
      },
    }),
  )
}

app.mount('#app')

function redactPath(path: string): string {
  return path.replaceAll(/\/\d+/g, '/[id]')
}
