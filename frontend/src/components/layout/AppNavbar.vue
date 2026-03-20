<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import NotificationBell from '@/components/NotificationBell.vue'

const auth = useAuthStore()
const router = useRouter()

const patientLinks = [
  { label: 'Dashboard', to: { name: 'dashboard' } },
  { label: 'Journal', to: { name: 'journal' } },
  { label: 'Activities', to: { name: 'activities' } },
  { label: 'Goals', to: { name: 'goals' } },
  { label: 'Interviews', to: { name: 'interviews' } },
  { label: 'AI Chat', to: { name: 'chat' } },
]

const therapistLinks = [
  { label: 'Patients', to: { name: 'therapist' } },
  { label: 'Calendar', to: { name: 'therapist-calendar' } },
]

const navLinks = computed(() => (auth.activeView === 'therapist' ? therapistLinks : patientLinks))

const homeRoute = computed(() => ({ name: auth.homeRouteName }))

const showViewToggle = computed(() => auth.canSwitchViews)

async function handleLogout() {
  await auth.logout()
  router.push('/')
}

function switchView(view: 'patient' | 'therapist') {
  auth.setActiveView(view)
  router.replace(homeRoute.value)
}
</script>

<template>
  <nav class="navbar">
    <div class="navbar-inner container">
      <router-link :to="homeRoute" class="navbar-brand">
        <span class="navbar-logo">M</span>
        <span class="navbar-title">MindTrack</span>
      </router-link>

      <div class="navbar-links">
        <router-link v-for="link in navLinks" :key="link.label" :to="link.to" class="nav-link">
          {{ link.label }}
        </router-link>
        <router-link v-if="auth.isAdmin" to="/admin" class="nav-link nav-link--admin">
          Admin
        </router-link>
      </div>

      <div class="navbar-actions">
        <div v-if="showViewToggle" class="view-toggle" role="group" aria-label="Active view">
          <button
            type="button"
            :class="['view-toggle-btn', { active: auth.activeView === 'patient' }]"
            @click="switchView('patient')"
          >
            Patient
          </button>
          <button
            type="button"
            :class="['view-toggle-btn', { active: auth.activeView === 'therapist' }]"
            @click="switchView('therapist')"
          >
            Therapist
          </button>
        </div>
        <div v-else-if="auth.isAuthenticated" class="view-badge">
          {{ auth.activeViewLabel }} view
        </div>
        <NotificationBell v-if="auth.isAuthenticated" />
        <router-link to="/profile" class="nav-link nav-link--subtle">
          {{ auth.user?.name || 'Profile' }}
        </router-link>
        <button class="btn-logout" @click="handleLogout">Logout</button>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  height: var(--navbar-height);
  background-color: var(--color-white);
  border-bottom: 1px solid var(--color-gray-200);
  box-shadow: var(--shadow-sm);
}

.navbar-inner {
  display: flex;
  align-items: center;
  height: 100%;
  gap: var(--space-8);
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: var(--font-weight-bold);
  font-size: var(--font-size-lg);
  color: var(--color-gray-900);
}

.navbar-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
  background-color: var(--color-primary);
  border-radius: var(--border-radius);
}

.navbar-links {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex: 1;
}

.nav-link {
  padding: var(--space-2) var(--space-3);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-600);
  border-radius: var(--border-radius-sm);
  transition:
    color var(--transition-fast),
    background-color var(--transition-fast);
}

.nav-link:hover {
  color: var(--color-primary);
  background-color: var(--color-primary-50);
}

.nav-link.router-link-active {
  color: var(--color-primary);
  background-color: var(--color-primary-50);
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.view-toggle {
  display: inline-flex;
  padding: 2px;
  background-color: var(--color-gray-100);
  border: 1px solid var(--color-gray-200);
  border-radius: 999px;
}

.view-toggle-btn {
  padding: var(--space-2) var(--space-3);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-500);
  border-radius: 999px;
  transition:
    color var(--transition-fast),
    background-color var(--transition-fast);
}

.view-toggle-btn.active {
  color: var(--color-gray-900);
  background-color: var(--color-white);
  box-shadow: var(--shadow-sm);
}

.view-badge {
  padding: var(--space-2) var(--space-3);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-600);
  background-color: var(--color-gray-100);
  border-radius: 999px;
}

.nav-link--subtle {
  color: var(--color-gray-500);
}

.nav-link--admin {
  color: var(--color-warning, #d97706);
}

.nav-link--admin.router-link-active {
  color: var(--color-warning, #d97706);
  background-color: #fffbeb;
}

.nav-link--therapist {
  color: var(--color-success, #059669);
}

.nav-link--therapist.router-link-active {
  color: var(--color-success, #059669);
  background-color: #f0fdf4;
}

.btn-logout {
  padding: var(--space-2) var(--space-4);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-500);
  border: 1px solid var(--color-gray-300);
  border-radius: var(--border-radius-sm);
  transition: all var(--transition-fast);
}

.btn-logout:hover {
  color: var(--color-error);
  border-color: var(--color-error);
}
</style>
