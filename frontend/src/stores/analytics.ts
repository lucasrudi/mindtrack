import { ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'

export interface DashboardSummary {
  totalJournalEntries: number
  averageMood: number | null
  totalActivitiesLogged: number
  activityCompletionRate: number | null
  totalGoals: number
  completedGoals: number
  activeGoals: number
  validatedGoals: number
  pendingValidationGoals: number
}

export interface MoodTrend {
  date: string
  averageMood: number
  entryCount: number
}

export interface ActivityStat {
  activityType: string
  totalLogs: number
  completedLogs: number
  completionRate: number
}

export interface GoalProgress {
  status: string
  count: number
}

export interface DateRange {
  from: string
  to: string
}

function defaultDateRange(): DateRange {
  const to = new Date()
  const from = new Date()
  from.setDate(from.getDate() - 30)
  return {
    from: from.toISOString().split('T')[0],
    to: to.toISOString().split('T')[0],
  }
}

export const useAnalyticsStore = defineStore('analytics', () => {
  const summary = ref<DashboardSummary | null>(null)
  const moodTrends = ref<MoodTrend[]>([])
  const activityStats = ref<ActivityStat[]>([])
  const goalProgress = ref<GoalProgress[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const dateRange = ref<DateRange>(defaultDateRange())

  function buildParams() {
    return `from=${dateRange.value.from}&to=${dateRange.value.to}`
  }

  async function fetchSummary() {
    try {
      const response = await api.get(`/analytics/summary?${buildParams()}`)
      summary.value = response.data
    } catch (err) {
      error.value = 'Failed to load dashboard summary'
      throw err
    }
  }

  async function fetchMoodTrends() {
    try {
      const response = await api.get(`/analytics/mood-trends?${buildParams()}`)
      moodTrends.value = response.data
    } catch (err) {
      error.value = 'Failed to load mood trends'
      throw err
    }
  }

  async function fetchActivityStats() {
    try {
      const response = await api.get(`/analytics/activity-stats?${buildParams()}`)
      activityStats.value = response.data
    } catch (err) {
      error.value = 'Failed to load activity stats'
      throw err
    }
  }

  async function fetchGoalProgress() {
    try {
      const response = await api.get('/analytics/goal-progress')
      goalProgress.value = response.data
    } catch (err) {
      error.value = 'Failed to load goal progress'
      throw err
    }
  }

  async function fetchAll() {
    loading.value = true
    error.value = null
    try {
      await Promise.all([
        fetchSummary(),
        fetchMoodTrends(),
        fetchActivityStats(),
        fetchGoalProgress(),
      ])
    } catch {
      // Individual errors already set
    } finally {
      loading.value = false
    }
  }

  function setDateRange(from: string, to: string) {
    dateRange.value = { from, to }
  }

  function clearError() {
    error.value = null
  }

  return {
    summary,
    moodTrends,
    activityStats,
    goalProgress,
    loading,
    error,
    dateRange,
    fetchSummary,
    fetchMoodTrends,
    fetchActivityStats,
    fetchGoalProgress,
    fetchAll,
    setDateRange,
    clearError,
  }
})
