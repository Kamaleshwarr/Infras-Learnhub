package com.company.learninghub.user.repository;

import com.company.learninghub.user.domain.Role;
import com.company.learninghub.user.domain.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleName name);
}

