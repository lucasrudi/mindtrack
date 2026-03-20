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

  it('books an appointment and sorts the list', async () => {
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
      endAt: '2026-04-20T09:50:00',
      reason: 'Follow-up',
      notes: '',
    })

    expect(api.post).toHaveBeenCalledWith('/therapist/patients/10/appointments', {
      startAt: '2026-04-20T09:00:00',
      endAt: '2026-04-20T09:50:00',
      reason: 'Follow-up',
      notes: '',
    })
    expect(store.appointments[0].id).toBe(2)
    expect(store.notice).toBe('Appointment booked')
  })

  it('updates calendar colors for a patient across loaded appointments', () => {
    const store = useAppointmentStore()
    store.appointments = mockAppointments

    store.updatePatientCalendarColor(10, '#10b981')

    expect(store.appointments[0].calendarColor).toBe('#10b981')
  })
})
