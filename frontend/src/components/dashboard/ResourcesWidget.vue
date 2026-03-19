<script setup lang="ts">
import type { ContentItem } from '@/stores/analytics'

defineProps<{
  items: ContentItem[]
}>()

function sourceIcon(sourceType: string | null): string {
  if (sourceType === 'YOUTUBE') return '📺'
  if (sourceType === 'BOOK') return '📖'
  return '💬'
}
</script>

<template>
  <div class="resources-widget">
    <h2 class="widget-title">Resources</h2>
    <div v-if="items.length > 0" class="resources-grid">
      <div
        v-for="(item, index) in items"
        :key="index"
        class="resource-card"
        data-testid="resource-card"
      >
        <span class="resource-icon" :aria-label="item.sourceLabel ?? item.type">
          {{ sourceIcon(item.sourceType) }}
        </span>
        <div class="resource-content">
          <p class="resource-category">{{ item.category }}</p>
          <h3 class="resource-title">
            <a
              v-if="item.url"
              :href="item.url"
              target="_blank"
              rel="noopener noreferrer"
              class="resource-link"
            >
              {{ item.title }}
            </a>
            <span v-else>{{ item.title }}</span>
          </h3>
          <p class="resource-body">{{ item.body }}</p>
          <span v-if="item.sourceLabel" class="resource-source-label">{{ item.sourceLabel }}</span>
        </div>
      </div>
    </div>
    <p v-else class="resources-empty">No resources available yet.</p>
  </div>
</template>

<style scoped>
.resources-widget {
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

.resources-grid {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.resource-card {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3);
  border: 1px solid var(--color-gray-100);
  border-radius: var(--border-radius);
}

.resource-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.resource-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.resource-category {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0;
}

.resource-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0;
}

.resource-link {
  color: var(--color-gray-900);
  text-decoration: none;
}

.resource-link:hover {
  color: var(--color-primary);
  text-decoration: underline;
}

.resource-body {
  font-size: var(--font-size-sm);
  line-height: var(--line-height-relaxed);
  color: var(--color-gray-600);
  margin: 0;
}

.resource-source-label {
  font-size: var(--font-size-xs);
  color: var(--color-gray-400);
}

.resources-empty {
  font-size: var(--font-size-sm);
  color: var(--color-gray-400);
  margin: 0;
}
</style>
