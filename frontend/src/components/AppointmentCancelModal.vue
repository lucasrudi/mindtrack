<script setup lang="ts">
import type { CancellationScope } from '@/stores/appointments'

const props = defineProps<{
  show: boolean
  isSeries: boolean
}>()

const emit = defineEmits<{
  confirm: [scope: CancellationScope]
  cancel: []
}>()

import { ref } from 'vue'

const selected = ref<CancellationScope>('SINGLE')

function handleConfirm() {
  emit('confirm', selected.value)
}

function handleCancel() {
  emit('cancel')
}
</script>

<template>
  <div
    v-if="show"
    class="modal-backdrop"
    role="dialog"
    aria-modal="true"
    aria-labelledby="cancel-modal-title"
  >
    <div class="modal-panel">
      <h3 id="cancel-modal-title">Cancel appointment</h3>

      <div v-if="props.isSeries" class="scope-options">
        <label class="scope-option">
          <input v-model="selected" type="radio" value="SINGLE" />
          <span>This appointment only</span>
        </label>
        <label class="scope-option">
          <input v-model="selected" type="radio" value="THIS_AND_FOLLOWING" />
          <span>This and all following appointments</span>
        </label>
        <label class="scope-option">
          <input v-model="selected" type="radio" value="ALL_IN_SERIES" />
          <span>All appointments in this series</span>
        </label>
      </div>

      <p v-else class="scope-message">This appointment will be cancelled.</p>

      <div class="modal-actions">
        <button class="btn btn-secondary" type="button" @click="handleCancel">Keep</button>
        <button class="btn btn-danger" type="button" @click="handleConfirm">
          Confirm cancellation
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(2, 6, 23, 0.7);
  backdrop-filter: blur(4px);
}

.modal-panel {
  width: min(420px, 92vw);
  padding: var(--space-6);
  border-radius: 20px;
  background: #0f172a;
  border: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow: 0 32px 64px rgb(2 6 23 / 0.5);
  color: #e5eef7;
}

.modal-panel h3 {
  margin: 0 0 var(--space-5);
  color: var(--color-white);
}

.scope-options {
  display: grid;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
}

.scope-option {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.14);
  cursor: pointer;
  transition: border-color var(--transition-fast);
}

.scope-option:has(input:checked) {
  border-color: rgba(251, 191, 36, 0.5);
  background: rgba(251, 191, 36, 0.06);
}

.scope-option input {
  accent-color: #fbbf24;
}

.scope-message {
  margin: 0 0 var(--space-5);
  color: rgba(226, 232, 240, 0.78);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}

.btn-danger {
  color: #fecaca;
  background: rgba(220, 38, 38, 0.2);
  border: 1px solid rgba(220, 38, 38, 0.4);
  padding: var(--space-2) var(--space-4);
  border-radius: 12px;
  cursor: pointer;
  transition:
    background var(--transition-fast),
    border-color var(--transition-fast);
}

.btn-danger:hover {
  background: rgba(220, 38, 38, 0.32);
  border-color: rgba(220, 38, 38, 0.6);
}
</style>
