import { createRouter, createWebHistory } from 'vue-router'
import type { RouteMeta, RouteRecordSingleView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProfileStore } from '@/stores/profile'

const publicRoute = (
  path: string,
  name: string,
  component: RouteRecordSingleView['component'],
): RouteRecordSingleView => ({
  path,
  name,
  component,
  meta: { requiresAuth: false },
})

const protectedRoute = (
  path: string,
  name: string,
  component: RouteRecordSingleView['component'],
  meta: RouteMeta = {},
): RouteRecordSingleView => ({
  path,
  name,
  component,
  meta: { requiresAuth: true, ...meta },
})

function shouldRedirectToDashboard(
  requiresAdmin: unknown,
  isAdmin: boolean,
  requiresTherapist: unknown,
  isTherapist: boolean,
): boolean {
  return (Boolean(requiresAdmin) && !isAdmin) || (Boolean(requiresTherapist) && !isTherapist)
}

function shouldRedirectToOnboarding(
  requiresAuth: unknown,
  isAuthenticated: boolean,
  routeName: string | symbol | null | undefined,
  onboardingCompleted: boolean | undefined,
): boolean {
  return (
    Boolean(requiresAuth) &&
    isAuthenticated &&
    routeName !== 'onboarding' &&
    onboardingCompleted === false
  )
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    publicRoute('/', 'landing', () => import('@/views/LandingView.vue')),
    publicRoute('/login', 'login', () => import('@/views/LoginView.vue')),
    protectedRoute('/dashboard', 'dashboard', () => import('@/views/DashboardView.vue')),
    protectedRoute('/interviews', 'interviews', () => import('@/views/InterviewsView.vue')),
    protectedRoute(
      '/interviews/new',
      'interview-new',
      () => import('@/views/InterviewFormView.vue'),
    ),
    protectedRoute(
      '/interviews/:id',
      'interview-detail',
      () => import('@/views/InterviewDetailView.vue'),
    ),
    protectedRoute(
      '/interviews/:id/edit',
      'interview-edit',
      () => import('@/views/InterviewFormView.vue'),
    ),
    protectedRoute('/activities', 'activities', () => import('@/views/ActivitiesView.vue')),
    protectedRoute('/activities/new', 'activity-new', () => import('@/views/ActivityFormView.vue')),
    protectedRoute(
      '/activities/:id/edit',
      'activity-edit',
      () => import('@/views/ActivityFormView.vue'),
    ),
    protectedRoute('/journal', 'journal', () => import('@/views/JournalView.vue')),
    protectedRoute('/journal/new', 'journal-new', () => import('@/views/JournalFormView.vue')),
    protectedRoute('/journal/:id', 'journal-detail', () => import('@/views/JournalDetailView.vue')),
    protectedRoute(
      '/journal/:id/edit',
      'journal-edit',
      () => import('@/views/JournalFormView.vue'),
    ),
    protectedRoute('/goals', 'goals', () => import('@/views/GoalsView.vue')),
    protectedRoute('/goals/new', 'goal-new', () => import('@/views/GoalFormView.vue')),
    protectedRoute('/goals/:id', 'goal-detail', () => import('@/views/GoalDetailView.vue')),
    protectedRoute('/goals/:id/edit', 'goal-edit', () => import('@/views/GoalFormView.vue')),
    protectedRoute('/chat', 'chat', () => import('@/views/ChatView.vue')),
    protectedRoute('/profile', 'profile', () => import('@/views/ProfileView.vue')),
    protectedRoute('/admin', 'admin', () => import('@/views/AdminView.vue'), {
      requiresAdmin: true,
    }),
    protectedRoute('/therapist', 'therapist', () => import('@/views/TherapistView.vue'), {
      requiresTherapist: true,
    }),
    protectedRoute('/onboarding', 'onboarding', () => import('@/views/OnboardingView.vue')),
    publicRoute('/invite/:token', 'invite', () => import('@/views/InviteView.vue')),
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  const profileStore = useProfileStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'landing' }
  }

  if (
    shouldRedirectToDashboard(
      to.meta.requiresAdmin,
      auth.isAdmin,
      to.meta.requiresTherapist,
      auth.isTherapist,
    )
  ) {
    return { name: 'dashboard' }
  }

  if (to.name === 'landing' && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }

  if (
    shouldRedirectToOnboarding(
      to.meta.requiresAuth,
      auth.isAuthenticated,
      to.name,
      profileStore.profile?.onboardingCompleted,
    )
  ) {
    return { name: 'onboarding' }
  }
})

export default router
