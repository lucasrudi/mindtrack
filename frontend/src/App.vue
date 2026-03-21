<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import AppNavbar from '@/components/layout/AppNavbar.vue'
import ErrorNotification from '@/components/notifications/ErrorNotification.vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()

const showNavbar = computed(() => {
  return route.meta.requiresAuth === true
})

onMounted(async () => {
  await auth.bootstrap()
})
</script>

<template>
  <div id="mindtrack-app">
    <ErrorNotification />
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
