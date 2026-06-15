package com.company.learninghub.user.domain;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleAssignmentTest {

    @Test
    void replaceRoleIsNoOpWhenUserAlreadyHasOnlyThatRole() {
        User user = new User("EMP001", "user@example.com", "Test User", "hash");
        Role employeeRole = role(RoleName.EMPLOYEE);
        user.replaceRole(employeeRole);

        assertThat(user.getUserRoles()).hasSize(1);

        user.replaceRole(employeeRole);

        assertThat(user.getUserRoles()).hasSize(1);
        assertThat(user.hasRoleName(RoleName.EMPLOYEE)).isTrue();
    }

    @Test
    void replaceRoleReplacesDifferentRoleAssignment() {
        User user = new User("EMP002", "other@example.com", "Other User", "hash");
        Role employeeRole = role(RoleName.EMPLOYEE);
        Role adminRole = role(RoleName.ADMIN);
        user.replaceRole(employeeRole);

        user.replaceRole(adminRole);

        assertThat(user.getUserRoles()).hasSize(1);
        assertThat(user.hasRoleName(RoleName.ADMIN)).isTrue();
        assertThat(user.hasRoleName(RoleName.EMPLOYEE)).isFalse();
    }

    private Role role(RoleName roleName) {
        Role role = new Role(roleName);
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        return role;
    }
}
