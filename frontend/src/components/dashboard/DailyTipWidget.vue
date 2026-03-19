<script setup lang="ts">
import type { ContentItem } from '@/stores/analytics'

defineProps<{
  tip: ContentItem | null
}>()
</script>

<template>
  <div class="daily-tip-widget">
    <h2 class="widget-title">Daily Tip</h2>
    <div v-if="tip" class="tip-card" data-testid="tip-card">
      <p class="tip-category">{{ tip.category }}</p>
      <h3 class="tip-heading">{{ tip.title }}</h3>
      <p class="tip-body">{{ tip.body }}</p>
    </div>
    <p v-else class="tip-empty">No tip available today.</p>
  </div>
</template>

<style scoped>
.daily-tip-widget {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-5);
}

.widget-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0 0 var(--space-4) 0;
}

.tip-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.tip-category {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0;
}

.tip-heading {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0;
}

.tip-body {
  font-size: var(--font-size-sm);
  line-height: var(--line-height-relaxed);
  color: var(--color-gray-600);
  margin: 0;
}

.tip-empty {
  font-size: var(--font-size-sm);
  color: var(--color-gray-400);
  margin: 0;
}
</style>
