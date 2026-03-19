<script setup lang="ts">
import type { ContentItem } from '@/stores/analytics'

defineProps<{
  items: ContentItem[]
}>()
</script>

<template>
  <div class="wellbeing-widget">
    <h2 class="widget-title">Well-being</h2>
    <div v-if="items.length > 0" class="wellbeing-list">
      <div
        v-for="(item, index) in items"
        :key="index"
        class="wellbeing-card"
        data-testid="wellbeing-card"
      >
        <h3 class="wellbeing-title">{{ item.title }}</h3>
        <p class="wellbeing-body">{{ item.body }}</p>
      </div>
    </div>
    <p v-else class="wellbeing-empty">Keep logging your mood to see trends here.</p>
  </div>
</template>

<style scoped>
.wellbeing-widget {
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

.wellbeing-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.wellbeing-card {
  padding: var(--space-3);
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: var(--border-radius);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.wellbeing-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0;
}

.wellbeing-body {
  font-size: var(--font-size-sm);
  line-height: var(--line-height-relaxed);
  color: var(--color-gray-600);
  margin: 0;
}

.wellbeing-empty {
  font-size: var(--font-size-sm);
  color: var(--color-gray-400);
  margin: 0;
}
</style>
