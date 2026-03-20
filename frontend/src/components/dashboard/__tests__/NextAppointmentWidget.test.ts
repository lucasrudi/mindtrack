import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import NextAppointmentWidget from '../NextAppointmentWidget.vue'

const mockGet = vi.fn()

vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

vi.mock('vue-router', () => ({
  RouterLink: {
    name: 'RouterLink',
    props: ['to'],
    template: '<a :href="to"><slot /></a>',
  },
}))

describe('NextAppointmentWidget', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockGet.mockReset()
  })

  it('shows the next upcoming appointment', async () => {
    mockGet.mockResolvedValueOnce({
      data: [
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
        {
          id: 2,
          therapistId: 3,
          therapistName: 'Dr. Lane',
          therapistEmail: 'therapist@test.com',
          patientId: 10,
          patientName: 'Patient One',
          patientEmail: 'patient@test.com',
          startAt: '2026-03-01T10:00:00',
          endAt: '2026-03-01T10:50:00',
          status: 'CANCELLED',
          reason: 'Past appointment',
          notes: null,
          durationMinutes: 50,
          createdAt: '2026-02-01T10:00:00',
          updatedAt: '2026-02-01T10:00:00',
        },
      ],
    })

    const wrapper = mount(NextAppointmentWidget)
    await flushPromises()

    expect(mockGet).toHaveBeenCalledWith('/patient/appointments')
    expect(wrapper.find('[data-testid="appointment-card"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Dr. Lane')
    expect(wrapper.text()).toContain('Follow-up')
    expect(wrapper.text()).toContain('View all appointments')
  })

  it('shows an empty state when there is no upcoming appointment', async () => {
    mockGet.mockResolvedValueOnce({ data: [] })

    const wrapper = mount(NextAppointmentWidget)
    await flushPromises()

    expect(wrapper.find('[data-testid="empty-state"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('No upcoming appointments scheduled')
  })
})
