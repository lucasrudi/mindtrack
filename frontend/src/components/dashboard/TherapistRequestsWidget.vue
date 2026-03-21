<script setup lang="ts">
import { computed } from 'vue'
import type { TherapistRequest } from '@/stores/patient'

const props = defineProps<{
  requests: TherapistRequest[]
  loading: boolean
}>()

const emit = defineEmits<{
  accept: [relationshipId: number]
  reject: [relationshipId: number]
}>()

const pendingRequests = computed(() => props.requests.filter((r) => r.status === 'PENDING'))
const activeConnections = computed(() => props.requests.filter((r) => r.status === 'ACTIVE'))

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}
</script>

<template>
  <section class="therapist-requests-widget" data-testid="therapist-requests-widget">
    <h2 class="widget-title">Therapist Connections</h2>

    <div v-if="loading" class="loading-state">
      <p>Loading therapist requests...</p>
    </div>

    <template v-else>
      <div v-if="activeConnections.length" class="connections-section">
        <p class="section-label">Connected</p>
        <div
          v-for="conn in activeConnections"
          :key="conn.relationshipId"
          class="connection-card connection-card--active"
          data-testid="active-connection"
        >
          <div class="connection-info">
            <span class="therapist-name">{{ conn.therapistName }}</span>
            <span class="therapist-email">{{ conn.therapistEmail }}</span>
          </div>
          <span class="status-badge status-active">Active</span>
        </div>
      </div>

      <div v-if="pendingRequests.length" class="connections-section">
        <p class="section-label">Pending Requests</p>
        <div
          v-for="req in pendingRequests"
          :key="req.relationshipId"
          class="connection-card connection-card--pending"
          data-testid="pending-request"
        >
          <div class="connection-info">
            <span class="therapist-name">{{ req.therapistName }}</span>
            <span class="therapist-email">{{ req.therapistEmail }}</span>
            <span class="request-date">Requested {{ formatDate(req.createdAt) }}</span>
          </div>
          <div class="action-buttons">
            <button
              class="btn btn-primary btn-sm"
              data-testid="accept-btn"
              @click="emit('accept', req.relationshipId)"
            >
              Accept
            </button>
            <button
              class="btn btn-secondary btn-sm"
              data-testid="reject-btn"
              @click="emit('reject', req.relationshipId)"
            >
              Reject
            </button>
          </div>
        </div>
      </div>

      <div
        v-if="!activeConnections.length && !pendingRequests.length"
        class="empty-state"
        data-testid="empty-state"
      >
        <p>No therapist connections yet.</p>
      </div>
    </template>
  </section>
</template>
