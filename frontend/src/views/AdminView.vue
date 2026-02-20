<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useAdminStore, type RolePermission } from '@/stores/admin'

const store = useAdminStore()
const activeTab = ref<'users' | 'roles'>('users')
const editingRole = ref<RolePermission | null>(null)
const editPermissionIds = ref<number[]>([])

onMounted(async () => {
  try {
    await store.fetchUsers()
  } catch {
    // Error state is handled by the store
  }
})

async function switchTab(tab: 'users' | 'roles') {
  activeTab.value = tab
  if (tab === 'roles') {
    await Promise.all([store.fetchRoles(), store.fetchPermissions()])
  }
}

async function handleRoleChange(userId: number, newRole: string) {
  await store.changeUserRole(userId, newRole)
}

async function handleToggleEnabled(userId: number, currentEnabled: boolean) {
  await store.setUserEnabled(userId, !currentEnabled)
}

function startEditPermissions(role: RolePermission) {
  editingRole.value = role
  editPermissionIds.value = role.permissions.map((p) => p.id)
}

function cancelEditPermissions() {
  editingRole.value = null
  editPermissionIds.value = []
}

function togglePermission(permissionId: number) {
  const idx = editPermissionIds.value.indexOf(permissionId)
  if (idx === -1) {
    editPermissionIds.value.push(permissionId)
  } else {
    editPermissionIds.value.splice(idx, 1)
  }
}

async function savePermissions() {
  if (!editingRole.value) return
  await store.updateRolePermissions(editingRole.value.roleId, editPermissionIds.value)
  editingRole.value = null
  editPermissionIds.value = []
}

function formatDate(dateStr: string | null): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

// Group permissions by resource for the matrix view
function groupPermissions(permissions: { id: number; resource: string; action: string }[]) {
  const groups: Record<string, { id: number; resource: string; action: string }[]> = {}
  for (const p of permissions) {
    if (!groups[p.resource]) {
      groups[p.resource] = []
    }
    groups[p.resource].push(p)
  }
  return groups
}

async function goToPage(page: number) {
  await store.fetchUsers(page)
}
</script>

<template>
  <div class="admin-view">
    <header class="page-header">
      <div>
        <h1>Admin Panel</h1>
        <p class="subtitle">Manage users, roles, and permissions</p>
      </div>
    </header>

    <div v-if="store.error" class="error-message">
      <p>{{ store.error }}</p>
      <button class="btn btn-secondary" @click="store.clearError()">Dismiss</button>
    </div>

    <div class="tabs">
      <button :class="['tab', { active: activeTab === 'users' }]" @click="switchTab('users')">
        Users
      </button>
      <button :class="['tab', { active: activeTab === 'roles' }]" @click="switchTab('roles')">
        Roles &amp; Permissions
      </button>
    </div>

    <!-- Users Tab -->
    <div v-if="activeTab === 'users'" class="tab-content">
      <div v-if="store.loading" class="loading">
        <p>Loading users...</p>
      </div>

      <div v-else-if="store.users" class="users-section">
        <table class="users-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th>Joined</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in store.users.content" :key="user.id">
              <td class="user-name">{{ user.name }}</td>
              <td class="user-email">{{ user.email }}</td>
              <td>
                <select
                  :value="user.role"
                  class="role-select"
                  @change="handleRoleChange(user.id, ($event.target as HTMLSelectElement).value)"
                >
                  <option value="ADMIN">Admin</option>
                  <option value="USER">User</option>
                  <option value="THERAPIST">Therapist</option>
                </select>
              </td>
              <td>
                <span :class="['status-badge', user.enabled ? 'enabled' : 'disabled']">
                  {{ user.enabled ? 'Active' : 'Disabled' }}
                </span>
              </td>
              <td class="date-cell">{{ formatDate(user.createdAt) }}</td>
              <td>
                <button
                  class="btn btn-sm"
                  :class="user.enabled ? 'btn-danger' : 'btn-primary'"
                  @click="handleToggleEnabled(user.id, user.enabled)"
                >
                  {{ user.enabled ? 'Disable' : 'Enable' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="store.users.totalPages > 1" class="pagination">
          <button
            class="btn btn-sm btn-secondary"
            :disabled="store.users.number === 0"
            @click="goToPage(store.users.number - 1)"
          >
            Previous
          </button>
          <span class="page-info">
            Page {{ store.users.number + 1 }} of {{ store.users.totalPages }}
          </span>
          <button
            class="btn btn-sm btn-secondary"
            :disabled="store.users.number >= store.users.totalPages - 1"
            @click="goToPage(store.users.number + 1)"
          >
            Next
          </button>
        </div>
      </div>
    </div>

    <!-- Roles & Permissions Tab -->
    <div v-if="activeTab === 'roles'" class="tab-content">
      <div v-if="!store.roles.length" class="loading">
        <p>Loading roles...</p>
      </div>

      <template v-else>
        <div v-for="role in store.roles" :key="role.roleId" class="role-card">
          <div class="role-header">
            <h3>{{ role.roleName }}</h3>
            <span class="permission-count">
              {{ role.permissions.length }} permission{{ role.permissions.length !== 1 ? 's' : '' }}
            </span>
            <button
              v-if="editingRole?.roleId !== role.roleId"
              class="btn btn-sm btn-secondary"
              @click="startEditPermissions(role)"
            >
              Edit
            </button>
          </div>

          <!-- View mode -->
          <div v-if="editingRole?.roleId !== role.roleId" class="permissions-grid">
            <div
              v-for="(perms, resource) in groupPermissions(role.permissions)"
              :key="resource"
              class="resource-group"
            >
              <span class="resource-name">{{ resource }}</span>
              <span v-for="p in perms" :key="p.id" class="permission-badge">{{ p.action }}</span>
            </div>
            <p v-if="!role.permissions.length" class="no-permissions">No permissions assigned</p>
          </div>

          <!-- Edit mode -->
          <div v-else class="permissions-edit">
            <div
              v-for="(perms, resource) in groupPermissions(store.permissions)"
              :key="resource"
              class="resource-edit-group"
            >
              <span class="resource-name">{{ resource }}</span>
              <label v-for="p in perms" :key="p.id" class="permission-checkbox">
                <input
                  type="checkbox"
                  :checked="editPermissionIds.includes(p.id)"
                  @change="togglePermission(p.id)"
                />
                {{ p.action }}
              </label>
            </div>
            <div class="edit-actions">
              <button class="btn btn-sm btn-secondary" @click="cancelEditPermissions">
                Cancel
              </button>
              <button class="btn btn-sm btn-primary" @click="savePermissions">
                Save Permissions
              </button>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.admin-view {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-8) var(--space-6);
}

.page-header {
  margin-bottom: var(--space-6);
}

.page-header h1 {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-gray-900);
  margin: 0;
}

