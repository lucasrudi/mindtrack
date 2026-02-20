package com.mindtrack.admin.repository;

import com.mindtrack.common.model.Permission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for permission persistence operations.
 */
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Finds all permissions assigned to a specific role.
     */
    @Query("SELECT p FROM Permission p JOIN RolePermission rp ON p.id = rp.permissionId "
            + "WHERE rp.roleId = :roleId ORDER BY p.resource, p.action")
    List<Permission> findByRoleId(Long roleId);
}
