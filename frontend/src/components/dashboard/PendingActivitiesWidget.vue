<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useActivitiesStore } from '@/stores/activities'

const store = useActivitiesStore()
const today = new Date().toISOString().split('T')[0]
const togglingIds = ref<Set<number>>(new Set())

const pendingItems = computed(() => store.checklist.filter((item) => !item.completed))

onMounted(async () => {
  try {
    await store.fetchChecklist(today)
  } catch {
    // error state handled by store
  }
})

async function markComplete(activityId: number) {
  if (togglingIds.value.has(activityId)) return
  togglingIds.value = new Set([...togglingIds.value, activityId])
  try {
    await store.logActivity(activityId, {
      logDate: today,
      completed: true,
      notes: '',
      moodRating: null,
    })
  } catch {
    // error state handled by store
  } finally {
    const next = new Set(togglingIds.value)
    next.delete(activityId)
    togglingIds.value = next
  }
}
</script>

<template>
  <section class="pending-activities-widget" data-testid="pending-activities-widget">
    <h2 class="widget-title">Today's Activities</h2>

    <div v-if="store.loading" class="loading-state" data-testid="loading-state">
      <p class="loading-text">Loading...</p>
    </div>

    <div v-else-if="store.error" class="error-state" data-testid="error-state">
      <p class="error-text">{{ store.error }}</p>
      <button class="btn btn-sm btn-secondary" @click="store.fetchChecklist(today)">Retry</button>
    </div>

    <template v-else>
      <div v-if="pendingItems.length === 0" class="empty-state" data-testid="empty-state">
        <p class="empty-text">All done for today — great work!</p>
        <RouterLink to="/activities/new" class="empty-link">
          Plan activities for your goals →
        </RouterLink>
      </div>

      <ul v-else class="checklist" data-testid="checklist">
        <li
          v-for="item in pendingItems"
          :key="item.activityId"
          class="checklist-item"
          :data-testid="`checklist-item-${item.activityId}`"
        >
          <button
            class="check-btn"
            :disabled="togglingIds.has(item.activityId)"
            :aria-label="`Mark ${item.activityName} as complete`"
            :data-testid="`check-btn-${item.activityId}`"
            @click="markComplete(item.activityId)"
          >
            <span class="check-icon" aria-hidden="true">
              {{ togglingIds.has(item.activityId) ? '...' : 'O' }}
            </span>
          </button>
          <span class="activity-name">{{ item.activityName }}</span>
        </li>
      </ul>
    </template>
  </section>
</template>

<style scoped>
.pending-activities-widget {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-5);
  margin-bottom: var(--space-6);
}

.widget-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0 0 var(--space-4) 0;
}

.loading-state,
.error-state {
  padding: var(--space-4) 0;
}

.loading-text {
  color: var(--color-gray-500);
  font-size: var(--font-size-sm);
  margin: 0;
}

.error-text {
  color: var(--color-error);
  font-size: var(--font-size-sm);
  margin: 0 0 var(--space-2) 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-2);
  padding: var(--space-2) 0;
}

.empty-text {
  color: var(--color-gray-600);
  font-size: var(--font-size-sm);
  margin: 0;
}

.empty-link {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-primary);
  text-decoration: none;
}

.empty-link:hover {
  text-decoration: underline;
}

.checklist {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.checklist-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) 0;
  border-bottom: 1px solid var(--color-gray-100);
}

.checklist-item:last-child {
  border-bottom: none;
}

.check-btn {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 2px solid var(--color-gray-300);
  background: var(--color-white);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition:
    border-color var(--transition-fast),
    background var(--transition-fast);
  padding: 0;
}

.check-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  background: #ede9fe;
}

.check-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.check-icon {
  font-size: 10px;
  color: var(--color-gray-400);
  line-height: 1;
}

.activity-name {
  font-size: var(--font-size-sm);
  color: var(--color-gray-800);
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-3) var(--space-5);
  border-radius: var(--border-radius);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
  cursor: pointer;
  border: none;
  transition: all var(--transition-fast);
}

.btn-sm {
  padding: var(--space-2) var(--space-3);
  font-size: var(--font-size-xs);
}

.btn-secondary {
  background: var(--color-gray-100);
  color: var(--color-gray-700);
}

.btn-secondary:hover {
  background: var(--color-gray-200);
}
</style>
