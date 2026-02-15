<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

onMounted(async () => {
  const token = route.query.token as string | undefined

  if (token) {
    auth.setToken(token)
    await auth.fetchCurrentUser()
    router.replace('/dashboard')
  } else if (auth.isAuthenticated) {
    router.replace('/dashboard')
  } else {
    router.replace('/')
  }
})
</script>

<template>
  <div class="login-view">
    <p>Signing you in...</p>
  </div>
</template>

<style scoped>
.login-view {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  color: var(--color-gray-500);
}
</style>