.subtitle {
  color: var(--color-gray-500);
  margin-top: var(--space-1);
}

.error-message {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: var(--color-error);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--border-radius);
  margin-bottom: var(--space-6);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tabs {
  display: flex;
  border-bottom: 2px solid var(--color-gray-200);
  margin-bottom: var(--space-6);
}

.tab {
  padding: var(--space-3) var(--space-5);
  border: none;
  background: none;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-500);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all var(--transition-fast);
}

.tab.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
}

.tab:hover:not(.active) {
  color: var(--color-gray-700);
}

.loading {
  text-align: center;
  padding: var(--space-12);
  color: var(--color-gray-500);
}

/* Users Table */
.users-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
}

.users-table th {
  text-align: left;
  padding: var(--space-3) var(--space-4);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-500);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  background: var(--color-gray-50);
  border-bottom: 1px solid var(--color-gray-200);
}

.users-table td {
  padding: var(--space-3) var(--space-4);
  font-size: var(--font-size-sm);
  color: var(--color-gray-700);
  border-bottom: 1px solid var(--color-gray-100);
}

.users-table tr:last-child td {
  border-bottom: none;
}

.user-name {
  font-weight: var(--font-weight-medium);
  color: var(--color-gray-900);
}

.user-email {
  color: var(--color-gray-500);
}

.role-select {
  padding: var(--space-1) var(--space-2);
  border: 1px solid var(--color-gray-300);
  border-radius: var(--border-radius);
  font-size: var(--font-size-xs);
  color: var(--color-gray-700);
  background: var(--color-white);
  cursor: pointer;
}

.status-badge {
  display: inline-block;
  font-size: var(--font-size-xs);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
}

.status-badge.enabled {
  background: #f0fdf4;
  color: var(--color-success);
}

.status-badge.disabled {
  background: #fef2f2;
  color: var(--color-error);
}

.date-cell {
  white-space: nowrap;
  color: var(--color-gray-500);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) 0;
}

.page-info {
  font-size: var(--font-size-sm);
  color: var(--color-gray-500);
}

/* Roles & Permissions */
.role-card {
  background: var(--color-white);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--border-radius-lg);
  padding: var(--space-5);
  margin-bottom: var(--space-4);
}

.role-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.role-header h3 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-900);
  margin: 0;
}

.permission-count {
  font-size: var(--font-size-xs);
  color: var(--color-gray-500);
  flex: 1;
}

.permissions-grid {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.resource-group {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.resource-name {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-gray-700);
  text-transform: capitalize;
}

.permission-badge {
  font-size: var(--font-size-xs);
  padding: var(--space-1) var(--space-2);
  background: var(--color-primary-50);
  color: var(--color-primary);
  border-radius: var(--border-radius-full);
  font-weight: var(--font-weight-medium);
}

.no-permissions {
  color: var(--color-gray-500);
  font-size: var(--font-size-sm);
  font-style: italic;
}

.permissions-edit {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.resource-edit-group {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) 0;
  border-bottom: 1px solid var(--color-gray-100);
}

.resource-edit-group:last-of-type {
  border-bottom: none;
}

.permission-checkbox {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-size-sm);
  color: var(--color-gray-700);
  cursor: pointer;
}

.permission-checkbox input[type='checkbox'] {
  cursor: pointer;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-gray-100);
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-3) var(--space-5);
  border-radius: var(--border-radius);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
  cursor: pointer;
  border: none;
  transition: all var(--transition-fast);
}

.btn-sm {
  padding: var(--space-2) var(--space-3);
  font-size: var(--font-size-xs);
}

.btn-primary {
  background: var(--color-primary);
  color: var(--color-white);
}

.btn-primary:hover {
  background: var(--color-primary-dark);
}

.btn-secondary {
  background: var(--color-gray-100);
  color: var(--color-gray-700);
}

.btn-secondary:hover {
  background: var(--color-gray-200);
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-danger {
  background: #fef2f2;
  color: var(--color-error);
}

.btn-danger:hover {
  background: #fee2e2;
}
</style>
