import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PatientAppointmentsView from '../PatientAppointmentsView.vue'

const appointments = [
  {
    id: 1,
    therapistId: 3,
    therapistName: 'Dr. Lane',
    therapistEmail: 'therapist@test.com',
    patientId: 10,
    patientName: 'Patient One',
    patientEmail: 'patient@test.com',
    startAt: '2026-04-20T10:00:00',
    endAt: '2026-04-20T10:50:00',
    status: 'SCHEDULED',
    reason: 'Follow-up',
    notes: null,
    durationMinutes: 50,
    createdAt: '2026-04-01T10:00:00',
    updatedAt: '2026-04-01T10:00:00',
  },
]

const mockGet = vi.fn()
const mockPatch = vi.fn()

vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: vi.fn(),
    put: vi.fn(),
    patch: (...args: unknown[]) => mockPatch(...args),
    delete: vi.fn(),
  },
}))

describe('PatientAppointmentsView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockGet.mockReset()
    mockPatch.mockReset()
  })

  it('renders the patient appointment list', async () => {
    mockGet.mockResolvedValueOnce({ data: appointments })

    const wrapper = mount(PatientAppointmentsView)
    await flushPromises()

    expect(wrapper.find('h1').text()).toContain('My Appointments')
    expect(wrapper.text()).toContain('Dr. Lane')
    expect(wrapper.text()).toContain('Follow-up')
  })

  it('cancels a patient appointment', async () => {
    mockGet.mockResolvedValueOnce({ data: appointments })
    mockPatch.mockResolvedValueOnce({
      data: {
        ...appointments[0],
        status: 'CANCELLED',
      },
    })

    const wrapper = mount(PatientAppointmentsView)
    await flushPromises()

    await wrapper.find('button.btn.btn-secondary').trigger('click')
    await flushPromises()

    expect(mockPatch).toHaveBeenCalledWith('/patient/appointments/1/cancel')
    expect(wrapper.text()).toContain('Appointment cancelled')
  })
})
