import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import TherapistCalendarView from '../TherapistCalendarView.vue'

function toIso(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const futureStart = new Date(Date.now() + 3 * 24 * 60 * 60 * 1000)
const futureEnd = new Date(futureStart.getTime() + 50 * 60 * 1000)
const pastStart = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
const pastEnd = new Date(pastStart.getTime() + 50 * 60 * 1000)

const patients = [
  {
    id: 10,
    name: 'Patient One',
    email: 'patient1@test.com',
    calendarColor: '#2563eb',
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
    calendarColor: '#2563eb',
    startAt: toIso(futureStart),
    endAt: toIso(futureEnd),
    status: 'SCHEDULED',
    reason: 'Follow-up',
    notes: 'Bring notes',
    durationMinutes: 50,
    recurrenceRule: null,
    seriesId: null,
    seriesIndex: null,
    createdAt: toIso(pastStart),
    updatedAt: toIso(pastStart),
  },
  {
    id: 2,
    therapistId: 3,
    patientId: 10,
    patientName: 'Patient One',
    patientEmail: 'patient1@test.com',
    startAt: toIso(pastStart),
    endAt: toIso(pastEnd),
    status: 'COMPLETED',
    reason: 'Past session',
    notes: null,
    durationMinutes: 50,
    recurrenceRule: null,
    seriesId: null,
    seriesIndex: null,
    createdAt: toIso(pastStart),
    updatedAt: toIso(pastStart),
  },
]

const mockGet = vi.fn()
const mockPost = vi.fn()
const mockPut = vi.fn()
const mockDelete = vi.fn()

vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: (...args: unknown[]) => mockPost(...args),
    put: (...args: unknown[]) => mockPut(...args),
    patch: vi.fn(),
    delete: (...args: unknown[]) => mockDelete(...args),
  },
}))

function formatInputDate(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

describe('TherapistCalendarView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockGet.mockReset()
    mockPost.mockReset()
    mockPut.mockReset()
    mockDelete.mockReset()
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

    const bookDate = new Date(Date.now() + 4 * 24 * 60 * 60 * 1000)
    const bookDateStr = formatInputDate(bookDate)
    const expectedStartAt = `${bookDateStr}T11:00:00`

    mockPost.mockResolvedValueOnce({
      data: {
        ...appointments[0],
        id: 3,
        startAt: expectedStartAt,
        endAt: toIso(new Date(new Date(expectedStartAt).getTime() + 50 * 60 * 1000)),
        reason: 'New follow-up',
      },
    })

    const wrapper = mount(TherapistCalendarView)
    await flushPromises()

    await wrapper.find('input[type="date"]').setValue(bookDateStr)
    await wrapper.find('input[type="time"]').setValue('11:00')
    await wrapper.find('input[type="text"]').setValue('New follow-up')
    await wrapper.find('textarea').setValue('Bring worksheets')

    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(mockPost).toHaveBeenCalledWith('/therapist/patients/10/appointments', {
      startAt: expectedStartAt,
      durationMinutes: 50,
      reason: 'New follow-up',
      notes: 'Bring worksheets',
      recurrence: 'NONE',
      recurrenceCount: undefined,
      recurrenceEndDate: undefined,
    })
    expect(wrapper.text()).toContain('Appointment booked')
  })

  it('saves a selected patient calendar color', async () => {
    mockGet.mockResolvedValueOnce({ data: patients })
    mockGet.mockResolvedValueOnce({ data: appointments })
    mockPut.mockResolvedValueOnce({
      data: {
        ...patients[0],
        calendarColor: '#10b981',
      },
    })

    const wrapper = mount(TherapistCalendarView)
    await flushPromises()

    await wrapper.findAll('.color-chip')[1].trigger('click')
    await flushPromises()

    expect(mockPut).toHaveBeenCalledWith('/therapist/patients/10/calendar-color', {
      calendarColor: '#10b981',
    })
  })

  it('shows the cancel button for scheduled appointments', async () => {
    mockGet.mockResolvedValueOnce({ data: patients })
    mockGet.mockResolvedValueOnce({ data: appointments })

    const wrapper = mount(TherapistCalendarView)
    await flushPromises()

    expect(wrapper.find('.btn-cancel-appointment').exists()).toBe(true)
    expect(wrapper.find('.btn-cancel-appointment').text()).toBe('Cancel')
  })
})
