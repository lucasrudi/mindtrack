<script setup lang="ts">
import { RouterLink } from 'vue-router'
import type { Goal } from '@/stores/goals'

defineProps<{
  goals: Goal[]
}>()

function getProgress(goal: Goal): number {
  if (!goal.totalMilestones || goal.totalMilestones === 0) return 0
  return Math.round((goal.completedMilestones / goal.totalMilestones) * 100)
}

function formatDate(date: string | null): string {
  if (!date) return 'No target date'
  return new Date(date).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}
</script>

<template>
  <section class="active-goals-widget">
    <h2 class="widget-title">Active Goals</h2>

    <div v-if="goals.length === 0" class="empty-state">
      <span class="empty-icon">🎯</span>
      <p class="empty-text">No active goals yet</p>
      <RouterLink to="/goals" class="empty-link">Create your first goal →</RouterLink>
    </div>

    <div v-else class="goals-grid">
      <RouterLink
        v-for="goal in goals"
        :key="goal.id"
        :to="{ name: 'goal-detail', params: { id: goal.id } }"
        class="goal-card"
      >
        <div class="goal-card__header">
          <span class="goal-icon">🎯</span>
          <span v-if="goal.category" class="category-pill">{{ goal.category }}</span>
        </div>

        <h3 class="goal-title">{{ goal.title }}</h3>

        <div class="progress-section">
          <div
            class="progress-bar"
            role="progressbar"
            :aria-valuenow="getProgress(goal)"
            aria-valuemin="0"
            aria-valuemax="100"
          >
            <div class="progress-bar__fill" :style="{ width: getProgress(goal) + '%' }"></div>
          </div>
          <span class="progress-label">
            {{ goal.completedMilestones }}/{{ goal.totalMilestones }} milestones ({{
              getProgress(goal)
            }}%)
          </span>
        </div>

        <p class="goal-date">{{ formatDate(goal.targetDate) }}</p>
      </RouterLink>
    </div>
  </section>
</template>

<style scoped>
.active-goals-widget {
  margin-bottom: var(--space-8);
}

.widget-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0 0 var(--space-4) 0;
}

.goals-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}

.goal-card {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  text-decoration: none;
  color: inherit;
  transition:
    border-color var(--transition-fast),
    box-shadow var(--transition-fast);
}

.goal-card:hover {
  border-color: var(--color-primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.goal-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.goal-icon {
  font-size: 1.25rem;
}

.category-pill {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  color: var(--color-primary);
  background: #ede9fe;
  border-radius: 999px;
  padding: 2px var(--space-2);
  text-transform: capitalize;
}

.goal-title {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0;
  line-height: 1.3;
}

.progress-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.progress-bar {
  height: 6px;
  background: var(--color-gray-200);
  border-radius: 999px;
  overflow: hidden;
}

.progress-bar__fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 999px;
  transition: width 0.3s ease;
}

.progress-label {
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
}

.goal-date {
  font-size: var(--font-size-xs);
  color: var(--color-gray-400);
  margin: 0;
}

.empty-state {
  background: var(--color-white);
  border: 1px dashed var(--color-gray-300);
  border-radius: var(--border-radius-lg);
  padding: var(--space-8);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  text-align: center;
}

.empty-icon {
  font-size: 2rem;
}

.empty-text {
  color: var(--color-gray-500);
  margin: 0;
  font-size: var(--font-size-sm);
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

@media (max-width: 768px) {
  .goals-grid {
    grid-template-columns: 1fr;
  }
}
</style>
