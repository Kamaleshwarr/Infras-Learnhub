package com.company.learninghub.user.repository;

import com.company.learninghub.user.domain.RoleName;
import com.company.learninghub.user.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    @Override
    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Optional<User> findById(UUID id);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmployeeIdIgnoreCase(String employeeId);

    Optional<User> findByEmployeeId(String employeeId);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    @Query("""
            SELECT DISTINCT user FROM User user
            JOIN user.userRoles userRole
            JOIN userRole.role role
            WHERE role.name = :roleName
              AND user.active = true
            """)
    List<User> findActiveByRoleName(@Param("roleName") RoleName roleName);
}

