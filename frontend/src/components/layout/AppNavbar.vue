<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function handleLogout() {
  auth.logout()
  router.push('/')
}
</script>

<template>
  <nav class="navbar">
    <div class="navbar-inner container">
      <router-link to="/dashboard" class="navbar-brand">
        <span class="navbar-logo">M</span>
        <span class="navbar-title">MindTrack</span>
      </router-link>

      <div class="navbar-links">
        <router-link to="/dashboard" class="nav-link">Dashboard</router-link>
        <router-link to="/journal" class="nav-link">Journal</router-link>
        <router-link to="/activities" class="nav-link">Activities</router-link>
        <router-link to="/goals" class="nav-link">Goals</router-link>
        <router-link to="/interviews" class="nav-link">Interviews</router-link>
        <router-link to="/chat" class="nav-link">AI Chat</router-link>
        <router-link v-if="auth.isAdmin" to="/admin" class="nav-link nav-link--admin">
          Admin
        </router-link>
      </div>

      <div class="navbar-actions">
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
