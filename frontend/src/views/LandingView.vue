<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import HeroSection from '@/components/landing/HeroSection.vue'
import FeaturesSection from '@/components/landing/FeaturesSection.vue'
import BenefitsSection from '@/components/landing/BenefitsSection.vue'
import FooterSection from '@/components/landing/FooterSection.vue'

const router = useRouter()
const auth = useAuthStore()

function handleSignIn() {
  if (auth.isAuthenticated) {
    router.push('/dashboard')
  } else {
    window.location.href = '/api/oauth2/authorization/google'
  }
}
</script>

<template>
  <div class="landing">
    <HeroSection :on-sign-in="handleSignIn" />
    <FeaturesSection />
    <BenefitsSection />
    <FooterSection />
  </div>
</template>

<style scoped>
.landing {
  min-height: 100vh;
}
</style>
