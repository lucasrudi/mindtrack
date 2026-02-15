<script setup lang="ts">
defineProps<{
  icon: string
  title: string
  description: string
}>()
</script>

<template>
  <div class="feature-card">
    <span class="feature-icon" role="img" :aria-label="title">{{ icon }}</span>
    <h3 class="feature-title">{{ title }}</h3>
    <p class="feature-description">{{ description }}</p>
  </div>
</template>

<style scoped>
.feature-card {
  padding: var(--space-8);
  text-align: center;
  background-color: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-xl);
  transition:
    box-shadow var(--transition-base),
    transform var(--transition-base);
}

.feature-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}

.feature-icon {
  display: inline-block;
  font-size: 2.5rem;
  margin-bottom: var(--space-4);
}

.feature-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin-bottom: var(--space-2);
}

.feature-description {
  font-size: var(--font-size-sm);
  line-height: var(--line-height-relaxed);
  color: var(--color-gray-500);
}
</style>
