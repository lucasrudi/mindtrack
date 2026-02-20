import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import AdminView from '../AdminView.vue'

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
}))

const mockGet = vi.fn().mockResolvedValue({
  data: { content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 },
})
const mockPatch = vi.fn()
const mockPut = vi.fn()
vi.mock('@/services/api', () => ({
  default: {
    get: (...args: unknown[]) => mockGet(...args),
    post: vi.fn(),
    put: (...args: unknown[]) => mockPut(...args),
    patch: (...args: unknown[]) => mockPatch(...args),
    delete: vi.fn(),
  },
}))

const sampleUsers = {
  content: [
    {
      id: 1,
      email: 'admin@test.com',
      name: 'Admin User',
      role: 'ADMIN',
      enabled: true,
      createdAt: '2025-01-01T10:00:00',
      updatedAt: null,
    },
    {
      id: 2,
      email: 'user@test.com',
      name: 'Regular User',
      role: 'USER',
      enabled: true,
      createdAt: '2025-01-02T10:00:00',
      updatedAt: null,
    },
  ],
  totalElements: 2,
  totalPages: 1,
  number: 0,
  size: 20,
}

const sampleRoles = [
  {
    roleId: 1,
    roleName: 'ADMIN',
    permissions: [{ id: 1, resource: 'users', action: 'read' }],
  },
  {
    roleId: 2,
    roleName: 'USER',
    permissions: [],
  },
]

const samplePermissions = [
  { id: 1, resource: 'users', action: 'read' },
  { id: 2, resource: 'users', action: 'write' },
]

describe('AdminView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockGet.mockReset().mockResolvedValue({
      data: { content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 },
    })
    mockPatch.mockReset()
    mockPut.mockReset()
  })

  it('renders page header', () => {
    const wrapper = mount(AdminView)
    expect(wrapper.find('h1').text()).toBe('Admin Panel')
    expect(wrapper.find('.subtitle').text()).toContain('Manage users')
  })

  it('shows loading state while fetching', async () => {
    let resolveGet: (value: unknown) => void
    mockGet.mockReturnValueOnce(
      new Promise((resolve) => {
        resolveGet = resolve
      }),
    )
    const wrapper = mount(AdminView)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.loading').exists()).toBe(true)

    resolveGet!({ data: sampleUsers })
    await flushPromises()

    expect(wrapper.find('.loading').exists()).toBe(false)
  })

  it('renders users table when data loads', async () => {
    mockGet.mockResolvedValueOnce({ data: sampleUsers })
    const wrapper = mount(AdminView)
    await flushPromises()

    expect(wrapper.find('.users-table').exists()).toBe(true)
    const rows = wrapper.findAll('.users-table tbody tr')
    expect(rows).toHaveLength(2)
    expect(rows[0].text()).toContain('Admin User')
    expect(rows[0].text()).toContain('admin@test.com')
    expect(rows[1].text()).toContain('Regular User')
  })

  it('renders role select dropdowns for each user', async () => {
    mockGet.mockResolvedValueOnce({ data: sampleUsers })
    const wrapper = mount(AdminView)
    await flushPromises()

    const selects = wrapper.findAll('.role-select')
    expect(selects).toHaveLength(2)
  })

  it('renders enable/disable buttons', async () => {
    mockGet.mockResolvedValueOnce({ data: sampleUsers })
    const wrapper = mount(AdminView)
    await flushPromises()

    const buttons = wrapper.findAll('.users-table tbody .btn')
    expect(buttons).toHaveLength(2)
    expect(buttons[0].text()).toBe('Disable')
  })

  it('shows status badges', async () => {
    mockGet.mockResolvedValueOnce({ data: sampleUsers })
    const wrapper = mount(AdminView)
    await flushPromises()

    const badges = wrapper.findAll('.status-badge')
    expect(badges).toHaveLength(2)
    expect(badges[0].text()).toBe('Active')
    expect(badges[0].classes()).toContain('enabled')
  })

  it('has two tabs: Users and Roles & Permissions', () => {
    const wrapper = mount(AdminView)
    const tabs = wrapper.findAll('.tab')
    expect(tabs).toHaveLength(2)
    expect(tabs[0].text()).toBe('Users')
    expect(tabs[1].text()).toContain('Roles')
  })

  it('shows users tab as active by default', () => {
    const wrapper = mount(AdminView)
    const tabs = wrapper.findAll('.tab')
    expect(tabs[0].classes()).toContain('active')
    expect(tabs[1].classes()).not.toContain('active')
  })

  it('switches to roles tab and fetches data', async () => {
    mockGet.mockResolvedValueOnce({
      data: { content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 },
    })
    const wrapper = mount(AdminView)
    await flushPromises()

    mockGet.mockResolvedValueOnce({ data: sampleRoles })
    mockGet.mockResolvedValueOnce({ data: samplePermissions })

    await wrapper.findAll('.tab')[1].trigger('click')
    await flushPromises()

    expect(mockGet).toHaveBeenCalledWith('/admin/roles')
    expect(mockGet).toHaveBeenCalledWith('/admin/permissions')
  })

  it('renders role cards in roles tab', async () => {
    mockGet.mockResolvedValueOnce({
      data: { content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 },
    })
    const wrapper = mount(AdminView)
    await flushPromises()

    mockGet.mockResolvedValueOnce({ data: sampleRoles })
    mockGet.mockResolvedValueOnce({ data: samplePermissions })

    await wrapper.findAll('.tab')[1].trigger('click')
    await flushPromises()

    const roleCards = wrapper.findAll('.role-card')
    expect(roleCards).toHaveLength(2)
    expect(roleCards[0].find('h3').text()).toBe('ADMIN')
    expect(roleCards[1].find('h3').text()).toBe('USER')
  })

  it('shows error message and dismiss button', async () => {
    mockGet.mockImplementationOnce(() => Promise.reject(new Error('Network error')))
    const wrapper = mount(AdminView)
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.error-message').exists()).toBe(true)
    expect(wrapper.find('.error-message').text()).toContain('Failed to load users')
  })

  it('formats dates correctly', async () => {
    mockGet.mockResolvedValueOnce({ data: sampleUsers })
    const wrapper = mount(AdminView)
    await flushPromises()

    const dateCells = wrapper.findAll('.date-cell')
    expect(dateCells[0].text()).toContain('Jan')
    expect(dateCells[0].text()).toContain('2025')
  })

  it('shows no permissions message for role without permissions', async () => {
    mockGet.mockResolvedValueOnce({
      data: { content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 },
    })
    const wrapper = mount(AdminView)
    await flushPromises()

    mockGet.mockResolvedValueOnce({ data: sampleRoles })
    mockGet.mockResolvedValueOnce({ data: samplePermissions })

    await wrapper.findAll('.tab')[1].trigger('click')
    await flushPromises()

    expect(wrapper.find('.no-permissions').exists()).toBe(true)
    expect(wrapper.find('.no-permissions').text()).toContain('No permissions assigned')
  })
})
