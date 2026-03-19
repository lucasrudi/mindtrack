import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import ActiveGoalsWidget from '../ActiveGoalsWidget.vue'
import type { Goal } from '@/stores/goals'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div />' } },
    { path: '/goals', name: 'goals', component: { template: '<div />' } },
    { path: '/goals/:id', name: 'goal-detail', component: { template: '<div />' } },
  ],
})

function makeGoal(overrides: Partial<Goal> = {}): Goal {
  return {
    id: 1,
    title: 'Run a 5K',
    description: null,
    category: 'Health',
    targetDate: '2026-06-01',
    status: 'IN_PROGRESS',
    validationStatus: null,
    totalMilestones: 4,
    completedMilestones: 2,
    milestones: [],
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
    ...overrides,
  }
}

describe('ActiveGoalsWidget', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders goal cards when goals are provided', async () => {
    const goals = [makeGoal(), makeGoal({ id: 2, title: 'Read 12 books' })]
    const wrapper = mount(ActiveGoalsWidget, {
      props: { goals },
      global: { plugins: [router] },
    })
    expect(wrapper.text()).toContain('Run a 5K')
    expect(wrapper.text()).toContain('Read 12 books')
  })

  it('shows empty state when no goals are provided', () => {
    const wrapper = mount(ActiveGoalsWidget, {
      props: { goals: [] },
      global: { plugins: [router] },
    })
    expect(wrapper.text()).toContain('No active goals yet')
    expect(wrapper.find('.empty-link').exists()).toBe(true)
  })

  it('shows 0% progress when goal has no milestones', () => {
    const goal = makeGoal({ totalMilestones: 0, completedMilestones: 0 })
    const wrapper = mount(ActiveGoalsWidget, {
      props: { goals: [goal] },
      global: { plugins: [router] },
    })
    expect(wrapper.text()).toContain('0%')
    const fill = wrapper.find('.progress-bar__fill')
    expect(fill.attributes('style')).toContain('width: 0%')
  })

  it('shows 100% progress when all milestones are complete', () => {
    const goal = makeGoal({ totalMilestones: 3, completedMilestones: 3 })
    const wrapper = mount(ActiveGoalsWidget, {
      props: { goals: [goal] },
      global: { plugins: [router] },
    })
    expect(wrapper.text()).toContain('100%')
    const fill = wrapper.find('.progress-bar__fill')
    expect(fill.attributes('style')).toContain('width: 100%')
  })

  it('renders goal cards as router-links to goal-detail', () => {
    const goal = makeGoal({ id: 42 })
    const wrapper = mount(ActiveGoalsWidget, {
      props: { goals: [goal] },
      global: { plugins: [router] },
    })
    const link = wrapper.find('a')
    expect(link.attributes('href')).toContain('42')
  })

  it('displays the category pill when category is set', () => {
    const goal = makeGoal({ category: 'Fitness' })
    const wrapper = mount(ActiveGoalsWidget, {
      props: { goals: [goal] },
      global: { plugins: [router] },
    })
    expect(wrapper.find('.category-pill').text()).toBe('Fitness')
  })

  it('shows "No target date" when targetDate is null', () => {
    const goal = makeGoal({ targetDate: null })
    const wrapper = mount(ActiveGoalsWidget, {
      props: { goals: [goal] },
      global: { plugins: [router] },
    })
    expect(wrapper.text()).toContain('No target date')
  })
})
