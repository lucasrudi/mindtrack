import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import TherapistCalendarView from '../TherapistCalendarView.vue'

const patients = [
  {
    id: 10,
    name: 'Patient One',
    email: 'patient1@test.com',
    interviewCount: 0,
    activeGoalCount: 0,
    activityCount: 0,
    lastInterviewDate: null,
  },
]

const appointments = [
  {
    id: 1,
    therapistId: 3,
    patientId: 10,
    patientName: 'Patient One',
    patientEmail: 'patient1@test.com',
    startAt: '2026-04-20T10:00:00',
    endAt: '2026-04-20T10:50:00',
    status: 'SCHEDULED',
    reason: 'Follow-up',
    notes: 'Bring notes',
    durationMinutes: 50,
    createdAt: '2026-04-01T10:00:00',
    updatedAt: '2026-04-01T10:00:00',
  },
  {
    id: 2,
    therapistId: 3,
    patientId: 10,
    patientName: 'Patient One',
    patientEmail: 'patient1@test.com',
    startAt: '2026-03-10T10:00:00',
    endAt: '2026-03-10T10:50:00',
    status: 'COMPLETED',
    reason: 'Past session',
    notes: null,
    durationMinutes: 50,
    createdAt: '2026-03-01T10:00:00',
    updatedAt: '2026-03-01T10:00:00',
  },
]

const mockGet = vi.fn()
const mockPost = vi.fn()
const mockPatch = vi.fn()

vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
    put: vi.fn(),
    patch: (...args: unknown[]) => mockPatch(...args),
    delete: vi.fn(),
  },
}))

describe('TherapistCalendarView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockGet.mockReset()
    mockPost.mockReset()
    mockPatch.mockReset()
  })

  it('renders the appointment agenda', async () => {
    mockGet.mockResolvedValueOnce({ data: patients })
    mockGet.mockResolvedValueOnce({ data: appointments })

    const wrapper = mount(TherapistCalendarView)
    await flushPromises()

    expect(wrapper.find('h1').text()).toContain('Appointments')
    expect(wrapper.text()).toContain('Upcoming')
    expect(wrapper.text()).toContain('Past')
    expect(wrapper.text()).toContain('Patient One')
    expect(wrapper.text()).toContain('Follow-up')
  })

  it('books an appointment from the form', async () => {
    mockGet.mockResolvedValueOnce({ data: patients })
    mockGet.mockResolvedValueOnce({ data: appointments })
    mockPost.mockResolvedValueOnce({
      data: {
        ...appointments[0],
        id: 3,
        startAt: '2026-04-22T11:00:00',
        endAt: '2026-04-22T11:50:00',
        reason: 'New follow-up',
      },
    })

    const wrapper = mount(TherapistCalendarView)
    await flushPromises()

    await wrapper.find('input[type="date"]').setValue('2026-04-22')
    await wrapper.find('input[type="time"]').setValue('11:00')
    await wrapper.find('input[type="text"]').setValue('New follow-up')
    await wrapper.find('textarea').setValue('Bring worksheets')

    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(mockPost).toHaveBeenCalledWith('/therapist/patients/10/appointments', {
      startAt: '2026-04-22T11:00:00',
      endAt: '2026-04-22T11:50:00',
      reason: 'New follow-up',
      notes: 'Bring worksheets',
    })
    expect(wrapper.text()).toContain('Appointment booked')
  })

  it('cancels a scheduled appointment', async () => {
    mockGet.mockResolvedValueOnce({ data: patients })
    mockGet.mockResolvedValueOnce({ data: appointments })
    mockPatch.mockResolvedValueOnce({
      data: {
        ...appointments[0],
        status: 'CANCELLED',
      },
    })

    const wrapper = mount(TherapistCalendarView)
    await flushPromises()

    const cancelButton = wrapper
      .findAll('button')
      .find((button) => button.text().includes('Cancel appointment'))
    await cancelButton!.trigger('click')
    await flushPromises()

    expect(mockPatch).toHaveBeenCalledWith('/therapist/appointments/1/cancel')
    expect(wrapper.text()).toContain('Appointment cancelled')
  })
})
