import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ActivityCompletionChart from '../ActivityCompletionChart.vue'
import GoalProgressChart from '../GoalProgressChart.vue'
import MoodTrendChart from '../MoodTrendChart.vue'

const barProps = vi.fn()
const doughnutProps = vi.fn()
const lineProps = vi.fn()

vi.mock('vue-chartjs', () => ({
  Bar: {
    props: ['data', 'options'],
    template: '<div class="bar-chart" />',
    setup(props: unknown) {
      barProps(props)
    },
  },
  Doughnut: {
    props: ['data', 'options'],
    template: '<div class="doughnut-chart" />',
    setup(props: unknown) {
      doughnutProps(props)
    },
  },
  Line: {
    props: ['data', 'options'],
    template: '<div class="line-chart" />',
    setup(props: unknown) {
      lineProps(props)
    },
  },
}))

describe('Chart components', () => {
  it('renders the activity completion fallback and chart data', async () => {
    const emptyWrapper = mount(ActivityCompletionChart, { props: { data: [] } })
    expect(emptyWrapper.text()).toContain('No activity data for this period')

    mount(ActivityCompletionChart, {
      props: {
        data: [
          { activityType: 'HOMEWORK_TASK', completionRate: 75, completedLogs: 3, totalLogs: 4 },
        ],
      },
    })

    const props = barProps.mock.calls.at(-1)?.[0] as {
      data: { labels: string[]; datasets: Array<{ data: number[] }> }
      options: {
        plugins: {
          tooltip: { callbacks: { afterLabel: (context: { dataIndex: number }) => string } }
        }
      }
    }
    expect(props.data.labels).toEqual(['Homework Task'])
    expect(props.data.datasets[0].data).toEqual([75])
    expect(props.options.plugins.tooltip.callbacks.afterLabel({ dataIndex: 0 })).toBe(
      '3/4 completed',
    )
  })

  it('renders the goal progress fallback and chart labels', () => {
    const emptyWrapper = mount(GoalProgressChart, { props: { data: [] } })
    expect(emptyWrapper.text()).toContain('No goals yet')

    mount(GoalProgressChart, {
      props: {
        data: [{ status: 'IN_PROGRESS', count: 2 }],
      },
    })

    const props = doughnutProps.mock.calls.at(-1)?.[0] as {
      data: { labels: string[]; datasets: Array<{ data: number[]; backgroundColor: string[] }> }
    }
    expect(props.data.labels).toEqual(['In Progress'])
    expect(props.data.datasets[0].data).toEqual([2])
    expect(props.data.datasets[0].backgroundColor).toEqual(['#6366f1'])
  })

  it('renders the mood trend fallback and formats tooltip labels', () => {
    const emptyWrapper = mount(MoodTrendChart, { props: { data: [] } })
    expect(emptyWrapper.text()).toContain('No mood data for this period')

    mount(MoodTrendChart, {
      props: {
        data: [
          { date: '2025-01-10', averageMood: 6.5, entryCount: 1 },
          { date: '2025-01-11', averageMood: 7, entryCount: 3 },
        ],
      },
    })

    const props = lineProps.mock.calls.at(-1)?.[0] as {
      data: { labels: string[]; datasets: Array<{ data: number[] }> }
      options: {
        plugins: {
          tooltip: { callbacks: { afterLabel: (context: { dataIndex: number }) => string } }
        }
      }
    }
    expect(props.data.labels).toEqual(['Jan 10', 'Jan 11'])
    expect(props.data.datasets[0].data).toEqual([6.5, 7])
    expect(props.options.plugins.tooltip.callbacks.afterLabel({ dataIndex: 0 })).toBe('1 entry')
    expect(props.options.plugins.tooltip.callbacks.afterLabel({ dataIndex: 1 })).toBe('3 entries')
  })
})
