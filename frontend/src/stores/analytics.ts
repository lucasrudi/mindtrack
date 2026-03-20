import { ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'
import { getCachedAnalytics, setCachedAnalytics } from './dashboardSessionCache'

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

export interface ContentItem {
  type: string
  title: string
  body: string
  category: string
  url: string | null
  sourceType: string | null
  sourceLabel: string | null
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
  const contentItems = ref<ContentItem[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const dateRange = ref<DateRange>(defaultDateRange())
  let refreshToken = 0

  function buildParams() {
    return `from=${dateRange.value.from}&to=${dateRange.value.to}`
  }

  function cacheKey() {
    return `${dateRange.value.from}:${dateRange.value.to}`
  }

  function applyDashboardData(data: {
    summary: DashboardSummary | null
    moodTrends: MoodTrend[]
    activityStats: ActivityStat[]
    goalProgress: GoalProgress[]
    contentItems: ContentItem[]
  }) {
    summary.value = data.summary
    moodTrends.value = data.moodTrends
    activityStats.value = data.activityStats
    goalProgress.value = data.goalProgress
    contentItems.value = data.contentItems
  }

  async function loadDashboardData() {
    const [
      summaryResponse,
      moodTrendsResponse,
      activityStatsResponse,
      goalProgressResponse,
      contentResponse,
    ] = await Promise.all([
      api.get(`/analytics/summary?${buildParams()}`),
      api.get(`/analytics/mood-trends?${buildParams()}`),
      api.get(`/analytics/activity-stats?${buildParams()}`),
      api.get('/analytics/goal-progress'),
      api.get<ContentItem[]>('/analytics/content'),
    ])

    return {
      summary: summaryResponse.data as DashboardSummary,
      moodTrends: moodTrendsResponse.data as MoodTrend[],
      activityStats: activityStatsResponse.data as ActivityStat[],
      goalProgress: goalProgressResponse.data as GoalProgress[],
      contentItems: contentResponse.data as ContentItem[],
    }
  }

  async function refreshDashboardSilently(key: string, token: number) {
    try {
      const data = await loadDashboardData()
      if (token !== refreshToken || key !== cacheKey()) return
      applyDashboardData(data)
      setCachedAnalytics(key, data)
      error.value = null
    } catch {
      // Keep cached data visible if background refresh fails.
    }
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

  async function fetchContent() {
    try {
      const response = await api.get<ContentItem[]>('/analytics/content')
      contentItems.value = response.data
    } catch (err) {
      error.value = 'Failed to load content'
      throw err
    }
  }

  async function fetchAll() {
    const key = cacheKey()
    const cached = getCachedAnalytics(key)
    const token = ++refreshToken

    error.value = null

    if (cached) {
      applyDashboardData(cached)
      loading.value = false
      void refreshDashboardSilently(key, token)
      return
    }

    loading.value = true
    try {
      await Promise.all([
        fetchSummary(),
        fetchMoodTrends(),
        fetchActivityStats(),
        fetchGoalProgress(),
        fetchContent(),
      ])
      if (token !== refreshToken) return
      setCachedAnalytics(key, {
        summary: summary.value,
        moodTrends: moodTrends.value,
        activityStats: activityStats.value,
        goalProgress: goalProgress.value,
        contentItems: contentItems.value,
      })
    } catch {
      // Individual errors already set by the underlying fetch methods
    } finally {
      if (token === refreshToken) {
        loading.value = false
      }
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
    contentItems,
    loading,
    error,
    dateRange,
    fetchSummary,
    fetchMoodTrends,
    fetchActivityStats,
    fetchGoalProgress,
    fetchContent,
    fetchAll,
    setDateRange,
    clearError,
  }
})
