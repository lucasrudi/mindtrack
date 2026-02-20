package com.mindtrack.admin.repository;

import com.mindtrack.admin.model.RolePermission;
import com.mindtrack.admin.model.RolePermissionId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for role-permission mapping operations.
 */
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    List<RolePermission> findByRoleId(Long roleId);

    void deleteByRoleId(Long roleId);
}
