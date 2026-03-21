<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ContentItem } from '@/stores/analytics'

const props = defineProps<{
  items: ContentItem[]
}>()

function getSecureRandomIndex(length: number) {
  if (length <= 1) return 0

  // Use the browser's Web Crypto API rather than a weak pseudorandom source.
  const cryptoApi = globalThis.crypto
  if (!cryptoApi?.getRandomValues) return 0

  const randomValue = new Uint32Array(1)
  const maxUint32Exclusive = 2 ** 32
  const unbiasedUpperBound = maxUint32Exclusive - (maxUint32Exclusive % length)

  do {
    cryptoApi.getRandomValues(randomValue)
  } while (randomValue[0] >= unbiasedUpperBound)

  return randomValue[0] % length
}

const currentIndex = ref(getSecureRandomIndex(props.items.length))

const current = computed(() => props.items[currentIndex.value] ?? null)

function prev() {
  if (props.items.length === 0) return
  currentIndex.value = (currentIndex.value - 1 + props.items.length) % props.items.length
}

function next() {
  if (props.items.length === 0) return
  currentIndex.value = (currentIndex.value + 1) % props.items.length
}
</script>

<template>
  <div class="video-widget">
    <h2 class="widget-title">Health Videos</h2>
    <div v-if="current" class="video-card" data-testid="video-card">
      <div class="video-embed">
        <iframe
          :src="`https://www.youtube-nocookie.com/embed/${current.url}`"
          :title="current.title"
          allow="
            accelerometer;
            autoplay;
            clipboard-write;
            encrypted-media;
            gyroscope;
            picture-in-picture;
          "
          allowfullscreen
          data-testid="video-iframe"
        ></iframe>
      </div>
      <div class="video-info">
        <p class="video-category">{{ current.category }}</p>
        <h3 class="video-title">{{ current.title }}</h3>
      </div>
      <div v-if="items.length > 1" class="video-nav">
        <button class="nav-btn" aria-label="Previous video" @click="prev">&#8592;</button>
        <span class="video-counter">Video {{ currentIndex + 1 }} of {{ items.length }}</span>
        <button class="nav-btn" aria-label="Next video" @click="next">&#8594;</button>
      </div>
    </div>
    <p v-else class="video-empty">No videos available.</p>
  </div>
</template>

<style scoped>
.video-widget {
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

.video-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.video-embed {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
}

.video-embed iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 0;
  border-radius: var(--border-radius);
}

.video-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.video-category {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0;
}

.video-title {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0;
}

.video-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-top: var(--space-1);
}

.nav-btn {
  background: var(--color-gray-100);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-sm);
  color: var(--color-gray-600);
  cursor: pointer;
  font-size: var(--font-size-sm);
  padding: var(--space-1) var(--space-3);
  transition: all var(--transition-fast);
}

.nav-btn:hover {
  background: var(--color-gray-200);
  color: var(--color-gray-900);
}

.video-counter {
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
}

.video-empty {
  font-size: var(--font-size-sm);
  color: var(--color-gray-400);
  margin: 0;
}
</style>
