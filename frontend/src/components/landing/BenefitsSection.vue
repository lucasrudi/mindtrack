<template>
  <section class="benefits">
    <div class="benefits-inner container">
      <div class="benefits-content">
        <h2 class="benefits-heading">Why MindTrack?</h2>
        <ul class="benefits-list">
          <li class="benefit-item">
            <span class="benefit-check" aria-hidden="true">&#10003;</span>
            <div>
              <strong>Private &amp; secure</strong>
              <p>Your data is encrypted and never shared. You control your information.</p>
            </div>
          </li>
          <li class="benefit-item">
            <span class="benefit-check" aria-hidden="true">&#10003;</span>
            <div>
              <strong>Therapist-friendly</strong>
              <p>Share session summaries with your therapist to make every appointment count.</p>
            </div>
          </li>
          <li class="benefit-item">
            <span class="benefit-check" aria-hidden="true">&#10003;</span>
            <div>
              <strong>Evidence-based insights</strong>
              <p>Analytics powered by your own data help you see real progress over time.</p>
            </div>
          </li>
          <li class="benefit-item">
            <span class="benefit-check" aria-hidden="true">&#10003;</span>
            <div>
              <strong>Always accessible</strong>
              <p>Works on any device. Check in via web, Telegram, or WhatsApp.</p>
            </div>
          </li>
        </ul>
      </div>
      <div class="benefits-visual">
        <div class="benefits-card">
          <div class="benefits-card-header">
            <span class="benefits-card-dot benefits-card-dot--green"></span>
            <span class="benefits-card-label">Weekly Progress</span>
          </div>
          <div class="benefits-card-stat">
            <span class="benefits-card-number">87%</span>
            <span class="benefits-card-unit">goal completion</span>
          </div>
          <div class="benefits-card-bar">
            <div class="benefits-card-bar-fill"></div>
          </div>
          <p class="benefits-card-text">You completed 13 of 15 weekly goals. Keep it up!</p>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.benefits {
  padding: var(--space-20) 0;
}

.benefits-inner {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-16);
  align-items: center;
}

.benefits-heading {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
  margin-bottom: var(--space-8);
}

.benefits-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.benefit-item {
  display: flex;
  gap: var(--space-4);
  align-items: flex-start;
}

.benefit-check {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  margin-top: 2px;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
  background-color: var(--color-success);
  border-radius: var(--border-radius-full);
}

.benefit-item strong {
  display: block;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin-bottom: var(--space-1);
}

.benefit-item p {
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
  line-height: var(--line-height-relaxed);
}

/* Decorative card */
.benefits-card {
  padding: var(--space-8);
  background-color: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-xl);
  box-shadow: var(--shadow-lg);
}

.benefits-card-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-6);
}

.benefits-card-dot {
  width: 10px;
  height: 10px;
  border-radius: var(--border-radius-full);
}

.benefits-card-dot--green {
  background-color: var(--color-success);
}

.benefits-card-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-600);
}

.benefits-card-stat {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.benefits-card-number {
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.benefits-card-unit {
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
}

.benefits-card-bar {
  height: 8px;
  background-color: var(--color-gray-100);
  border-radius: var(--border-radius-full);
  margin-bottom: var(--space-4);
  overflow: hidden;
}

.benefits-card-bar-fill {
  width: 87%;
  height: 100%;
  background-color: var(--color-primary);
  border-radius: var(--border-radius-full);
}

.benefits-card-text {
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
}

@media (max-width: 768px) {
  .benefits-inner {
    grid-template-columns: 1fr;
    gap: var(--space-10);
  }

  .benefits-heading {
    font-size: var(--font-size-2xl);
  }
}
</style>
