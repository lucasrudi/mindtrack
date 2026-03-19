import { describe, it, expect } from 'vitest'
import { useGoalIcon } from '../useGoalIcon'

describe('useGoalIcon', () => {
  const { getGoalIcon } = useGoalIcon()

  it('returns brain emoji for Mental Health', () => {
    const icon = getGoalIcon('Mental Health')
    expect(icon.emoji).toBe('🧠')
    expect(icon.label).toBe('Mental Health')
  })

  it('returns default icon for unknown category', () => {
    const icon = getGoalIcon('Unknown Category')
    expect(icon.emoji).toBe('🎯')
    expect(icon.label).toBe('Goal')
  })

  it('returns default icon for null', () => {
    const icon = getGoalIcon(null)
    expect(icon.emoji).toBe('🎯')
  })

  it('returns default icon for undefined', () => {
    const icon = getGoalIcon(undefined)
    expect(icon.emoji).toBe('🎯')
  })

  it('returns correct icons for all mapped categories', () => {
    expect(getGoalIcon('Wellness').emoji).toBe('✨')
    expect(getGoalIcon('Health').emoji).toBe('❤️')
    expect(getGoalIcon('Sleep').emoji).toBe('🌙')
    expect(getGoalIcon('Stress').emoji).toBe('💨')
    expect(getGoalIcon('Fitness').emoji).toBe('🏃')
    expect(getGoalIcon('Work').emoji).toBe('💼')
    expect(getGoalIcon('Relationships').emoji).toBe('👥')
  })
})
