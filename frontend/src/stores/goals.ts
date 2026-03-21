import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'
import { getCachedGoals, setCachedGoals } from './dashboardSessionCache'

export type GoalStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'PAUSED' | 'CANCELLED'
export type GoalValidationStatus = 'PENDING_VALIDATION' | 'VALIDATED' | 'OVERRIDDEN' | 'REJECTED'

export interface Milestone {
  id: number
  goalId: number
  title: string
  targetDate: string | null
  completedAt: string | null
  completed: boolean
  suggested: boolean
  notes: string | null
  createdAt: string
}

export interface Goal {
  id: number
  title: string
  description: string | null
  category: string | null
  targetDate: string | null
  status: GoalStatus
  validationStatus: GoalValidationStatus | null
  totalMilestones: number
  completedMilestones: number
  milestones: Milestone[]
  createdAt: string
  updatedAt: string
}

export interface GoalForm {
  title: string
  description: string
  category: string
  targetDate: string | null
}

export interface MilestoneForm {
  title: string
  targetDate: string | null
  notes: string
}

export const useGoalsStore = defineStore('goals', () => {
  const goals = ref<Goal[]>([])
  const currentGoal = ref<Goal | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  let refreshToken = 0

  const activeGoals = computed(() =>
    goals.value.filter((g) => g.status === 'IN_PROGRESS' || g.status === 'NOT_STARTED'),
  )

  const completedGoals = computed(() => goals.value.filter((g) => g.status === 'COMPLETED'))

  function syncGoalsCache() {
    setCachedGoals(goals.value)
  }

  async function refreshGoalsSilently(token: number) {
    try {
      const response = await api.get('/goals')
      if (token !== refreshToken) return
      goals.value = response.data
      syncGoalsCache()
      error.value = null
    } catch {
      // Keep cached goals visible if background refresh fails.
    }
  }

  async function fetchGoals() {
    error.value = null

    const cached = getCachedGoals()
    const token = ++refreshToken

    if (cached) {
      goals.value = cached
      loading.value = false
      void refreshGoalsSilently(token)
      return
    }

    loading.value = true
    try {
      const response = await api.get('/goals')
      if (token !== refreshToken) return
      goals.value = response.data
      syncGoalsCache()
    } catch (err) {
      error.value = 'Failed to load goals'
      throw err
    } finally {
      if (token === refreshToken) {
        loading.value = false
      }
    }
  }

  async function fetchGoal(id: number) {
    loading.value = true
    error.value = null
    try {
      const response = await api.get(`/goals/${id}`)
      currentGoal.value = response.data
      return response.data
    } catch (err) {
      error.value = 'Failed to load goal'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function createGoal(form: GoalForm) {
    loading.value = true
    error.value = null
    try {
      const response = await api.post('/goals', form)
      goals.value.unshift(response.data)
      syncGoalsCache()
      return response.data
    } catch (err) {
      error.value = 'Failed to create goal'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateGoal(id: number, form: GoalForm) {
    loading.value = true
    error.value = null
    try {
      const response = await api.put(`/goals/${id}`, form)
      const index = goals.value.findIndex((g) => g.id === id)
      if (index !== -1) {
        goals.value[index] = response.data
        syncGoalsCache()
      }
      currentGoal.value = response.data
      return response.data
    } catch (err) {
      error.value = 'Failed to update goal'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateStatus(id: number, status: GoalStatus) {
    error.value = null
    try {
      const response = await api.patch(`/goals/${id}/status`, { status })
      const index = goals.value.findIndex((g) => g.id === id)
      if (index !== -1) {
        goals.value[index] = response.data
        syncGoalsCache()
      }
      if (currentGoal.value?.id === id) {
        currentGoal.value = response.data
      }
      return response.data
    } catch (err) {
      error.value = 'Failed to update goal status'
      throw err
    }
  }

  async function markGoalStarted(id: number) {
    return updateStatus(id, 'IN_PROGRESS')
  }

  async function deleteGoal(id: number) {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/goals/${id}`)
      goals.value = goals.value.filter((g) => g.id !== id)
      syncGoalsCache()
      if (currentGoal.value?.id === id) {
        currentGoal.value = null
      }
    } catch (err) {
      error.value = 'Failed to delete goal'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function addMilestone(goalId: number, form: MilestoneForm) {
    error.value = null
    try {
      const response = await api.post(`/goals/${goalId}/milestones`, form)
      const goal = goals.value.find((g) => g.id === goalId)
      if (goal) {
        goal.milestones.push(response.data)
        goal.totalMilestones++
        syncGoalsCache()
      }
      if (currentGoal.value?.id === goalId) {
        currentGoal.value.milestones.push(response.data)
        currentGoal.value.totalMilestones++
      }
      return response.data
    } catch (err) {
      error.value = 'Failed to add milestone'
      throw err
    }
  }

  async function toggleMilestone(goalId: number, milestoneId: number) {
    error.value = null
    try {
      const response = await api.patch(`/goals/${goalId}/milestones/${milestoneId}/toggle`)
      const goal = goals.value.find((g) => g.id === goalId)
      if (goal) {
        const idx = goal.milestones.findIndex((m) => m.id === milestoneId)
        if (idx !== -1) {
          const wasCompleted = goal.milestones[idx].completed
          goal.milestones[idx] = response.data
          goal.completedMilestones += wasCompleted ? -1 : 1
          syncGoalsCache()
        }
      }
      if (currentGoal.value?.id === goalId) {
        const idx = currentGoal.value.milestones.findIndex((m) => m.id === milestoneId)
        if (idx !== -1) {
          const wasCompleted = currentGoal.value.milestones[idx].completed
          currentGoal.value.milestones[idx] = response.data
          currentGoal.value.completedMilestones += wasCompleted ? -1 : 1
        }
      }
      return response.data
    } catch (err) {
      error.value = 'Failed to update milestone'
      throw err
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    goals,
    currentGoal,
    loading,
    error,
    activeGoals,
    completedGoals,
    fetchGoals,
    fetchGoal,
    createGoal,
    updateGoal,
    updateStatus,
    markGoalStarted,
    deleteGoal,
    addMilestone,
    toggleMilestone,
    clearError,
  }
})
