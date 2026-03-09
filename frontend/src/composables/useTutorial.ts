import { ref, computed } from 'vue'
import { useProfileStore } from '@/stores/profile'

export interface TutorialStep {
  target: string
  title: string
  description: string
  placement: 'top' | 'bottom' | 'left' | 'right'
}

const tutorialSteps: TutorialStep[] = [
  {
    target: '.navbar-brand',
    title: 'Welcome to MindTrack!',
    description: "Let's take a quick tour of the key features to help you get started.",
    placement: 'bottom',
  },
  {
    target: 'a[href="/journal"]',
    title: 'Journal',
    description:
      'Track your mood and thoughts in your journal. Write daily entries to monitor your mental health.',
    placement: 'bottom',
  },
  {
    target: 'a[href="/activities"]',
    title: 'Activities',
    description:
      'Log daily activities and build healthy habits. Track homework, exercises, and custom routines.',
    placement: 'bottom',
  },
  {
    target: 'a[href="/goals"]',
    title: 'Goals',
    description:
      'Set personal goals and track milestones. Break down big objectives into achievable steps.',
    placement: 'bottom',
  },
  {
    target: 'a[href="/interviews"]',
    title: 'Interviews',
    description:
      'Record psychiatrist session notes, including mood ratings, topics discussed, and audio recordings.',
    placement: 'bottom',
  },
  {
    target: 'a[href="/chat"]',
    title: 'AI Coach',
    description:
      'Chat with your AI coach for personalized guidance based on your journal entries and activities.',
    placement: 'bottom',
  },
]

const isActive = ref(false)
const currentStepIndex = ref(0)

export function useTutorial() {
  const profileStore = useProfileStore()

  const currentStep = computed(() =>
    isActive.value ? tutorialSteps[currentStepIndex.value] : null,
  )

  const totalSteps = computed(() => tutorialSteps.length)

  const isLastStep = computed(() => currentStepIndex.value === tutorialSteps.length - 1)

  function start() {
    currentStepIndex.value = 0
    isActive.value = true
  }

  function next() {
    if (currentStepIndex.value < tutorialSteps.length - 1) {
      currentStepIndex.value++
    } else {
      complete()
    }
  }

  function previous() {
    if (currentStepIndex.value > 0) {
      currentStepIndex.value--
    }
  }

  async function complete() {
    isActive.value = false
    currentStepIndex.value = 0
    try {
      await profileStore.updateProfile({ tutorialCompleted: true } as unknown as Parameters<
        typeof profileStore.updateProfile
      >[0])
    } catch {
      // Profile update is best-effort
    }
  }

  const skip = complete

  return {
    isActive,
    currentStep,
    currentStepIndex,
    totalSteps,
    isLastStep,
    start,
    next,
    previous,
    complete,
    skip,
    steps: tutorialSteps,
  }
}
