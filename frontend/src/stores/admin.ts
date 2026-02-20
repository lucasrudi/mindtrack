import { ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/services/api'

export interface AdminUser {
  id: number
  email: string
  name: string
  role: string
  enabled: boolean
  createdAt: string
  updatedAt: string | null
}

export interface PagedUsers {
  content: AdminUser[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface Permission {
  id: number
  resource: string
  action: string
}

export interface RolePermission {
  roleId: number
  roleName: string
  permissions: Permission[]
}

export const useAdminStore = defineStore('admin', () => {
  const users = ref<PagedUsers | null>(null)
  const roles = ref<RolePermission[]>([])
  const permissions = ref<Permission[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchUsers(page = 0, size = 20) {
    loading.value = true
    error.value = null
    try {
      const response = await api.get(`/admin/users?page=${page}&size=${size}`)
      users.value = response.data
    } catch (err) {
      error.value = 'Failed to load users'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function changeUserRole(userId: number, role: string) {
    error.value = null
    try {
      const response = await api.patch(`/admin/users/${userId}/role`, { role })
      if (users.value) {
        const index = users.value.content.findIndex((u) => u.id === userId)
        if (index !== -1) {
          users.value.content[index] = response.data
        }
      }
      return response.data
    } catch (err) {
      error.value = 'Failed to change user role'
      throw err
    }
  }

  async function setUserEnabled(userId: number, enabled: boolean) {
    error.value = null
    try {
      const response = await api.patch(`/admin/users/${userId}/enabled`, { enabled })
      if (users.value) {
        const index = users.value.content.findIndex((u) => u.id === userId)
        if (index !== -1) {
          users.value.content[index] = response.data
        }
      }
      return response.data
    } catch (err) {
      error.value = 'Failed to update user status'
      throw err
    }
  }

  async function fetchRoles() {
    error.value = null
    try {
      const response = await api.get('/admin/roles')
      roles.value = response.data
    } catch (err) {
      error.value = 'Failed to load roles'
      throw err
    }
  }

  async function fetchPermissions() {
    error.value = null
    try {
      const response = await api.get('/admin/permissions')
      permissions.value = response.data
    } catch (err) {
      error.value = 'Failed to load permissions'
      throw err
    }
  }

  async function updateRolePermissions(roleId: number, permissionIds: number[]) {
    error.value = null
    try {
      const response = await api.put(`/admin/roles/${roleId}/permissions`, { permissionIds })
      const index = roles.value.findIndex((r) => r.roleId === roleId)
      if (index !== -1) {
        roles.value[index] = response.data
      }
      return response.data
    } catch (err) {
      error.value = 'Failed to update role permissions'
      throw err
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    users,
    roles,
    permissions,
    loading,
    error,
    fetchUsers,
    changeUserRole,
    setUserEnabled,
    fetchRoles,
    fetchPermissions,
    updateRolePermissions,
    clearError,
  }
})
