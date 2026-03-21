import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAppointmentStore } from '../appointments'
import type { AppointmentSummary } from '../appointments'

const mockAppointments: AppointmentSummary[] = [
  {
    id: 1,
    therapistId: 3,
    patientId: 10,
    patientName: 'Patient One',
    patientEmail: 'patient1@test.com',
    calendarColor: '#2563eb',
    startAt: '2026-04-20T10:00:00',
    endAt: '2026-04-20T10:50:00',
    status: 'SCHEDULED',
    reason: 'Weekly check-in',
    notes: 'Bring journal',
    durationMinutes: 50,
    recurrenceRule: null,
    seriesId: null,
    seriesIndex: null,
    createdAt: '2026-04-01T10:00:00',
    updatedAt: '2026-04-01T10:00:00',
  },
]

const mockSeriesAppointments: AppointmentSummary[] = [
  {
    id: 10,
    therapistId: 3,
    patientId: 10,
    patientName: 'Patient One',
    patientEmail: 'patient1@test.com',
    calendarColor: '#2563eb',
    startAt: '2026-05-01T10:00:00',
    endAt: '2026-05-01T10:50:00',
    status: 'SCHEDULED',
    reason: 'Recurring therapy',
    notes: null,
    durationMinutes: 50,
    recurrenceRule: 'WEEKLY',
    seriesId: 'series-uuid-abc',
    seriesIndex: 0,
    createdAt: '2026-04-01T10:00:00',
    updatedAt: '2026-04-01T10:00:00',
  },
  {
    id: 11,
    therapistId: 3,
    patientId: 10,
    patientName: 'Patient One',
    patientEmail: 'patient1@test.com',
    calendarColor: '#2563eb',
    startAt: '2026-05-08T10:00:00',
    endAt: '2026-05-08T10:50:00',
    status: 'SCHEDULED',
    reason: 'Recurring therapy',
    notes: null,
    durationMinutes: 50,
    recurrenceRule: 'WEEKLY',
    seriesId: 'series-uuid-abc',
    seriesIndex: 1,
    createdAt: '2026-04-01T10:00:00',
    updatedAt: '2026-04-01T10:00:00',
  },
]

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('useAppointmentStore', () => {
  let api: {
    get: ReturnType<typeof vi.fn>
    post: ReturnType<typeof vi.fn>
    delete: ReturnType<typeof vi.fn>
  }

  beforeEach(async () => {
    setActivePinia(createPinia())
    const module = await import('@/services/api')
    api = module.default as unknown as typeof api
    vi.clearAllMocks()
  })

  it('fetches appointments', async () => {
    api.get.mockResolvedValueOnce({ data: mockAppointments })
    const store = useAppointmentStore()

    await store.fetchAppointments()

    expect(api.get).toHaveBeenCalledWith('/therapist/appointments')
    expect(store.appointments).toEqual(mockAppointments)
  })

  it('books a single appointment and sorts the list', async () => {
    api.post.mockResolvedValueOnce({
      data: {
        ...mockAppointments[0],
        id: 2,
        startAt: '2026-04-20T09:00:00',
      },
    })
    const store = useAppointmentStore()
    store.appointments = mockAppointments

    await store.bookAppointment(10, {
      startAt: '2026-04-20T09:00:00',
      durationMinutes: 50,
      reason: 'Follow-up',
      notes: '',
      recurrence: 'NONE',
    })

    expect(api.post).toHaveBeenCalledWith('/therapist/patients/10/appointments', {
      startAt: '2026-04-20T09:00:00',
      durationMinutes: 50,
      reason: 'Follow-up',
      notes: '',
      recurrence: 'NONE',
    })
    expect(store.appointments[0].id).toBe(2)
    expect(store.notice).toBe('Appointment booked')
  })

  it('books a recurring series and sets the correct notice', async () => {
    api.post.mockResolvedValueOnce({
      data: mockSeriesAppointments[0],
    })
    const store = useAppointmentStore()

    await store.bookAppointment(10, {
      startAt: '2026-05-01T10:00:00',
      durationMinutes: 50,
      reason: 'Recurring therapy',
      notes: '',
      recurrence: 'WEEKLY',
      recurrenceCount: 12,
    })

    expect(api.post).toHaveBeenCalledWith('/therapist/patients/10/appointments', {
      startAt: '2026-05-01T10:00:00',
      durationMinutes: 50,
      reason: 'Recurring therapy',
      notes: '',
      recurrence: 'WEEKLY',
      recurrenceCount: 12,
    })
    expect(store.notice).toBe('Recurring series booked')
  })

  it('cancels a single appointment and marks it cancelled in the list', async () => {
    api.delete.mockResolvedValueOnce({})
    const store = useAppointmentStore()
    store.appointments = mockAppointments

    await store.cancelAppointment(1, 'SINGLE')

    expect(api.delete).toHaveBeenCalledWith('/therapist/appointments/1', {
      params: { scope: 'SINGLE' },
    })
    expect(store.appointments[0].status).toBe('CANCELLED')
    expect(store.notice).toBe('Appointment cancelled')
  })

  it('cancels all in series and marks all series appointments cancelled', async () => {
    api.delete.mockResolvedValueOnce({})
    const store = useAppointmentStore()
    store.appointments = mockSeriesAppointments

    await store.cancelAppointment(10, 'ALL_IN_SERIES')

    expect(store.appointments.every((a) => a.status === 'CANCELLED')).toBe(true)
  })

  it('cancels this-and-following by seriesIndex', async () => {
    api.delete.mockResolvedValueOnce({})
    const store = useAppointmentStore()
    store.appointments = mockSeriesAppointments

    await store.cancelAppointment(11, 'THIS_AND_FOLLOWING')

    // index 0 stays SCHEDULED, index 1 gets CANCELLED
    expect(store.appointments[0].status).toBe('SCHEDULED')
    expect(store.appointments[1].status).toBe('CANCELLED')
  })

  it('updates calendar colors for a patient across loaded appointments', () => {
    const store = useAppointmentStore()
    store.appointments = mockAppointments

    store.updatePatientCalendarColor(10, '#10b981')

    expect(store.appointments[0].calendarColor).toBe('#10b981')
  })
})
