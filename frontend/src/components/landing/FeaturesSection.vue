<script setup lang="ts">
import FeatureCard from './FeatureCard.vue'

const features = [
  {
    icon: '📋',
    title: 'Interview Tracking',
    description:
      'Log psychiatrist sessions with structured notes, audio transcription, and key takeaways.',
  },
  {
    icon: '🎯',
    title: 'Activity Monitoring',
    description:
      'Track homework, habits, and custom activities. See patterns in your daily routines.',
  },
  {
    icon: '📓',
    title: 'Journaling',
    description:
      'Express your thoughts freely. Mood-tagged entries help you reflect on your emotional journey.',
  },
  {
    icon: '🏆',
    title: 'Goal Setting',
    description: 'Set meaningful goals with milestones. Break big changes into achievable steps.',
  },
  {
    icon: '🤖',
    title: 'AI Coaching',
    description:
      'Get personalized guidance powered by AI that understands your history and progress.',
  },
  {
    icon: '💬',
    title: 'Messaging Integration',
    description:
      'Check in via Telegram or WhatsApp. Log thoughts and moods spontaneously, wherever you are.',
  },
]
</script>

<template>
  <section class="features">
    <div class="features-inner container">
      <h2 class="features-heading">Everything you need to thrive</h2>
      <p class="features-subheading">
        A comprehensive toolkit designed to support your mental health journey, all in one place.
      </p>
      <div class="features-grid">
        <FeatureCard
          v-for="feature in features"
          :key="feature.title"
          :icon="feature.icon"
          :title="feature.title"
          :description="feature.description"
        />
      </div>
    </div>
  </section>
</template>

<style scoped>
.features {
  padding: var(--space-20) 0;
  background-color: var(--color-gray-50);
}

.features-heading {
  text-align: center;
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
  margin-bottom: var(--space-4);
}

.features-subheading {
  text-align: center;
  font-size: var(--font-size-lg);
  color: var(--color-gray-500);
  max-width: 600px;
  margin: 0 auto var(--space-12);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-8);
}

@media (max-width: 960px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .features-grid {
    grid-template-columns: 1fr;
  }

  .features-heading {
    font-size: var(--font-size-2xl);
  }
}
</style>
