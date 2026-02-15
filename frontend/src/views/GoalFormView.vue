<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useGoalsStore, type GoalForm } from '@/stores/goals'

const route = useRoute()
const router = useRouter()
const store = useGoalsStore()

const isEditing = computed(() => route.name === 'goal-edit')
const goalId = computed(() => (isEditing.value ? Number(route.params.id) : null))
const submitting = ref(false)

const form = reactive<GoalForm>({
  title: '',
  description: '',
  category: '',
  targetDate: null,
})

onMounted(async () => {
  if (isEditing.value && goalId.value) {
    await store.fetchGoals()
    const goal = store.goals.find((g) => g.id === goalId.value)
    if (goal) {
      form.title = goal.title
      form.description = goal.description ?? ''
      form.category = goal.category ?? ''
      form.targetDate = goal.targetDate
    }
  }
})

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEditing.value && goalId.value) {
      await store.updateGoal(goalId.value, form)
    } else {
      await store.createGoal(form)
    }
    router.push({ name: 'goals' })
  } catch {
    // error captured in store
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push({ name: 'goals' })
}
</script>

<template>
  <div class="form-view">
    <div class="form-header">
      <button class="btn-back" @click="goBack">&larr; Back</button>
      <h1>{{ isEditing ? 'Edit Goal' : 'New Goal' }}</h1>
    </div>

    <div v-if="store.error" class="error-banner" role="alert">
      {{ store.error }}
      <button class="error-dismiss" @click="store.clearError()">&times;</button>
    </div>

    <form class="goal-form" @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="goal-title">Title *</label>
        <input
          id="goal-title"
          v-model="form.title"
          type="text"
          class="form-input"
          required
          placeholder="e.g., Learn to meditate, Run a 5K"
        />
      </div>

      <div class="form-group">
        <label for="goal-description">Description</label>
        <textarea
          id="goal-description"
          v-model="form.description"
          class="form-textarea"
          rows="3"
          placeholder="What do you want to achieve?"
        />
      </div>

      <div class="form-group">
        <label for="goal-category">Category</label>
        <input
          id="goal-category"
          v-model="form.category"
          type="text"
          class="form-input"
          placeholder="e.g., Health, Personal, Career"
        />
      </div>

      <div class="form-group">
        <label for="goal-target-date">Target Date</label>
        <input id="goal-target-date" v-model="form.targetDate" type="date" class="form-input" />
      </div>

      <div class="form-actions">
        <button type="button" class="btn btn-secondary" @click="goBack">Cancel</button>
        <button type="submit" class="btn btn-primary" :disabled="submitting">
          {{ submitting ? 'Saving...' : isEditing ? 'Update Goal' : 'Create Goal' }}
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.form-view {
  max-width: 600px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

.form-header {
  margin-bottom: var(--space-6);
}

.btn-back {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  padding: var(--space-1) 0;
  margin-bottom: var(--space-3);
  display: inline-block;
}

.btn-back:hover {
  color: var(--color-primary-dark);
}

.form-header h1 {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
}

.error-banner {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: var(--color-error);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--border-radius);
  margin-bottom: var(--space-6);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--font-size-sm);
}

.error-dismiss {
  background: none;
  border: none;
  color: var(--color-error);
  font-size: var(--font-size-lg);
  cursor: pointer;
}

.goal-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-group label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-700);
}

.form-input,
.form-textarea {
  padding: var(--space-3);
  border: 1px solid var(--color-gray-300);
  border-radius: var(--border-radius);
  font-size: var(--font-size-base);
  font-family: var(--font-sans);
  color: var(--color-gray-900);
  background: var(--color-white);
  transition: border-color var(--transition-fast);
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-50);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
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

.btn-primary {
  background: var(--color-primary);
  color: var(--color-white);
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-primary-dark);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: var(--color-gray-100);
  color: var(--color-gray-700);
}

.btn-secondary:hover {
  background: var(--color-gray-200);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-gray-100);
}
</style>
