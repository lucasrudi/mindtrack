<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const token = route.params.token as string
const preview = ref<{ initiatorName: string; initiatorRole: string; status: string | null } | null>(
  null,
)
const loading = ref(true)
const accepting = ref(false)
const rejecting = ref(false)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    const res = await fetch(`/api/invites/${token}`)
    if (!res.ok) throw new Error('Invalid or expired invite link')
    preview.value = await res.json()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'An error occurred'
  } finally {
    loading.value = false
  }
})

async function accept() {
  if (!auth.isAuthenticated) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  accepting.value = true
  try {
    const res = await fetch(`/api/invites/${token}/accept`, { method: 'POST' })
    if (!res.ok) throw new Error('Failed to accept invite')
    router.push({ name: auth.homeRouteName })
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'An error occurred'
  } finally {
    accepting.value = false
  }
}

async function reject() {
  if (!auth.isAuthenticated) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  rejecting.value = true
  try {
    const res = await fetch(`/api/invites/${token}/reject`, { method: 'POST' })
    if (!res.ok) throw new Error('Failed to reject request')
    router.push({ name: 'dashboard' })
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'An error occurred'
  } finally {
    rejecting.value = false
  }
}
</script>

<template>
  <div class="invite-view">
    <div class="invite-card">
      <div v-if="loading" class="loading">Loading invite...</div>
      <div v-else-if="error" class="error-msg">{{ error }}</div>
      <div v-else-if="preview">
        <span v-if="preview.status" class="status-pill">{{ preview.status }}</span>
        <h1 class="invite-title">
          {{ preview.status === 'PENDING' ? 'Therapist request received' : "You've been invited" }}
        </h1>
        <p class="invite-body">
          <strong>{{ preview.initiatorName }}</strong>
          ({{ preview.initiatorRole === 'THERAPIST' ? 'Therapist' : 'Patient' }}) wants to connect
          on MindTrack.
        </p>
        <div class="action-row">
          <button class="accept-btn" :disabled="accepting || rejecting" @click="accept">
            {{ accepting ? 'Accepting...' : 'Accept Request' }}
          </button>
          <button class="reject-btn" :disabled="accepting || rejecting" @click="reject">
            {{ rejecting ? 'Rejecting...' : 'Reject' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.invite-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-gray-50, #f9fafb);
}
.invite-card {
  background: white;
  border-radius: 16px;
  padding: 2.5rem;
  max-width: 440px;
  width: 100%;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  text-align: center;
}
.invite-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
}
.invite-body {
  color: var(--color-gray-600);
  margin-bottom: 1.5rem;
  line-height: 1.6;
}
.status-pill {
  display: inline-block;
  margin-bottom: 0.75rem;
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  background: var(--color-primary-50);
  color: var(--color-primary);
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}
.action-row {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
  flex-wrap: wrap;
}
.accept-btn {
  padding: 0.75rem 2rem;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}
.accept-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.reject-btn {
  padding: 0.75rem 2rem;
  background: transparent;
  color: var(--color-gray-700);
  border: 1px solid var(--color-gray-300);
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}
.reject-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.error-msg {
  color: var(--color-error);
}
.loading {
  color: var(--color-gray-500);
}
</style>
