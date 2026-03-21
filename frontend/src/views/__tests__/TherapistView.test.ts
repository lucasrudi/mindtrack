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
    status: 'ACTIVE',
  },
  {
    id: 2,
    name: 'Patient Two',
    email: 'patient2@test.com',
    interviewCount: 0,
    activeGoalCount: 0,
    activityCount: 0,
    lastInterviewDate: null,
    status: 'ACTIVE',
  },
]

const mockPendingPatients = [
  {
    id: 3,
    name: 'Pending Patient',
    email: 'pending@test.com',
    interviewCount: 0,
    activeGoalCount: 0,
    activityCount: 0,
    lastInterviewDate: null,
    status: 'PENDING',
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
const mockPost = vi.fn()
vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('TherapistView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockGet.mockReset().mockResolvedValue({ data: [] })
    mockPost.mockReset().mockResolvedValue({
      data: { token: 'request-token', url: 'http://localhost:3000/invite/request-token' },
    })
  })

  it('renders therapist overview and patient list', async () => {
    mockGet.mockResolvedValueOnce({ data: mockPatients })
    mockGet.mockResolvedValueOnce({ data: mockPendingPatients })
    const wrapper = mount(TherapistView)
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('Therapist Dashboard')
    expect(wrapper.find('.subtitle').text()).toContain('Aggregate caseload overview')
    expect(wrapper.findAll('.overview-card')).toHaveLength(4)
    expect(wrapper.find('.overview-panel').text()).toContain('Assigned patients')
    expect(wrapper.find('.data-table').exists()).toBe(true)
    expect(wrapper.findAll('.patient-row')).toHaveLength(2)
  })

  it('shows aggregate counts from the assigned patient list', async () => {
    mockGet.mockResolvedValueOnce({ data: mockPatients })
    mockGet.mockResolvedValueOnce({ data: mockPendingPatients })
    const wrapper = mount(TherapistView)
    await flushPromises()

    const cards = wrapper.findAll('.overview-card')
    expect(cards[0].text()).toContain('2')
    expect(cards[1].text()).toContain('3')
    expect(cards[2].text()).toContain('2')
    expect(cards[3].text()).toContain('5')
  })

  it('renders pending requests section', async () => {
    mockGet.mockResolvedValueOnce({ data: mockPatients })
    mockGet.mockResolvedValueOnce({ data: mockPendingPatients })
    const wrapper = mount(TherapistView)
    await flushPromises()

    expect(wrapper.text()).toContain('Pending requests')
    expect(wrapper.text()).toContain('Pending Patient')
    expect(wrapper.text()).toContain('PENDING')
  })

  it('loads patient detail without hiding the overview list', async () => {
    mockGet.mockResolvedValueOnce({ data: mockPatients })
    mockGet.mockResolvedValueOnce({ data: mockPendingPatients })
    const wrapper = mount(TherapistView)
    await flushPromises()

    mockGet.mockResolvedValueOnce({ data: mockPatientDetail })
    await wrapper.findAll('.patient-row')[0].trigger('click')
    await flushPromises()

    expect(mockGet).toHaveBeenCalledWith('/therapist/patients/1')
    expect(wrapper.find('.patient-detail-panel').text()).toContain('Patient One')
    expect(wrapper.find('.patient-detail-panel').text()).toContain(
      'Showing patient-specific insights only',
    )
    expect(wrapper.findAll('.patient-row')).toHaveLength(2)
    expect(wrapper.findAll('.snapshot-card')).toHaveLength(4)
    expect(wrapper.find('.patient-row.selected').text()).toContain('Patient One')
  })

  it('shows tabs and selected patient insights', async () => {
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
    expect(wrapper.find('.tab-content').text()).toContain('Good session')
  })

  it('submits a patient request from the dashboard', async () => {
    mockGet.mockResolvedValueOnce({ data: mockPatients })
    mockGet.mockResolvedValueOnce({ data: mockPendingPatients })
    const wrapper = mount(TherapistView)
    await flushPromises()

    const requestInput = wrapper.find('input[type="email"]')
    await requestInput.setValue('newpatient@test.com')
    await wrapper.find('.request-panel .btn-primary').trigger('click')
    await flushPromises()

    expect(mockGet).toHaveBeenCalledWith('/therapist/patients/pending')
    expect(wrapper.text()).toContain('Request sent')
  })

  it('returns to the overview when the selected patient is cleared', async () => {
    mockGet.mockResolvedValueOnce({ data: mockPatients })
    mockGet.mockResolvedValueOnce({ data: mockPendingPatients })
    const wrapper = mount(TherapistView)
    await flushPromises()

    mockGet.mockResolvedValueOnce({ data: mockPatientDetail })
    await wrapper.findAll('.patient-row')[0].trigger('click')
    await flushPromises()

    await wrapper.find('.back-btn').trigger('click')
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.patient-detail-panel').text()).toContain(
      'Select a patient to view their individual insights',
    )
    expect(wrapper.find('.patient-row.selected').exists()).toBe(false)
  })

  it('shows an error message when patients fail to load', async () => {
    mockGet.mockRejectedValueOnce(new Error('Network error'))
    const wrapper = mount(TherapistView)
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.error-message').exists()).toBe(true)
    expect(wrapper.find('.error-message').text()).toContain('Failed to load patients')
  })
})
