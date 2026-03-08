<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

onMounted(async () => {
  await auth.fetchCurrentUser()
  if (auth.isAuthenticated) {
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
