<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import AppNavbar from '@/components/layout/AppNavbar.vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()

const showNavbar = computed(() => {
  return route.meta.requiresAuth === true
})

onMounted(async () => {
  if (auth.isAuthenticated && !auth.user) {
    await auth.fetchCurrentUser()
  }
})
</script>

<template>
  <div id="mindtrack-app">
    <AppNavbar v-if="showNavbar" />
    <main :class="{ 'has-navbar': showNavbar }">
      <RouterView />
    </main>
  </div>
</template>

<style>
.has-navbar {
  padding-top: 0;
}
</style>
