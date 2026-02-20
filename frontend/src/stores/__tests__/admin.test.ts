import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAdminStore } from '../admin'

const mockUsers = {
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

const mockRoles = [
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

const mockPermissions = [
  { id: 1, resource: 'users', action: 'read' },
  { id: 2, resource: 'users', action: 'write' },
  { id: 3, resource: 'journal', action: 'read' },
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

describe('useAdminStore', () => {
  let api: {
    get: ReturnType<typeof vi.fn>
    post: ReturnType<typeof vi.fn>
    put: ReturnType<typeof vi.fn>
    patch: ReturnType<typeof vi.fn>
    delete: ReturnType<typeof vi.fn>
  }

  beforeEach(async () => {
    setActivePinia(createPinia())
    const module = await import('@/services/api')
    api = module.default as unknown as typeof api
    vi.clearAllMocks()
  })

  describe('fetchUsers', () => {
    it('fetches paginated users', async () => {
      api.get.mockResolvedValueOnce({ data: mockUsers })
      const store = useAdminStore()

      await store.fetchUsers()

      expect(api.get).toHaveBeenCalledWith('/admin/users?page=0&size=20')
      expect(store.users).toEqual(mockUsers)
      expect(store.loading).toBe(false)
    })

    it('fetches users with custom page and size', async () => {
      api.get.mockResolvedValueOnce({ data: mockUsers })
      const store = useAdminStore()

      await store.fetchUsers(2, 10)

      expect(api.get).toHaveBeenCalledWith('/admin/users?page=2&size=10')
    })

    it('sets loading state during fetch', async () => {
      let resolvePromise: (value: unknown) => void
      const promise = new Promise((resolve) => {
        resolvePromise = resolve
      })
      api.get.mockReturnValueOnce(promise)
      const store = useAdminStore()

      const fetchPromise = store.fetchUsers()
      expect(store.loading).toBe(true)

      resolvePromise!({ data: mockUsers })
      await fetchPromise
      expect(store.loading).toBe(false)
    })

    it('sets error on failure', async () => {
      api.get.mockRejectedValueOnce(new Error('Network error'))
      const store = useAdminStore()

      await expect(store.fetchUsers()).rejects.toThrow()
      expect(store.error).toBe('Failed to load users')
      expect(store.loading).toBe(false)
    })
  })

  describe('changeUserRole', () => {
    it('changes user role and updates list', async () => {
      const updatedUser = { ...mockUsers.content[1], role: 'ADMIN' }
      api.patch.mockResolvedValueOnce({ data: updatedUser })
      const store = useAdminStore()
      store.users = { ...mockUsers, content: [...mockUsers.content] }

      await store.changeUserRole(2, 'ADMIN')

      expect(api.patch).toHaveBeenCalledWith('/admin/users/2/role', { role: 'ADMIN' })
      expect(store.users!.content[1].role).toBe('ADMIN')
    })

    it('sets error on failure', async () => {
      api.patch.mockRejectedValueOnce(new Error('Forbidden'))
      const store = useAdminStore()

      await expect(store.changeUserRole(2, 'ADMIN')).rejects.toThrow()
      expect(store.error).toBe('Failed to change user role')
    })
  })

  describe('setUserEnabled', () => {
    it('disables a user', async () => {
      const disabledUser = { ...mockUsers.content[1], enabled: false }
      api.patch.mockResolvedValueOnce({ data: disabledUser })
      const store = useAdminStore()
      store.users = { ...mockUsers, content: [...mockUsers.content] }

      await store.setUserEnabled(2, false)

      expect(api.patch).toHaveBeenCalledWith('/admin/users/2/enabled', { enabled: false })
      expect(store.users!.content[1].enabled).toBe(false)
    })

    it('enables a user', async () => {
      const enabledUser = { ...mockUsers.content[1], enabled: true }
      api.patch.mockResolvedValueOnce({ data: enabledUser })
      const store = useAdminStore()
      store.users = {
        ...mockUsers,
        content: [mockUsers.content[0], { ...mockUsers.content[1], enabled: false }],
      }

      await store.setUserEnabled(2, true)

      expect(api.patch).toHaveBeenCalledWith('/admin/users/2/enabled', { enabled: true })
      expect(store.users!.content[1].enabled).toBe(true)
    })

    it('sets error on failure', async () => {
      api.patch.mockRejectedValueOnce(new Error('Error'))
      const store = useAdminStore()

      await expect(store.setUserEnabled(2, false)).rejects.toThrow()
      expect(store.error).toBe('Failed to update user status')
    })
  })

  describe('fetchRoles', () => {
    it('fetches roles with permissions', async () => {
      api.get.mockResolvedValueOnce({ data: mockRoles })
      const store = useAdminStore()

      await store.fetchRoles()

      expect(api.get).toHaveBeenCalledWith('/admin/roles')
      expect(store.roles).toEqual(mockRoles)
    })

    it('sets error on failure', async () => {
      api.get.mockRejectedValueOnce(new Error('Error'))
      const store = useAdminStore()

      await expect(store.fetchRoles()).rejects.toThrow()
      expect(store.error).toBe('Failed to load roles')
    })
  })

  describe('fetchPermissions', () => {
    it('fetches all permissions', async () => {
      api.get.mockResolvedValueOnce({ data: mockPermissions })
      const store = useAdminStore()

      await store.fetchPermissions()

      expect(api.get).toHaveBeenCalledWith('/admin/permissions')
      expect(store.permissions).toEqual(mockPermissions)
    })

    it('sets error on failure', async () => {
      api.get.mockRejectedValueOnce(new Error('Error'))
      const store = useAdminStore()

      await expect(store.fetchPermissions()).rejects.toThrow()
      expect(store.error).toBe('Failed to load permissions')
    })
  })

  describe('updateRolePermissions', () => {
    it('updates role permissions', async () => {
      const updated = { roleId: 2, roleName: 'USER', permissions: mockPermissions.slice(0, 2) }
      api.put.mockResolvedValueOnce({ data: updated })
      const store = useAdminStore()
      store.roles = [...mockRoles]

      await store.updateRolePermissions(2, [1, 2])

      expect(api.put).toHaveBeenCalledWith('/admin/roles/2/permissions', {
        permissionIds: [1, 2],
      })
      expect(store.roles[1].permissions).toHaveLength(2)
    })

    it('sets error on failure', async () => {
      api.put.mockRejectedValueOnce(new Error('Error'))
      const store = useAdminStore()

      await expect(store.updateRolePermissions(2, [1, 2])).rejects.toThrow()
      expect(store.error).toBe('Failed to update role permissions')
    })
  })

  describe('clearError', () => {
    it('clears the error state', () => {
      const store = useAdminStore()
      store.error = 'Some error'

      store.clearError()

      expect(store.error).toBeNull()
    })
  })
})
