export interface GoalIcon {
  emoji: string
  label: string
}

const CATEGORY_ICON_MAP: Record<string, GoalIcon> = {
  'Mental Health': { emoji: '🧠', label: 'Mental Health' },
  Wellness: { emoji: '✨', label: 'Wellness' },
  Health: { emoji: '❤️', label: 'Health' },
  Sleep: { emoji: '🌙', label: 'Sleep' },
  Stress: { emoji: '💨', label: 'Stress' },
  Fitness: { emoji: '🏃', label: 'Fitness' },
  Work: { emoji: '💼', label: 'Work' },
  Relationships: { emoji: '👥', label: 'Relationships' },
}

const DEFAULT_ICON: GoalIcon = { emoji: '🎯', label: 'Goal' }

export function useGoalIcon() {
  function getGoalIcon(category: string | null | undefined): GoalIcon {
    if (!category) return DEFAULT_ICON
    return CATEGORY_ICON_MAP[category] ?? DEFAULT_ICON
  }

  return { getGoalIcon }
}
