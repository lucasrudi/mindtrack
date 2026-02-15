<script setup lang="ts">
defineProps<{
  onSignIn: () => void
}>()
</script>

<template>
  <section class="hero">
    <div class="hero-inner container">
      <span class="hero-badge">Your mental health companion</span>
      <h1 class="hero-title">
        Track your wellbeing.
        <span class="hero-title-accent">Grow with AI coaching.</span>
      </h1>
      <p class="hero-description">
        MindTrack helps you log interviews, track activities, journal your thoughts, set meaningful
        goals, and get personalized AI coaching — all in one place.
      </p>
      <div class="hero-actions">
        <button class="btn btn-primary btn-lg" @click="onSignIn">
          <svg class="google-icon" width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
            <path
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"
            />
            <path
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
            />
            <path
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
            />
            <path
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
            />
          </svg>
          Sign in with Google
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero {
  padding: var(--space-24) 0 var(--space-20);
  text-align: center;
  background: linear-gradient(180deg, var(--color-primary-50) 0%, var(--color-white) 100%);
}

.hero-inner {
  max-width: 720px;
}

.hero-badge {
  display: inline-block;
  padding: var(--space-1) var(--space-4);
  margin-bottom: var(--space-6);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-primary);
  background-color: var(--color-primary-100);
  border-radius: var(--border-radius-full);
}

.hero-title {
  font-size: var(--font-size-5xl);
  font-weight: var(--font-weight-bold);
  line-height: var(--line-height-tight);
  color: var(--color-gray-900);
  margin-bottom: var(--space-6);
}

.hero-title-accent {
  display: block;
  color: var(--color-primary);
}

.hero-description {
  font-size: var(--font-size-xl);
  line-height: var(--line-height-relaxed);
  color: var(--color-gray-600);
  margin-bottom: var(--space-10);
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: var(--space-4);
}

.google-icon {
  width: 20px;
  height: 20px;
}

@media (max-width: 640px) {
  .hero {
    padding: var(--space-16) 0 var(--space-12);
  }

  .hero-title {
    font-size: var(--font-size-3xl);
  }

  .hero-description {
    font-size: var(--font-size-lg);
  }
}
</style>
