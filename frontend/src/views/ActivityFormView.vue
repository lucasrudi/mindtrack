<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useActivitiesStore, type ActivityForm, type ActivityType } from '@/stores/activities'

const route = useRoute()
const router = useRouter()
const store = useActivitiesStore()

const isEditing = computed(() => route.name === 'activity-edit')
const activityId = computed(() => (isEditing.value ? Number(route.params.id) : null))
const submitting = ref(false)

const activityTypes: { value: ActivityType; label: string }[] = [
  { value: 'EXERCISE', label: 'Exercise' },
  { value: 'MEDITATION', label: 'Meditation' },
  { value: 'SOCIAL', label: 'Social' },
  { value: 'THERAPY', label: 'Therapy' },
  { value: 'MEDICATION', label: 'Medication' },
  { value: 'HOBBY', label: 'Hobby' },
  { value: 'SELF_CARE', label: 'Self Care' },
  { value: 'OTHER', label: 'Other' },
]

const form = reactive<ActivityForm>({
  type: 'EXERCISE',
  name: '',
  description: '',
  frequency: '',
  linkedInterviewId: null,
})

onMounted(async () => {
  if (isEditing.value && activityId.value) {
    await store.fetchActivities()
    const activity = store.activities.find((a) => a.id === activityId.value)
    if (activity) {
      form.type = activity.type
      form.name = activity.name
      form.description = activity.description ?? ''
      form.frequency = activity.frequency ?? ''
      form.linkedInterviewId = activity.linkedInterviewId
    }
  }
})

async function handleSubmit() {
  submitting.value = true
  try {
    if (isEditing.value && activityId.value) {
      await store.updateActivity(activityId.value, form)
    } else {
      await store.createActivity(form)
    }
    router.push({ name: 'activities' })
  } catch {
    // error captured in store
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push({ name: 'activities' })
}
</script>

<template>
  <div class="form-view">
    <div class="form-header">
      <button class="btn-back" @click="goBack">← Back</button>
      <h1>{{ isEditing ? 'Edit Activity' : 'New Activity' }}</h1>
    </div>

    <div v-if="store.error" class="error-banner" role="alert">
      {{ store.error }}
      <button class="error-dismiss" @click="store.clearError()">×</button>
    </div>

    <form class="activity-form" @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="activity-type">Type *</label>
        <select id="activity-type" v-model="form.type" class="form-input" required>
          <option v-for="t in activityTypes" :key="t.value" :value="t.value">
            {{ t.label }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label for="activity-name">Name *</label>
        <input
          id="activity-name"
          v-model="form.name"
          type="text"
          class="form-input"
          required
          placeholder="e.g., Morning jog, 10-min meditation"
        />
      </div>

      <div class="form-group">
        <label for="activity-description">Description</label>
        <textarea
          id="activity-description"
          v-model="form.description"
          class="form-textarea"
          rows="3"
          placeholder="What does this activity involve?"
        />
      </div>

      <div class="form-group">
        <label for="activity-frequency">Frequency</label>
        <input
          id="activity-frequency"
          v-model="form.frequency"
          type="text"
          class="form-input"
          placeholder="e.g., Daily, 3x per week"
        />
      </div>

      <div class="form-actions">
        <button type="button" class="btn btn-secondary" @click="goBack">Cancel</button>
        <button type="submit" class="btn btn-primary" :disabled="submitting">
          {{ submitting ? 'Saving...' : isEditing ? 'Update Activity' : 'Create Activity' }}
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

.activity-form {
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
