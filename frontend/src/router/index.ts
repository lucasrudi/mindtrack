import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'landing',
      component: () => import('@/views/LandingView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/interviews',
      name: 'interviews',
      component: () => import('@/views/InterviewsView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/interviews/new',
      name: 'interview-new',
      component: () => import('@/views/InterviewFormView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/interviews/:id',
      name: 'interview-detail',
      component: () => import('@/views/InterviewDetailView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/interviews/:id/edit',
      name: 'interview-edit',
      component: () => import('@/views/InterviewFormView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/activities',
      name: 'activities',
      component: () => import('@/views/ActivitiesView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/activities/new',
      name: 'activity-new',
      component: () => import('@/views/ActivityFormView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/activities/:id/edit',
      name: 'activity-edit',
      component: () => import('@/views/ActivityFormView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/journal',
      name: 'journal',
      component: () => import('@/views/JournalView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/journal/new',
      name: 'journal-new',
      component: () => import('@/views/JournalFormView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/journal/:id',
      name: 'journal-detail',
      component: () => import('@/views/JournalDetailView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/journal/:id/edit',
      name: 'journal-edit',
      component: () => import('@/views/JournalFormView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/goals',
      name: 'goals',
      component: () => import('@/views/GoalsView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/goals/new',
      name: 'goal-new',
      component: () => import('@/views/GoalFormView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/goals/:id',
      name: 'goal-detail',
      component: () => import('@/views/GoalDetailView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/goals/:id/edit',
      name: 'goal-edit',
      component: () => import('@/views/GoalFormView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/chat',
      name: 'chat',
      component: () => import('@/views/ChatView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/ProfileView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/AdminView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/therapist',
      name: 'therapist',
      component: () => import('@/views/TherapistView.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'landing' }
  }

  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return { name: 'dashboard' }
  }

  if (to.name === 'landing' && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }
})

export default router
