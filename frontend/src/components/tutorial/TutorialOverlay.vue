<script setup lang="ts">
import { ref, watch, nextTick, onUnmounted } from 'vue'
import { useTutorial } from '@/composables/useTutorial'

const { isActive, currentStep, currentStepIndex, totalSteps, isLastStep, next, previous, skip } =
  useTutorial()

const tooltipStyle = ref<Record<string, string>>({})
const spotlightStyle = ref<Record<string, string>>({})

function positionTooltip() {
  if (!currentStep.value) return

  const el = document.querySelector(currentStep.value.target)
  if (!el) {
    // If target not found, center tooltip
    tooltipStyle.value = {
      top: '50%',
      left: '50%',
      transform: 'translate(-50%, -50%)',
    }
    spotlightStyle.value = { display: 'none' }
    return
  }

  const rect = el.getBoundingClientRect()
  const pad = 8

  // Spotlight
  spotlightStyle.value = {
    top: `${rect.top - pad}px`,
    left: `${rect.left - pad}px`,
    width: `${rect.width + pad * 2}px`,
    height: `${rect.height + pad * 2}px`,
  }

  // Tooltip position based on placement
  const placement = currentStep.value.placement
  const tooltipWidth = 320

  if (placement === 'bottom') {
    tooltipStyle.value = {
      top: `${rect.bottom + 16}px`,
      left: `${Math.max(16, rect.left + rect.width / 2 - tooltipWidth / 2)}px`,
      width: `${tooltipWidth}px`,
    }
  } else if (placement === 'top') {
    tooltipStyle.value = {
      bottom: `${window.innerHeight - rect.top + 16}px`,
      left: `${Math.max(16, rect.left + rect.width / 2 - tooltipWidth / 2)}px`,
      width: `${tooltipWidth}px`,
    }
  } else if (placement === 'right') {
    tooltipStyle.value = {
      top: `${rect.top}px`,
      left: `${rect.right + 16}px`,
      width: `${tooltipWidth}px`,
    }
  } else {
    tooltipStyle.value = {
      top: `${rect.top}px`,
      right: `${window.innerWidth - rect.left + 16}px`,
      width: `${tooltipWidth}px`,
    }
  }
}

watch(
  () => currentStepIndex.value,
  async () => {
    await nextTick()
    positionTooltip()
  },
)

watch(
  () => isActive.value,
  async (active) => {
    if (active) {
      await nextTick()
      positionTooltip()
      window.addEventListener('resize', positionTooltip)
    } else {
      window.removeEventListener('resize', positionTooltip)
    }
  },
)

onUnmounted(() => {
  window.removeEventListener('resize', positionTooltip)
})
</script>

<template>
  <Teleport to="body">
    <div v-if="isActive && currentStep" class="tutorial-overlay" data-testid="tutorial-overlay">
      <!-- Backdrop -->
      <div class="tutorial-backdrop" @click="skip" />

      <!-- Spotlight -->
      <div class="tutorial-spotlight" :style="spotlightStyle" />

      <!-- Tooltip -->
      <div class="tutorial-tooltip" :style="tooltipStyle" data-testid="tutorial-tooltip">
        <div class="tooltip-header">
          <span class="step-counter"> {{ currentStepIndex + 1 }} / {{ totalSteps }} </span>
          <button class="skip-btn" @click="skip">Skip</button>
        </div>

        <h3 class="tooltip-title">{{ currentStep.title }}</h3>
        <p class="tooltip-description">{{ currentStep.description }}</p>

        <div class="tooltip-actions">
          <button
            v-if="currentStepIndex > 0"
            class="btn btn-secondary"
            data-testid="tutorial-prev"
            @click="previous"
          >
            Back
          </button>
          <button class="btn btn-primary" data-testid="tutorial-next" @click="next">
            {{ isLastStep ? 'Get Started' : 'Next' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.tutorial-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
}

.tutorial-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
}

.tutorial-spotlight {
  position: fixed;
  border-radius: 8px;
  box-shadow:
    0 0 0 9999px rgba(0, 0, 0, 0.5),
    0 0 0 4px rgba(79, 70, 229, 0.4);
  z-index: 10000;
  pointer-events: none;
  transition: all 0.3s ease;
}

.tutorial-tooltip {
  position: fixed;
  background: var(--color-white, #fff);
  border-radius: 12px;
  padding: 20px;
  z-index: 10001;
  box-shadow:
    0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 8px 10px -6px rgba(0, 0, 0, 0.1);
  animation: tooltip-fade-in 0.2s ease-out;
}

@keyframes tooltip-fade-in {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.tooltip-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.step-counter {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary, #4f46e5);
  background: var(--color-primary-50, #eef2ff);
  padding: 2px 10px;
  border-radius: 12px;
}

.skip-btn {
  background: none;
  border: none;
  color: var(--color-gray-400, #9ca3af);
  font-size: 13px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
}

.skip-btn:hover {
  color: var(--color-gray-600, #4b5563);
  background: var(--color-gray-100, #f3f4f6);
}

.tooltip-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-gray-900, #111827);
  margin: 0 0 8px;
}

.tooltip-description {
  font-size: 14px;
  color: var(--color-gray-600, #4b5563);
  line-height: 1.5;
  margin: 0 0 16px;
}

.tooltip-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  cursor: pointer;
  border: none;
  transition: all 0.15s ease;
}

.btn-primary {
  background: var(--color-primary, #4f46e5);
  color: #fff;
}

.btn-primary:hover {
  background: var(--color-primary-dark, #4338ca);
}

.btn-secondary {
  background: var(--color-gray-100, #f3f4f6);
  color: var(--color-gray-700, #374151);
}

.btn-secondary:hover {
  background: var(--color-gray-200, #e5e7eb);
}
</style>
