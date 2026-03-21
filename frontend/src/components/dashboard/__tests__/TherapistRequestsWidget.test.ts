import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import TherapistRequestsWidget from '../TherapistRequestsWidget.vue'
import type { TherapistRequest } from '@/stores/patient'

function makeRequest(overrides: Partial<TherapistRequest> = {}): TherapistRequest {
  return {
    relationshipId: 1,
    therapistId: 10,
    therapistName: 'Dr. Smith',
    therapistEmail: 'smith@example.com',
    status: 'PENDING',
    createdAt: '2026-01-01T10:00:00',
    ...overrides,
  }
}

describe('TherapistRequestsWidget', () => {
  it('shows empty state when no requests', () => {
    const wrapper = mount(TherapistRequestsWidget, {
      props: { requests: [], loading: false },
    })
    expect(wrapper.find('[data-testid="empty-state"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="pending-request"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="active-connection"]').exists()).toBe(false)
  })

  it('renders pending requests with accept and reject buttons', () => {
    const wrapper = mount(TherapistRequestsWidget, {
      props: { requests: [makeRequest({ status: 'PENDING' })], loading: false },
    })
    const pending = wrapper.find('[data-testid="pending-request"]')
    expect(pending.exists()).toBe(true)
    expect(pending.text()).toContain('Dr. Smith')
    expect(pending.text()).toContain('smith@example.com')
    expect(wrapper.find('[data-testid="accept-btn"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="reject-btn"]').exists()).toBe(true)
  })

  it('renders active connections without action buttons', () => {
    const wrapper = mount(TherapistRequestsWidget, {
      props: { requests: [makeRequest({ status: 'ACTIVE' })], loading: false },
    })
    const active = wrapper.find('[data-testid="active-connection"]')
    expect(active.exists()).toBe(true)
    expect(active.text()).toContain('Dr. Smith')
    expect(wrapper.find('[data-testid="accept-btn"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="reject-btn"]').exists()).toBe(false)
  })

  it('emits accept event with relationshipId when accept is clicked', async () => {
    const wrapper = mount(TherapistRequestsWidget, {
      props: { requests: [makeRequest({ relationshipId: 42, status: 'PENDING' })], loading: false },
    })
    await wrapper.find('[data-testid="accept-btn"]').trigger('click')
    expect(wrapper.emitted('accept')).toEqual([[42]])
  })

  it('emits reject event with relationshipId when reject is clicked', async () => {
    const wrapper = mount(TherapistRequestsWidget, {
      props: { requests: [makeRequest({ relationshipId: 7, status: 'PENDING' })], loading: false },
    })
    await wrapper.find('[data-testid="reject-btn"]').trigger('click')
    expect(wrapper.emitted('reject')).toEqual([[7]])
  })

  it('shows loading state', () => {
    const wrapper = mount(TherapistRequestsWidget, {
      props: { requests: [], loading: true },
    })
    expect(wrapper.find('.loading-state').exists()).toBe(true)
    expect(wrapper.find('[data-testid="empty-state"]').exists()).toBe(false)
  })

  it('does not show empty state when loading', () => {
    const wrapper = mount(TherapistRequestsWidget, {
      props: { requests: [], loading: true },
    })
    expect(wrapper.find('[data-testid="empty-state"]').exists()).toBe(false)
  })
})
