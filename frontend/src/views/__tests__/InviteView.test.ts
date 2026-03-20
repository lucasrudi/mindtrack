import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import InviteView from '../InviteView.vue'
import { useAuthStore } from '@/stores/auth'

const mockPush = vi.fn()
const mockRoute = {
  params: { token: 'invite-token' },
  fullPath: '/invite/invite-token',
}

vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => ({ push: mockPush }),
}))

const fetchMock = vi.fn()

describe('InviteView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockPush.mockReset()
    fetchMock.mockReset()
    vi.stubGlobal('fetch', fetchMock)
  })

  it('shows loading state before preview resolves', async () => {
    fetchMock.mockReturnValue(new Promise(() => {}))

    const wrapper = mount(InviteView)

    expect(wrapper.text()).toContain('Loading invite...')
  })

  it('renders invite preview details after loading', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        initiatorName: 'Dr Smith',
        initiatorRole: 'THERAPIST',
        status: 'PENDING',
      }),
    })

    const wrapper = mount(InviteView)
    await flushPromises()

    expect(fetchMock).toHaveBeenCalledWith('/api/invites/invite-token')
    expect(wrapper.text()).toContain('Therapist request received')
    expect(wrapper.text()).toContain('Dr Smith')
    expect(wrapper.text()).toContain('(Therapist)')
    expect(wrapper.text()).toContain('PENDING')
  })

  it('shows preview fetch error when invite is invalid', async () => {
    fetchMock.mockResolvedValueOnce({ ok: false })

    const wrapper = mount(InviteView)
    await flushPromises()

    expect(wrapper.find('.error-msg').text()).toContain('Invalid or expired invite link')
  })

  it('redirects unauthenticated users to login when accepting', async () => {
    fetchMock.mockResolvedValueOnce({
      ok: true,
      json: async () => ({ initiatorName: 'Alex', initiatorRole: 'PATIENT', status: null }),
    })

    const wrapper = mount(InviteView)
    await flushPromises()

    await wrapper.find('.accept-btn').trigger('click')

    expect(mockPush).toHaveBeenCalledWith({
      name: 'login',
      query: { redirect: '/invite/invite-token' },
    })
  })

  it('accepts invite and redirects authenticated users to dashboard', async () => {
    const authStore = useAuthStore()
    authStore.setUser({
      id: '1',
      email: 'test@example.com',
      name: 'Test User',
      role: 'PATIENT',
      isPatient: true,
      isTherapist: false,
    })

    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          initiatorName: 'Dr Smith',
          initiatorRole: 'THERAPIST',
          status: 'PENDING',
        }),
      })
      .mockResolvedValueOnce({ ok: true })

    const wrapper = mount(InviteView)
    await flushPromises()

    await wrapper.find('.accept-btn').trigger('click')
    await flushPromises()

    expect(fetchMock).toHaveBeenLastCalledWith('/api/invites/invite-token/accept', {
      method: 'POST',
    })
    expect(mockPush).toHaveBeenCalledWith({ name: 'dashboard' })
  })

  it('shows an error when invite acceptance fails', async () => {
    const authStore = useAuthStore()
    authStore.setUser({
      id: '1',
      email: 'test@example.com',
      name: 'Test User',
      role: 'PATIENT',
      isPatient: true,
      isTherapist: false,
    })

    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          initiatorName: 'Dr Smith',
          initiatorRole: 'THERAPIST',
          status: 'PENDING',
        }),
      })
      .mockResolvedValueOnce({ ok: false })

    const wrapper = mount(InviteView)
    await flushPromises()

    await wrapper.find('.accept-btn').trigger('click')
    await flushPromises()

    expect(wrapper.find('.error-msg').text()).toContain('Failed to accept invite')
    expect(mockPush).not.toHaveBeenCalledWith({ name: 'dashboard' })
  })

  it('rejects request and redirects authenticated users to dashboard', async () => {
    const authStore = useAuthStore()
    authStore.setUser({
      id: '1',
      email: 'test@example.com',
      name: 'Test User',
      role: 'PATIENT',
      isPatient: true,
      isTherapist: false,
    })

    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          initiatorName: 'Dr Smith',
          initiatorRole: 'THERAPIST',
          status: 'PENDING',
        }),
      })
      .mockResolvedValueOnce({ ok: true })

    const wrapper = mount(InviteView)
    await flushPromises()

    await wrapper.find('.reject-btn').trigger('click')
    await flushPromises()

    expect(fetchMock).toHaveBeenLastCalledWith('/api/invites/invite-token/reject', {
      method: 'POST',
    })
    expect(mockPush).toHaveBeenCalledWith({ name: 'dashboard' })
  })
})
