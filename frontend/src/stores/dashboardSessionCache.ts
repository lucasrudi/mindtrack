import type {
  ActivityStat,
  ContentItem,
  DashboardSummary,
  GoalProgress,
  MoodTrend,
} from './analytics'
import type { DailyChecklistItem } from './activities'
import type { Goal } from './goals'
import type { UserProfile } from './profile'

export interface AnalyticsCacheEntry {
  summary: DashboardSummary | null
  moodTrends: MoodTrend[]
  activityStats: ActivityStat[]
  goalProgress: GoalProgress[]
  contentItems: ContentItem[]
}

interface DashboardSessionCache {
  analytics: Record<string, AnalyticsCacheEntry>
  goals: Goal[] | null
  profile: UserProfile | null
  checklist: Record<string, DailyChecklistItem[]>
}

const DASHBOARD_SESSION_CACHE_KEY = 'mindtrack:dashboard-session-cache'

function createEmptyCache(): DashboardSessionCache {
  return {
    analytics: {},
    goals: null,
    profile: null,
    checklist: {},
  }
}

function hasSessionStorage(): boolean {
  return typeof sessionStorage !== 'undefined'
}

function readCache(): DashboardSessionCache {
  if (!hasSessionStorage()) return createEmptyCache()

  try {
    const raw = sessionStorage.getItem(DASHBOARD_SESSION_CACHE_KEY)
    if (!raw) return createEmptyCache()

    const parsed = JSON.parse(raw) as Partial<DashboardSessionCache>
    return {
      analytics: parsed.analytics ?? {},
      goals: parsed.goals ?? null,
      profile: parsed.profile ?? null,
      checklist: parsed.checklist ?? {},
    }
  } catch {
    return createEmptyCache()
  }
}

function writeCache(cache: DashboardSessionCache) {
  if (!hasSessionStorage()) return
  sessionStorage.setItem(DASHBOARD_SESSION_CACHE_KEY, JSON.stringify(cache))
}

function updateCache(updater: (cache: DashboardSessionCache) => void) {
  const cache = readCache()
  updater(cache)
  writeCache(cache)
}

export function getCachedAnalytics(rangeKey: string): AnalyticsCacheEntry | null {
  return readCache().analytics[rangeKey] ?? null
}

export function setCachedAnalytics(rangeKey: string, entry: AnalyticsCacheEntry) {
  updateCache((cache) => {
    cache.analytics[rangeKey] = entry
  })
}

export function getCachedGoals(): Goal[] | null {
  return readCache().goals
}

export function setCachedGoals(goals: Goal[]) {
  updateCache((cache) => {
    cache.goals = goals
  })
}

export function getCachedProfile(): UserProfile | null {
  return readCache().profile
}

export function setCachedProfile(profile: UserProfile | null) {
  updateCache((cache) => {
    cache.profile = profile
  })
}

export function getCachedChecklist(date: string): DailyChecklistItem[] | null {
  return readCache().checklist[date] ?? null
}

export function setCachedChecklist(date: string, checklist: DailyChecklistItem[]) {
  updateCache((cache) => {
    cache.checklist[date] = checklist
  })
}

export function clearDashboardSessionCache() {
  if (!hasSessionStorage()) return
  sessionStorage.removeItem(DASHBOARD_SESSION_CACHE_KEY)
}
