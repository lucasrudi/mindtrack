import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import TherapistView from '../TherapistView.vue'

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
}))

const mockPatients = [
  {
    id: 1,
    name: 'Patient One',
    email: 'patient1@test.com',
    interviewCount: 3,
    activeGoalCount: 2,
    activityCount: 5,
    lastInterviewDate: '2025-01-15T10:00:00',
  },
  {
    id: 2,
    name: 'Patient Two',
    email: 'patient2@test.com',
    interviewCount: 0,
    activeGoalCount: 0,
    activityCount: 0,
    lastInterviewDate: null,
  },
]

const mockPatientDetail = {
  patientId: 1,
  patientName: 'Patient One',
  patientEmail: 'patient1@test.com',
  interviews: [
    {
      id: 1,
      interviewDate: '2025-01-15',
      moodBefore: 5,
      moodAfter: 7,
      topics: ['anxiety'],
      notes: 'Good session',
      createdAt: '2025-01-15T10:00:00',
    },
  ],
  activities: [
    {
      id: 1,
      type: 'HOMEWORK',
      name: 'Breathing exercises',
      description: 'Daily practice',
      active: true,
      createdAt: '2025-01-10T10:00:00',
    },
  ],
  goals: [
    {
      id: 1,
      title: 'Reduce anxiety',
      description: null,
      status: 'IN_PROGRESS',
      targetDate: '2025-06-01',
      totalMilestones: 3,
      completedMilestones: 1,
      createdAt: '2025-01-01T10:00:00',
    },
  ],
  sharedJournalEntries: [
    {
      id: 1,
      entryDate: '2025-01-14',
      title: 'Feeling better',
      content: 'Today was good.',
      mood: 7,
      tags: ['positive'],
      createdAt: '2025-01-14T20:00:00',
    },
  ],
}

const mockGet = vi.fn().mockResolvedValue({ data: [] })
vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('TherapistView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockGet.mockReset().mockResolvedValue({ data: [] })
  })

  it('renders page header', () => {
    const wrapper = mount(TherapistView)
    expect(wrapper.find('h1').text()).toBe('Patient Dashboard')
    expect(wrapper.find('.subtitle').text()).toContain('patients')
  })

  it('shows loading state while fetching', async () => {
    let resolveGet: (value: unknown) => void
    mockGet.mockReturnValueOnce(
      new Promise((resolve) => {
        resolveGet = resolve
      }),
    )
    const wrapper = mount(TherapistView)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.loading').exists()).toBe(true)

    resolveGet!({ data: mockPatients })
    await flushPromises()

    expect(wrapper.find('.loading').exists()).toBe(false)
  })

  it('renders patient table when data loads', async () => {
    mockGet.mockResolvedValueOnce({ data: mockPatients })
    const wrapper = mount(TherapistView)
    await flushPromises()

    expect(wrapper.find('.data-table').exists()).toBe(true)
    const rows = wrapper.findAll('.patient-row')
    expect(rows).toHaveLength(2)
    expect(rows[0].text()).toContain('Patient One')
    expect(rows[0].text()).toContain('patient1@test.com')
  })

  it('shows empty state when no patients', async () => {
    mockGet.mockResolvedValueOnce({ data: [] })
    const wrapper = mount(TherapistView)
    await flushPromises()

    expect(wrapper.find('.empty-state').exists()).toBe(true)
    expect(wrapper.find('.empty-state').text()).toContain('No patients')
  })

  it('navigates to patient detail on row click', async () => {
    mockGet.mockResolvedValueOnce({ data: mockPatients })
    const wrapper = mount(TherapistView)
    await flushPromises()

    mockGet.mockResolvedValueOnce({ data: mockPatientDetail })
    await wrapper.findAll('.patient-row')[0].trigger('click')
    await flushPromises()

    expect(mockGet).toHaveBeenCalledWith('/therapist/patients/1')
    expect(wrapper.find('.patient-header h2').text()).toBe('Patient One')
    expect(wrapper.find('.patient-email').text()).toBe('patient1@test.com')
  })

  it('shows tabs in patient detail view', async () => {
    mockGet.mockResolvedValueOnce({ data: mockPatients })
    const wrapper = mount(TherapistView)
    await flushPromises()

    mockGet.mockResolvedValueOnce({ data: mockPatientDetail })
    await wrapper.findAll('.patient-row')[0].trigger('click')
    await flushPromises()

    const tabs = wrapper.findAll('.tab')
    expect(tabs).toHaveLength(4)
    expect(tabs[0].text()).toContain('Interviews')
    expect(tabs[1].text()).toContain('Activities')
    expect(tabs[2].text()).toContain('Goals')
    expect(tabs[3].text()).toContain('Shared Journal')
  })

  it('shows back button that returns to patient list', async () => {
    mockGet.mockResolvedValueOnce({ data: mockPatients })
    const wrapper = mount(TherapistView)
    await flushPromises()

    mockGet.mockResolvedValueOnce({ data: mockPatientDetail })
    await wrapper.findAll('.patient-row')[0].trigger('click')
    await flushPromises()

    expect(wrapper.find('.back-btn').exists()).toBe(true)

    await wrapper.find('.back-btn').trigger('click')
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.patient-header').exists()).toBe(false)
    expect(wrapper.findAll('.patient-row')).toHaveLength(2)
  })

  it('shows error message on fetch failure', async () => {
    mockGet.mockRejectedValueOnce(new Error('Network error'))
    const wrapper = mount(TherapistView)
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.error-message').exists()).toBe(true)
    expect(wrapper.find('.error-message').text()).toContain('Failed to load patients')
  })

  it('formats dates correctly', async () => {
    mockGet.mockResolvedValueOnce({ data: mockPatients })
    const wrapper = mount(TherapistView)
    await flushPromises()

    const dateCells = wrapper.findAll('.cell-date')
    expect(dateCells[0].text()).toContain('Jan')
    expect(dateCells[0].text()).toContain('2025')
  })
})
