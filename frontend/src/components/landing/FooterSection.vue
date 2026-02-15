<template>
  <footer class="footer">
    <div class="footer-inner container">
      <div class="footer-brand">
        <span class="footer-logo">M</span>
        <span class="footer-name">MindTrack</span>
      </div>
      <p class="footer-text">Your mental health companion. Track, reflect, grow.</p>
      <p class="footer-copyright">
        &copy; {{ new Date().getFullYear() }} MindTrack. All rights reserved.
      </p>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  padding: var(--space-12) 0;
  background-color: var(--color-gray-900);
  color: var(--color-gray-400);
  text-align: center;
}

.footer-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.footer-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
  background-color: var(--color-primary);
  border-radius: var(--border-radius);
}

.footer-name {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
}

.footer-text {
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-4);
}

.footer-copyright {
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
}
</style>
